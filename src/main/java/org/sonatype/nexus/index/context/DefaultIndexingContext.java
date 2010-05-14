/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.context;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.SerialMergeScheduler;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.artifact.M2GavCalculator;
import org.sonatype.nexus.index.ArtifactInfo;

/**
 * The default {@link IndexingContext} implementation.
 * 
 * @author Jason van Zyl
 * @author Tamas Cservenak
 */
public class DefaultIndexingContext
    implements IndexingContext
{
    /**
     * A standard location for indices served up by a webserver.
     */
    private static final String INDEX_DIRECTORY = ".index";

    private static final String FLD_DESCRIPTOR = "DESCRIPTOR";

    private static final String FLD_DESCRIPTOR_CONTENTS = "NexusIndex";

    private static final String FLD_IDXINFO = "IDXINFO";

    private static final String VERSION = "1.0";

    private static final Term DESCRIPTOR_TERM = new Term( FLD_DESCRIPTOR, FLD_DESCRIPTOR_CONTENTS );

    private Object indexLock = new Object();

    private Directory indexDirectory;

    private File indexDirectoryFile;

    private String id;

    private boolean searchable;

    private String repositoryId;

    private File repository;

    private String repositoryUrl;

    private String indexUpdateUrl;

    private IndexReader indexReader;

    private NexusIndexSearcher indexSearcher;

    private NexusIndexWriter indexWriter;

    private Date timestamp;

    private List<? extends IndexCreator> indexCreators;

    /**
     * Currently nexus-indexer knows only M2 reposes
     * <p>
     * XXX move this into a concrete Scanner implementation
     */
    private GavCalculator gavCalculator;

    private DefaultIndexingContext( String id, String repositoryId,
                                    File repository, //
                                    String repositoryUrl, String indexUpdateUrl,
                                    List<? extends IndexCreator> indexCreators )
    {
        this.id = id;

        this.searchable = true;

        this.repositoryId = repositoryId;

        this.repository = repository;

        this.repositoryUrl = repositoryUrl;

        this.indexUpdateUrl = indexUpdateUrl;

        this.indexReader = null;

        this.indexWriter = null;

        this.indexCreators = indexCreators;

        // eh?
        // Guice does NOT initialize these, and we have to do manually?
        // While in Plexus, all is well, but when in guice-shim,
        // these objects are still LazyHintedBeans or what not and IndexerFields are NOT registered!
        for ( IndexCreator indexCreator : indexCreators )
        {
            indexCreator.getIndexerFields();
        }

        this.gavCalculator = new M2GavCalculator();
    }

    public DefaultIndexingContext( String id, String repositoryId, File repository, File indexDirectoryFile,
                                   String repositoryUrl, String indexUpdateUrl,
                                   List<? extends IndexCreator> indexCreators, boolean reclaimIndex )
        throws IOException, UnsupportedExistingLuceneIndexException
    {
        this( id, repositoryId, repository, repositoryUrl, indexUpdateUrl, indexCreators );

        this.indexDirectoryFile = indexDirectoryFile;

        this.indexDirectory = FSDirectory.getDirectory( indexDirectoryFile );

        prepareIndex( reclaimIndex );
    }

    public DefaultIndexingContext( String id, String repositoryId, File repository, Directory indexDirectory,
                                   String repositoryUrl, String indexUpdateUrl,
                                   List<? extends IndexCreator> indexCreators, boolean reclaimIndex )
        throws IOException, UnsupportedExistingLuceneIndexException
    {
        this( id, repositoryId, repository, repositoryUrl, indexUpdateUrl, indexCreators );

        this.indexDirectory = indexDirectory;

        if ( indexDirectory instanceof FSDirectory )
        {
            this.indexDirectoryFile = ( (FSDirectory) indexDirectory ).getFile();
        }

        prepareIndex( reclaimIndex );
    }

    public Directory getIndexDirectory()
    {
        return indexDirectory;
    }

    public File getIndexDirectoryFile()
    {
        return indexDirectoryFile;
    }

    private void prepareIndex( boolean reclaimIndex )
        throws IOException, UnsupportedExistingLuceneIndexException
    {
        if ( IndexReader.indexExists( indexDirectory ) )
        {
            try
            {
                // unlock the dir forcibly
                if ( IndexReader.isLocked( indexDirectory ) )
                {
                    IndexReader.unlock( indexDirectory );
                }

                checkAndUpdateIndexDescriptor( reclaimIndex );
            }
            catch ( IOException e )
            {
                if ( reclaimIndex )
                {
                    prepareCleanIndex( true );
                }
                else
                {
                    throw e;
                }
            }
        }
        else
        {
            prepareCleanIndex( false );
        }

        timestamp = IndexUtils.getTimestamp( indexDirectory );
    }

    private void prepareCleanIndex( boolean deleteExisting )
        throws IOException
    {
        if ( deleteExisting )
        {
            if ( indexReader != null )
            {
                indexReader.close();
            }

            indexReader = null;

            if ( indexWriter != null && !indexWriter.isClosed() )
            {
                indexWriter.close();
            }

            indexWriter = null;

            // unlock the dir forcibly
            if ( IndexReader.isLocked( indexDirectory ) )
            {
                IndexReader.unlock( indexDirectory );
            }

            indexDirectory.close();
            FileUtils.deleteDirectory( indexDirectoryFile );
            indexDirectoryFile.mkdirs();

            indexDirectory = FSDirectory.getDirectory( indexDirectoryFile );
        }

        if ( StringUtils.isEmpty( getRepositoryId() ) )
        {
            throw new IllegalArgumentException( "The repositoryId cannot be null when creating new repository!" );
        }

        // create empty idx and store descriptor
        new NexusIndexWriter( getIndexDirectory(), new NexusAnalyzer(), true ).close();

        storeDescriptor();
    }

    private void checkAndUpdateIndexDescriptor( boolean reclaimIndex )
        throws IOException, UnsupportedExistingLuceneIndexException
    {
        if ( reclaimIndex )
        {
            // forcefully "reclaiming" the ownership of the index as ours
            storeDescriptor();
            return;
        }

        Hits hits = getIndexSearcher().search( new TermQuery( DESCRIPTOR_TERM ) );

        if ( hits == null || hits.length() == 0 )
        {
            throw new UnsupportedExistingLuceneIndexException( "The existing index has no NexusIndexer descriptor" );
        }

        Document descriptor = hits.doc( 0 );

        if ( hits.length() != 1 )
        {
            storeDescriptor();
            return;
        }

        String[] h = StringUtils.split( descriptor.get( FLD_IDXINFO ), ArtifactInfo.FS );
        // String version = h[0];
        String repoId = h[1];

        // // compare version
        // if ( !VERSION.equals( version ) )
        // {
        // throw new UnsupportedExistingLuceneIndexException(
        // "The existing index has version [" + version + "] and not [" + VERSION + "] version!" );
        // }

        if ( getRepositoryId() == null )
        {
            repositoryId = repoId;
        }
        else if ( !getRepositoryId().equals( repoId ) )
        {
            throw new UnsupportedExistingLuceneIndexException( "The existing index is for repository " //
                + "[" + repoId + "] and not for repository [" + getRepositoryId() + "]" );
        }
    }

    private void storeDescriptor()
        throws IOException
    {
        Document hdr = new Document();

        hdr.add( new Field( FLD_DESCRIPTOR, FLD_DESCRIPTOR_CONTENTS, Field.Store.YES, Field.Index.UN_TOKENIZED ) );

        hdr
            .add( new Field( FLD_IDXINFO, VERSION + ArtifactInfo.FS + getRepositoryId(), Field.Store.YES,
                             Field.Index.NO ) );

        IndexWriter w = getIndexWriter();

        w.updateDocument( DESCRIPTOR_TERM, hdr );

        w.flush();
    }

    private void deleteIndexFiles()
        throws IOException
    {
        String[] names = indexDirectory.list();

        if ( names != null )
        {
            for ( int i = 0; i < names.length; i++ )
            {
                indexDirectory.deleteFile( names[i] );
            }
        }

        IndexUtils.deleteTimestamp( indexDirectory );
    }

    public boolean isSearchable()
    {
        return searchable;
    }

    public void setSearchable( boolean searchable )
    {
        this.searchable = searchable;
    }

    public String getId()
    {
        return id;
    }

    public void updateTimestamp()
        throws IOException
    {
        updateTimestamp( false );
    }

    public void updateTimestamp( boolean save )
        throws IOException
    {
        updateTimestamp( save, new Date() );
    }

    public void updateTimestamp( boolean save, Date timestamp )
        throws IOException
    {
        this.timestamp = timestamp;

        if ( save )
        {
            IndexUtils.updateTimestamp( indexDirectory, getTimestamp() );
        }
    }

    public Date getTimestamp()
    {
        return timestamp;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public File getRepository()
    {
        return repository;
    }

    public String getRepositoryUrl()
    {
        return repositoryUrl;
    }

    public String getIndexUpdateUrl()
    {
        if ( repositoryUrl != null )
        {
            if ( indexUpdateUrl == null || indexUpdateUrl.trim().length() == 0 )
            {
                return repositoryUrl + ( repositoryUrl.endsWith( "/" ) ? "" : "/" ) + INDEX_DIRECTORY;
            }
        }
        return indexUpdateUrl;
    }

    public Analyzer getAnalyzer()
    {
        return new NexusAnalyzer();
    }

    public IndexWriter getIndexWriter()
        throws IOException
    {
        synchronized ( indexLock )
        {
            if ( indexWriter == null || indexWriter.isClosed() )
            {
                indexWriter = new NexusIndexWriter( getIndexDirectory(), new NexusAnalyzer(), false );

                indexWriter.setRAMBufferSizeMB( 2 );

                indexWriter.setMergeScheduler( new SerialMergeScheduler() );
            }

            return indexWriter;
        }
    }

    public IndexReader getIndexReader()
        throws IOException
    {
        synchronized ( indexLock )
        {
            if ( indexReader == null || !indexReader.isCurrent() )
            {
                if ( indexReader != null )
                {
                    indexReader.close();
                }

                indexReader = IndexReader.open( indexDirectory );
            }

            return indexReader;
        }
    }

    public IndexSearcher getIndexSearcher()
        throws IOException
    {
        synchronized ( indexLock )
        {
            if ( indexSearcher == null || getIndexReader() != indexSearcher.getIndexReader() )
            {
                if ( indexSearcher != null )
                {
                    indexSearcher.close();

                    // the reader was supplied explicitly
                    indexSearcher.getIndexReader().close();
                }

                indexSearcher = new NexusIndexSearcher( this );
            }

            return indexSearcher;
        }
    }

    public void optimize()
        throws CorruptIndexException, IOException
    {
        IndexWriter w = getIndexWriter();

        try
        {
            w.optimize();

            w.flush();
        }
        finally
        {
            w.close();
        }
    }

    public void close( boolean deleteFiles )
        throws IOException
    {
        if ( indexDirectory != null )
        {
            synchronized ( indexLock )
            {
                IndexUtils.updateTimestamp( indexDirectory, getTimestamp() );

                closeReaders();

                if ( deleteFiles )
                {
                    deleteIndexFiles();
                }

                indexDirectory.close();
            }
        }

        // TODO: this will prevent from reopening them, but needs better solution
        indexDirectory = null;
    }

    public void purge()
        throws IOException
    {
        synchronized ( indexLock )
        {
            closeReaders();

            deleteIndexFiles();

            try
            {
                prepareIndex( true );
            }
            catch ( UnsupportedExistingLuceneIndexException e )
            {
                // just deleted it
            }

            rebuildGroups();

            updateTimestamp( true, null );
        }
    }

    // XXX need some locking for reader/writer
    public void replace( Directory directory )
        throws IOException
    {
        synchronized ( indexLock )
        {
            closeReaders();

            deleteIndexFiles();

            Directory.copy( directory, indexDirectory, false );

            // reclaim the index as mine
            storeDescriptor();

            timestamp = IndexUtils.getTimestamp( directory );

            IndexUtils.updateTimestamp( indexDirectory, getTimestamp() );

            optimize();
        }
    }

    public void merge( Directory directory )
        throws IOException
    {
        merge( directory, null );
    }

    public void merge( Directory directory, DocumentFilter filter )
        throws IOException
    {
        synchronized ( indexLock )
        {
            closeReaders();

            IndexWriter w = getIndexWriter();

            IndexSearcher s = getIndexSearcher();

            IndexReader r = IndexReader.open( directory );

            try
            {
                int numDocs = r.maxDoc();

                for ( int i = 0; i < numDocs; i++ )
                {
                    if ( r.isDeleted( i ) )
                    {
                        continue;
                    }

                    Document d = r.document( i );

                    if ( filter != null && !filter.accept( d ) )
                    {
                        continue;
                    }

                    String uinfo = d.get( ArtifactInfo.UINFO );

                    if ( uinfo != null )
                    {
                        Hits hits = s.search( new TermQuery( new Term( ArtifactInfo.UINFO, uinfo ) ) );

                        if ( hits.length() == 0 )
                        {
                            w.addDocument( IndexUtils.updateDocument( d, this, false ) );
                        }
                    }
                    else
                    {
                        String deleted = d.get( ArtifactInfo.DELETED );

                        if ( deleted != null )
                        {
                            // Deleting the document loses history that it was delete,
                            // so incrementals wont work. Therefore, put the delete
                            // document in as well
                            w.deleteDocuments( new Term( ArtifactInfo.UINFO, deleted ) );
                            w.addDocument( d );
                        }
                    }
                }

            }
            finally
            {
                r.close();
                closeReaders();
            }

            rebuildGroups();

            Date mergedTimestamp = IndexUtils.getTimestamp( directory );

            if ( getTimestamp() != null && mergedTimestamp != null && mergedTimestamp.after( getTimestamp() ) )
            {
                // we have both, keep the newest
                updateTimestamp( true, mergedTimestamp );
            }
            else
            {
                updateTimestamp( true );
            }

            optimize();
        }
    }

    private void closeReaders()
        throws CorruptIndexException, IOException
    {
        if ( indexWriter != null )
        {
            if ( !indexWriter.isClosed() )
            {
                indexWriter.close();
            }

            indexWriter = null;
        }
        if ( indexSearcher != null )
        {
            indexSearcher.close();

            // the reader was supplied explicitly
            indexSearcher.getIndexReader().close();

            indexSearcher = null;
        }
        if ( indexReader != null )
        {
            indexReader.close();

            indexReader = null;
        }
    }

    public GavCalculator getGavCalculator()
    {
        return gavCalculator;
    }

    public List<IndexCreator> getIndexCreators()
    {
        return Collections.unmodifiableList( indexCreators );
    }

    // groups

    public void rebuildGroups()
        throws IOException
    {
        synchronized ( indexLock )
        {
            IndexUtils.rebuildGroups( this );
        }
    }

    /*
     * (non-Javadoc)
     * @see org.sonatype.nexus.index.context.IndexingContext#getAllGroups()
     */
    public Set<String> getAllGroups()
        throws IOException
    {
        return IndexUtils.getAllGroups( this );
    }

    public void setAllGroups( Collection<String> groups )
        throws IOException
    {
        IndexUtils.setAllGroups( this, groups );
    }

    public Set<String> getRootGroups()
        throws IOException
    {
        return IndexUtils.getRootGroups( this );
    }

    public void setRootGroups( Collection<String> groups )
        throws IOException
    {
        IndexUtils.setRootGroups( this, groups );
    }

    @Override
    public String toString()
    {
        return id + " : " + timestamp;
    }

}
