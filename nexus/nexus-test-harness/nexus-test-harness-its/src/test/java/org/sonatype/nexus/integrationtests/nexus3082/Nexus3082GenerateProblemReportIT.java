package org.sonatype.nexus.integrationtests.nexus3082;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ErrorReportResponse;
import org.sonatype.nexus.test.utils.ErrorReportUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus3082GenerateProblemReportIT
    extends AbstractNexusIntegrationTest
{
    @Test
    public void generateReport()
        throws Exception
    {
        ErrorReportResponse response = ErrorReportUtil.generateProblemReport( "sometitle", "somedescription" );

        Assert.assertNotNull( response );

        Assert.assertNotNull( response.getData().getJiraUrl() );
    }

    @Test
    public void generateReportWithFailure()
        throws Exception
    {
        ErrorReportUtil.generateProblemReport( null, "somedescription" );
    }

}
