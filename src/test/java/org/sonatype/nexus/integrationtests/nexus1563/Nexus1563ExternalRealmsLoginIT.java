package org.sonatype.nexus.integrationtests.nexus1563;

import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.test.utils.UserCreationUtil;
import org.sonatype.security.rest.model.RoleResource;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Nexus1563ExternalRealmsLoginIT
    extends AbstractPrivilegeTest
{

    @BeforeClass
    public static void security()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void loginExternalUser()
        throws Exception
    {
        TestContext testContext = TestContainer.getInstance().getTestContext();

        RoleResource role = new RoleResource();
        role.setId( "role-123" );
        role.setName( "Role role-123" );
        role.setDescription( "Role role-123 external map" );
        role.setSessionTimeout( 60 );
        role.addRole( "admin" );
        testContext.useAdminForRequests();
        roleUtil.createRole( role );

        testContext.setUsername( "admin-simple" );
        testContext.setPassword( "admin123" );
        Status status = UserCreationUtil.login();
        AssertJUnit.assertTrue( "Unable to login " + status, status.isSuccess() );
    }
}
