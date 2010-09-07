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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.QualityRange;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.crypto.api.StreamVerifierAttributes;
import org.apache.maven.mercury.crypto.api.StreamVerifierException;
import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.crypto.pgp.PgpStreamVerifierFactory;
import org.apache.maven.mercury.crypto.sha.SHA1VerifierFactory;
import org.apache.maven.mercury.repository.api.MetadataResults;
import org.apache.maven.mercury.repository.api.ArtifactResults;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.transport.api.Server;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id: AbstractRepositoryReaderM2Test.java 762963 2009-04-07 21:01:07Z ogusakov $
 *
 */
public abstract class AbstractRepositoryReaderM2Test
extends TestCase
{
  Repository repo;
  DependencyProcessor mdProcessor;
  RepositoryReader reader;
  List<ArtifactMetadata> query;
  
  ArtifactMetadata bmd;
  
  private static final String publicKeyFile = "/pgp/pubring.gpg";
  private static final String secretKeyFile = "/pgp/secring.gpg";
  
  boolean goodOs = false;
  
  Server server;
  HashSet<StreamVerifierFactory> factories;
  
  
  @Override
  protected void setUp()
  throws Exception
  {
    String os = System.getProperty("os.name");
    
    if( "Mac OS X".equals( os ) )
      goodOs = true;
    
    File sn = new File("target/test-classes/repo/a/a/5-SNAPSHOT/a-5-SNAPSHOT.jar");
    
    sn.setLastModified( new Date().getTime() + 10000L );
    
  }
  
    @Override
    protected void tearDown()
        throws Exception
    {
        if( query != null )
            query.clear();
    }
  //------------------------------------------------------------------------------
  public void testReadReleaseVersion()
  throws IllegalArgumentException, RepositoryException
  {
    bmd = new ArtifactMetadata("a:a:[3,3]");
    query.add( bmd );
    
    MetadataResults res = reader.readVersions( query );
    
    assertNotNull( res );
    assertFalse( res.hasExceptions() );
    assertTrue( res.hasResults() );
    
    assertEquals( 1, res.getResults().size() );
    
    List<ArtifactMetadata> ror = res.getResult( bmd );
    
    assertNotNull( ror );
    
    if( res.hasExceptions() )
      System.out.println( res.getExceptions() );

    assertEquals( 1, ror.size() );
    
//    System.out.println(qr);
  }
  //------------------------------------------------------------------------------
  public void testReadReleaseRange()
  throws IllegalArgumentException, RepositoryException
  {
    repo.setRepositoryQualityRange( QualityRange.RELEASES_ONLY );
  
    bmd = new ArtifactMetadata("a:a:[3,)");
    query.add( bmd );
    
    MetadataResults res = reader.readVersions( query );
    
    assertNotNull( res );
    
    if( res.hasExceptions() )
      System.out.println( res.getExceptions() );

    assertFalse( res.hasExceptions() );
    assertTrue( res.hasResults() );
    
    assertEquals( 1, res.getResults().size() );
    
    List<ArtifactMetadata> qr = res.getResult( bmd );
    
    assertNotNull( qr );
    assertTrue( qr.size() > 1 );
    
    assertFalse( qr.contains( new ArtifactMetadata("a:a:5-SNAPSHOT") ) );
    
    System.out.println("query "+bmd+"->"+qr);
    
    MetadataResults depRes = reader.readDependencies( qr );
    
    assertNotNull( depRes );
    assertFalse( depRes.hasExceptions() );
    assertTrue( depRes.hasResults() );
    
    ArtifactMetadata a3 = new ArtifactMetadata("a:a:3");
    
    assertTrue( depRes.hasResults( a3 ) );
    
    List<ArtifactMetadata> deps = depRes.getResult( a3 );
    assertNotNull( deps );
    assertFalse( deps.isEmpty() );

    System.out.println(deps);

    assertTrue( deps.contains( new ArtifactMetadata("b:b:2") ) );
    assertTrue( deps.contains( new ArtifactMetadata("c:c:(1,)") ) );
    
  }
  //------------------------------------------------------------------------------
  public void testReadArtifacts()
  throws IllegalArgumentException, RepositoryException
  {
    bmd = new ArtifactMetadata("a:a:3");
    query.add( bmd );

    ArtifactResults ror = reader.readArtifacts( query );
    
    assertNotNull( ror );
    
    if( ror.hasExceptions() )
      System.out.println( ror.getExceptions() );
    
    assertFalse( ror.hasExceptions() );
    assertTrue( ror.hasResults() );
    
    List<Artifact> res = ror.getResults(bmd);
    
    assertNotNull( res );
    assertEquals( 1, res.size() );
    
    Artifact da = res.get( 0 );
    
    assertNotNull( da );
    assertNotNull( da.getFile() );
    assertTrue( da.getFile().exists() );
    assertNotNull( da.getPomBlob() );
  }
  //------------------------------------------------------------------------------
  public void testReadSnapshot()
  throws IllegalArgumentException, RepositoryException
  {
    bmd = new ArtifactMetadata("a:a:5-SNAPSHOT");
    query.add( bmd );

    ArtifactResults ror = reader.readArtifacts( query );
    
    assertNotNull( ror );
    
    if( ror.hasExceptions() )
      System.out.println( ror.getExceptions() );
    
    assertFalse( ror.hasExceptions() );
    assertTrue( ror.hasResults() );
    
    List<Artifact> res = ror.getResults(bmd);
    
    assertNotNull( res );
    assertEquals( 1, res.size() );
    
    Artifact da = res.get( 0 );
    
    assertNotNull( da );
    assertNotNull( da.getFile() );
    assertTrue( da.getFile().exists() );
    assertEquals( 159630, da.getFile().length() );
    assertNotNull( da.getPomBlob() );
  }
  //------------------------------------------------------------------------------
  public void testReadSnapshotTS()
  throws IllegalArgumentException, RepositoryException
  {
    bmd = new ArtifactMetadata("a:a:5-20080807.234713-11");
    query.add( bmd );

    ArtifactResults ror = reader.readArtifacts( query );
    
    assertNotNull( ror );
    
    if( ror.hasExceptions() )
      System.out.println( ror.getExceptions() );
    
    assertFalse( ror.hasExceptions() );
    assertTrue( ror.hasResults() );
    
    List<Artifact> res = ror.getResults(bmd);
    
    assertNotNull( res );
    assertEquals( 1, res.size() );
    
    Artifact da = res.get( 0 );
    
    assertNotNull( da );
    assertNotNull( da.getFile() );
    assertTrue( da.getFile().exists() );
    assertEquals( 14800, da.getFile().length() );
    assertNotNull( da.getPomBlob() );
  }
  //------------------------------------------------------------------------------
  public void testReadVersionsLatest()
  throws IllegalArgumentException, RepositoryException
  {
    bmd = new ArtifactMetadata("a:a:LATEST");
    query.add( bmd );

    MetadataResults ror = reader.readVersions( query );
    
    assertNotNull( ror );
    
    if( ror.hasExceptions() )
    {
      Map<ArtifactMetadata, Exception> exs = ror.getExceptions();
      
      for( ArtifactMetadata bmd : exs.keySet() )
      {
        System.out.println( "\n==========> "+bmd.toString());
        exs.get( bmd ).printStackTrace();
      }
    }
    
    assertFalse( ror.hasExceptions() );
    assertTrue( ror.hasResults() );
    
    List<ArtifactMetadata> deps = ror.getResult(bmd);
    
    assertNotNull( deps );
    assertEquals( 1, deps.size() );
    assertTrue( deps.contains( new ArtifactMetadata("a:a:5-SNAPSHOT") ) );
    
  }
  //------------------------------------------------------------------------------
  public void testReadVersionsRelease()
  throws IllegalArgumentException, RepositoryException
  {
    bmd = new ArtifactMetadata("a:a:RELEASE");
    query.add( bmd );

    MetadataResults ror = reader.readVersions( query );
    
    assertNotNull( ror );
    
    if( ror.hasExceptions() )
      System.out.println( ror.getExceptions() );
    
    assertFalse( ror.hasExceptions() );
    assertTrue( ror.hasResults() );
    
    List<ArtifactMetadata> deps = ror.getResult(bmd);
    
    assertNotNull( deps );
    assertEquals( 1, deps.size() );
    assertTrue( deps.contains( new ArtifactMetadata("a:a:4") ) );
    
  }
  //------------------------------------------------------------------------------
  public void testReadLatest()
  throws IllegalArgumentException, RepositoryException
  {
    bmd = new ArtifactMetadata("a:a:LATEST");
    query.add( bmd );

    ArtifactResults ror = reader.readArtifacts( query );
    
    assertNotNull( ror );
    
    if( ror.hasExceptions() )
      System.out.println( ror.getExceptions() );
  
    assertFalse( ror.hasExceptions() );
    assertTrue( ror.hasResults() );
    
    List<Artifact> res = ror.getResults(bmd);
    
    assertNotNull( res );
    assertEquals( 1, res.size() );
    
    Artifact da = res.get( 0 );
    
    assertNotNull( da );
    assertEquals( "5-SNAPSHOT", da.getVersion() );
    
    assertNotNull( da.getFile() );
    assertTrue( da.getFile().exists() );
    assertEquals( 159630, da.getFile().length() );
    assertNotNull( da.getPomBlob() );
    
  }
  //------------------------------------------------------------------------------
  public void testReadRelease()
  throws IllegalArgumentException, RepositoryException
  {
    bmd = new ArtifactMetadata("a:a:RELEASE");
    query.add( bmd );

    ArtifactResults ror = reader.readArtifacts( query );
    
    assertNotNull( ror );
    
    if( ror.hasExceptions() )
      System.out.println( ror.getExceptions() );
    
    assertFalse( ror.hasExceptions() );
    assertTrue( ror.hasResults() );
    
    List<Artifact> res = ror.getResults(bmd);
    
    assertNotNull( res );
    assertEquals( 1, res.size() );
    
    Artifact da = res.get( 0 );
    
    assertNotNull( da );
    assertEquals( "4", da.getVersion() );
    
    assertNotNull( da.getFile() );
    assertTrue( da.getFile().exists() );
    assertEquals( 14800, da.getFile().length() );
    assertNotNull( da.getPomBlob() );
  }
  //------------------------------------------------------------------------------
  public void testReadAndVerifyGoodArtifact()
  throws IllegalArgumentException, RepositoryException, StreamVerifierException
  {
    // verifiers
    factories = new HashSet<StreamVerifierFactory>();       
    factories.add( 
        new PgpStreamVerifierFactory(
                new StreamVerifierAttributes( PgpStreamVerifierFactory.DEFAULT_EXTENSION, false, true )
                , getClass().getResourceAsStream( publicKeyFile )
                                    )
                  );
    factories.add( new SHA1VerifierFactory( true, false ) );
    
    if( goodOs )
      server.setReaderStreamVerifierFactories(factories);

    bmd = new ArtifactMetadata("a:a:4");
    query.add( bmd );

    ArtifactResults ror = reader.readArtifacts( query );
    
    assertNotNull( ror );
    
    if( ror.hasExceptions() )
    {
      System.out.println("===> unexpected Exceptions");
      for( Exception e : ror.getExceptions().values() )
        System.out.println( e.getClass().getName()+": "+e.getMessage() );
      System.out.println("<=== unexpected Exceptions");
    }
    
    assertFalse( ror.hasExceptions() );
    assertTrue( ror.hasResults() );
    
    List<Artifact> res = ror.getResults(bmd);
    
    assertNotNull( res );
    assertEquals( 1, res.size() );
    
    Artifact da = res.get( 0 );
    
    assertNotNull( da );
    assertNotNull( da.getFile() );
    assertTrue( da.getFile().exists() );
    assertNotNull( da.getPomBlob() );
  }
  //------------------------------------------------------------------------------
  public void testReadAndVerifyArtifactNoSig()
  throws IllegalArgumentException, StreamVerifierException
  {
    // verifiers
    factories = new HashSet<StreamVerifierFactory>();       
    factories.add( 
        new PgpStreamVerifierFactory(
                new StreamVerifierAttributes( PgpStreamVerifierFactory.DEFAULT_EXTENSION, false, true )
                , getClass().getResourceAsStream( publicKeyFile )
                                    )
                  );
    
    server.setReaderStreamVerifierFactories(factories);

    bmd = new ArtifactMetadata("a:a:3");
    query.add( bmd );

    ArtifactResults ror = null;
    try
    {
      ror = reader.readArtifacts( query );
    }
    catch( RepositoryException e )
    {
      System.out.println( "Expected exception: "+e.getMessage() );
      return;
    }
    assertNotNull( ror );
    if( !ror.hasExceptions() )
      fail( "Artifact a:a:3 does not have .asc signature, PGP verifier is not lenient, but this did not cause a RepositoryException" );

    System.out.println("Expected Exceptions: "+ror.getExceptions() );
  }
  //------------------------------------------------------------------------------
  public void testReadAndVerifyArtifactBadSig()
  throws IllegalArgumentException, StreamVerifierException
  {
    // verifiers
    factories = new HashSet<StreamVerifierFactory>();       
    factories.add( 
        new PgpStreamVerifierFactory(
                new StreamVerifierAttributes( PgpStreamVerifierFactory.DEFAULT_EXTENSION, false, true )
                , getClass().getResourceAsStream( publicKeyFile )
                                    )
                  );
    server.setReaderStreamVerifierFactories(factories);

    bmd = new ArtifactMetadata("a:a:2");
    query.add( bmd );

    ArtifactResults ror = null;
    try
    {
      ror = reader.readArtifacts( query );
    }
    catch( RepositoryException e )
    {
      System.out.println( "Expected exception: "+e.getMessage() );
      return;
    }
    assertNotNull( ror );
    if( !ror.hasExceptions() )
      fail( "Artifact a:a:2 does have a bad .asc (PGP) signature, PGP verifier is not lenient, but this did not cause a RepositoryException" );

    System.out.println("Expected Exceptions: "+ror.getExceptions() );
  }
  //------------------------------------------------------------------------------
  public void testReadAndVerifyArtifactNoSigLenientVerifier()
  throws IllegalArgumentException, StreamVerifierException
  {
    // verifiers
    factories = new HashSet<StreamVerifierFactory>();       
    factories.add( 
        new PgpStreamVerifierFactory(
                new StreamVerifierAttributes( PgpStreamVerifierFactory.DEFAULT_EXTENSION, true, true )
                , getClass().getResourceAsStream( publicKeyFile )
                                    )
                  );
    if( goodOs )
    {
      factories.add( new SHA1VerifierFactory(true,false) );
      server.setReaderStreamVerifierFactories(factories);
    }

    bmd = new ArtifactMetadata("a:a:3");
    query.add( bmd );

    ArtifactResults ror = null;
    try
    {
      ror = reader.readArtifacts( query );
    }
    catch( RepositoryException e )
    {
      fail( "Artifact a:a:3 does not have .asc signature, PGP verifier is lenient, but still caused a RepositoryException: "+e.getMessage() );
    }
    if( ror.hasExceptions() )
      fail( "Artifact a:a:3 does not have .asc signature, PGP verifier is lenient, but still caused exceptions: "+ror.getExceptions() );
  }
  //------------------------------------------------------------------------------
  //------------------------------------------------------------------------------
}
