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
package org.sonatype.nexus.proxy.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.mapping.RequestRepositoryMapper;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.util.ContextUtils;

/**
 * An abstract group repository. The specific behaviour (ie. metadata merge) should be implemented in subclases.
 * 
 * @author cstamas
 */
public abstract class AbstractGroupRepository
    extends AbstractRepository
    implements GroupRepository
{
    @Requirement
    private RepositoryRegistry repoRegistry;

    @Requirement
    private RequestRepositoryMapper requestRepositoryMapper;

    private List<String> memberRepoIds = new ArrayList<String>();

    @Override
    public void initialize()
        throws InitializationException
    {
        super.initialize();

        repoRegistry.addProximityEventListener( this );
    }

    @Override
    public void onProximityEvent( AbstractEvent evt )
    {
        super.onProximityEvent( evt );

        // act automatically on repo removal. Remove it from myself if member.
        if ( evt instanceof RepositoryRegistryEventRemove )
        {
            RepositoryRegistryEventRemove revt = (RepositoryRegistryEventRemove) evt;

            if ( revt.getRepository() == this )
            {
                // we are being removed
                repoRegistry.removeProximityEventListener( this );
            }
            else
            {
                // remove it from members (will nothing happen if not amongs them)
                removeMemberRepository( revt.getRepository().getId() );
            }
        }
    }

    @Override
    protected Collection<StorageItem> doListItems( RepositoryItemUid uid, Map<String, Object> context )
        throws ItemNotFoundException,
            StorageException
    {
        HashSet<String> names = new HashSet<String>();
        ArrayList<StorageItem> result = new ArrayList<StorageItem>();
        boolean found = false;
        try
        {
            addItems( names, result, getLocalStorage().listItems( this, context, uid.getPath() ) );

            found = true;
        }
        catch ( ItemNotFoundException ignored )
        {
            // ignored
        }

        for ( Repository repo : getMemberRepositories() )
        {
            if ( !ContextUtils.collContains( context, ResourceStoreRequest.CTX_PROCESSED_REPOSITORIES, repo.getId() ) )
            {
                try
                {
                    RepositoryItemUid memberUid = repo.createUid( uid.getPath() );

                    ResourceStoreRequest req = new ResourceStoreRequest( memberUid, true );

                    req.setRequestContext( context );

                    addItems( names, result, repo.list( req ) );

                    found = true;
                }
                catch ( ItemNotFoundException e )
                {
                    // ignored
                }
                catch ( IllegalOperationException e )
                {
                    // ignored
                }
                catch ( StorageException e )
                {
                    // ignored
                }
                catch ( AccessDeniedException e )
                {
                    // ignored
                }
            }
            else
            {
                getLogger()
                    .info(
                        "A repository CYCLE detected (doListItems()), while processing group ID='"
                            + this.getId()
                            + "'. The repository with ID='"
                            + repo.getId()
                            + "' was already processed during this request! This repository is skipped from processing. Request: "
                            + uid.toString() );
            }
        }

        if ( !found )
        {
            throw new ItemNotFoundException( uid );
        }

        return result;
    }

    private static void addItems( HashSet<String> names, ArrayList<StorageItem> result,
        Collection<StorageItem> listItems )
    {
        for ( StorageItem item : listItems )
        {
            if ( names.add( item.getPath() ) )
            {
                result.add( item );
            }
        }
    }

    @Override
    protected StorageItem doRetrieveItem( RepositoryItemUid uid, Map<String, Object> context )
        throws IllegalOperationException,
            ItemNotFoundException,
            StorageException
    {
        try
        {
            // local always wins
            return super.doRetrieveItem( uid, context );
        }
        catch ( ItemNotFoundException ignored )
        {
            // ignored
        }

        for ( Repository repo : getRequestRepositories( uid ) )
        {
            if ( !ContextUtils.collContains( context, ResourceStoreRequest.CTX_PROCESSED_REPOSITORIES, repo.getId() ) )
            {
                try
                {
                    RepositoryItemUid memberUid = repo.createUid( uid.getPath() );

                    StorageItem item = repo.retrieveItem( memberUid, context );

                    if ( item instanceof StorageCollectionItem )
                    {
                        item = new DefaultStorageCollectionItem( this, uid.getPath(), true, false );
                    }

                    return item;
                }
                catch ( IllegalOperationException e )
                {
                    // ignored
                }
                catch ( ItemNotFoundException e )
                {
                    // ignored
                }
                catch ( StorageException e )
                {
                    // ignored
                }
            }
            else
            {
                getLogger()
                    .info(
                        "A repository CYCLE detected (doRetrieveItem()), while processing group ID='"
                            + this.getId()
                            + "'. The repository with ID='"
                            + repo.getId()
                            + "' was already processed during this request! This repository is skipped from processing. Request: "
                            + uid.toString() );
            }
        }

        throw new ItemNotFoundException( uid );
    }

    public List<Repository> getMemberRepositories()
    {
        ArrayList<Repository> result = new ArrayList<Repository>();

        try
        {
            for ( String repoId : memberRepoIds )
            {
                Repository repo = repoRegistry.getRepository( repoId );

                result.add( repo );
            }
        }
        catch ( NoSuchRepositoryException e )
        {
            // XXX throw new StorageException( e );
        }

        return result;
    }

    protected List<Repository> getRequestRepositories( RepositoryItemUid uid )
        throws StorageException
    {
        List<Repository> members = getMemberRepositories();

        try
        {
            return requestRepositoryMapper.getMappedRepositories( repoRegistry, uid, members );
        }
        catch ( NoSuchResourceStoreException e )
        {
            throw new StorageException( e );
        }
    }

    public void setMemberRepositories( List<String> repositories )
    {
        memberRepoIds = new ArrayList<String>( repositories );
    }

    public void removeMemberRepository( String repositoryId )
    {
        memberRepoIds.remove( repositoryId );
    }

    public List<StorageItem> doRetrieveItems( RepositoryItemUid uid, Map<String, Object> context )
        throws StorageException
    {
        ArrayList<StorageItem> items = new ArrayList<StorageItem>();

        for ( Repository repository : getRequestRepositories( uid ) )
        {
            RepositoryItemUid muid = repository.createUid( uid.getPath() );

            try
            {
                StorageItem item = repository.retrieveItem( muid, context );

                items.add( item );
            }
            catch ( StorageException e )
            {
                throw e;
            }
            catch ( IllegalOperationException e )
            {
                getLogger().warn( "Member repository request failed", e );
            }
            catch ( ItemNotFoundException e )
            {
                // that's okay
            }
        }

        return items;
    }

}
