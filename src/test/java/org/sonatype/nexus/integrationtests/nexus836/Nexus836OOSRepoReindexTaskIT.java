package org.sonatype.nexus.integrationtests.nexus836;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.ReindexTaskDescriptor;
import org.sonatype.nexus.test.utils.RepositoryStatusMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Nexus836OOSRepoReindexTaskIT
    extends AbstractNexusIntegrationTest
{

    @BeforeMethod
    public void putOutOfService()
        throws Exception
    {
        RepositoryStatusMessageUtil.putOutOfService( REPO_TEST_HARNESS_SHADOW, "hosted" );
    }

    @Test
    public void expireAllRepos()
        throws Exception
    {
        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setId( "repositoryOrGroupId" );
        prop.setValue( "all_repo" );

        ScheduledServiceListResource task = TaskScheduleUtil.runTask( ReindexTaskDescriptor.ID, prop );

        AssertJUnit.assertNotNull( task );
        AssertJUnit.assertEquals( "SUBMITTED", task.getStatus() );
    }
}
