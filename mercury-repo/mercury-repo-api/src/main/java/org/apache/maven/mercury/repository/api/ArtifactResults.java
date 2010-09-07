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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactMetadata;

/**
 * @author Oleg Gusakov
 * @version $Id: ArtifactResults.java 762963 2009-04-07 21:01:07Z ogusakov $
 */
public class ArtifactResults
    extends AbstractRepOpResult
{
    Map<ArtifactMetadata, List<Artifact>> _result = new HashMap<ArtifactMetadata, List<Artifact>>( 8 );

    public ArtifactResults()
    {
    }

    public ArtifactResults( ArtifactMetadata query, List<Artifact> result )
    {
        this._result.put( query, result );
    }

    public void add( ArtifactMetadata query, Artifact result )
    {
        List<Artifact> res = _result.get( query );
        if ( res == null )
        {
            res = new ArrayList<Artifact>( 8 );
            _result.put( query, res );
        }

        res.add( result );
    }

    public void addAll( ArtifactMetadata query, List<Artifact> result )
    {
        List<Artifact> res = _result.get( query );
        if ( res == null )
        {
            res = new ArrayList<Artifact>( 8 );
            _result.put( query, res );
        }

        res.addAll( result );
    }

    public Map<ArtifactMetadata, List<Artifact>> getResults()
    {
        return _result;
    }

    public List<Artifact> getResults( ArtifactMetadata query )
    {
        return _result.get( query );
    }

    @Override
    public boolean hasResults()
    {
        return !_result.isEmpty();
    }

    @Override
    public boolean hasResults( ArtifactMetadata key )
    {
        return !_result.isEmpty() && _result.containsKey( key ) && !_result.get( key ).isEmpty();
    }

}
