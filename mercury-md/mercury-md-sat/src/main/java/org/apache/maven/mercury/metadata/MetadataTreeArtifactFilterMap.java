/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package org.apache.maven.mercury.metadata;

import java.util.Collection;
import java.util.Map;

import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.util.Util;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id: MetadataTreeArtifactFilterMap.java 762963 2009-04-07 21:01:07Z ogusakov $
 *
 */
public class MetadataTreeArtifactFilterMap
    implements MetadataTreeArtifactFilter
{
    Map<String, Collection<String>> _vetos;
    
    public MetadataTreeArtifactFilterMap(Map<String, Collection<String>> vetos)
    {
        _vetos = vetos;
    }
    
    public boolean veto( ArtifactMetadata md )
    {
        String key = md.toManagementString();
        
        Collection<String> ver = _vetos.get( key );
        
        if( Util.isEmpty( ver ) )
            return false;
        
        return ver.contains( md.getVersion() );
    }

}
