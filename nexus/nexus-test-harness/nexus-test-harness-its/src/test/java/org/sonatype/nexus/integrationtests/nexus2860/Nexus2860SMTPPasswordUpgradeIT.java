package org.sonatype.nexus.integrationtests.nexus2860;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus2860SMTPPasswordUpgradeIT
    extends AbstractNexusIntegrationTest
{

    @Test
    public void upgradeSmtp()
        throws Exception
    {
        String pw = getNexusConfigUtil().getNexusConfig().getSmtpConfiguration().getPassword();
        // ensuring it wasn't encrypted twice
        Assert.assertEquals( "IT-password", pw );
    }
}
