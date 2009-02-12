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
package org.sonatype.nexus.proxy.registry;

import java.util.Set;

/**
 * This is the registry of known repository types. Just like RepositoryRegistry holds the "active" instances of
 * Repositories, this registry does the discovery of them. Hint: we are using String for role intentionally, to be able
 * to do reloads of plugins that contributes new repository roles to system.
 * 
 * @author cstamas
 */
public interface RepositoryTypeRegistry
{
    /**
     * Returns the set of classes that are known that provides Repository components.
     * 
     * @return a set of repository roles or empty set.
     */
    Set<String> getRepositoryRoles();

    /**
     * Returns the set of hints for the given repository role.
     * 
     * @param role
     * @return a set of repository hints or empty set.
     */
    Set<String> getExistingRepositoryHints( String role );

    /**
     * Returns the ContentClass for the given Repository component.
     * 
     * @param role
     * @param hint
     * @return the content class instance or null if repository does not exists.
     */
    ContentClass getRepositoryContentClass( String role, String hint );

    /**
     * Returns the repository description for the given Repository component.
     * 
     * @param role
     * @param hint
     * @return the description (will fallback to "" if no description found) or null if repository does not exists.
     */
    String getRepositoryDescription( String role, String hint );
}
