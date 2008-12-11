/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.integrationtests.nexus634;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Test SnapshotRemoverTask to remove all artifacts
 * 
 * @author marvin
 */
public class Nexus634RemoveAllTest
    extends AbstractSnapshotRemoverTest
{

    @Test
    public void removeAllSnapshots()
        throws Exception
    {
        // This is THE important part
        runSnapshotRemover( "nexus-test-harness-snapshot-repo", 0, 0, true );

        // this IT is wrong: nexus will remove the parent folder too, if the GAV folder is emptied completely
        // Collection<File> jars = listFiles( artifactFolder, new String[] { "jar" }, false );
        // Assert.assertTrue( "All artifacts should be deleted by SnapshotRemoverTask. Found: " + jars, jars.isEmpty()
        // );

        // looking at the IT resources, there is only one artifact in there, hence, the dir should be removed
        Assert.assertFalse(
            "The folder should be removed since all artifacts should be gone, instead there are files left!",
            artifactFolder.exists() );
    }

}
