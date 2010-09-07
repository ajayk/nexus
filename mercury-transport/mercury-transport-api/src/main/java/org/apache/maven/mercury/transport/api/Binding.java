/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file                                                                                            
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.maven.mercury.transport.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.MercuryLoggerManager;

/**
 * Binding <p/> A Binding represents a remote uri whose contents are to be
 * downloaded and stored in a locally, or a local resource whose contents are to
 * be uploaded to the remote uri.
 */
public class Binding
{
  private static final IMercuryLogger LOG = MercuryLoggerManager.getLogger( Binding.class );
  
  protected URL                 remoteResource;

  protected File                localFile;

  /** 
   * inbound in-memory binding for reading remote content.
   * It is created by the constructor
   */
  protected ByteArrayOutputStream localOS;

  /**
   * this is outbound in-memory binding. IS is passed by the client
   */
  protected InputStream         localIS;
  
  /** indicates that this transfer is exempt from stream verification */
  boolean exempt = false;

  protected Exception error;

  public Binding()
  {
  }

  public Binding( URL remoteUrl, File localFile)
  {
    this.remoteResource = remoteUrl;
    this.localFile = localFile;
  }

  public Binding( URL remoteUrl, File localFile, boolean exempt )
  {
      this( remoteUrl,localFile );
      this.exempt = exempt;
  }

  /** 
   * this is in-memory binding for writing remote content into localOS
   * 
   * @param remoteUrl
   * @param lenientChecksum
   */
  public Binding( URL remoteUrl )
  {
    this.remoteResource = remoteUrl;
    // let's assume 4k on average
    this.localOS = new ByteArrayOutputStream( 4*1024 );
  }

  public Binding( URL remoteUrl, boolean exempt )
  {
    this( remoteUrl );
    this.exempt = exempt;
  }

  /**
   * outbound constructor - send contents of the stream to remoteUrl
   * 
   * @param remoteUrl
   * @param is
   */
  public Binding( URL remoteUrl, InputStream is )
  {
    this.remoteResource = remoteUrl;
    this.localIS = is;
  }
  public Binding( URL remoteUrl, InputStream is, boolean exempt )
  {
    this( remoteUrl, is );
    this.exempt = exempt;
  }

  /**
   * inbound constructor - read contents of the remoteUrl to the stream
   * 
   * @param remoteUrl
   * @param is
   */
  public Binding( URL remoteUrl, ByteArrayOutputStream os )
  {
    this.remoteResource = remoteUrl;
    this.localOS = os;
  }

  public Binding( URL remoteUrl, ByteArrayOutputStream os, boolean exempt  )
  {
    this( remoteUrl, os );
    this.exempt = exempt;
  }

  public URL getRemoteResource()
  {
    return remoteResource;
  }

  public void setRemoteResource( URL remoteResource )
  {
    this.remoteResource = remoteResource;
  }

  public Exception getError()
  {
    return error;
  }

  public void setError( Exception error )
  {
    this.error = error;
  }
  
  public boolean isInMemory()
  {
    return (!isFile() && (localIS != null || localOS != null));
  }
  
  public boolean isFile()
  {
    return localFile != null;
  }
  
  public boolean isExempt()
  {
      return exempt;
  }
  
  public void setExempt( boolean exempt )
  {
      this.exempt = exempt;
  }
  
  public byte [] getInboundContent()
  {
    if( localOS != null )
      return localOS.toByteArray();
    
    return null;
  }
  
  public OutputStream getLocalOutputStream()
  {
      return localOS;
  }
  
  public InputStream getLocalInputStream()
  {
      return localIS;
  }
  
  public File getLocalFile ()
  {
      return localFile;
  }

  @Override
  public String toString()
  {
    return '['
            + (exempt ? "(exempt)" : "")
            + (remoteResource == null ? "null URL" : remoteResource.toString() )+" <=> "
            + (localFile == null ?  ( localIS == null ? (localOS == null ? "null local Res" : localOS) : "localIS" ) : localFile.getAbsolutePath() )
            +']'
    ;
  }

}
