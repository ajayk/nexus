/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.integrationtests.nexus1375;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.LogConfigResource;

/**
 * @author juven
 */
public class Nexus1375LogConfigTest
    extends AbstractNexusIntegrationTest
{

    protected LogConfigMessageUtil messageUtil;

    public Nexus1375LogConfigTest()
    {
        messageUtil = new LogConfigMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML );

        TestContainer.getInstance().getTestContext().setSecureTest( true );

        TestContainer.getInstance().getTestContext().useAdminForRequests();
    }

    @Test
    public void getLogConfig()
        throws Exception
    {
        LogConfigResource resource = messageUtil.getLogConfig();

        Assert.assertEquals( "DEBUG, logfile", resource.getRootLogger() );

        Assert.assertEquals( "%4d{yyyy-MM-dd HH:mm:ss} %-5p [%-15.15t] - %c - %m%n", resource.getFileAppenderPattern() );

        File logConfigFile = new File( getBasedir(), "target/logs/nexus.log" );

        Assert.assertEquals( logConfigFile.getAbsolutePath(), resource.getFileAppenderLocation() );
    }

    @Test
    public void updateLogConfig()
        throws Exception
    {
        LogConfigResource resource = messageUtil.getLogConfig();

        Assert.assertEquals( "DEBUG, logfile", resource.getRootLogger() );

        resource.setRootLogger( "INFO, logfile" );

        messageUtil.updateLogConfig( resource );

        Assert.assertEquals( "INFO, logfile", messageUtil.getLogConfig().getRootLogger() );

        resource.setRootLogger( "DEBUG, logfile" );

        messageUtil.updateLogConfig( resource );

        Assert.assertEquals( "DEBUG, logfile", messageUtil.getLogConfig().getRootLogger() );
    }
}
