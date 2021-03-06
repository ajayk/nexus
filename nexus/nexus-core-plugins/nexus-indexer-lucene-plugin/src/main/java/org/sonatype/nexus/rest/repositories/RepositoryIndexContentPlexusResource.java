/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest.repositories;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.indextreeview.AbstractIndexContentPlexusResource;
import org.sonatype.nexus.rest.indextreeview.IndexBrowserTreeViewResponseDTO;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * Repository index content resource.
 * 
 * @author dip
 */
@Component( role = PlexusResource.class, hint = "repoIndexResource" )
@Path( RepositoryIndexContentPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
public class RepositoryIndexContentPlexusResource
    extends AbstractIndexContentPlexusResource
{
    public static final String REPOSITORY_ID_KEY = "repositoryId";
    
    public static final String RESOURCE_URI = "/repositories/{" + REPOSITORY_ID_KEY + "}/index_content"; 

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/repositories/*/index_content/**", "authcBasic,tiperms" );
    }

    @Override
    protected String getRepositoryId( Request request )
    {
        return String.valueOf( request.getAttributes().get( REPOSITORY_ID_KEY ) );
    }
    
    /**
     * Get the index content from the specified repository. at the specified path (path is appended to the end of the uri).
     * 
     * @param repositoryId The repository to retrieve the index content for.
     */
    @Override
    @GET
    @ResourceMethodSignature( pathParams = { @PathParam( RepositoryIndexContentPlexusResource.REPOSITORY_ID_KEY ) }, 
                              output = IndexBrowserTreeViewResponseDTO.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        return super.get( context, request, response, variant );
    }
}
