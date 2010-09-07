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
package org.apache.maven.mercury.repository.tests;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.maven.mercury.MavenDependencyProcessor;
import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.MercuryLoggerManager;
import org.apache.maven.mercury.repository.api.ArtifactResults;
import org.apache.maven.mercury.repository.api.LocalRepository;
import org.apache.maven.mercury.repository.api.RemoteRepository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.api.RepositoryWriter;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.local.m2.MetadataProcessorMock;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;
import org.apache.maven.mercury.spi.http.server.HttpTestServer;
import org.apache.maven.mercury.transport.api.Server;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id: ReadWriteTest.java 762963 2009-04-07 21:01:07Z ogusakov $
 *
 */
public class ReadWriteTest
extends TestCase
{
  private static final IMercuryLogger log = MercuryLoggerManager.getLogger( ReadWriteTest.class ); 

  File remoteRepoBase = new File("./target/test-classes/repo");
  public String port;
  HttpTestServer httpServer;

  RemoteRepository rr;
  LocalRepository  lr;

  DependencyProcessor mdProcessor;
  RepositoryReader reader;

  File localRepoBase;
  RepositoryWriter writer;

  List<ArtifactMetadata> query;
  
  ArtifactMetadata bmd;
  
  Server server;
  
  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
      throws Exception
  {
    httpServer = new HttpTestServer( remoteRepoBase, "/repo" );
    httpServer.start();
    port = String.valueOf( httpServer.getPort() );

    server = new Server( "test", new URL("http://localhost:"+port+"/repo") );
    rr = new RemoteRepositoryM2( "testRepo", server, new MavenDependencyProcessor() );

    mdProcessor = new MetadataProcessorMock();
    rr.setDependencyProcessor( mdProcessor );
    reader = rr.getReader();
    
    localRepoBase = File.createTempFile( "local", "repo" );
    localRepoBase.delete();
    localRepoBase.mkdir();
    log.info("local repo is in "+localRepoBase);
    
    lr = new LocalRepositoryM2( "lr", localRepoBase, new MavenDependencyProcessor() );
    writer = lr.getWriter(); 

    query = new ArrayList<ArtifactMetadata>();
  }

  protected void tearDown()
  throws Exception
  {
    super.tearDown();
    httpServer.stop();
    httpServer.destroy();
  }
  
  public void testOneArtifact()
  throws IllegalArgumentException, RepositoryException
  {
    bmd = new ArtifactMetadata("a:a:4");
    query.add( bmd );
    
    ArtifactResults res = reader.readArtifacts( query );
    
    assertTrue( res != null );
    assertFalse( res.hasExceptions() );
    assertTrue( res.hasResults() );
    
    Map< ArtifactMetadata, List<Artifact>> resMap = res.getResults();
    
    assertNotNull( resMap );
    assertFalse( resMap.isEmpty() );
    
    List<Artifact> al = resMap.get( bmd );
    
    assertNotNull( al );
    assertFalse( al.isEmpty() );
    
    Artifact a = al.get( 0 );
    
    writer.writeArtifacts( al );
    
    File aBin = new File( localRepoBase, "a/a/4/a-4.jar" );
    assertTrue( aBin.exists() );
    
    File aPom = new File( localRepoBase, "a/a/4/a-4.pom" );
    assertTrue( aPom.exists() );
    
    assertNotNull( a.getPomBlob() );
    assertTrue( a.getPomBlob().length > 10 );
    log.info( a+" - pom length is "+a.getPomBlob().length );
  }
  
  public void testOneArtifactWithClassifier()
  throws IllegalArgumentException, RepositoryException
  {
    ArtifactMetadata bm = new ArtifactMetadata("a:a:4:sources");
    query.add( bm );
    
    ArtifactResults res = reader.readArtifacts( query );
    
    assertTrue( res != null );
    assertFalse( res.hasExceptions() );
    assertTrue( res.hasResults() );
    
    Map< ArtifactMetadata, List<Artifact>> resMap = res.getResults();
    
    assertNotNull( resMap );
    assertFalse( resMap.isEmpty() );
    
    List<Artifact> al = resMap.get( bm );
    
    assertNotNull( al );
    assertFalse( al.isEmpty() );
    
    Artifact a = al.get( 0 );
    
    writer.writeArtifacts( al );
    
    File aBin = new File( localRepoBase, "a/a/4/a-4-sources.jar" );
    assertTrue( aBin.exists() );

    log.info( a+" - pom length is "+a.getPomBlob().length );
  }

}
