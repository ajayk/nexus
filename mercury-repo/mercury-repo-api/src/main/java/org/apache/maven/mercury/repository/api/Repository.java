package org.apache.maven.mercury.repository.api;

import org.apache.maven.mercury.artifact.Quality;
import org.apache.maven.mercury.artifact.QualityRange;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.transport.api.Server;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 */
public interface Repository
{
    String getId();

    /**
     * repository type - m2, nexus, ivy, p2 - to name a few. It defines the RepositoryReader/Writer that will be
     * searched for in the registry.
     */
    public String getType();

    /**
     * Indicates whether this is local Repository. This flag defines the necessity to download the artifact, if it was
     * cleared by the conflict resolver but not read from a localRepo.
     */
    public boolean isLocal();

    /**
     * Indicates whether it's possible to read from this Repository.
     */
    public boolean isReadable();

    /**
     * Indicates whether it's possible to write to this Repository. Good example is the flat repo, which is used to only
     * collect dependencies for some 3rd party reasons, but not read them. If there are multiple localRepo's and
     * Artifact needs to be downloaded - it will be "written" to all "local" repositories that are writeable.
     */
    public boolean isWriteable();

    /**
     * Indicates whether this repository contains releases
     */
    public boolean isReleases();

    /**
     * Indicates whether this repository contains snapshots
     */
    public boolean isSnapshots();

    /**
     * indicates if the supplied code quality is served by this repository
     */
    public boolean isAcceptedQuality( Quality quality );

    /**
     * defines the code quality range for this repository
     */
    public QualityRange getRepositoryQualityRange();
    void setRepositoryQualityRange( QualityRange qualityRange );

    /**
     * defines how VersionRnage treats upper boundary - which Artifacts should be treated as belonging to the vicinity -
     * http://docs.codehaus.org/x/twDPBQ
     * 
     * note: don't mix this with repository quality range - this one is for version range calculations only!
     * 
     */
    public QualityRange getVersionRangeQualityRange();
    public void setVersionRangeQualityRange( QualityRange qualityRange );
    
    /**
     * get default reader, if any
     * 
     * @return default reader or null, if none exists
     * @throws RepositoryException
     */
    RepositoryReader getReader()
        throws RepositoryException;

    /**
     * get protocol specific reader, if any
     * 
     * @param protocol
     * @return reader instance for the specified protocol
     * @throws NonExistentProtocolException if protocol not supported
     */
    RepositoryReader getReader( String protocol )
        throws RepositoryException;

    /**
     * get default writer, if any
     * 
     * @return default writer or null, if none exists
     * @throws RepositoryException
     */
    RepositoryWriter getWriter()
        throws RepositoryException;

    /**
     * @param protocol
     * @return writer instance for the specified protocol
     * @throws NonExistentProtocolException if protocol not supported
     * @throws RepositoryException 
     */
    RepositoryWriter getWriter( String protocol )
        throws NonExistentProtocolException, RepositoryException;

    /**
     * server where this repo resides. For local repo - folder as URL and stream verifiers are important.
     * 
     * @return server
     */
    boolean hasServer();

    Server getServer();

    /**
     * DependencyProcessor used by this repo resides
     * 
     * @return server
     */
    boolean hasDependencyProcessor();

    DependencyProcessor getDependencyProcessor();

    void setDependencyProcessor( DependencyProcessor dependencyProcessor );

    /**
     * maven-metadata.xml file name for this repository. This is internal to repository and should never be used outside
     * of readers and wrters
     * 
     * @return server
     */
    String getMetadataName();
}
