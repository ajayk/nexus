package org.sonatype.nexus.rest.repositories;

import java.util.Set;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.NexusRepositoryTypeListResource;
import org.sonatype.nexus.rest.model.NexusRepositoryTypeListResourceResponse;

public abstract class AbstractRepositoryTypeRegistryPlexusResource
    extends AbstractNexusPlexusResource
{
    @Requirement
    private RepositoryTypeRegistry repositoryTypeRegistry;

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    protected abstract Class<?> getRole( Request request );

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        NexusRepositoryTypeListResourceResponse result = new NexusRepositoryTypeListResourceResponse();

        // get role from request
        Class<?> role = getRole( request );

        Set<String> hints = repositoryTypeRegistry.getExistingRepositoryHints( role.getName() );

        // check if valid role
        if ( hints.isEmpty() )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
        }

        // loop and convert all objects of this role to a PlexusComponentListResource
        for ( String hint : hints )
        {

            NexusRepositoryTypeListResource resource = new NexusRepositoryTypeListResource();

            resource.setProvider( hint );

            ContentClass contentClass = repositoryTypeRegistry.getRepositoryContentClass( role.getName(), hint );

            if ( contentClass != null )
            {
                resource.setFormat( contentClass.getId() );
            }
            else
            {
                resource.setFormat( null );
            }

            String description = repositoryTypeRegistry.getRepositoryDescription( role.getName(), hint );

            if ( !StringUtils.isEmpty( description ) )
            {
                resource.setDescription( description );
            }
            else
            {
                resource.setDescription( hint );
            }

            // add it to the collection
            result.addData( resource );
        }

        return result;
    }
}
