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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.maven.mercury.MavenDependencyProcessor;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.crypto.api.StreamVerifierAttributes;
import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.crypto.pgp.PgpStreamVerifierFactory;
import org.apache.maven.mercury.crypto.sha.SHA1VerifierFactory;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.local.m2.MetadataProcessorMock;
import org.apache.maven.mercury.transport.api.Server;
import org.apache.maven.mercury.util.FileUtil;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id: LocalRepositoryWriterM2Test.java 762963 2009-04-07 21:01:07Z ogusakov $
 *
 */
public class LocalRepositoryWriterM2Test
extends AbstractRepositoryWriterM2Test
{
  public static final String SYSTEM_PARAMETER_SKIP_LOCK_TESTS = "maven.mercury.tests.skip.lock";
  boolean skipLockTests = Boolean.parseBoolean( System.getProperty( SYSTEM_PARAMETER_SKIP_LOCK_TESTS, "true" ) );
  
  //------------------------------------------------------------------------------
  @Override
  protected void setUp()
  throws Exception
  {
    super.setUp();

    targetDirectory = new File("./target/test-classes/tempRepo");
    FileUtil.copy( new File("./target/test-classes/repo"), targetDirectory, true );
    FileUtil.delete( new File(targetDirectory, "org") );
    
    query = new ArrayList<ArtifactMetadata>();
    
    server = new Server( "test", targetDirectory.toURL() );
    // verifiers
    factories = new HashSet<StreamVerifierFactory>();       
    factories.add( 
        new PgpStreamVerifierFactory(
                new StreamVerifierAttributes( PgpStreamVerifierFactory.DEFAULT_EXTENSION, false, true )
                , getClass().getResourceAsStream( secretKeyFile )
                , keyId
                , secretKeyPass
                                    )
                  );
    factories.add( new SHA1VerifierFactory(false,false) );
    server.setWriterStreamVerifierFactories(factories);
      
    repo = new LocalRepositoryM2( server, new MavenDependencyProcessor() );
    mdProcessor = new MetadataProcessorMock();
    repo.setDependencyProcessor( mdProcessor );
    
    reader = repo.getReader();
    writer = repo.getWriter();
  }

  @Override
  void setReleases()
      throws MalformedURLException
  {
  }

  @Override
  void setSnapshots()
      throws MalformedURLException
  {
  }
  //-------------------------------------------------------------------------
  @Override
  public void testWriteContentionMultipleArtifacts()
      throws Exception
  {
    if( skipLockTests )
      System.out.println("skipping");
    else
      super.testWriteContentionMultipleArtifacts();
  }
  
  @Override
  public void testWriteContentionSingleArtifact()
      throws Exception
  {
    if( skipLockTests )
      System.out.println("skipping");
    else
      super.testWriteContentionSingleArtifact();
  }
  
}
