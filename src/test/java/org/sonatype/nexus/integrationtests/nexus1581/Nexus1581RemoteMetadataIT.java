package org.sonatype.nexus.integrationtests.nexus1581;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.rest.model.MirrorResource;
import org.sonatype.nexus.rest.model.MirrorResourceListResponse;
import org.sonatype.nexus.test.utils.MirrorMessageUtils;
import org.sonatype.nexus.test.utils.TestProperties;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class Nexus1581RemoteMetadataIT
    extends AbstractNexusProxyIntegrationTest
{

    public Nexus1581RemoteMetadataIT()
    {
        super( "with-mirror-proxy-repo" );
    }
    
    @Test
    public void testGetRemoteMirrorList() throws IOException
    {
        MirrorMessageUtils mirrorUtils = new MirrorMessageUtils( this.getJsonXStream(), MediaType.APPLICATION_JSON );
        MirrorResourceListResponse response = mirrorUtils.getPredefinedMirrors( this.getTestRepositoryId() );
        
        List<MirrorResource> mirrorResources = response.getData();
        
        HashMap<String, String> mirrorIdMap = new HashMap<String, String>();
        
        for ( MirrorResource mirrorResource : mirrorResources )
        {
            mirrorIdMap.put( mirrorResource.getId(), mirrorResource.getUrl() );
        }
        
        AssertJUnit.assertTrue( mirrorIdMap.containsKey( "mirror1" ) );
        AssertJUnit.assertEquals( TestProperties.getString( "proxy.repo.base.url" )+"/mirror-repo", mirrorIdMap.get( "mirror1" ) );
        
        AssertJUnit.assertTrue( mirrorIdMap.containsKey( "mirror2" ) );
        AssertJUnit.assertEquals( TestProperties.getString( "proxy.repo.base.url" )+"/void", mirrorIdMap.get( "mirror2" ) );
        
        
        AssertJUnit.assertEquals( 2, mirrorResources.size() );
    }
    


}
