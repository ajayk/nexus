/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.proxy.cache;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.plexus.components.ehcache.PlexusEhCacheWrapper;

/**
 * The Class EhCacheCacheManager is a thin wrapper around EhCache, just to make things going.
 * 
 * @author cstamas
 */
@Component(role = CacheManager.class )
public class EhCacheCacheManager
    extends AbstractLogEnabled
    implements CacheManager
{
    @Requirement
    private PlexusEhCacheWrapper cacheManager;

   
    public PathCache getPathCache( String cache )
    {
        net.sf.ehcache.CacheManager ehCacheManager = this.cacheManager.getEhCacheManager();
        if ( !ehCacheManager.cacheExists( cache ) )
        {
            ehCacheManager.addCache( cache );
        }

        return new EhCachePathCache( ehCacheManager.getEhcache( cache ) );
    }


    public void startService()
        throws Exception
    {
        cacheManager.start();
    }


    public void stopService()
        throws Exception
    {
       cacheManager.stop();
        
    }
}
