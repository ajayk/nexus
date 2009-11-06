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
package org.sonatype.nexus.integrationtests.proxy.nexus1089;

import java.io.File;

import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.JettyInstaceFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class Nexus1089SecureProxyIT
    extends AbstractNexusProxyIntegrationTest
{

    @Override
    protected ServletServer createProxyServer()
        throws Exception
    {
        return JettyInstaceFactory.getDefaultSecureFileServer( proxyServerPort );
    }

    @Test
    public void downloadArtifact()
        throws Exception
    {
        File localFile = this.getLocalFile( "release-proxy-repo-1", "nexus1089", "artifact", "1.0", "jar" );

        File artifact = this.downloadArtifact( "nexus1089", "artifact", "1.0", "jar", null, "target/downloads" );

        AssertJUnit.assertTrue( FileTestingUtils.compareFileSHA1s( artifact, localFile ) );

    }
}
