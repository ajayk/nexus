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
package org.apache.maven.mercury.dependency.tests;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.apache.maven.mercury.MavenDependencyProcessor;
import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.ArtifactScopeEnum;
import org.apache.maven.mercury.artifact.MetadataTreeNode;
import org.apache.maven.mercury.event.DumbListener;
import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.MercuryLoggerManager;
import org.apache.maven.mercury.metadata.DependencyBuilder;
import org.apache.maven.mercury.metadata.DependencyBuilderFactory;
import org.apache.maven.mercury.metadata.MetadataTreeException;
import org.apache.maven.mercury.repository.api.ArtifactResults;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.api.RepositoryUpdateIntervalPolicy;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;
import org.apache.maven.mercury.repository.virtual.VirtualRepositoryReader;
import org.apache.maven.mercury.transport.api.Server;


/**
 * 
 * @author Oleg Gusakov
 * @version $Id: DependencyTreeBuilderTest.java 762963 2009-04-07 21:01:07Z ogusakov $
 */
public class DependencyTreeBuilderTest
extends TestCase
{
  private static final IMercuryLogger _log = MercuryLoggerManager.getLogger( DependencyTreeBuilderTest.class ); 
  
  String repoUrl = "http://repo1.maven.org/maven2";
//  String repoUrl = "http://repository.sonatype.org/content/groups/public";
//  String repoUrl = "http://nexus:8081/nexus/content/groups/public";

  File repoDir;
  
  DependencyBuilder depBuilder;
  LocalRepositoryM2 localRepo;
  RemoteRepositoryM2 remoteRepo;
  List<Repository> reps;
  
  VirtualRepositoryReader vReader;
  
  //----------------------------------------------------------------------------------------------
  @Override
  protected void setUp()
  throws Exception
  {
    
    Logger.getLogger("").setLevel(Level.ALL);

    repoDir = new File( "./target/local");
//    repoDir = File.createTempFile( "local-repo-","-it");
    repoDir.delete();
    repoDir.mkdirs();
    
    _log.info( "temporary local repository at "+repoDir );
    
    reps = new ArrayList<Repository>();
    
    localRepo = new LocalRepositoryM2( "testLocalRepo", repoDir, new MavenDependencyProcessor() );
    reps.add(  localRepo );
    
    Server server = new Server( "testRemoteRepo", new URL(repoUrl) );
    remoteRepo = new RemoteRepositoryM2(server, new MavenDependencyProcessor());
    reps.add( remoteRepo );
    
//    Server central = new Server("central", new URL("http://repo1.maven.org/maven2") );
//    RemoteRepositoryM2 centralRepo = new RemoteRepositoryM2(central);
//    reps.add(centralRepo);

    depBuilder = DependencyBuilderFactory.create( DependencyBuilderFactory.JAVA_DEPENDENCY_MODEL, reps );
    depBuilder.register( new DumbListener() );
    
    vReader = new VirtualRepositoryReader( reps );
  }
  //----------------------------------------------------------------------------------------------
  @Override
  protected void tearDown()
  throws Exception
  {
    super.tearDown();
  }
  //----------------------------------------------------------------------------------------------
  private static boolean assertHasArtifact( List<ArtifactMetadata> res, String gav )
  {
    ArtifactMetadata gavMd = new ArtifactMetadata(gav);
    
    for( ArtifactMetadata md : res )
      if( md.sameGAV( gavMd ) )
        return true;
    
    return false;
  }
   //----------------------------------------------------------------------------------------------
  public void testDummy()
  throws MetadataTreeException
  {
  }
  //----------------------------------------------------------------------------------------------
  /**
   * this test relies on MavenVersionRange maven.mercury.osgi.version being set to false, it's default value.
   * <strong>Do not</strong> run maven with -Dmaven.mercury.osgi.version=true   
   */
  public void testResolveConflicts()
  throws Exception
  {
//    String artifactId = "org.testng:testng:5.7";
    String artifactId = "asm:asm-xml:3.0";
//  String artifactId = "org.apache.maven:maven-core:2.0.9";
//  String artifactId = "qdox:qdox:1.6.1";
    
    
    ArtifactMetadata md = new ArtifactMetadata( artifactId );

    MetadataTreeNode root = depBuilder.buildTree( md, ArtifactScopeEnum.compile );

    assertNotNull( "null tree built", root );
    
//    assertTrue( "wrong tree size, expected gte 4", 4 <= root.countNodes() );

    List<ArtifactMetadata> res = depBuilder.resolveConflicts( root );
    
    assertNotNull( res );
    
    assertTrue( res.size() > 1 );
    
    System.out.println("\n---------------------------------\nclasspath: "+res);    
    System.out.println("---------------------------------");    
    for( ArtifactMetadata amd : res )
    {
      System.out.println(amd + ( amd.getTracker() == null ? " [no tracker]" : " ["+((RepositoryReader)amd.getTracker()).getRepository().getId()+"]" ) );
    }
    System.out.println("---------------------------------");    

    
    assertEquals( 4, res.size() );
    
    assertTrue( assertHasArtifact( res, "asm:asm-xml:3.0" ) );
    assertTrue( assertHasArtifact( res, "asm:asm-util:3.0" ) );
    assertTrue( assertHasArtifact( res, "asm:asm-tree:3.0" ) );
    assertTrue( assertHasArtifact( res, "asm:asm:3.0" ) );
    
    ArtifactResults aRes = vReader.readArtifacts( res );
    
    assertNotNull( aRes );
    
    assertFalse( aRes.hasExceptions() );
    
    assertTrue( aRes.hasResults() );
    
    List<Artifact> artifacts = new ArrayList<Artifact>();
    
    for( ArtifactMetadata abm : aRes.getResults().keySet() )
      artifacts.addAll(  aRes.getResults(abm) );
    
    localRepo.getWriter().writeArtifacts( artifacts );
    
    System.out.println("Saved "+artifacts.size()+" artifacts to "+localRepo.getDirectory() );
    
  }
  //----------------------------------------------------------------------------------------------
  private static void showClasspath( List<ArtifactMetadata> cp )
  {
    TreeSet<String> scp = new TreeSet<String>();
    
    for( ArtifactMetadata m : cp )
      scp.add( m.getArtifactId()+"-"+m.getVersion()+"."+m.getType() );
    
    System.out.println("\n========> Classpath: "+cp.size()+" elements");
    for( String s : scp )
    {
      System.out.println(s);
    }
    System.out.println("<======== Classpath\n");
    
  }
  //----------------------------------------------------------------------------------------------
  /**
   * this test relies on MavenVersionRange maven.mercury.osgi.version being set to false, it's default value.
   * <strong>Do not</strong> run maven with -Dmaven.mercury.osgi.version=true   
   */
  public void testResolvePlugin()
  throws Exception
  {
    String centralUrl = "http://repo1.maven.org/maven2";

    String artifactId = "org.apache.maven.plugins:maven-clean-plugin:2.2";
    
    reps.clear();
    
    File pluginRepo = new File( "./target/repoPlugin" );
    localRepo = new LocalRepositoryM2( "testLocalPluginRepo", pluginRepo, new MavenDependencyProcessor() );
    reps.add(  localRepo );

    Server server = new Server( "id", new URL(centralUrl) );
    remoteRepo = new RemoteRepositoryM2(server, new MavenDependencyProcessor());
    remoteRepo.setUpdatePolicy( RepositoryUpdateIntervalPolicy.UPDATE_POLICY_NEVER );
    reps.add( remoteRepo );
    
    depBuilder = DependencyBuilderFactory.create( DependencyBuilderFactory.JAVA_DEPENDENCY_MODEL, reps );
//    depBuilder.register( new DumbListener() );
    
    ArtifactMetadata md = new ArtifactMetadata( artifactId );

    MetadataTreeNode root = depBuilder.buildTree( md, ArtifactScopeEnum.compile );

    assertNotNull( "null tree built", root );
    
//    assertTrue( "wrong tree size, expected gte 4", 4 <= root.countNodes() );

    List<ArtifactMetadata> res = depBuilder.resolveConflicts( root );
    
    assertNotNull( res );
    
    assertTrue( res.size() > 1 );

    showClasspath( res );

    artifactId = "org.apache.maven.plugins:maven-compiler-plugin:2.0.2";
    md = new ArtifactMetadata( artifactId );
    root = depBuilder.buildTree( md, ArtifactScopeEnum.compile );
    assertNotNull( "null tree built", root );
    res = depBuilder.resolveConflicts( root );
    showClasspath( res );
    
    ArtifactResults ar = vReader.readArtifacts( res );
    
    assertNotNull( ar );
    
    assertFalse( ar.hasExceptions() );

    assertTrue( ar.hasResults() );
    
    Map<ArtifactMetadata, List<Artifact>> arts = ar.getResults();
    
    for( List<Artifact> al : arts.values() )
    {
      for( Artifact a : al )
        System.out.println( a.toString()+" -> "+a.getFile() );
    }
  }
  //----------------------------------------------------------------------------------------------
  public void testResolvePluginAsTree()
  throws Exception
  {
    String centralUrl = "http://repo1.maven.org/maven2";

    String artifactId = "org.apache.maven.plugins:maven-clean-plugin:2.2";
    
    reps.clear();
    
    File pluginRepo = new File( "./target/repoPlugin" );
    localRepo = new LocalRepositoryM2( "testLocalPluginRepo", pluginRepo, new MavenDependencyProcessor() );
    reps.add(  localRepo );

    Server server = new Server( "id", new URL(centralUrl) );
    remoteRepo = new RemoteRepositoryM2(server, new MavenDependencyProcessor());
    remoteRepo.setUpdatePolicy( RepositoryUpdateIntervalPolicy.UPDATE_POLICY_NEVER );
    reps.add( remoteRepo );
    
    depBuilder = DependencyBuilderFactory.create( DependencyBuilderFactory.JAVA_DEPENDENCY_MODEL, reps );
//    depBuilder.register( new DumbListener() );
    
    ArtifactMetadata md = new ArtifactMetadata( artifactId );

    MetadataTreeNode root = depBuilder.buildTree( md, ArtifactScopeEnum.compile );

    assertNotNull( root );
    
    MetadataTreeNode res = depBuilder.resolveConflictsAsTree( root );
    
    assertNotNull( res );
    
    int nodeCount = res.countNodes();
    
    assertEquals( 15, nodeCount);
    
    System.out.println("Unique feature - junit is in the compile scope thanks to plexus-container-default 1.0.9");

    MetadataTreeNode.showNode( res, 0 );

  }
  //----------------------------------------------------------------------------------------------
  //----------------------------------------------------------------------------------------------
}