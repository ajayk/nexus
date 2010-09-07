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
package org.apache.maven.mercury.builder.api;

import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.artifact.ArtifactMetadata;

public interface DependencyProcessor
{
    /** dummy processor to create M2 repositories when metadata processing is not required */
    public static final DependencyProcessor NULL_PROCESSOR = new DependencyProcessor()
    {
        public List<ArtifactMetadata> getDependencies( ArtifactMetadata bmd, MetadataReader mdReader,
                                                            Map<String, String> env, Map<?, ?> sysProps )
            throws MetadataReaderException, DependencyProcessorException
        {
            return null;
        }
    };

    // TODO: shouldn't sysProps be changed to Properties because of System.getProperties() API?
    // Oleg: sysProp may help to experiment with Objects, different from Strings, so I'd 
    //  stay with the Map until this is stable enough
    public List<ArtifactMetadata> getDependencies( ArtifactMetadata bmd, MetadataReader mdReader,
                                                        Map<String, String> env, Map<?, ?> sysProps )
        throws MetadataReaderException, DependencyProcessorException;
}
