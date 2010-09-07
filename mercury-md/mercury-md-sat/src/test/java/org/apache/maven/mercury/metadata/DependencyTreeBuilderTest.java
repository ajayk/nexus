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
package org.apache.maven.mercury.metadata;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.ArtifactScopeEnum;
import org.apache.maven.mercury.artifact.MetadataTreeNode;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.MercuryLoggerManager;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.local.m2.MetadataProcessorMock;


/**
 * 
 * @author Oleg Gusakov
 * @version $Id: DependencyTreeBuilderTest.java 762963 2009-04-07 21:01:07Z ogusakov $
 */
public class DependencyTreeBuilderTest
extends TestCase
{
  private static final IMercuryLogger LOG = MercuryLoggerManager.getLogger( DependencyTreeBuilderTest.class ); 
  
//  ArtifactMetadata md = new ArtifactMetadata( "pmd:pmd:3.9" );
//  File repo = new File("./target/test-classes/localRepo");

  File repoDir = new File("./target/test-classes/controlledRepo");
  
  DependencyBuilder mt;
  LocalRepositoryM2 localRepo;
  List<Repository> reps;
  DependencyProcessor processor;
  
  //----------------------------------------------------------------------------------------------
  @Override
  protected void setUp()
  throws Exception
  {
    processor = new MetadataProcessorMock();
    localRepo = new LocalRepositoryM2( "local", repoDir, new MetadataProcessorMock() );
    
    reps = new ArrayList<Repository>(4);
    reps.add(  localRepo );

    mt = new DependencyTreeBuilder( reps, null, null, null );
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
  public void testCircularDependency()
  {
    ArtifactMetadata circularMd = new ArtifactMetadata( "a:a:1" );
    try
    {
      mt.buildTree( circularMd, null );
    }
    catch (MetadataTreeException e)
    {
      assertTrue( "expected circular dependency exception, but got "+e.getClass().getName()
          , e instanceof MetadataTreeCircularDependencyException
      );
      return;
    }
    fail("circular dependency was not detected");
  }
  //----------------------------------------------------------------------------------------------
  public void testBuildTree()
  throws MetadataTreeException
  {
    ArtifactMetadata md = new ArtifactMetadata( "a:a:2" );
    
    MetadataTreeNode root = mt.buildTree( md, null );
    assertNotNull( "null tree built", root );
    assertEquals( "wrong tree size", 4, root.countNodes() );
  }
  //----------------------------------------------------------------------------------------------
  public void testResolveConflicts()
  throws MetadataTreeException
  {
    ArtifactMetadata md = new ArtifactMetadata( "a:a:2" );
    
    MetadataTreeNode root = mt.buildTree( md, ArtifactScopeEnum.compile );
    assertNotNull( "null tree built", root );
    assertEquals( "wrong tree size", 4, root.countNodes() );

    List<ArtifactMetadata> res = mt.resolveConflicts( root );
    assertNotNull( "null resolution", res );
    assertEquals( "wrong tree size", 3, res.size() );
    
    assertTrue( "no a:a:2 in the result", assertHasArtifact( res, "a:a:2" ) );
    assertTrue( "no b:b:2 in the result", assertHasArtifact( res, "b:b:2" ) );
    assertTrue( "no c:c:2 in the result", assertHasArtifact( res, "c:c:2" ) );
    
    System.out.println( "testResolveConflicts: " + res );
  }
  //----------------------------------------------------------------------------------------------
  public void testResolveScopedConflicts()
  throws MetadataTreeException
  {
    ArtifactMetadata md = new ArtifactMetadata( "a:a:4" );
    
    MetadataTreeNode root = mt.buildTree( md, ArtifactScopeEnum.compile );
    assertNotNull( "null tree built", root );
    assertEquals( "wrong tree size", 3, root.countNodes() );

    List<ArtifactMetadata> res = mt.resolveConflicts( root );
    assertNotNull( "null resolution", res );
    assertEquals( "wrong tree size", 2, res.size() );

    System.out.println( "testResolveScopedConflicts: " + res );
    
    assertTrue( "no a:a:4 in the result", assertHasArtifact( res, "a:a:4" ) );
    assertTrue( "no c:c:3 in the result", assertHasArtifact( res, "c:c:3" ) );
    
  }
  //----------------------------------------------------------------------------------------------
  public void testResolveScopedConflictsWithFiltering()
  throws MetadataTreeException
  {
    String title = "testResolveScopedConflictsWithFiltering";
    ArtifactMetadata md = new ArtifactMetadata( "a:a:4" );
    
    List<ArtifactMetadata> exclusions = new ArrayList<ArtifactMetadata>();
    exclusions.add( new ArtifactMetadata("c:c:3") );
    md.setExclusions( exclusions );
    
    MetadataTreeNode root = mt.buildTree( md, ArtifactScopeEnum.compile );
    assertNotNull( "null tree built", root );
    assertEquals( "wrong tree size", 2, root.countNodes() );

    List<ArtifactMetadata> res = mt.resolveConflicts( root );
    assertNotNull( "null resolution", res );
    assertEquals( "wrong tree size", 2, res.size() );

    System.out.println( title+": " + res );
    
    assertTrue( assertHasArtifact( res, "a:a:4" ) );
    assertFalse( assertHasArtifact( res, "c:c:3" ) );
    
  }
  //----------------------------------------------------------------------------------------------
  public void testResolveScopedConflictsWithFilteringOne()
  throws MetadataTreeException
  {
    String title = "testResolveScopedConflictsWithFilteringOne";
    ArtifactMetadata md = new ArtifactMetadata( "a:a:2" );
    
    List<ArtifactMetadata> exclusions = new ArrayList<ArtifactMetadata>();
    exclusions.add( new ArtifactMetadata("c:c:2") );
    md.setExclusions( exclusions );
    
    MetadataTreeNode root = mt.buildTree( md, ArtifactScopeEnum.compile );
    assertNotNull( "null tree built", root );
    assertEquals( "wrong tree size", 3, root.countNodes() );

    List<ArtifactMetadata> res = mt.resolveConflicts( root );
    assertNotNull( "null resolution", res );
    assertEquals( "wrong tree size", 2, res.size() );
    
    assertTrue( "no a:a:2 in the result", assertHasArtifact( res, "a:a:2" ) );
    assertTrue( "no b:b:2 in the result", assertHasArtifact( res, "b:b:2" ) );
    assertFalse( "no c:c:2 in the result", assertHasArtifact( res, "c:c:2" ) );
    
    System.out.println( title+": " + res );
    
  }
  //----------------------------------------------------------------------------------------------
  public void testResolveScopedConflictsWithFilteringTwo()
  throws MetadataTreeException
  {
    String title = "testResolveScopedConflictsWithFilteringTwo";
    ArtifactMetadata md = new ArtifactMetadata( "a:a:2" );
    
    List<ArtifactMetadata> exclusions = new ArrayList<ArtifactMetadata>();
    exclusions.add( new ArtifactMetadata("b:b:2") );
    exclusions.add( new ArtifactMetadata("c:c:2") );
    md.setExclusions( exclusions );
    
    MetadataTreeNode root = mt.buildTree( md, ArtifactScopeEnum.compile );
    assertNotNull( "null tree built", root );
    assertEquals( "wrong tree size", 2, root.countNodes() );

    List<ArtifactMetadata> res = mt.resolveConflicts( root );
    assertNotNull( "null resolution", res );
    assertEquals( "wrong tree size", 2, res.size() );
    
    assertTrue( "no a:a:2 in the result", assertHasArtifact( res, "a:a:2" ) );
    assertTrue( "no b:b:2 in the result", assertHasArtifact( res, "b:b:1" ) );
    assertFalse( "no b:b:2 in the result", assertHasArtifact( res, "b:b:2" ) );
    assertFalse( "no c:c:2 in the result", assertHasArtifact( res, "c:c:2" ) );
    
    System.out.println( title+": " + res );
    
  }
  //----------------------------------------------------------------------------------------------
  public void testResolveScopedConflictsWithFilteringAll()
  throws MetadataTreeException
  {
    String title = "testResolveScopedConflictsWithFilteringTwo";
    ArtifactMetadata md = new ArtifactMetadata( "a:a:2" );
    
    List<ArtifactMetadata> exclusions = new ArrayList<ArtifactMetadata>();
    exclusions.add( new ArtifactMetadata("b:b:1") );
    exclusions.add( new ArtifactMetadata("b:b:2") );
    exclusions.add( new ArtifactMetadata("c:c:2") );
    md.setExclusions( exclusions );
    
    MetadataTreeNode root = mt.buildTree( md, ArtifactScopeEnum.compile );
    assertNotNull( "null tree built", root );
    assertEquals( "wrong tree size", 1, root.countNodes() );

    List<ArtifactMetadata> res = mt.resolveConflicts( root );
    assertNotNull( "null resolution", res );
    assertEquals( "wrong tree size", 1, res.size() );
    
    assertTrue( "no a:a:2 in the result", assertHasArtifact( res, "a:a:2" ) );
    assertFalse( "no b:b:1 in the result", assertHasArtifact( res, "b:b:1" ) );
    assertFalse( "no b:b:2 in the result", assertHasArtifact( res, "b:b:2" ) );
    assertFalse( "no c:c:2 in the result", assertHasArtifact( res, "c:c:2" ) );
    
    System.out.println( title+": " + res );
    
  }
  //----------------------------------------------------------------------------------------------
  public void testResolveBigConflicts()
  throws MetadataTreeException
  {
    ArtifactMetadata md = new ArtifactMetadata( "a:a:3" );
    
    MetadataTreeNode root = mt.buildTree( md, ArtifactScopeEnum.compile );
    assertNotNull( "null tree built", root );
    assertTrue( "wrong tree size, expected gte 4", 4 <= root.countNodes() );

    List<ArtifactMetadata> res = mt.resolveConflicts( root );
    assertNotNull( "null resolution", res );

System.out.println("BigRes: "+res);    
    
    assertEquals( "wrong tree size", 3, res.size() );
    
//    assertTrue( "no a:a:2 in the result", assertHasArtifact( res, "a:a:2" ) );
//    assertTrue( "no b:b:1 in the result", assertHasArtifact( res, "b:b:1" ) );
//    assertTrue( "no c:c:2 in the result", assertHasArtifact( res, "c:c:2" ) );
  }
  //----------------------------------------------------------------------------------------------
  public void testResolveMultiple()
  throws MetadataTreeException
  {
    ArtifactMetadata md1 = new ArtifactMetadata( "a:a:3" );
    ArtifactMetadata md2 = new ArtifactMetadata( "a:a:4" );
    
    MetadataTreeNode root = mt.buildTree( md1, ArtifactScopeEnum.compile );
    assertNotNull( "null tree built", root );
    assertTrue( "wrong tree size, expected gte 4", 4 <= root.countNodes() );

    List<ArtifactMetadata> res = mt.resolveConflicts( root );
    assertNotNull( "null resolution", res );

    System.out.println("BigRes: "+res);    
    
    assertEquals( "wrong tree size", 3, res.size() );
    
//    assertTrue( "no a:a:2 in the result", assertHasArtifact( res, "a:a:2" ) );
//    assertTrue( "no b:b:1 in the result", assertHasArtifact( res, "b:b:1" ) );
//    assertTrue( "no c:c:2 in the result", assertHasArtifact( res, "c:c:2" ) );
  }
  //----------------------------------------------------------------------------------------------
  //----------------------------------------------------------------------------------------------
}
