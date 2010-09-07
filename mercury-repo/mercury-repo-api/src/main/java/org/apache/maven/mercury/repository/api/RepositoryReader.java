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

import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.builder.api.MetadataReader;
import org.apache.maven.mercury.builder.api.MetadataReaderException;

/**
 * Repository reader API to be implemented by any repo implementation that wishes to serve artifacts to the build
 * process
 * 
 * @author Oleg Gusakov
 * @version $Id: RepositoryReader.java 762963 2009-04-07 21:01:07Z ogusakov $
 */
public interface RepositoryReader
    extends RepositoryOperator, MetadataReader
{
    /**
     * given basic coordinates query - instantiate all available matches as ArtifactBasicMetadata objects. <b>Analogous
     * to reading maven-metadata.xml</b> file from GA folder i.e. this transforms GA[Vrange] -> [GAV1, GAV2, ... GAVn]
     * 
     * @param query list of MD coordinate queries to find
     * @return map of results - lists of available matches. <b>If no results are found, reader should return null<b> If
     *         there were exceptions, map element will indicate it with hasExceptions()
     * @throws RepositoryException
     */
    public MetadataResults readVersions( Collection<ArtifactMetadata> query )
        throws RepositoryException;

    /**
     * given basic coordinates query read dependencies as a GAV list with dependencies as queries i.e. each dependency
     * at this stage is an ArtifactBasicMetadata <b>Analogous to reading pom.xml</b> file for given GAV
     * 
     * @param query list of MD coordinate queries to read. They are found by previous call to findMetadata
     * @return result as a map GAV -> [GAV1, GAV2, ... GAVn]
     * @throws RepositoryException
     */
    public MetadataResults readDependencies( Collection<ArtifactMetadata> query )
        throws RepositoryException;

    /**
     * Given basic coordinates query read Artifact objects Analogous to downloading artifact binary file into local repo
     * for given GAV
     * 
     * @param query list of MD coordinate queries to read.
     * @return array of results - lists of available matches. Order is the same as in query list. null means not found
     *         or worse
     * @throws RepositoryException
     */
    public ArtifactResults readArtifacts( Collection<ArtifactMetadata> query )
        throws RepositoryException;

    /**
     * Need if for explanation function - where and how(protocol) this artifact is found.
     */
    public Repository getRepository();

    /**
     * Abstracted POM reader. First projectBuilder, then any type of dependency reader
     */
    public void setDependencyProcessor( DependencyProcessor mdProcessor );

    public DependencyProcessor getDependencyProcessor();

    /**
     * MetadataReader field. Single repository uses itself, virtual reader injects itself to be able to find metadata
     * across repositories
     */
    public void setMetadataReader( MetadataReader mdReader );

    public MetadataReader getMetadataReader();

    /**
     * Abstracted metadata cache is used to store/retrieve metadata faster. It usually implements repository update
     * policy
     */
    public void setMetadataCache( RepositoryMetadataCache mdCache );

    public RepositoryMetadataCache getMetadataCache();

    /**
     * read content pointed by relative path. It will return content bytes
     * 
     * @param path - realative resource path in this repository
     * @return byte [] of the resource content, pointed by the path
     * @throws MetadataReaderException
     */
    public byte[] readRawData( String path, boolean exempt )
        throws MetadataReaderException;

    public byte[] readRawData( String path )
        throws MetadataReaderException;

    public static final RepositoryReader NULL_READER = new RepositoryReader()
    {
        public DependencyProcessor getDependencyProcessor()
        {
            return null;
        }

        public RepositoryMetadataCache getMetadataCache()
        {
            return null;
        }

        public Repository getRepository()
        {
            return null;
        }

        public ArtifactResults readArtifacts( Collection<ArtifactMetadata> query )
            throws RepositoryException
        {
            return null;
        }

        public MetadataResults readDependencies( Collection<ArtifactMetadata> query )
            throws RepositoryException
        {
            return null;
        }

        public byte[] readRawData( String path, boolean exempt )
            throws MetadataReaderException
        {
            return null;
        }

        public byte[] readRawData( String path )
            throws MetadataReaderException
        {
            return null;
        }

        public MetadataResults readVersions( Collection<ArtifactMetadata> query )
            throws RepositoryException
        {
            return null;
        }

        public void setDependencyProcessor( DependencyProcessor mdProcessor )
        {
        }

        public void setMetadataCache( RepositoryMetadataCache mdCache )
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

        public byte[] readMetadata( ArtifactMetadata bmd, boolean exempt )
            throws MetadataReaderException
        {
            return null;
        }

        public byte[] readMetadata( ArtifactMetadata bmd )
            throws MetadataReaderException
        {
            return null;
        }

        public byte[] readRawData( ArtifactMetadata bmd, String classifier, String type, boolean exempt )
            throws MetadataReaderException
        {
            return null;
        }

        public byte[] readRawData( ArtifactMetadata bmd, String classifier, String type )
            throws MetadataReaderException
        {
            return null;
        }

        public MetadataReader getMetadataReader()
        {
            return null;
        }

        public void setMetadataReader( MetadataReader mdReader )
        {
        }

    };

}
