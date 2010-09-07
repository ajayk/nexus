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
package org.apache.maven.mercury.repository.local.m2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.builder.api.MetadataReader;
import org.apache.maven.mercury.builder.api.MetadataReaderException;
import org.xml.sax.SAXException;

/**
 * 
 * a temporary thing to be replaced with real projectBuilder implementation
 *
 * @author Oleg Gusakov
 * @version $Id: MetadataProcessorMock.java 762963 2009-04-07 21:01:07Z ogusakov $
 *
 */
public class MetadataProcessorMock
implements DependencyProcessor
{

  public List<ArtifactMetadata> getDependencies( ArtifactMetadata bmd, MetadataReader mdReader, Map env, Map sysProps )
  throws MetadataReaderException
  {
    List<ArtifactMetadata> deps = null;
    
    try
    {
      byte [] pomBytes = mdReader.readMetadata( bmd, false );
      if( pomBytes == null )
      {
        throw new MetadataReaderException("no metadata found for "+bmd);
      }
      deps = getDeps(  pomBytes );
      
      return deps;
    }
    catch( Exception e )
    {
      throw new MetadataReaderException( e );
    }
  }
  
  private static final List<ArtifactMetadata> getDeps( byte [] pom )
  throws IOException, SAXException
  {
    if( pom == null )
      return null;
    
    DependencyCreator dc = new DependencyCreator();
    Digester digester = new Digester();
    digester.push( dc );
    
    digester.addCallMethod("project/dependencies/dependency", "addMD", 6 );
    digester.addCallParam("project/dependencies/dependency/groupId",0);
    digester.addCallParam("project/dependencies/dependency/artifactId",1);
    digester.addCallParam("project/dependencies/dependency/version",2);
    digester.addCallParam("project/dependencies/dependency/type",3);
    digester.addCallParam("project/dependencies/dependency/scope",4);
    digester.addCallParam("project/dependencies/dependency/optional",5);
    
    digester.parse( new ByteArrayInputStream(pom) );
    
    return dc.mds;
  }

}
//==============================================================================================
class DependencyCreator
{
  List<ArtifactMetadata> mds = new ArrayList<ArtifactMetadata>(8);
  
  public void addMD( String g, String a, String v, String t, String s, String o)
  {
    ArtifactMetadata md = new ArtifactMetadata();
    md.setGroupId(g);
    md.setArtifactId(a);
    md.setVersion(v);
    md.setType(t);
    md.setScope(s);
    md.setOptional(o);

    mds.add(md);
  }
}
//==============================================================================================
