package org.apache.maven.mercury.ant.tasks;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.util.Util;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 * @author Oleg Gusakov
 * @version $Id: Dependency.java 762963 2009-04-07 21:01:07Z ogusakov $
 */

public class Dependency
{
    private static final Language LANG = new DefaultLanguage( Dependency.class );

    protected ArtifactMetadata _amd;

    protected String _pom;

    protected boolean _optional = false;

    /** dependency processor type, if any */
    protected String _processor;
    
    protected List<String> _inclusions;
    
    protected List<String> _exclusions;
    
    private static long _count = 0L;
    
    public ArtifactMetadata getMetadata()
    {
        _amd.setInclusions( getInclusions() );
        
        _amd.setExclusions( getExclusions() );
        
        return _amd;
    }
    
    private Collection<ArtifactMetadata> getExclusions()
    {
        if( Util.isEmpty( _exclusions ))
            return null;
        
        List<ArtifactMetadata> res = new ArrayList<ArtifactMetadata>( _exclusions.size() );
        
        for( String name : _exclusions )
            res.add( new ArtifactMetadata(name) );
        
        return res;
    }

    private Collection<ArtifactMetadata> getInclusions()
    {
        if( Util.isEmpty( _inclusions ))
            return null;
        
        List<ArtifactMetadata> res = new ArrayList<ArtifactMetadata>( _inclusions.size() );
        
        for( String name : _inclusions )
            res.add( new ArtifactMetadata(name) );
        
        return res;
    }

    public void setName( String name )
    {
        if( _pom != null )
            throw new IllegalArgumentException( LANG.getMessage( "dependency.amd.pom.exists", _pom, name ) );
        
        _amd = new ArtifactMetadata( name );

        _amd.setOptional( _optional );
    }

    public void setId( String name )
    {
        setName( name );
    }

    public void setOptional( boolean optional )
    {
        this._optional = optional;

        if ( _amd != null )
        {
            _amd.setOptional( optional );
        }
    }

    public void setPom( String pom )
    {
        if( _amd != null )
            throw new IllegalArgumentException( LANG.getMessage( "dependency.pom.amd.exists", _amd.toString(), pom ) );

        setId( "__ant_fake:_ant_fake:" + (_count++)+"::pom" );
        
        int pos = pom.indexOf( ':' );

        if ( pos != -1 )
        {
            this._processor = pom.substring( 0, pos );
            this._pom = pom.substring( pos + 1 );
        }
        else
        {
            this._pom = pom;
        }
    }

    public void setSource( String pom )
    {
        setPom( pom );
    }

    public void setGroupId( String groupId )
    {
        if ( _amd == null )
        {
            _amd = new ArtifactMetadata();
        }

        _amd.setGroupId( groupId );
    }

    public void setGroupid( String groupId )
    {
        setGroupId( groupId );
    }

    public void setArtifactId( String artifactId )
    {
        if ( _amd == null )
        {
            _amd = new ArtifactMetadata();
        }

        _amd.setArtifactId( artifactId );
    }

    public void setArtifactid( String artifactId )
    {
        setArtifactId( artifactId );
    }

    public void setVersion( String version )
    {
        if ( _amd == null )
        {
            _amd = new ArtifactMetadata();
        }

        _amd.setVersion( version );
    }
    
    public void addConfiguredExclusions( Exclusions ex )
    {
        _exclusions = ex._list;
    }
    
    public void addConfiguredInclusions( Inclusions in )
    {
        _inclusions = in._list;
    }
}
