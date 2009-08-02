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
package org.sonatype.nexus.proxy.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.configuration.AbstractConfigurable;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CPathMappingItem;
import org.sonatype.nexus.configuration.model.CRepositoryGrouping;
import org.sonatype.nexus.configuration.model.CRepositoryGroupingCoreConfiguration;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.mapping.RepositoryPathMapping.MappingType;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.utils.ResourceStoreUtils;

/**
 * The Class PathBasedRequestRepositoryMapper filters repositories to search using supplied list of filter expressions.
 * It is parametrized by java,util.Map, the contents: </p> <tt>
 * regexp1=repo1,repo2...
 * regexp2=repo3,repo4...
 * ...
 * </tt>
 * <p>
 * An example (with grouped Router and two repositories, one for central and one for inhouse in same group):
 * </p>
 * <tt>
 * /com/company/=inhouse
 * /org/apache/=central
 * </tt>
 * 
 * @author cstamas
 */
@Component( role = RequestRepositoryMapper.class )
public class DefaultRequestRepositoryMapper
    extends AbstractConfigurable
    implements RequestRepositoryMapper
{
    @Requirement
    private Logger logger;

    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    @Requirement
    private RepositoryRegistry repositoryRegistry;

    /** The compiled flag. */
    private volatile boolean compiled = false;

    private volatile List<RepositoryPathMapping> blockings = new CopyOnWriteArrayList<RepositoryPathMapping>();

    private volatile List<RepositoryPathMapping> inclusions = new CopyOnWriteArrayList<RepositoryPathMapping>();

    private volatile List<RepositoryPathMapping> exclusions = new CopyOnWriteArrayList<RepositoryPathMapping>();

    // ==

    protected Logger getLogger()
    {
        return logger;
    }

    // ==

    @Override
    protected void initializeConfiguration()
        throws ConfigurationException
    {
        if ( getApplicationConfiguration().getConfigurationModel() != null )
        {
            configure( getApplicationConfiguration() );
        }
    }


    protected ApplicationConfiguration getApplicationConfiguration()
    {
        return applicationConfiguration;
    }

    @Override
    protected CRepositoryGrouping getCurrentConfiguration( boolean forWrite )
    {
        return ( (CRepositoryGroupingCoreConfiguration) getCurrentCoreConfiguration() ).getConfiguration( forWrite );
    }

    @Override
    protected Configurator getConfigurator()
    {
        return null;
    }

    @Override
    protected CoreConfiguration wrapConfiguration( Object configuration )
        throws ConfigurationException
    {
        if ( configuration instanceof ApplicationConfiguration )
        {
            return new CRepositoryGroupingCoreConfiguration( (ApplicationConfiguration) configuration );
        }
        else
        {
            throw new ConfigurationException( "The passed configuration object is of class \""
                + configuration.getClass().getName() + "\" and not the required \""
                + ApplicationConfiguration.class.getName() + "\"!" );
        }
    }

    public boolean commitChanges()
        throws ConfigurationException
    {
        boolean wasDirty = super.commitChanges();

        if ( wasDirty )
        {
            compiled = false;
        }

        return wasDirty;
    }

    // ==

    public List<Repository> getMappedRepositories( Repository repository, ResourceStoreRequest request,
                                                   List<Repository> resolvedRepositories )
        throws NoSuchRepositoryException
    {
        if ( !compiled )
        {
            compile();
        }

        ArrayList<Repository> reposList = new ArrayList<Repository>( resolvedRepositories );

        ArrayList<RepositoryPathMapping> appliedMappings = new ArrayList<RepositoryPathMapping>();

        // if include found, add it to the list.
        boolean firstAdd = true;

        for ( RepositoryPathMapping mapping : blockings )
        {
            if ( mapping.matches( repository, request ) )
            {
                reposList.clear();

                getLogger().info(
                                  "The request path [" + request.toString() + "] is blocked by rule "
                                      + mapping.toString() );

                return reposList;
            }
        }

        // include, if found a match
        for ( RepositoryPathMapping mapping : inclusions )
        {
            if ( mapping.matches( repository, request ) )
            {
                appliedMappings.add( mapping );

                if ( firstAdd )
                {
                    reposList.clear();

                    firstAdd = false;
                }

                // add only those that are in initial resolvedRepositories list and that are non-user managed
                // (preserve ordering)
                if ( mapping.getMappedRepositories().size() == 1
                    && "*".equals( mapping.getMappedRepositories().get( 0 ) ) )
                {
                    for ( Repository repo : resolvedRepositories )
                    {
                        reposList.add( repo );
                    }
                }
                else
                {
                    for ( Repository repo : resolvedRepositories )
                    {
                        if ( mapping.getMappedRepositories().contains( repo.getId() ) || !repo.isUserManaged() )
                        {
                            reposList.add( repo );
                        }
                    }
                }
            }
        }

        // then, if exlude found, remove those
        for ( RepositoryPathMapping mapping : exclusions )
        {
            if ( mapping.matches( repository, request ) )
            {
                appliedMappings.add( mapping );

                if ( mapping.getMappedRepositories().size() == 1
                    && "*".equals( mapping.getMappedRepositories().get( 0 ) ) )
                {
                    reposList.clear();

                    break;
                }

                for ( String repositoryId : mapping.getMappedRepositories() )
                {
                    Repository store = repositoryRegistry.getRepository( repositoryId );

                    // but only if is user managed
                    if ( store.isUserManaged() )
                    {
                        reposList.remove( store );
                    }
                }
            }
        }

        if ( getLogger().isDebugEnabled() )
        {
            if ( appliedMappings.isEmpty() )
            {
                getLogger().debug( "No mapping exists for request path [" + request.toString() + "]" );
            }
            else
            {
                StringBuilder sb =
                    new StringBuilder( "Request for path \"" + request.toString()
                        + "\" with the initial list of processable repositories of \""
                        + ResourceStoreUtils.getResourceStoreListAsString( resolvedRepositories )
                        + "\" got these mappings applied:\n" );

                for ( RepositoryPathMapping mapping : appliedMappings )
                {
                    sb.append( " * " ).append( mapping.toString() ).append( "\n" );
                }

                getLogger().debug( sb.toString() );

                if ( reposList.size() == 0 )
                {
                    getLogger().debug(
                                       "Mapping for path [" + request.toString()
                                           + "] excluded all storages from servicing the request." );
                }
                else
                {
                    getLogger().debug(
                                       "Request path for [" + request.toString() + "] is MAPPED to reposes: "
                                           + ResourceStoreUtils.getResourceStoreListAsString( reposList ) );
                }
            }
        }

        return reposList;
    }

    // ==

    protected synchronized void compile()
        throws NoSuchRepositoryException
    {
        if ( compiled )
        {
            return;
        }

        blockings.clear();

        inclusions.clear();

        exclusions.clear();

        if ( getCurrentConfiguration( false ) == null )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "No Routes defined, have nothing to compile." );
            }

            return;
        }

        List<CPathMappingItem> pathMappings = getCurrentConfiguration( false ).getPathMappings();

        for ( CPathMappingItem item : pathMappings )
        {
            if ( CPathMappingItem.BLOCKING_RULE_TYPE.equals( item.getRouteType() ) )
            {
                blockings.add( convert( item ) );
            }
            else if ( CPathMappingItem.INCLUSION_RULE_TYPE.equals( item.getRouteType() ) )
            {
                inclusions.add( convert( item ) );
            }
            else if ( CPathMappingItem.EXCLUSION_RULE_TYPE.equals( item.getRouteType() ) )
            {
                exclusions.add( convert( item ) );
            }
            else
            {
                getLogger().warn( "Unknown route type: " + item.getRouteType() );

                throw new IllegalArgumentException( "Unknown route type: " + item.getRouteType() );
            }
        }

        compiled = true;
    }

    protected RepositoryPathMapping convert( CPathMappingItem item )
        throws IllegalArgumentException
    {
        MappingType type = null;

        if ( CPathMappingItem.BLOCKING_RULE_TYPE.equals( item.getRouteType() ) )
        {
            type = MappingType.BLOCKING;
        }
        else if ( CPathMappingItem.INCLUSION_RULE_TYPE.equals( item.getRouteType() ) )
        {
            type = MappingType.INCLUSION;
        }
        else if ( CPathMappingItem.EXCLUSION_RULE_TYPE.equals( item.getRouteType() ) )
        {
            type = MappingType.EXCLUSION;
        }
        else
        {
            getLogger().warn( "Unknown route type: " + item.getRouteType() );

            throw new IllegalArgumentException( "Unknown route type: " + item.getRouteType() );
        }

        return new RepositoryPathMapping( item.getId(), type, item.getGroupId(), item.getRoutePatterns(), item
            .getRepositories() );
    }

    protected CPathMappingItem convert( RepositoryPathMapping item )
    {
        String routeType = null;

        if ( MappingType.BLOCKING.equals( item.getMappingType() ) )
        {
            routeType = CPathMappingItem.BLOCKING_RULE_TYPE;
        }
        else if ( MappingType.INCLUSION.equals( item.getMappingType() ) )
        {
            routeType = CPathMappingItem.INCLUSION_RULE_TYPE;
        }
        else if ( MappingType.EXCLUSION.equals( item.getMappingType() ) )
        {
            routeType = CPathMappingItem.EXCLUSION_RULE_TYPE;
        }

        CPathMappingItem result = new CPathMappingItem();
        result.setId( item.getId() );
        result.setGroupId( item.getGroupId() );
        result.setRepositories( item.getMappedRepositories() );
        result.setRouteType( routeType );
        ArrayList<String> patterns = new ArrayList<String>( item.getPatterns().size() );
        for ( Pattern pattern : item.getPatterns() )
        {
            patterns.add( pattern.toString() );
        }
        result.setRoutePatterns( patterns );
        return result;
    }

    // ==

    public boolean addMapping( RepositoryPathMapping mapping )
    {
        removeMapping( mapping.getId() );

        getCurrentConfiguration( true ).addPathMapping( convert( mapping ) );

        return true;
    }

    public boolean removeMapping( String id )
    {
        for ( Iterator<CPathMappingItem> i = getCurrentConfiguration( true ).getPathMappings().iterator(); i.hasNext(); )
        {
            CPathMappingItem mapping = i.next();

            if ( mapping.getId().equals( id ) )
            {
                i.remove();

                return true;
            }
        }

        return false;
    }

    public Map<String, RepositoryPathMapping> getMappings()
    {
        List<CPathMappingItem> items = getCurrentConfiguration( false ).getPathMappings();

        HashMap<String, RepositoryPathMapping> result = new HashMap<String, RepositoryPathMapping>( items.size() );

        for ( CPathMappingItem item : items )
        {
            RepositoryPathMapping mapping = convert( item );

            result.put( mapping.getId(), mapping );
        }

        return Collections.unmodifiableMap( result );
    }

}
