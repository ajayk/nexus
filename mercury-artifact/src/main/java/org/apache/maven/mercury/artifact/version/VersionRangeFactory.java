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
package org.apache.maven.mercury.artifact.version;

import java.util.Collection;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.QualityRange;

/**
 * lack of IoC container makes me throw this class in.
 *
 * @author Oleg Gusakov
 * @version $Id: VersionRangeFactory.java 746441 2009-02-21 06:37:18Z ogusakov $
 */
public class VersionRangeFactory
{

    public static VersionRange create( final String version )
        throws VersionException
    {
        return new MavenVersionRange( version );
    }

    public static VersionRange create( final String version, final QualityRange qRange )
        throws VersionException
    {
        return new MavenVersionRange( version, qRange );
    }

    // --------------------------------------------------------------------------------------------
    /**
     * helpful latest version calculator
     *
     * @param versions
     * @param noSnapshots
     * @return
     */
    public static final String findLatest( final Collection<String> versions, final boolean noSnapshots )
    {
        DefaultArtifactVersion tempDav = null;
        DefaultArtifactVersion tempDav2 = null;
        String version = null;

        // find latest
        for ( String vn : versions )
        {
            // RELEASE?
            if ( noSnapshots )
            {
                if( vn.endsWith( Artifact.SNAPSHOT_VERSION ) || vn.matches( Artifact.SNAPSHOT_TS_REGEX ) )
                continue;
            }

            if ( version == null )
            {
                version = vn;
                tempDav = new DefaultArtifactVersion( vn );
                continue;
            }

            tempDav2 = new DefaultArtifactVersion( vn );
            if ( tempDav2.compareTo( tempDav ) > 0 )
            {
                version = vn;
                tempDav = tempDav2;
            }
        }
        return version;
    }

}
