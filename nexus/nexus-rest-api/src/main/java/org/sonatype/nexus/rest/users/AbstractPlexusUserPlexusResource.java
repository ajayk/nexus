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
package org.sonatype.nexus.rest.users;

import org.sonatype.jsecurity.locators.users.PlexusRole;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.PlexusRoleResource;
import org.sonatype.nexus.rest.model.PlexusUserResource;

public abstract class AbstractPlexusUserPlexusResource
    extends AbstractNexusPlexusResource
{
    protected PlexusUserResource nexusToRestModel( PlexusUser user )
    {
        PlexusUserResource resource = new PlexusUserResource();
        
        resource.setUserId( user.getUserId() );
        resource.setSource( user.getSource() );
        resource.setName( user.getName() );
        resource.setEmail( user.getEmailAddress() );
        
        for ( PlexusRole role : user.getRoles() )
        {   
            resource.addRole( this.nexusToRestModel( role ) );
        }
        
        return resource;
    }
    
    protected PlexusRoleResource nexusToRestModel( PlexusRole role )
    {
        PlexusRoleResource roleResource = new PlexusRoleResource();
        roleResource.setRoleId( role.getRoleId() );
        roleResource.setName( role.getName() );
        roleResource.setSource( role.getSource() );
        
        return roleResource;
    }
}
