package org.sonatype.nexus.integrationtests.nexus2190;

import org.restlet.data.Method;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.test.utils.ErrorReportUtil;
import org.testng.annotations.Test;

public class Nexus2190ErrorReportBundleIT
    extends AbstractNexusIntegrationTest
{    
    @Test
    public void validateBundle()
        throws Exception
    {
        RequestFacade.sendMessage( "service/local/exception", Method.POST, null );
        
        ErrorReportUtil.validateZipContents( nexusWorkDir );
    }
}

