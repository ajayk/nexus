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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.MavenDependencyProcessor;
import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactExclusionList;
import org.apache.maven.mercury.artifact.ArtifactInclusionList;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.ArtifactQueryList;
import org.apache.maven.mercury.artifact.ArtifactScopeEnum;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.MercuryLoggerManager;
import org.apache.maven.mercury.metadata.DependencyBuilder;
import org.apache.maven.mercury.metadata.DependencyBuilderFactory;
import org.apache.maven.mercury.repository.api.ArtifactResults;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.local.map.DefaultStorage;
import org.apache.maven.mercury.repository.local.map.LocalRepositoryMap;
import org.apache.maven.mercury.repository.local.map.Storage;
import org.apache.maven.mercury.repository.local.map.StorageException;
import org.apache.maven.mercury.repository.virtual.VirtualRepositoryReader;
import org.apache.maven.mercury.util.Util;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.ResourceCollection;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 * @author Oleg Gusakov
 * @version $Id: Dep.java 762963 2009-04-07 21:01:07Z ogusakov $
 */
public class Dep
    extends AbstractDataType
    implements ResourceCollection
{
    private static final Language LANG = new DefaultLanguage( Dep.class );

    private static final IMercuryLogger LOG = MercuryLoggerManager.getLogger( Dep.class );

    private List<Dependency> _dependencies;

    private List<Artifact> _artifacts;

    private List<File> _files;

    private String _configId;

    private ArtifactScopeEnum _scope = ArtifactScopeEnum.compile;

    private boolean _transitive = true;

    private LocalRepositoryMap _pomRepo;

    private Storage _pomStorage;

    private Dependency _sourceDependency;
    
    List<String> _inclusions;
    
    List<String> _exclusions;

    private List<ArtifactMetadata> getDependencies( VirtualRepositoryReader vr )
        throws RepositoryException
    {
        if ( Util.isEmpty( _dependencies ) )
        {
            return null;
        }

        List<ArtifactMetadata> res = new ArrayList<ArtifactMetadata>( _dependencies.size() );

        for ( Dependency d : _dependencies )
        {
            if ( d._amd == null )
            {
                throw new IllegalArgumentException( LANG.getMessage( "dep.dependency.name.mandatory" ) );
            }

            if ( Util.isEmpty( d._pom ) )
            {
                res.add( d.getMetadata() );
            }
            else
            {
                String key = d._amd.getGAV();

                ArtifactMetadata deps = null;

                File pomFile = new File( d._pom );

                if ( !pomFile.exists() )
                    throw new RepositoryException( "pom file " + d._pom + " does not exist" );

                try
                {
                    _pomStorage.add( key, pomFile );
                    
                    d._amd.setTracker( _pomRepo.getReader() );

                    deps = vr.readDependencies( d._amd );

                    _pomStorage.removeRaw( key );
                }
                catch ( StorageException e )
                {
                    throw new RepositoryException( e );
                }

                if ( deps != null && !Util.isEmpty( deps.getDependencies() ) )
                {
                    for ( ArtifactMetadata bmd : deps.getDependencies() )
                    {
                        res.add( bmd );
                    }
                }
            }
        }

        return res;
    }

    public Dependency createDependency()
    {
        if ( _dependencies == null )
        {
            _dependencies = new ArrayList<Dependency>( 8 );
        }

        Dependency dep = new Dependency();

        _dependencies.add( dep );

        return dep;
    }

    // ----------------------------------------------------------------------------------------
    protected List<Artifact> resolve()
        throws Exception
    {
        Config config = AbstractAntTask.findConfig( getProject(), _configId );

        return resolve( config, _scope );
    }

    // ----------------------------------------------------------------------------------------
    protected List<Artifact> resolve( Config config, ArtifactScopeEnum scope )
        throws Exception
    {
        if ( !Util.isEmpty( _artifacts ) )
        {
            return _artifacts;
        }

        if ( Util.isEmpty( _dependencies ) )
        {
            return null;
        }

        List<Repository> repos = config.getRepositories();

        DependencyProcessor dp = new MavenDependencyProcessor();

        DependencyBuilder db = DependencyBuilderFactory.create( DependencyBuilderFactory.JAVA_DEPENDENCY_MODEL, repos );

        _pomStorage = new DefaultStorage();

        _pomRepo = new LocalRepositoryMap( "inMemMdRepo", dp, _pomStorage );

        repos.add( 0, _pomRepo );

        VirtualRepositoryReader vr = new VirtualRepositoryReader( repos );
        
        _pomRepo.setMetadataReader( vr );

        List<ArtifactMetadata> depList = getDependencies( vr );
        
        ArtifactQueryList aql = new ArtifactQueryList( depList );
        
        ArtifactInclusionList ail = getInclusions();
        
        ArtifactExclusionList ael = getExclusions();

        List<ArtifactMetadata> res =
            _transitive ? db.resolveConflicts( scope, aql, ail, ael )
                            : toArtifactMetadataList( depList );
            
        db.close();

        if ( Util.isEmpty( res ) )
        {
            throw new BuildException( LANG.getMessage( "resolve.empty.classpath", scope.toString(), depList.toString() ) );
        }

        ArtifactResults aRes = vr.readArtifacts( res );

        if ( aRes == null )
        {
            throw new BuildException( LANG.getMessage( "resolve.cannot.read", config.getId(), res.toString() ) );
        }

        if ( aRes == null )
        {
            throw new Exception( LANG.getMessage( "vr.error", aRes.getExceptions().toString() ) );
        }

        if ( !aRes.hasResults() )
        {
            if( aRes.hasExceptions() )
                throw new Exception( LANG.getMessage( "vr.error", aRes.getExceptions().toString() ) );
            
            return null;
        }

        Map<ArtifactMetadata, List<Artifact>> resMap = aRes.getResults();

        int count = 0;
        for ( ArtifactMetadata key : resMap.keySet() )
        {
            List<Artifact> artifacts = resMap.get( key );
            if ( artifacts != null )
            {
                count += artifacts.size();
            }
        }

        if ( count == 0 )
        {
            return null;
        }

        _artifacts = new ArrayList<Artifact>( count );

        for ( ArtifactMetadata key : resMap.keySet() )
        {
            List<Artifact> artifacts = resMap.get( key );

            if ( !Util.isEmpty( artifacts ) )
            {
                for ( Artifact a : artifacts )
                {
                    _artifacts.add( a );
                }
            }
        }

        return _artifacts;
    }

    private ArtifactExclusionList getExclusions()
    {
        if( Util.isEmpty( _exclusions ) )
            return null;
        
        return new ArtifactExclusionList( _exclusions.toArray( new String [_exclusions.size()] ) );
    }

    private ArtifactInclusionList getInclusions()
    {
        if( Util.isEmpty( _inclusions ) )
            return null;
        
        return new ArtifactInclusionList( _inclusions.toArray( new String [_inclusions.size()] ) );
    }

    /**
     * @param depList
     * @return
     */
    private List<ArtifactMetadata> toArtifactMetadataList( List<ArtifactMetadata> depList )
    {
        if ( Util.isEmpty( depList ) )
            return null;

        List<ArtifactMetadata> res = new ArrayList<ArtifactMetadata>( depList.size() );

        for ( ArtifactMetadata bmd : depList )
            res.add( new ArtifactMetadata( bmd ) );

        return res;
    }

    // attributes
    public void setConfigid( String configid )
    {
        this._configId = configid;
    }

    public void setScope( ArtifactScopeEnum scope )
    {
        this._scope = scope;
    }

//    @Override
//    public void setId( String id )
//    {
//        super.setId( id );
//
//        if ( _sourceDependency != null )
//            _sourceDependency.setId( id );
//    }

    public void setSource( String pom )
    {
        _sourceDependency = createDependency();
        
        _sourceDependency.setSource( pom );
    }

    public void setTransitive( boolean val )
    {
        this._transitive = val;
    }
    
    public void addConfiguredExclusions( Exclusions ex )
    {
        _exclusions = ex._list;
    }
    
    public void addConfiguredInclusions( Inclusions in )
    {
        _inclusions = in._list;
    }

    protected void setList( List<Dependency> dependencies )
    {
        _dependencies = dependencies;
    }

    protected void setExclusions( List<String> exList )
    {
        if( _exclusions == null )
            _exclusions = exList;
        else
            _exclusions.addAll( exList );
    }

    protected void setInclusions( List<String> inList )
    {
        if( _inclusions == null )
            _inclusions = inList;
        else
            _inclusions.addAll( inList );
    }

    // ----------------------------------------------------------------------------------------
    public boolean isFilesystemOnly()
    {
        return true;
    }

    // ----------------------------------------------------------------------------------------
    public Iterator<File> iterator()
    {
        try
        {
            if ( _files != null )
            {
                return _files.iterator();
            }

            List<Artifact> artifacts = resolve();

            if ( Util.isEmpty( artifacts ) )
            {
                return null;
            }

            _files = new ArrayList<File>( artifacts.size() );

            for ( Artifact a : _artifacts )
            {
                _files.add( a.getFile() );
            }

            return _files.iterator();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );

            return null;
        }
    }

    // ----------------------------------------------------------------------------------------
    public int size()
    {
        try
        {
            List<Artifact> artifacts = resolve();

            if ( Util.isEmpty( artifacts ) )
            {
                return 0;
            }

            return artifacts.size();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );

            return 0;
        }
    }
    // ----------------------------------------------------------------------------------------
    // ----------------------------------------------------------------------------------------
}