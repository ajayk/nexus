package org.sonatype.nexus.integrationtests.nexus2120;

import java.io.File;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.sonatype.jettytestsuite.ControlledServer;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.indextreeview.IndexBrowserTreeNode;
import org.sonatype.nexus.rest.indextreeview.IndexBrowserTreeViewResponseDTO;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.tasks.ReindexTask;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.sonatype.nexus.test.utils.TestProperties;
import org.sonatype.nexus.test.utils.XStreamFactory;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.thoughtworks.xstream.XStream;

public class Nexus2120EnableDownloadRemoteIndexIT
    extends AbstractNexusIntegrationTest
{

    final String URI = "service/local/repositories/basic/index_content/";

    protected static final int webProxyPort;

    static
    {
        webProxyPort = TestProperties.getInteger( "webproxy-server-port" );
    }

    @BeforeClass
    public static void init()
        throws Exception
    {
        cleanWorkDir();
    }

    protected ControlledServer server;

    private RepositoryMessageUtil repoUtil;

    @BeforeClass
    public void start()
        throws Exception
    {
        server = (ControlledServer) lookup( ControlledServer.ROLE );
        repoUtil =
            new RepositoryMessageUtil( XStreamFactory.getXmlXStream(), MediaType.APPLICATION_XML,
                                       getRepositoryTypeRegistry() );
    }

    @AfterClass
    public void stop()
        throws Exception
    {
        server.stop();
        server = null;
        repoUtil = null;
    }

    @Test
    public void downloadChecksumTest()
        throws Exception
    {
        RepositoryResource basic = (RepositoryResource) repoUtil.getRepository( "basic" );
        // ensure URL
        AssertJUnit.assertEquals( "http://localhost:" + webProxyPort + "/repository",
                                  basic.getRemoteStorage().getRemoteStorageUrl() );
        // ensure is not downloading index
        AssertJUnit.assertFalse( basic.isDownloadRemoteIndexes() );

        File basicRemoteRepo = getTestFile( "basic" );
        List<String> repoUrls = server.addServer( "repository", basicRemoteRepo );

        server.start();

        // reindex once
        RepositoryMessageUtil.updateIndexes( "basic" );
        TaskScheduleUtil.waitForAllTasksToStop( ReindexTask.class );

        // first try, download remote index set to false
        AssertJUnit.assertTrue( "nexus should not download remote indexes!!! " + repoUrls, repoUrls.isEmpty() );
        // server changed here, a 404 is no longer returned if index_context is empty, 404 will only be returned
        // if index_context does not exist (or repo does not exist)
        Response response = RequestFacade.doGetRequest( URI );
        AssertJUnit.assertTrue( "Error downloading index content\n" + response.getStatus(),
                                response.getStatus().isSuccess() );

        XStream xstream = XStreamFactory.getXmlXStream();

        xstream.processAnnotations( IndexBrowserTreeNode.class );
        xstream.processAnnotations( IndexBrowserTreeViewResponseDTO.class );

        XStreamRepresentation re =
            new XStreamRepresentation( xstream, response.getEntity().getText(), MediaType.APPLICATION_XML );
        IndexBrowserTreeViewResponseDTO resourceResponse =
            (IndexBrowserTreeViewResponseDTO) re.getPayload( new IndexBrowserTreeViewResponseDTO() );

        AssertJUnit.assertTrue( "index response should have 0 entries",
                                resourceResponse.getData().getChildren().isEmpty() );

        // I changed my mind, I do wanna remote index
        basic.setDownloadRemoteIndexes( true );
        repoUtil.updateRepo( basic );

        // reindex again
        RepositoryMessageUtil.updateIndexes( "basic" );
        TaskScheduleUtil.waitForAllTasksToStop( ReindexTask.class );

        // did nexus downloaded indexes?
        AssertJUnit.assertTrue( "nexus should download remote indexes!!! " + repoUrls,
                                repoUrls.contains( "/repository/.index/nexus-maven-repository-index.gz" ) );
        response = RequestFacade.doGetRequest( URI );
        AssertJUnit.assertTrue( "Error downloading index content\n" + response.getStatus(),
                                response.getStatus().isSuccess() );
    }
}
