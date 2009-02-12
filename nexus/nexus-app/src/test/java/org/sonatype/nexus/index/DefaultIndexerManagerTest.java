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
package org.sonatype.nexus.index;

import java.util.Collection;

import org.apache.lucene.search.Query;
import org.sonatype.nexus.AbstractMavenRepoContentTests;
import org.sonatype.nexus.tasks.ReindexTask;
import org.sonatype.scheduling.ScheduledTask;

public class DefaultIndexerManagerTest
    extends AbstractMavenRepoContentTests
{
    private IndexerManager indexerManager;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        indexerManager = lookup( IndexerManager.class );
    }

    protected void tearDown()
        throws Exception
    {
        indexerManager.shutdown( false );

        super.tearDown();
    }

    public void testRepoReindex()
        throws Exception
    {
        fillInRepo();

        ReindexTask reindexTask = defaultNexus.createTaskInstance( ReindexTask.class );

        ScheduledTask<Object> st = defaultNexus.submit( "reindexAll", reindexTask );

        // make it block until finished
        st.get();

        Query query = indexerManager.getNexusIndexer().constructQuery( ArtifactInfo.GROUP_ID, "org.sonatype.nexus" );
        FlatSearchRequest request = new FlatSearchRequest( query );
        
        FlatSearchResponse response = indexerManager.getNexusIndexer().searchFlat( request );
        
        Collection<ArtifactInfo> result = response.getResults(); 

        // expected result set
        // org.sonatype.nexus:nexus-indexer:1.0-beta-5-SNAPSHOT:null:jar, 
        // org.sonatype.nexus:nexus-indexer:1.0-beta-4:null:jar, 
        // org.sonatype.nexus:nexus-indexer:1.0-beta-4-SNAPSHOT:null:jar, 
        // org.sonatype.nexus:nexus-indexer:1.0-beta-4-SNAPSHOT:cli:jar, 
        // org.sonatype.nexus:nexus-indexer:1.0-beta-4-SNAPSHOT:jdk14:jar, 
        // org.sonatype.nexus:nexus-indexer:1.0-beta-4-SNAPSHOT:sources:jar

        assertEquals( result.toString(), 7, result.size() );
    }
}
