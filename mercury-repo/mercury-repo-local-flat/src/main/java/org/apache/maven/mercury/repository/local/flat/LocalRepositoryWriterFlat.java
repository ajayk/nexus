/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.maven.mercury.repository.local.flat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.MercuryLoggerManager;
import org.apache.maven.mercury.repository.api.AbstractRepository;
import org.apache.maven.mercury.repository.api.AbstractRepositoryWriter;
import org.apache.maven.mercury.repository.api.LocalRepository;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryWriter;
import org.apache.maven.mercury.transport.api.Server;
import org.apache.maven.mercury.util.FileLockBundle;
import org.apache.maven.mercury.util.FileUtil;
import org.apache.maven.mercury.util.Util;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

public class LocalRepositoryWriterFlat
extends AbstractRepositoryWriter
implements RepositoryWriter
{
  public static final String SYSTEM_PROPERTY_PARALLEL_WORKERS = "mercury.local.repo.workers";
  public static final int  PARALLEL_WORKERS = Integer.parseInt( System.getProperty( SYSTEM_PROPERTY_PARALLEL_WORKERS, "4" ) );
  
  public static final long SLEEP_FOR_WORKERS_TICK = 20l;

  public static final String SYSTEM_PROPERTY_SLEEP_FOR_LOCK = "mercury.local.lock.wait.millis";
  public static final long SLEEP_FOR_LOCK = Long.parseLong(  System.getProperty( SYSTEM_PROPERTY_SLEEP_FOR_LOCK, "5000" ) );
  
  public static final long SLEEP_FOR_LOCK_TICK = 5l;

  private static final IMercuryLogger LOG = MercuryLoggerManager.getLogger( LocalRepositoryWriterFlat.class ); 
  private static final Language LANG = new DefaultLanguage( LocalRepositoryWriterFlat.class );
  //---------------------------------------------------------------------------------------------------------------
  private static final String [] _protocols = new String [] { "file" };
  
  private final LocalRepository _repo;
  private final File _repoDir;
  private final ArtifactQueue _aq;

  private static final ArifactWriteData LAST_ARTIFACT = new ArifactWriteData( null, null );
  
  //---------------------------------------------------------------------------------------------------------------
  public LocalRepositoryWriterFlat( LocalRepository repo )
  {
    if( repo == null )
      throw new IllegalArgumentException("localRepo cannot be null");
    
    _repoDir = repo.getDirectory();
    if( _repoDir == null )
      throw new IllegalArgumentException("localRepo directory cannot be null");
    
    if( !_repoDir.exists() )
      throw new IllegalArgumentException("localRepo directory \""+_repoDir.getAbsolutePath()+"\" should exist");

    _repo = repo;
    _aq = null;
  }
  //---------------------------------------------------------------------------------------------------------------
  private LocalRepositoryWriterFlat( LocalRepository repo, File repoDir, ArtifactQueue aq )
  {
    _repo = repo;
    _repoDir = repoDir;
    _aq = aq;
  }
  //---------------------------------------------------------------------------------------------------------------
  public Repository getRepository()
  {
    return _repo;
  }
  //---------------------------------------------------------------------------------------------------------------
  public boolean canHandle( String protocol )
  {
    return AbstractRepository.DEFAULT_LOCAL_READ_PROTOCOL.equals( protocol );
  }
  //---------------------------------------------------------------------------------------------------------------
  public String[] getProtocols()
  {
    return _protocols;
  }
  //---------------------------------------------------------------------------------------------------------------
  public void close()
  {
  }
  //---------------------------------------------------------------------------------------------------------------
  public void writeArtifacts( Collection<Artifact> artifacts )
  throws RepositoryException
  {
    if( artifacts == null || artifacts.size() < 1 )
      return;
    
    int nWorkers = PARALLEL_WORKERS;
    if( artifacts.size() < nWorkers )
      nWorkers = artifacts.size();
    
    ArtifactQueue aq = new ArtifactQueue();
    LocalRepositoryWriterFlat [] workers = new LocalRepositoryWriterFlat[ nWorkers ];
    
    for( int i=0; i<nWorkers; i++ )
      workers[ i ] = new LocalRepositoryWriterFlat( _repo, _repoDir, aq );
    
    for( Artifact artifact : artifacts )
    {
      Set<StreamVerifierFactory> vFacs = null;
      Server server = _repo.getServer();
      if( server != null && server.hasWriterStreamVerifierFactories() )
        vFacs = server.getWriterStreamVerifierFactories();
      
      if( vFacs == null ) // let it be empty, but not null
        vFacs = new HashSet<StreamVerifierFactory>(1);

      aq.addArtifact( new ArifactWriteData( artifact, vFacs ) );
    }
    aq.addArtifact( LAST_ARTIFACT );

    for( int i=0; i<nWorkers; i++ )
      workers[ i ].start();
    
    boolean alive = true;
    while( alive )
    {
      alive = false;
      for( int i=0; i<nWorkers; i++ )
        if( workers[ i ].isAlive() )
        {
          alive = true;
          try { sleep( SLEEP_FOR_WORKERS_TICK ); } catch( InterruptedException ie ) {}
        }
    }
  }
  //---------------------------------------------------------------------------------------------------------------
  /* (non-Javadoc)
   * @see java.lang.Thread#run()
   */
  @Override
  public void run()
  {
    try
    {
      for(;;)
      {
        ArifactWriteData awd = _aq.getArtifact();

        if( awd == null || awd.artifact == null )
            break;
        
        writeArtifact( awd.artifact, awd.vFacs );
      }
    }
    catch (InterruptedException e)
    {
    }
    catch( RepositoryException e )
    {
      throw new RuntimeException(e);
    }
  }
  //---------------------------------------------------------------------------------------------------------------
  public void writeArtifact( final Artifact artifact, final Set<StreamVerifierFactory> vFacs )
      throws RepositoryException
  {
    if( artifact == null )
      return;
    
    boolean isPom = "pom".equals( artifact.getType() );
    
    byte [] pomBlob = artifact.getPomBlob();
    boolean hasPomBlob = pomBlob != null && pomBlob.length > 0;
    
    InputStream in = artifact.getStream();
    if( in == null )
    {
      File aFile = artifact.getFile();
      if( aFile == null && !isPom )
      {
        throw new RepositoryException( LANG.getMessage( "artifact.no.stream", artifact.toString() ) );
      }

      try
      {
        in = new FileInputStream( aFile );
      }
      catch( FileNotFoundException e )
      {
        if( !isPom )
          throw new RepositoryException( e );
      }
    }

    String relGroupPath = ((LocalRepositoryFlat)_repo).isCreateGroupFolders() ? artifact.getGroupId() : "";
    String versionPath = _repoDir.getAbsolutePath() + (Util.isEmpty( relGroupPath ) ? "" : "/"+relGroupPath);
    
    String lockDir = null;
    FileLockBundle fLock = null;

    try
    {
      
      if( isPom )
      {
        if( in == null && !hasPomBlob )
          throw new RepositoryException( LANG.getMessage( "pom.artifact.no.stream", artifact.toString() ) );
        
        if( in != null )
        {
          byte [] pomBlobBytes = FileUtil.readRawData( in );
          hasPomBlob = pomBlobBytes != null && pomBlobBytes.length > 0;
          if( hasPomBlob )
            pomBlob = pomBlobBytes;
        }
      }

      // create folders
      lockDir = versionPath;

      File gav = new File( lockDir );
      gav.mkdirs();

      fLock = FileUtil.lockDir( lockDir, SLEEP_FOR_LOCK, SLEEP_FOR_LOCK_TICK );
      if( fLock == null )
        throw new RepositoryException( LANG.getMessage( "cannot.lock.gav", lockDir, ""+SLEEP_FOR_LOCK ) );

      String fName = versionPath+'/'+artifact.getBaseName()+'.'+artifact.getType();
      
      if( !isPom ) // first - take care of the binary
        FileUtil.writeAndSign( fName, in, vFacs );

      // if classier - nothing else to do :)
      if( artifact.hasClassifier() )
        return;
      
      if( ((LocalRepositoryFlat)_repo).isCreatePoms() && hasPomBlob )
      {
        FileUtil.writeAndSign( versionPath
                              +'/'+artifact.getArtifactId()+'-'+artifact.getVersion()+".pom", pomBlob, vFacs
                              );
      }
        
    }
    catch( Exception e )
    {
      throw new RepositoryException( e );
    }
    finally
    {
      if( fLock != null )
        fLock.release();
    }
    
  }
  //---------------------------------------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------------------------------------
}
//=================================================================================================================
class ArifactWriteData
{
  Artifact artifact;
  Set<StreamVerifierFactory> vFacs;
  
  public ArifactWriteData(Artifact artifact, Set<StreamVerifierFactory> vFacs)
  {
    this.artifact = artifact;
    this.vFacs = vFacs;
  }
}
//=================================================================================================================
class ArtifactQueue
{
  LinkedList<ArifactWriteData> queue = new LinkedList<ArifactWriteData>();
  boolean empty = false;
  
  public synchronized void addArtifact( ArifactWriteData awd )
  {
    queue.addLast( awd );
    empty = false;
    notify();
  }

  public synchronized ArifactWriteData getArtifact()
  throws InterruptedException
  {
    if( empty )
      return null;

    while( queue.isEmpty() )
      wait();
    
    ArifactWriteData res = queue.removeFirst();
    
    if( res.artifact == null )
    {
      empty = true;
      return null;
    }

    return res;
  }
}
//=================================================================================================================
