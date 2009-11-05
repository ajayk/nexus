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
package org.sonatype.nexus.integrationtests.client.nexus758;

import org.sonatype.nexus.client.NexusClient;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Tests the Soft Start, Stop, Restart, and isNexusStarted methods in the rest-client.
 */
public class Nexus758StatusService
    extends AbstractNexusIntegrationTest
{

    private NexusClient getConnectedNexusClient()
        throws Exception
    {

        NexusClient client = (NexusClient) container.lookup( NexusClient.ROLE );
        TestContext context = TestContainer.getInstance().getTestContext();
        client.connect( baseNexusUrl, context.getAdminUsername(), context.getAdminPassword() );

        return client;
    }

    // @Test
    // public void startAndStopTest()
    // throws Exception
    // {
    // NexusClient client = this.getConnectedNexusClient();
    //
    // // stop Nexus
    // client.stopNexus(); // blocking
    // AssertJUnit.assertTrue( "Expected Nexus to be Stopped", NexusStatusUtil.waitForStop() );
    //
    // // start Nexus
    // client.startNexus(); // blocking
    // AssertJUnit.assertTrue( "Expected Nexus to be Started", NexusStatusUtil.waitForStart() );
    //
    // client.disconnect();
    // }

    @Test
    public void restartTest()
        throws Exception
    {
        NexusClient client = this.getConnectedNexusClient();

        // restart Nexus
        client.restartNexus(); // this is blocking
        AssertJUnit.assertTrue( "Expected Nexus to be Started", client.isNexusStarted( false ) );

        client.disconnect();
    }

    // @Test
    // public void waitForStartTest()
    // throws Exception
    // {
    // NexusClient client = null;
    //
    // try
    // {
    // appBooter.stop();
    //
    // client = this.getConnectedNexusClient();
    // // turn down the timeout to speed up the tests
    //
    // AssertJUnit.assertTrue( "Wait for start, timed out.", NexusStatusUtil.waitForStop( client ) );
    //
    // appBooter.setSleepAfterStart( 0 );
    // appBooter.start();
    // // set the timeout back to 16 sec
    // AssertJUnit.assertTrue( "Wait for start, timed out.", NexusStatusUtil.waitForStart( client ) );
    //
    // }
    // finally
    // {
    // // hack, but we needed to be able to test the timeout
    // appBooter.setSleepAfterStart( 6000 );
    // }
    //
    // client.disconnect();
    // }

}
