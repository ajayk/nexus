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
package org.apache.maven.mercury.repository.api;

import java.util.Collection;

import org.apache.maven.mercury.artifact.Artifact;

/**
 * Repository writer API to be implemented by any repo implementation that wishes to store artifacts for Maven. All
 * operations are asynchronous and can generate callback events
 * 
 * @author Oleg Gusakov
 * @version $Id: RepositoryWriter.java 762963 2009-04-07 21:01:07Z ogusakov $
 */
public interface RepositoryWriter
    extends RepositoryOperator
{
    /**
     * write (upload) given artifact to the repository
     * 
     * @param artifact to upload
     * @throws RepositoryException
     */
    public void writeArtifacts( Collection<Artifact> artifact )
        throws RepositoryException;

    public void setMetadataCache( RepositoryMetadataCache mdCache );

    public RepositoryMetadataCache getMetadataCache();

    public static final RepositoryWriter NULL_WRITER = new RepositoryWriter()
    {

        public RepositoryMetadataCache getMetadataCache()
        {
            return null;
        }

        public void setMetadataCache( RepositoryMetadataCache mdCache )
        {
        }

        public void writeArtifacts( Collection<Artifact> artifact )
            throws RepositoryException
        {
        }

        public boolean canHandle( String protocol )
        {
            return false;
        }

        public void close()
        {
        }

        public String[] getProtocols()
        {
            return null;
        }

    };

}
