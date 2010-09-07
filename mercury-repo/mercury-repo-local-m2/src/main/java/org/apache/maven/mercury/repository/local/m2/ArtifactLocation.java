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

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.version.DefaultArtifactVersion;
import org.apache.maven.mercury.util.FileUtil;

/**
 * artifact relative location data object - used by repositories to hold on to intermediate path calculations 
 *
 *
 * @author Oleg Gusakov
 * @version $Id: ArtifactLocation.java 762963 2009-04-07 21:01:07Z ogusakov $
 *
 */
public class ArtifactLocation
{
  public static final String POM_EXT = ".pom";

  private String prefix;
  
  private String gaPath;
  private String versionDir;
  private String baseName;
  private String version;
  private String classifier;
  private String type;
  
  private ArtifactMetadata bmd;
  
  public ArtifactLocation( String prefix, ArtifactMetadata bmd )
  {
    if( prefix == null || bmd == null || bmd.getGroupId() == null || bmd.getArtifactId() == null || bmd.getVersion() == null )
      return;
    
    this.bmd = bmd;

    this.prefix     = prefix;
    this.gaPath     = bmd.getGroupId().replace( '.', FileUtil.SEP_CHAR ) + FileUtil.SEP + bmd.getArtifactId();
    this.version    = bmd.getVersion();
    this.baseName   = bmd.getArtifactId();
    this.versionDir = this.version;
    this.classifier = bmd.getClassifier();
    this.type       = bmd.getType();
  }
  
  public String getRelPath()
  {
    return gaPath+FileUtil.SEP+calculateVersionDir( version )+FileUtil.SEP+baseName+FileUtil.DASH+version+getDashedClassifier()+'.'+type;
  }
  
  public String getRelPomPath()
  {
    return gaPath+FileUtil.SEP+calculateVersionDir( version )+FileUtil.SEP+baseName+FileUtil.DASH+version+POM_EXT;
  }
  
  public String getAbsPath()
  {
    if( prefix == null )
      return null;

    return getSeparatedPrefix() + getRelPath();
  }
  
  public String getAbsPomPath()
  {
    if( prefix == null )
      return null;

    return getSeparatedPrefix() + getRelPomPath();
  }
  
  public String getAbsGavPath()
  {
      if( prefix == null )
          return null;

    return getSeparatedPrefix() + getGavPath();
  }
  
  public String getGavPath()
  {
    return getGaPath() + FileUtil.SEP + calculateVersionDir( version );
  }
  
  public String getBaseVersion()
  {
    if( version == null )
      return null;
    
    DefaultArtifactVersion dav = new DefaultArtifactVersion( version );
    return dav.getBase();
  }
  
  public String getVersionWithoutTS(  )
  {
    if( version == null )
      return null;
    
    int li = version.lastIndexOf( '-' );
    
    if( li < 1 )
        return version;
    
    int li2 = version.substring( 0, li ).lastIndexOf( '-' );
    
    if( li2 > 0 )
        return version.substring( 0, li2 );
    
    return version;
  }
  
  public static String stripTS( String ver  )
  {
    if( ver == null )
      return null;
    
    int li = ver.lastIndexOf( '-' );
    
    if( li < 1 )
        return ver;
    
    int li2 = ver.substring( 0, li ).lastIndexOf( '-' );
    
    if( li2 > 0 )
        return ver.substring( 0, li2 );
    
    return ver;
  }
  
  public static String stripSN( String ver  )
  {
    if( ver == null )
      return null;
    
    int li = ver.lastIndexOf( '-' );
    
    if( li < 1 )
        return ver;
    
    return ver.substring( 0, li );
  }
  
  public static String getTS( String ver  )
  {
    if( ver == null )
      return null;
    
    int li = ver.lastIndexOf( '-' );
    
    if( li < 1 )
        return ver;
    
    int li2 = ver.substring( 0, li ).lastIndexOf( '-' );
    
    if( li2 > 0 )
        return ver.substring( li2+1 );
    
    return ver;
  }
  //---------------------------------------------------------------------------------------------------------------
  public static String calculateVersionDir( String ver )
  {
      if( ver == null )
          return ver;
      
      if( ver.matches( Artifact.SNAPSHOT_TS_REGEX ) )
          return stripTS( ver )+FileUtil.DASH+Artifact.SNAPSHOT_VERSION;
      
      return ver;
  }
  
  //---------------------------------------------------------
  public String getGaPath()
  {
    return gaPath;
  }
  public void setGaPath( String gaPath )
  {
    this.gaPath = gaPath;
  }
  public String getVersionDir()
  {
    return versionDir;
  }
  public void setVersionDir( String versionDir )
  {
    this.versionDir = versionDir;
  }
  public String getBaseName()
  {
    return baseName;
  }
  public void setBaseName( String baseName )
  {
    this.baseName = baseName;
  }
  public String getVersion()
  {
    return version;
  }
  public void setVersion( String version )
  {
    this.version = version;
  }
  public String getClassifier()
  {
    return classifier;
  }
  public String getDashedClassifier()
  {
    return (classifier == null||classifier.length()<1) ? "" : FileUtil.DASH+classifier;
  }
  public void setClassifier( String classifier )
  {
    this.classifier = classifier;
  }
  public String getType()
  {
    return type;
  }
  public void setType( String type )
  {
    this.type = type;
  }
  public String getPrefix()
  {
    return prefix;
  }
  public String getSeparatedPrefix()
  {
    if( prefix == null )
      return null;

    return prefix+(prefix.endsWith( FileUtil.SEP ) ? "" : FileUtil.SEP);
  }

  public void setPrefix( String prefix )
  {
    this.prefix = prefix;
  }

  @Override
  public String toString()
  {
    return bmd == null ? "no ArtifactBasicMetadata" : bmd.toString();
  }
  
}