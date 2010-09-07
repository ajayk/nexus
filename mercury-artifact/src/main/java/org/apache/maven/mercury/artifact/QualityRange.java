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

import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 * @author Oleg Gusakov
 * @version $Id: QualityRange.java 762963 2009-04-07 21:01:07Z ogusakov $
 */
public class QualityRange
{
    private static final Language LANG = new DefaultLanguage( QualityRange.class );
    
    public static final QualityRange SNAPSHOTS_ONLY =
        new QualityRange( Quality.SNAPSHOT_QUALITY, true, Quality.SNAPSHOT_TS_QUALITY, true );

    public static final QualityRange ALPHA_ONLY =
        new QualityRange( Quality.SNAPSHOT_QUALITY, false, Quality.BETA_QUALITY, false );

    public static final QualityRange BETA_ONLY =
        new QualityRange( Quality.ALPHA_QUALITY, false, Quality.RELEASE_QUALITY, false );

    public static final QualityRange RELEASES_ONLY =
        new QualityRange( Quality.RELEASE_QUALITY, true, Quality.RELEASE_QUALITY, true );

    public static final QualityRange ALL =
        new QualityRange( Quality.SNAPSHOT_QUALITY, true, Quality.RELEASE_QUALITY, true );

    protected Quality qualityFrom = Quality.SNAPSHOT_QUALITY;

    protected boolean fromInclusive = true;

    protected Quality qualityTo = Quality.RELEASE_QUALITY;

    protected boolean toInclusive = true;

    /**
     * @param qualityFrom
     * @param fromInclusive
     * @param qualityTo
     * @param toInclusive
     */
    public QualityRange( Quality qualityFrom, boolean fromInclusive, Quality qualityTo, boolean toInclusive )
    {
        this.qualityFrom = qualityFrom;
        this.fromInclusive = fromInclusive;

        this.qualityTo = qualityTo;
        this.toInclusive = toInclusive;
    }
    
    public static QualityRange create( boolean releases, boolean snapshots )
    {
        if( releases && snapshots )
            return ALL;
        else
            if( releases )
                return new QualityRange( Quality.ALPHA_QUALITY, true, Quality.RELEASE_QUALITY, true );
            else
                if( snapshots )
                    return new QualityRange( Quality.SNAPSHOT_QUALITY, true, Quality.SNAPSHOT_TS_QUALITY, true );
        
        throw new IllegalArgumentException( LANG.getMessage( "quality.no.sn.no.rel" ) );
    }

    // ---------------------------------------------------------------------------
    public boolean isAcceptedQuality( Quality quality )
    {
        // null quality means anything goes
        if ( quality == null )
        {
            return true;
        }

        int from = quality.compareTo( qualityFrom );
        if ( from == 0 )
        {
            return fromInclusive;
        }

        int to = quality.compareTo( qualityTo );
        if ( to == 0 )
        {
            return toInclusive;
        }

        return from > 0 && to < 0;
    }

}
