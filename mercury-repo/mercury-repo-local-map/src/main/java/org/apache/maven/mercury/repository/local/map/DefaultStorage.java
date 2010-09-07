package org.apache.maven.mercury.repository.local.map;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.util.FileUtil;
import org.apache.maven.mercury.util.Util;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id: DefaultStorage.java 762963 2009-04-07 21:01:07Z ogusakov $
 *
 */
public class DefaultStorage
implements Storage
{
    private static final Language _lang = new DefaultLanguage( DefaultStorage.class );
    
    Map< String, Artifact > _artifacts;

    Map< String, File > _files;
    
    File _dir;
        
    public DefaultStorage( File dir )
    throws StorageException
    {
        if( dir == null )
        {
            try
            {
                _dir = File.createTempFile( "temp-", "-mercury-default-storage" );
                _dir.deleteOnExit();
            }
            catch ( IOException e )
            {
                throw new StorageException(e);
            }
            
            _dir.delete();
            
            _dir.mkdirs();
        }
        else
        {
            if( !dir.exists() )
                dir.mkdirs();
            else
                if( dir.isDirectory() )
                    throw new StorageException( _lang.getMessage( "default.storage.bad.dir", dir.getAbsolutePath() ) );
            
            _dir = dir;
        }
    }

    public DefaultStorage()
    throws StorageException
    {
        this( null );
    }

    public void add( ArtifactMetadata bmd, Artifact artifact )
    {
        if( _artifacts == null )
            _artifacts = new HashMap<String, Artifact>(32);
        
        _artifacts.put( bmd.toString(), artifact );
    }

    public Artifact findArtifact( ArtifactMetadata bmd )
    {
        if( _artifacts == null )
            return null;
        
        return _artifacts.get( bmd.getGAV() );
    }

    public byte[] findRaw( String key )
    throws StorageException
    {
        if( Util.isEmpty( _files ) )
            return null;
        
        File f = _files.get( key );
        
        if( f == null )
            return null;
        
        try
        {
            return FileUtil.readRawData( f );
        }
        catch ( IOException e )
        {
            throw new StorageException(e);
        }
    }

    public void add( String key, byte[] bytes )
    throws StorageException
    {
        try
        {
            add( key, FileUtil.writeTempData( bytes ) );
        }
        catch ( IOException e )
        {
            throw new StorageException(e);
        }
    }

    public void add( String key, File file )
    throws StorageException
    {
        if( file == null || !file.exists() )
            throw new StorageException( _lang.getMessage( "defaultStorage.add.file.no.file", key ) );
        
        if( _files == null )
            _files = new HashMap<String, File>(32);
        
        _files.put( key, file );
    }

    /* (non-Javadoc)
     * @see org.apache.maven.mercury.repository.local.map.Storage#removeRaw(java.lang.String)
     */
    public void removeRaw( String key )
    throws StorageException
    {
        if( Util.isEmpty( _files ) )
            throw new StorageException( _lang.getMessage( "dep.cannot.remove", key ) );
    }
    
}
