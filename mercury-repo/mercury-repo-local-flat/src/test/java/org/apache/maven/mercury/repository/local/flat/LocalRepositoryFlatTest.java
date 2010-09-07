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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.DefaultArtifact;
import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.MercuryLoggerManager;
import org.apache.maven.mercury.repository.api.RepositoryWriter;
import org.apache.maven.mercury.util.FileUtil;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id: LocalRepositoryFlatTest.java 762963 2009-04-07 21:01:07Z ogusakov $
 *
 */
public class LocalRepositoryFlatTest
    extends TestCase
{
  private static final IMercuryLogger LOG = MercuryLoggerManager.getLogger( LocalRepositoryFlatTest.class ); 

  File _dir;
  LocalRepositoryFlat _repo;
  
  String repoUrl = "http://repo1.sonatype.org";
//  String repoUrl = "http://repository.sonatype.org/content/groups/public";
  
  Artifact a;
  Artifact b;

  @Override
  protected void setUp()
      throws Exception
  {
    _dir = File.createTempFile( "test-flat-", "-repo" );
    _dir.delete();
    _dir.mkdirs();
    
    _repo = new LocalRepositoryFlat("testFlatRepo", _dir, false, false );
    
    byte [] pomBlob = "pomblob".getBytes();
    
    a = new DefaultArtifact( new ArtifactMetadata("a:a:1.0.0") );
    
    File ant = File.createTempFile( "test-flat", "-repo" );
    ant.deleteOnExit();
    InputStream in = LocalRepositoryFlatTest.class.getResourceAsStream( "/ant-1.6.5.jar" );
    FileUtil.writeRawData( in, ant );
    a.setFile( ant );
    a.setPomBlob( pomBlob );
    
    b = new DefaultArtifact( new ArtifactMetadata("b:b:1.0.0") );
    
    File antlr = File.createTempFile( "test-flat", "-repo" );
    antlr.deleteOnExit();
    in = LocalRepositoryFlatTest.class.getResourceAsStream( "/antlr-2.7.7.jar" );
    FileUtil.writeRawData( in, antlr );
    b.setFile( antlr );
    b.setPomBlob( pomBlob );
  }

  @Override
  protected void tearDown()
      throws Exception
  {
    FileUtil.delete( _dir );
  }  

  public void testWriteFlat()
  throws Exception
  {
    String test = "testWriteFlat()";
    
    System.out.println(test+": test repo is in "+_repo.getDirectory());

    List<Artifact> artifacts = new ArrayList<Artifact>();
    artifacts.add( a );
    artifacts.add( b );
    
    RepositoryWriter rw = _repo.getWriter();
    rw.writeArtifacts( artifacts );
    
    File af = new File ( _dir, "a-1.0.0.jar" );
    
    assertTrue( af.exists() );
    assertEquals( 1034049L, af.length() );
    
    File bf = new File ( _dir, "b-1.0.0.jar" );

    assertTrue( bf.exists() );
    assertEquals( 445288L, bf.length() );
  }
  
  
  public void testWriteFlatWithPom()
  throws Exception
  {
    String test = "testWriteFlatWithPom()";
    
    _repo.setCreatePoms( true );
    
    System.out.println(test+": test repo is in "+_repo.getDirectory());

    List<Artifact> artifacts = new ArrayList<Artifact>();
    artifacts.add( a );
    artifacts.add( b );
    
    RepositoryWriter rw = _repo.getWriter();
    rw.writeArtifacts( artifacts );
    
    File af = new File ( _dir, "a-1.0.0.jar" );
    
    assertTrue( af.exists() );
    assertEquals( 1034049L, af.length() );
    
    File ap = new File ( _dir, "a-1.0.0.pom" );
    assertTrue( ap.exists() );
    
    File bf = new File ( _dir, "b-1.0.0.jar" );

    assertTrue( bf.exists() );
    assertEquals( 445288L, bf.length() );
    
    File bp = new File ( _dir, "b-1.0.0.pom" );
    assertTrue( bp.exists() );
  }
  
  public void testWriteFlatWithGroup()
  throws Exception
  {
    String test = "testWriteFlatWithGroup()";
    
    _repo.setCreateGroupFolders( true );
    
    System.out.println(test+": test repo is in "+_repo.getDirectory());

    List<Artifact> artifacts = new ArrayList<Artifact>();
    artifacts.add( a );
    artifacts.add( b );
    
    RepositoryWriter rw = _repo.getWriter();
    rw.writeArtifacts( artifacts );
    
    File af = new File ( _dir, "a/a-1.0.0.jar" );
    
    assertTrue( af.exists() );
    assertEquals( 1034049L, af.length() );
    
    File bf = new File ( _dir, "b/b-1.0.0.jar" );

    assertTrue( bf.exists() );
    assertEquals( 445288L, bf.length() );
  }
  
  public void testWriteFlatWithGroupAndPom()
  throws Exception
  {
    String test = "testWriteFlatWithGroupAndPom()";
    
    _repo.setCreateGroupFolders( true );
    _repo.setCreatePoms( true );
    
    System.out.println(test+": test repo is in "+_repo.getDirectory());

    List<Artifact> artifacts = new ArrayList<Artifact>();
    artifacts.add( a );
    artifacts.add( b );
    
    RepositoryWriter rw = _repo.getWriter();
    rw.writeArtifacts( artifacts );
    
    File af = new File ( _dir, "a/a-1.0.0.jar" );
    
    assertTrue( af.exists() );
    assertEquals( 1034049L, af.length() );
    
    File ap = new File ( _dir, "a/a-1.0.0.pom" );
    assertTrue( ap.exists() );
    
    File bf = new File ( _dir, "b/b-1.0.0.jar" );

    assertTrue( bf.exists() );
    assertEquals( 445288L, bf.length() );
    
    File bp = new File ( _dir, "b/b-1.0.0.pom" );
    assertTrue( bp.exists() );
  }
  

}
