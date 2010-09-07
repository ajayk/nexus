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
import java.io.IOException;

import org.apache.maven.mercury.repository.api.AbstractRepository;
import org.apache.maven.mercury.repository.api.LocalRepository;
import org.apache.maven.mercury.repository.api.NonExistentProtocolException;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.api.RepositoryWriter;

public class LocalRepositoryFlat
extends AbstractRepository
implements LocalRepository
{
  public static final String FLAT_REPOSITORY_TYPE = "flat";
  
  public static final String METADATA_FILE_NAME = "maven-metadata-local.xml";
  
    private File directory;
    
    private boolean createPoms         = false;
    private boolean createGroupFolders = false;

    //----------------------------------------------------------------------------------
    public LocalRepositoryFlat( File directory )
    throws IOException
    {
        this( directory, false, false );
    }
    //----------------------------------------------------------------------------------
    public LocalRepositoryFlat( File directory, boolean createGroupFolders, boolean createPoms )
    throws IOException
    {
        this( directory.getCanonicalPath(), directory, createGroupFolders, createPoms );
    }
    //----------------------------------------------------------------------------------
    public LocalRepositoryFlat( String id, File directory, boolean createGroupFolders, boolean createPoms )
    {
        super( id, FLAT_REPOSITORY_TYPE );
        this.directory = directory;
        this.createGroupFolders = createGroupFolders;
        this.createPoms = createPoms;
    }
    //----------------------------------------------------------------------------------
    public File getDirectory()
    {
        return directory;
    }
    //----------------------------------------------------------------------------------
    public RepositoryReader getReader() 
    {
      return RepositoryReader.NULL_READER;
    }
    //----------------------------------------------------------------------------------
    public RepositoryReader getReader( String protocol )
    {
       return RepositoryReader.NULL_READER;
    }
    //----------------------------------------------------------------------------------
    public RepositoryWriter getWriter()
    {
      return new LocalRepositoryWriterFlat(this);
    }
    //----------------------------------------------------------------------------------
    public RepositoryWriter getWriter( String protocol )
        throws NonExistentProtocolException
    {
      return getWriter();
    }
    //----------------------------------------------------------------------------------
    public boolean isLocal()
    {
      return true;
    }
    //----------------------------------------------------------------------------------
    public boolean isReadable()
    {
      return false;
    }
    //----------------------------------------------------------------------------------
    public boolean isWriteable()
    {
      return true;
    }
    //----------------------------------------------------------------------------------
    public String getType()
    {
      return DEFAULT_REPOSITORY_TYPE;
    }
    //----------------------------------------------------------------------------------
    public boolean isCreatePoms()
    {
      return createPoms;
    }
    public void setCreatePoms( boolean createPoms )
    {
      this.createPoms = createPoms;
    }
    public boolean isCreateGroupFolders()
    {
      return createGroupFolders;
    }
    public void setCreateGroupFolders( boolean createGroupFolders )
    {
      this.createGroupFolders = createGroupFolders;
    }
    public String getMetadataName()
    {
      return METADATA_FILE_NAME;
    }
    //----------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------
}
