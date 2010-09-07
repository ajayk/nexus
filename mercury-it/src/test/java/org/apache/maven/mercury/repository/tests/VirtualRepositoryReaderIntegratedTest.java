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
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.maven.mercury.MavenDependencyProcessor;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.repository.api.MetadataResults;
import org.apache.maven.mercury.repository.api.ArtifactResults;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryMetadataCache;
import org.apache.maven.mercury.repository.api.RepositoryUpdateIntervalPolicy;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.local.m2.MetadataProcessorMock;
import org.apache.maven.mercury.repository.metadata.AddVersionOperation;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.StringOperand;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;
import org.apache.maven.mercury.repository.virtual.VirtualRepositoryReader;
import org.apache.maven.mercury.spi.http.server.HttpTestServer;
import org.apache.maven.mercury.transport.api.Server;
import org.apache.maven.mercury.util.FileUtil;
import org.apache.maven.mercury.util.Util;

public class VirtualRepositoryReaderIntegratedTest
extends TestCase
{
  File _testBase;
  File _localRepoBase;
  
  public String _port;
  HttpTestServer _server;
  
  List<ArtifactMetadata> _query;
  
  RemoteRepositoryM2 _remoteRepo;
  LocalRepositoryM2  _localRepo;
  
  
  VirtualRepositoryReader _vr;

  //-------------------------------------------------------------------------
  @Override
  protected void setUp()
  throws Exception
  {
    _testBase = new File("./target/test-classes/repoVr");
    _localRepoBase = new File("./target/localRepo");
    
    FileUtil.delete( _localRepoBase );
    _localRepoBase.mkdirs();
    
    _server = new HttpTestServer( _testBase, "/repo" );
    _server.start();
    _port = String.valueOf( _server.getPort() );

    _query = new ArrayList<ArtifactMetadata>();

    DependencyProcessor mdProcessor = new MetadataProcessorMock();

    Server server = new Server( "testRemoteRepo", new URL("http://localhost:"+_port+"/repo") );
    _remoteRepo = new RemoteRepositoryM2( server, new MavenDependencyProcessor() );
    _remoteRepo.setUpdatePolicy( new RepositoryUpdateIntervalPolicy("interval2").setInterval( 2000L ) );
    _remoteRepo.setDependencyProcessor( mdProcessor );
    
    _localRepo = new LocalRepositoryM2( "testLocalRepo", _localRepoBase, new MavenDependencyProcessor() );
    _localRepo.setDependencyProcessor( mdProcessor );
    
    List<Repository> reps = new ArrayList<Repository>();
    reps.add( _remoteRepo );
    reps.add( _localRepo );

    _vr = new VirtualRepositoryReader( reps );
  }
  //-------------------------------------------------------------------------
  @Override
  protected void tearDown()
  throws Exception
  {
    super.tearDown();
    _server.stop();
    _server.destroy();
  }
  //-------------------------------------------------------------------------
  public void testReadArtifact()
  throws Exception
  {
    try
    {
    ArtifactMetadata bmd = new ArtifactMetadata("a:a:[1,)");
    List<ArtifactMetadata> q = THelper.toList( bmd );
    
    MetadataResults vres = _vr.readVersions( q );
     
    assertNotNull( vres );
     
    assertFalse( vres.hasExceptions() );
    
    assertTrue( vres.hasResults() );
    
    assertTrue( vres.hasResults(bmd) );
    
    List<ArtifactMetadata> versions = vres.getResult( bmd );
    
    assertNotNull( versions );
    
    assertEquals( 5, versions.size() );
    
    // add version 6 to GA metadata
    File mdf = new File( _testBase, "a/a/maven-metadata.xml");
    Metadata md = MetadataBuilder.getMetadata( FileUtil.readRawData( mdf ) );
    
    byte [] newBytes = MetadataBuilder.changeMetadata( md, new AddVersionOperation(new StringOperand("6")) );
    
    FileUtil.writeRawData( mdf, newBytes );
    
    // version MD is in memory, there should be still be 5 versions
    vres = _vr.readVersions( q );
    
    assertNotNull( vres );
     
    assertFalse( vres.hasExceptions() );
    
    assertTrue( vres.hasResults() );
    
    assertTrue( vres.hasResults(bmd) );
    
    versions = vres.getResult( bmd );
    
    assertNotNull( versions );
    
    assertEquals( 5, versions.size() );
    
    // clean in-memory cache, so that on-disk expiration rules apply
    RepositoryMetadataCache cache = _vr.getCache();
    
    cache.clearSession();
    
    Thread.sleep( 4000L );
    
    // We are past the expiration point of 5 sec - should now have 6 versions.  
    vres = _vr.readVersions( q );
    
    assertNotNull( vres );
     
    assertFalse( vres.hasExceptions() );
    
    assertTrue( vres.hasResults() );
    
    assertTrue( vres.hasResults(bmd) );
    
    versions = vres.getResult( bmd );
    
    assertNotNull( versions );
    
    assertEquals( 6, versions.size() );
    
    }
    finally
    {
      // restore back 5 versions
      File mdf = new File( _testBase, "a/a/maven-metadata.xml");
      InputStream in = VirtualRepositoryReaderIntegratedTest.class.getResourceAsStream( "/repoVr/a.a-maven-metadata.xml" );
      FileUtil.writeRawData( in, mdf );
    }
  }
  //-------------------------------------------------------------------------
  public void testReadBadVersions()
  {
    ArtifactMetadata bmd = new ArtifactMetadata("does.not:exist:1.0");
    List<ArtifactMetadata> q = THelper.toList( bmd );
    
    MetadataResults vres = null;
    try
    {
        vres = _vr.readVersions( q );
    }
    catch ( Exception e )
    {
        fail("reading non-existing artifact throws an exception");
    }
     
    assertNull( vres );
    
  }
  //-------------------------------------------------------------------------
  public void testReadBadDependencies()
  {
    ArtifactMetadata bmd = new ArtifactMetadata("does.not:exist:1.0");
    
    ArtifactMetadata vres = null;
    try
    {
        vres = _vr.readDependencies( bmd );
    }
    catch ( Exception e )
    {
        fail("reading non-existing artifact throws an exception");
    }
     
    assertTrue( Util.isEmpty( vres.getDependencies() ) );
    
  }
  //-------------------------------------------------------------------------
  public void testReadBadArtifact()
  {
      ArtifactMetadata bmd = new ArtifactMetadata("does.not:exist:1.0");
      List<ArtifactMetadata> q = THelper.toList( bmd );
      
    ArtifactResults vres = null;
    try
    {
        vres = _vr.readArtifacts(  q );
    }
    catch ( Exception e )
    {
        fail("reading non-existing artifact throws an exception");
    }
    
    assertNotNull( vres );
    
    assertFalse( vres.hasResults() );
    
  }
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
}
