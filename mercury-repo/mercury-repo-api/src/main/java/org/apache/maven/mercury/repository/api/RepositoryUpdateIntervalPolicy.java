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

import java.text.ParseException;

import org.apache.maven.mercury.artifact.Quality;
import org.apache.maven.mercury.util.TimeUtil;
import org.apache.maven.mercury.util.Util;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 * implements current maven update policy
 * 
 * @author Oleg Gusakov
 * @version $Id: RepositoryUpdateIntervalPolicy.java 762963 2009-04-07 21:01:07Z ogusakov $
 */
public class RepositoryUpdateIntervalPolicy
    implements RepositoryUpdatePolicy
{
    private static final Language _lang = new DefaultLanguage( RepositoryUpdateIntervalPolicy.class );

    public static final String UPDATE_POLICY_NAME_NEVER = "never";

    public static final String UPDATE_POLICY_NAME_ALWAYS = "always";

    /** same as always - do it NOW */
    public static final String UPDATE_POLICY_NAME_NOW = "now";

    public static final String UPDATE_POLICY_NAME_DAILY = "daily";

    public static final String UPDATE_POLICY_NAME_INTERVAL = "interval";

    private static final int UPDATE_POLICY_INTERVAL_LENGTH = UPDATE_POLICY_NAME_INTERVAL.length();

    public static final String DEFAULT_UPDATE_POLICY_NAME = UPDATE_POLICY_NAME_DAILY;

    public static final RepositoryUpdateIntervalPolicy UPDATE_POLICY_NEVER =
        new RepositoryUpdateIntervalPolicy( UPDATE_POLICY_NAME_NEVER );

    public static final RepositoryUpdateIntervalPolicy UPDATE_POLICY_ALWAYS =
        new RepositoryUpdateIntervalPolicy( UPDATE_POLICY_NAME_ALWAYS );

    public static final RepositoryUpdateIntervalPolicy UPDATE_POLICY_DAILY =
        new RepositoryUpdateIntervalPolicy( UPDATE_POLICY_NAME_DAILY );

    /** this is the default policy - don't update unless asked */
    public static final RepositoryUpdateIntervalPolicy DEFAULT_UPDATE_POLICY = UPDATE_POLICY_NEVER;

    private static final long NEVER = -1L;

    private static final long ALWAYS = 0L;
    
    private static final long DAYLY = 3600000L * 24L;

    protected long interval = DAYLY;

    public RepositoryUpdateIntervalPolicy()
    {
    }

    public RepositoryUpdateIntervalPolicy( String policy )
    {
        init( policy );
    }

    /**
     * used mostly for testing as it's too much waiting for a minute to test expiration
     * 
     * @param interval
     */
    public RepositoryUpdateIntervalPolicy setInterval( long interval )
    {
        this.interval = interval;
        return this;
    }

    public void init( String policy )
    {
        interval = parsePolicy( policy );
    }

    public static long parsePolicy( String policy )
    {
        if ( Util.isEmpty( policy ) )
            throw new IllegalArgumentException( _lang.getMessage( "empty.policy", policy ) );

        if ( policy.startsWith( UPDATE_POLICY_NAME_ALWAYS ) )
            return ALWAYS;
        else if ( policy.startsWith( UPDATE_POLICY_NAME_NOW ) )
            return ALWAYS;
        else if ( policy.startsWith( UPDATE_POLICY_NAME_DAILY ) )
            return DAYLY;
        else if ( policy.startsWith( UPDATE_POLICY_NAME_NEVER ) )
            return NEVER;
        else if ( policy.startsWith( UPDATE_POLICY_NAME_INTERVAL ) )
        {
            int len = policy.length();
            if ( len <= UPDATE_POLICY_INTERVAL_LENGTH )
                throw new IllegalArgumentException( _lang.getMessage( "bad.interval.policy", policy ) );

            return Integer.parseInt( policy.substring( len - 1 ) ) * 60000L;
        }
        else
            throw new IllegalArgumentException( _lang.getMessage( "bad.policy", policy ) );
    }

    public boolean timestampExpired( long lastUpdateMillis, Quality quality )
    {
        // save a couple of nannos 
        if ( interval == NEVER )
            return false;

        if ( interval == ALWAYS )
            return true;

        long now;
        try
        {
            now = TimeUtil.toMillis( TimeUtil.getUTCTimestamp() );
        }
        catch ( ParseException e )
        {
            throw new IllegalArgumentException( e );
        }

        boolean res = ( ( now - lastUpdateMillis ) > interval );

        return res;
    }

}
