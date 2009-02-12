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
package org.sonatype.nexus.rest.groups;

import java.io.IOException;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.rest.model.RepositoryGroupResourceResponse;
import org.sonatype.nexus.rest.repositories.AbstractRepositoryPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;

/**
 * Resource handler for Repository resource.
 * 
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "RepositoryGroupPlexusResource" )
public class RepositoryGroupPlexusResource
    extends AbstractRepositoryGroupPlexusResource
{

    public RepositoryGroupPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new RepositoryGroupResourceResponse();
    }

    @Override
    public String getResourceUri()
    {
        return "/repo_groups/{" + GROUP_ID_KEY + "}";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/repo_groups/*", "authcBasic,perms[nexus:repogroups]" );
    }

    protected String getGroupId( Request request )
    {
        return request.getAttributes().get( GROUP_ID_KEY ).toString();
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        RepositoryGroupResourceResponse result = new RepositoryGroupResourceResponse();
        try
        {
            CRepositoryGroup group = getNexus().readRepositoryGroup( getGroupId( request ) );

            GroupRepository groupRepo = getNexus()
                .getRepositoryWithFacet( getGroupId( request ), GroupRepository.class );

            RepositoryGroupResource resource = new RepositoryGroupResource();

            resource.setId( group.getGroupId() );

            resource.setName( group.getName() );

            resource.setProvider( group.getType() );

            resource.setRepoType( AbstractRepositoryPlexusResource.REPO_TYPE_GROUP );

            resource.setFormat( groupRepo.getRepositoryContentClass().getId() );

            // just to trigger list creation, and not stay null coz of XStream serialization
            resource.getRepositories();

            for ( String repoId : (List<String>) group.getRepositories() )
            {
                RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();

                member.setId( repoId );

                member.setName( getNexus().getRepository( repoId ).getName() );

                member.setResourceURI( createChildReference( request, repoId ).toString() );

                resource.addRepository( member );
            }

            result.setData( resource );
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().warn( "Cannot find a repository declared within a group!", e );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
        }
        return result;
    }

    @Override
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        RepositoryGroupResourceResponse groupRequest = (RepositoryGroupResourceResponse) payload;
        RepositoryGroupResourceResponse result = null;

        if ( groupRequest != null )
        {
            RepositoryGroupResource resource = groupRequest.getData();

            if ( resource.getRepositories() == null || resource.getRepositories().size() == 0 )
            {
                getLogger().info(
                    "The repository group with ID=" + getGroupId( request ) + " have zero repository members!" );

                throw new PlexusResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "The group cannot have zero repository members!",
                    getNexusErrorResponse( "repositories", "The group cannot have zero repository members!" ) );
            }

            if ( StringUtils.isEmpty( resource.getId() ) )
            {
                getLogger().warn( "Repository group id is empty! " );

                throw new PlexusResourceException(
                    Status.CLIENT_ERROR_NOT_FOUND,
                    "Repository group id is empty! ",
                    getNexusErrorResponse( "repositories", "Repository group id can't be empty! " ) );
            }

            createOrUpdateRepositoryGroup( resource, false );
        }
        return result;
    }

    @Override
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        try
        {
            getNexus().deleteRepositoryGroup( getGroupId( request ) );
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().warn( "Repository group not found, id=" + getGroupId( request ) );

            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Repository Group Not Found" );
        }
        catch ( IOException e )
        {
            getLogger().warn( "Got IO Exception!", e );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
        }
    }

}
