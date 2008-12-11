/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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

import java.io.File;
import java.util.Collection;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Test SnapshotRemoverTask to remove old artifacts but keep updated artifacts
 * @author marvin
 */
public class Nexus634KeepTwoSnapshotsTest
    extends AbstractSnapshotRemoverTest
{

    @Test
    public void keepTwoSnapshots()
        throws Exception
    {

        // This is THE important part
        runSnapshotRemover( "nexus-test-harness-snapshot-repo", 2, 0, true );

        Collection<File> jars = listFiles( artifactFolder, new String[] { "jar" }, false );
        Assert.assertEquals( "SnapshotRemoverTask should remove only old artifacts", 2, jars.size() );
    }

}
