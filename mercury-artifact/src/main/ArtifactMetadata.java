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
package org.apache.maven.mercury.artifact;

import java.util.Map;

/**
 * Artifact Metadata that is resolved independent of Artifact itself. It's built on top of ArtifactBasicMetadata
 *
 * @author Oleg Gusakov
 * @version $Id: ArtifactMetadata.java 762963 2009-04-07 21:01:07Z ogusakov $
 */
public class ArtifactMetadata
    extends ArtifactBasicMetadata
{
    /**
     * for testing - required for mock MetadataSource
     */
    public ArtifactMetadata()
    {
    }

    // ------------------------------------------------------------------

    public ArtifactMetadata( String groupId, String name, String version, String type, ArtifactScopeEnum artifactScope,
                             String classifier, String artifactUri, String why, boolean resolved, String error,
                             Map<String, String> attributes )
    {
        this.groupId = groupId;
        this.artifactId = name;
        setVersion( version );
        this.type = type;
        this.artifactScope = artifactScope;
        this.classifier = classifier;
        this.artifactUri = artifactUri;
        this.attributes = attributes;
    }

    public ArtifactMetadata( ArtifactBasicMetadata bmd )
    {
        this( bmd.getGroupId(), bmd.getArtifactId(), bmd.getVersion(), bmd.getType(), null, bmd.getClassifier(), null,
              null, true, null, bmd.getAttributes() );
    }

    public ArtifactMetadata( String gav )
    {
        this( new ArtifactBasicMetadata( gav ) );
    }

}
