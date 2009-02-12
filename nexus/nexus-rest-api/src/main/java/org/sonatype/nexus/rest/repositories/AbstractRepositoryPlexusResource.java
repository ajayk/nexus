/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.rest.repositories;

import java.util.Collection;

import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRemoteStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.RemoteStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.global.AbstractGlobalConfigurationPlexusResource;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceRemoteStorage;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;

public abstract class AbstractRepositoryPlexusResource
    extends AbstractNexusPlexusResource
{
    /** Key to store Repo with which we work against. */
    public static final String REPOSITORY_ID_KEY = "repositoryId";

    /** Repo type hosted. */
    public static final String REPO_TYPE_HOSTED = "hosted";

    /** Repo type proxied. */
    public static final String REPO_TYPE_PROXIED = "proxy";

    /** Repo type virtual (shadow in nexus). */
    public static final String REPO_TYPE_VIRTUAL = "virtual";

    /** Repo type group. */
    public static final String REPO_TYPE_GROUP = "group";

    @Requirement
    private RepositoryTypeRegistry repositoryTypeRegistry;

    /**
     * Pull the repository Id out of the Request.
     * 
     * @param request
     * @return
     */
    protected String getRepositoryId( Request request )
    {
        return request.getAttributes().get( REPOSITORY_ID_KEY ).toString();
    }

    protected String getRepoFormat( Class<?> role, String type )
    {
        ContentClass cc = repositoryTypeRegistry.getRepositoryContentClass( role.getName(), type );

        if ( cc != null )
        {
            return cc.getId();
        }
        else
        {
            return null;
        }
    }

    protected String _getHintForRoleAndFormat( Class<?> role, String format )
    {
        // going strict
        if ( Repository.class.equals( role ) )
        {
            if ( "maven1".equals( format ) )
            {
                return format;
            }
            else if ( "maven2".equals( format ) )
            {
                return format;
            }
            else
            {
                return null;
            }
        }
        else if ( ShadowRepository.class.equals( role ) )
        {
            if ( "maven2".equals( format ) )
            {
                return "m1-m2-shadow";
            }
            else if ( "maven1".equals( format ) )
            {
                return "m2-m1-shadow";
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    /**
     * Converting App model to REST DTO.
     */
    public RepositoryShadowResource getRepositoryShadowRestModel( CRepositoryShadow model )
    {
        RepositoryShadowResource resource = new RepositoryShadowResource();

        resource.setId( model.getId() );

        resource.setName( model.getName() );

        resource.setProvider( model.getType() );

        resource.setRepoType( REPO_TYPE_VIRTUAL );

        resource.setFormat( getRepoFormat( ShadowRepository.class, model.getType() ) );

        resource.setShadowOf( model.getShadowOf() );

        resource.setSyncAtStartup( model.isSyncAtStartup() );

        return resource;
    }

    /**
     * Converting REST DTO + possible App model to App model. If app model is given, "update" happens, otherwise if
     * target is null, "create".
     * 
     * @param model
     * @param target
     * @return app model, merged or created
     */
    public CRepositoryShadow getRepositoryShadowAppModel( RepositoryShadowResource model, CRepositoryShadow target )
    {
        CRepositoryShadow appModel = new CRepositoryShadow();

        if ( target == null )
        {
            appModel.setLocalStatus( Configuration.LOCAL_STATUS_IN_SERVICE );
        }
        else
        {
            appModel.setLocalStatus( target.getLocalStatus() );
        }

        appModel.setId( model.getId() );

        appModel.setName( model.getName() );

        appModel.setShadowOf( model.getShadowOf() );

        appModel.setSyncAtStartup( model.isSyncAtStartup() );

        appModel.setType( model.getProvider() );

        return appModel;
    }

    /**
     * Converting App model to REST DTO.
     */
    public RepositoryResource getRepositoryRestModel( CRepository model )
    {
        String repoType = getRestRepoType( model );

        RepositoryResource resource = null;

        if ( REPO_TYPE_PROXIED.equals( repoType ) )
        {
            resource = getRepositoryProxyRestModel( model );
        }
        else
        {
            resource = new RepositoryResource();
        }

        resource.setProvider( model.getType() );

        resource.setFormat( getRepoFormat( Repository.class, model.getType() ) );

        resource.setRepoType( repoType );

        resource.setId( model.getId() );

        resource.setName( model.getName() );

        resource.setAllowWrite( model.isAllowWrite() );

        resource.setBrowseable( model.isBrowseable() );

        resource.setIndexable( model.isIndexable() );

        resource.setNotFoundCacheTTL( model.getNotFoundCacheTTL() );

        resource.setDefaultLocalStorageUrl( model.defaultLocalStorageUrl );

        resource.setOverrideLocalStorageUrl( model.getLocalStorage() != null ? model.getLocalStorage().getUrl() : null );

        resource.setRepoPolicy( getRestRepoPolicy( model ) );

        resource.setChecksumPolicy( getRestChecksumPolicy( model ) );

        resource.setDownloadRemoteIndexes( model.isDownloadRemoteIndexes() );

        return resource;

    }

    /**
     * Converting App model to REST DTO.
     */
    public RepositoryProxyResource getRepositoryProxyRestModel( CRepository model )
    {
        RepositoryProxyResource resource = new RepositoryProxyResource();

        resource.setRemoteStorage( new RepositoryResourceRemoteStorage() );

        resource.getRemoteStorage().setRemoteStorageUrl( model.getRemoteStorage().getUrl() );

        resource.getRemoteStorage().setAuthentication(
            AbstractGlobalConfigurationPlexusResource.convert( model.getRemoteStorage().getAuthentication() ) );

        resource.getRemoteStorage().setConnectionSettings(
            AbstractGlobalConfigurationPlexusResource.convert( model.getRemoteStorage().getConnectionSettings() ) );

        resource.getRemoteStorage().setHttpProxySettings(
            AbstractGlobalConfigurationPlexusResource.convert( model.getRemoteStorage().getHttpProxySettings() ) );

        resource.setArtifactMaxAge( model.getArtifactMaxAge() );

        resource.setMetadataMaxAge( model.getMetadataMaxAge() );

        return resource;
    }

    /**
     * Converting REST DTO + possible App model to App model. If app model is given, "update" happens, otherwise if
     * target is null, "create".
     * 
     * @param model
     * @param target
     * @return app model, merged or created
     */
    public CRepository getRepositoryAppModel( RepositoryResource model, CRepository target )
    {
        CRepository appModel = new CRepository();

        if ( target != null )
        {
            appModel.setLocalStatus( target.getLocalStatus() );
        }

        appModel.setId( model.getId() );

        appModel.setName( model.getName() );

        appModel.setType( model.getProvider() );

        appModel.setAllowWrite( model.isAllowWrite() );

        appModel.setBrowseable( model.isBrowseable() );

        appModel.setIndexable( model.isIndexable() );

        appModel.setNotFoundCacheTTL( model.getNotFoundCacheTTL() );

        appModel.setRepositoryPolicy( model.getRepoPolicy() );

        appModel.setChecksumPolicy( model.getChecksumPolicy() );

        appModel.setDownloadRemoteIndexes( model.isDownloadRemoteIndexes() );

        if ( model.getOverrideLocalStorageUrl() != null )
        {
            appModel.setLocalStorage( new CLocalStorage() );
            appModel.getLocalStorage().setUrl( model.getOverrideLocalStorageUrl() );
        }
        else
        {
            appModel.setLocalStorage( null );
        }

        if ( RepositoryProxyResource.class.isAssignableFrom( model.getClass() ) )
        {
            appModel = getRepositoryProxyAppModel( (RepositoryProxyResource) model, appModel );
        }

        return appModel;
    }

    /**
     * Converting REST DTO + possible App model to App model. If app model is given, "update" happens, otherwise if
     * target is null, "create".
     * 
     * @param model
     * @param target
     * @return app model, merged or created
     */
    public CRepository getRepositoryProxyAppModel( RepositoryProxyResource model, CRepository target )
    {
        target.setArtifactMaxAge( model.getArtifactMaxAge() );

        target.setMetadataMaxAge( model.getMetadataMaxAge() );

        if ( target.getRemoteStorage() == null )
        {
            target.setRemoteStorage( new CRemoteStorage() );
        }

        target.getRemoteStorage().setUrl( model.getRemoteStorage().getRemoteStorageUrl() );

        if ( model.getRemoteStorage().getAuthentication() != null )
        {
            if ( target.getRemoteStorage().getAuthentication() == null )
            {
                target.getRemoteStorage().setAuthentication( new CRemoteAuthentication() );
            }

            target.getRemoteStorage().getAuthentication().setUsername(
                model.getRemoteStorage().getAuthentication().getUsername() );

            target.getRemoteStorage().getAuthentication().setPassword(
                model.getRemoteStorage().getAuthentication().getPassword() );

            target.getRemoteStorage().getAuthentication().setNtlmDomain(
                model.getRemoteStorage().getAuthentication().getNtlmDomain() );

            target.getRemoteStorage().getAuthentication().setNtlmHost(
                model.getRemoteStorage().getAuthentication().getNtlmHost() );

            target.getRemoteStorage().getAuthentication().setPrivateKey(
                model.getRemoteStorage().getAuthentication().getPrivateKey() );

            target.getRemoteStorage().getAuthentication().setPassphrase(
                model.getRemoteStorage().getAuthentication().getPassphrase() );
        }
        else
        {
            target.getRemoteStorage().setAuthentication( null );
        }

        if ( model.getRemoteStorage().getConnectionSettings() != null )
        {
            if ( target.getRemoteStorage().getConnectionSettings() == null )
            {
                target.getRemoteStorage().setConnectionSettings( new CRemoteConnectionSettings() );
            }

            target.getRemoteStorage().getConnectionSettings().setConnectionTimeout(
                model.getRemoteStorage().getConnectionSettings().getConnectionTimeout() * 1000 );

            target.getRemoteStorage().getConnectionSettings().setRetrievalRetryCount(
                model.getRemoteStorage().getConnectionSettings().getRetrievalRetryCount() );

            target.getRemoteStorage().getConnectionSettings().setUserAgentCustomizationString(
                model.getRemoteStorage().getConnectionSettings().getUserAgentString() );

            target.getRemoteStorage().getConnectionSettings().setQueryString(
                model.getRemoteStorage().getConnectionSettings().getQueryString() );
        }
        else
        {
            target.getRemoteStorage().setConnectionSettings( null );
        }

        if ( model.getRemoteStorage().getHttpProxySettings() != null )
        {
            if ( target.getRemoteStorage().getHttpProxySettings() == null )
            {
                target.getRemoteStorage().setHttpProxySettings( new CRemoteHttpProxySettings() );
            }

            target.getRemoteStorage().getHttpProxySettings().setProxyHostname(
                model.getRemoteStorage().getHttpProxySettings().getProxyHostname() );

            target.getRemoteStorage().getHttpProxySettings().setProxyPort(
                model.getRemoteStorage().getHttpProxySettings().getProxyPort() );

            if ( model.getRemoteStorage().getHttpProxySettings().getAuthentication() != null )
            {

                if ( target.getRemoteStorage().getHttpProxySettings().getAuthentication() == null )
                {
                    target.getRemoteStorage().getHttpProxySettings().setAuthentication( new CRemoteAuthentication() );
                }

                target.getRemoteStorage().getHttpProxySettings().getAuthentication().setUsername(
                    model.getRemoteStorage().getHttpProxySettings().getAuthentication().getUsername() );

                target.getRemoteStorage().getHttpProxySettings().getAuthentication().setPassword(
                    model.getRemoteStorage().getHttpProxySettings().getAuthentication().getPassword() );

                target.getRemoteStorage().getHttpProxySettings().getAuthentication().setNtlmDomain(
                    model.getRemoteStorage().getHttpProxySettings().getAuthentication().getNtlmDomain() );

                target.getRemoteStorage().getHttpProxySettings().getAuthentication().setNtlmHost(
                    model.getRemoteStorage().getHttpProxySettings().getAuthentication().getNtlmHost() );

                target.getRemoteStorage().getHttpProxySettings().getAuthentication().setPrivateKey(
                    model.getRemoteStorage().getHttpProxySettings().getAuthentication().getPrivateKey() );

                target.getRemoteStorage().getHttpProxySettings().getAuthentication().setPassphrase(
                    model.getRemoteStorage().getHttpProxySettings().getAuthentication().getPassphrase() );
            }

        }
        else
        {
            target.getRemoteStorage().setHttpProxySettings( null );
        }

        return target;
    }

    public String getRestRepoType( Object model )
    {
        if ( CRepository.class.isAssignableFrom( model.getClass() ) )
        {
            CRepository m = (CRepository) model;

            if ( m.getRemoteStorage() != null && m.getRemoteStorage().getUrl() != null )
            {
                return REPO_TYPE_PROXIED;
            }
            else
            {
                return REPO_TYPE_HOSTED;
            }
        }
        else if ( CRepositoryShadow.class.isAssignableFrom( model.getClass() ) )
        {
            return REPO_TYPE_VIRTUAL;
        }
        else if ( CRepositoryGroup.class.isAssignableFrom( model.getClass() ) )
        {
            return REPO_TYPE_GROUP;
        }
        else
        {
            throw new IllegalArgumentException( "The passed model with class" + model.getClass().getName()
                + " is not a Repository or RepositoryShadow!" );
        }
    }

    public String getRestRepoPolicy( Object model )
    {
        if ( CRepository.class.isAssignableFrom( model.getClass() ) )
        {
            CRepository m = (CRepository) model;

            return m.getRepositoryPolicy();
        }
        else
        {
            throw new IllegalArgumentException( "The passed model with class" + model.getClass().getName()
                + " is not a hosted or proxied Repository!" );
        }
    }

    public String getRestChecksumPolicy( Object model )
    {
        if ( CRepository.class.isAssignableFrom( model.getClass() ) )
        {
            CRepository m = (CRepository) model;

            return m.getChecksumPolicy();
        }
        else
        {
            throw new IllegalArgumentException( "The passed model with class" + model.getClass().getName()
                + " is not a hosted or proxied Repository!" );
        }
    }

    public String getRestRepoLocalStatus( Object model )
    {
        if ( CRepository.class.isAssignableFrom( model.getClass() ) )
        {
            CRepository m = (CRepository) model;

            return m.getLocalStatus();
        }
        else if ( CRepositoryShadow.class.isAssignableFrom( model.getClass() ) )
        {
            CRepositoryShadow m = (CRepositoryShadow) model;

            return m.getLocalStatus();
        }
        else
        {
            throw new IllegalArgumentException( "The passed model with class" + model.getClass().getName()
                + " is not a Repository or RepositoryShadow!" );
        }
    }

    public String getRestRepoRemoteStatus( Object model, Request request, Response response )
        throws ResourceException
    {
        try
        {
            if ( CRepository.class.isAssignableFrom( model.getClass() ) )
            {
                Form form = request.getResourceRef().getQueryAsForm();

                boolean forceCheck = form.getFirst( "forceCheck" ) != null;

                CRepository m = (CRepository) model;

                Repository repository = getNexus().getRepository( m.getId() );

                RemoteStatus rs = null;

                if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
                {
                    rs = repository.adaptToFacet( ProxyRepository.class ).getRemoteStatus( forceCheck );
                }

                if ( RemoteStatus.UNKNOWN.equals( rs ) )
                {
                    // set status to ACCEPTED, since we have incomplete info
                    response.setStatus( Status.SUCCESS_ACCEPTED );
                }

                return rs == null ? null : rs.toString();
            }
            else
            {
                return null;
            }
        }
        catch ( NoSuchRepositoryException e )
        {
            return null;
        }
    }

    public String getRestRepoProxyMode( Object model )
    {
        if ( CRepository.class.isAssignableFrom( model.getClass() ) )
        {
            CRepository m = (CRepository) model;

            return m.getProxyMode().toString();
        }
        else
        {
            return null;
        }
    }

    protected RepositoryResourceResponse getRepositoryResourceResponse( String repoId, Nexus nexus )
        throws ResourceException
    {
        RepositoryResourceResponse result = new RepositoryResourceResponse();

        try
        {
            RepositoryBaseResource resource = null;
            try
            {
                CRepository model = nexus.readRepository( repoId );

                resource = getRepositoryRestModel( model );
            }
            catch ( NoSuchRepositoryException e )
            {
                CRepositoryShadow model = nexus.readRepositoryShadow( repoId );

                resource = getRepositoryShadowRestModel( model );
            }

            result.setData( resource );
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().warn( "Repository not found, id=" + repoId );

            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Repository Not Found" );
        }
        return result;
    }

    protected RepositoryListResourceResponse listRepositories( Request request, boolean allReposes )
        throws ResourceException
    {
        RepositoryListResourceResponse result = new RepositoryListResourceResponse();

        RepositoryListResource repoRes;

        Collection<CRepository> repositories = getNexus().listRepositories();

        for ( CRepository repository : repositories )
        {
            if ( allReposes || repository.isUserManaged() )
            {
                repoRes = new RepositoryListResource();

                repoRes.setResourceURI( createRepositoryReference( request, repository.getId() ).toString() );

                repoRes.setRepoType( getRestRepoType( repository ) );

                repoRes.setFormat( getRepoFormat( Repository.class, repository.getType() ) );

                repoRes.setId( repository.getId() );

                repoRes.setName( repository.getName() );

                repoRes.setUserManaged( repository.isUserManaged() );

                repoRes.setExposed( repository.isExposed() );

                repoRes.setEffectiveLocalStorageUrl( repository.getLocalStorage() != null
                    && repository.getLocalStorage().getUrl() != null
                    ? repository.getLocalStorage().getUrl()
                    : repository.defaultLocalStorageUrl );

                repoRes.setRepoPolicy( getRestRepoPolicy( repository ) );

                if ( REPO_TYPE_PROXIED.equals( repoRes.getRepoType() ) )
                {
                    if ( repository.getRemoteStorage() != null )
                    {
                        repoRes.setRemoteUri( repository.getRemoteStorage().getUrl() );
                    }
                }

                result.addData( repoRes );
            }
        }

        Collection<CRepositoryShadow> shadows = getNexus().listRepositoryShadows();

        for ( CRepositoryShadow shadow : shadows )
        {
            if ( allReposes || shadow.isUserManaged() )
            {
                repoRes = new RepositoryListResource();

                repoRes.setId( shadow.getId() );

                repoRes.setFormat( getRepoFormat( ShadowRepository.class, shadow.getType() ) );

                repoRes.setResourceURI( createRepositoryReference( request, shadow.getId() ).toString() );

                repoRes.setRepoType( getRestRepoType( shadow ) );

                repoRes.setName( shadow.getName() );

                repoRes.setUserManaged( shadow.isUserManaged() );

                repoRes.setEffectiveLocalStorageUrl( shadow.defaultLocalStorageUrl );

                repoRes.setExposed( shadow.isExposed() );

                result.addData( repoRes );
            }
        }

        return result;
    }
}
