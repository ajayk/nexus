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

import junit.framework.TestCase;

import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.MetadataTreeNode;

public abstract class AbstractSimpleTreeTest
extends TestCase
{
  //       b:b:1
  //      / 
  // a:a:1       b:b:2
  //    \     /
  //     c:c:1
  //          \ b:b:1
  
  ArtifactMetadata mdaa1 = new ArtifactMetadata("a:a:1");
  ArtifactMetadata mdbb1 = new ArtifactMetadata("b:b:1");
  ArtifactMetadata mdbb2 = new ArtifactMetadata("b:b:2");
  ArtifactMetadata mdcc1 = new ArtifactMetadata("c:c:1");
  
  MetadataTreeNode aa1;
  MetadataTreeNode bb1;
  MetadataTreeNode cc1;
  MetadataTreeNode cc1bb1;
  MetadataTreeNode cc1bb2;
  
  @Override
  protected void setUp() throws Exception
  {
    aa1 = new MetadataTreeNode( mdaa1, null, mdaa1 );
    bb1 = new MetadataTreeNode( mdbb1, aa1, mdbb1 );
    aa1.addChild(bb1);
    cc1 = new MetadataTreeNode( mdcc1, aa1, mdcc1 );
    aa1.addChild(cc1);
    cc1bb1 = new MetadataTreeNode( mdbb1, cc1, mdbb1 );
    cc1.addChild( cc1bb1 );
    cc1bb2 = new MetadataTreeNode( mdbb2, cc1, mdbb2 );
    cc1.addChild( cc1bb2 );
  }

}
