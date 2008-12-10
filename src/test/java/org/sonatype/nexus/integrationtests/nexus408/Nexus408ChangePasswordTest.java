/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright � 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.integrationtests.nexus408;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.ChangePasswordUtils;

/**
 * Test change password service. 
 */
public class Nexus408ChangePasswordTest
    extends AbstractNexusIntegrationTest
{

    @Test
    public void changeUserPassword()
        throws Exception
    {
        Status status = ChangePasswordUtils.changePassword( "test-user", "admin123", "123admin" );
        Assert.assertEquals( Status.SUCCESS_ACCEPTED.getCode(), status.getCode() );
    }

}
