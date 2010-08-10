/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.artifact.VersionUtils;
import org.sonatype.nexus.index.creator.JarFileContentsIndexCreator;
import org.sonatype.nexus.index.creator.MavenPluginArtifactInfoIndexCreator;
import org.sonatype.nexus.index.creator.MinimalArtifactInfoIndexCreator;

/**
 * ArtifactInfo holds the values known about an repository artifact. This is a simple Value Object kind of stuff.
 * Phasing out.
 * 
 * @author Jason van Zyl
 * @author Eugene Kuleshov
 */
public class ArtifactInfo
    extends ArtifactInfoRecord
{
    private static final long serialVersionUID = 6028843453477511104L;

    // --

    public static final String ROOT_GROUPS = "rootGroups";

    public static final String ROOT_GROUPS_VALUE = "rootGroups";

    public static final String ROOT_GROUPS_LIST = "rootGroupsList";

    public static final String ALL_GROUPS = "allGroups";

    public static final String ALL_GROUPS_VALUE = "allGroups";

    public static final String ALL_GROUPS_LIST = "allGroupsList";

    // ----------

    /**
     * Unique groupId, artifactId, version, classifier, extension (or packaging). Stored, indexed untokenized
     */
    public static final String UINFO = FLD_UINFO.getKey();

    /**
     * Field that contains {@link #UINFO} value for deleted artifact
     */
    public static final String DELETED = FLD_DELETED.getKey();

    /**
     * GroupId. Not stored, indexed untokenized
     */
    public static final String GROUP_ID = MinimalArtifactInfoIndexCreator.FLD_GROUP_ID_KW.getKey();

    /**
     * ArtifactId. Not stored, indexed tokenized
     */
    public static final String ARTIFACT_ID = MinimalArtifactInfoIndexCreator.FLD_ARTIFACT_ID_KW.getKey();

    /**
     * Version. Not stored, indexed tokenized
     */
    public static final String VERSION = MinimalArtifactInfoIndexCreator.FLD_VERSION_KW.getKey();

    /**
     * Packaging. Not stored, indexed untokenized
     */
    public static final String PACKAGING = MinimalArtifactInfoIndexCreator.FLD_PACKAGING.getKey();

    /**
     * Classifier. Not stored, indexed untokenized
     */
    public static final String CLASSIFIER = MinimalArtifactInfoIndexCreator.FLD_CLASSIFIER.getKey();

    /**
     * Info: packaging, lastModified, size, sourcesExists, javadocExists, signatureExists. Stored, not indexed.
     */
    public static final String INFO = MinimalArtifactInfoIndexCreator.FLD_INFO.getKey();

    /**
     * Name. Stored, not indexed
     */
    public static final String NAME = MinimalArtifactInfoIndexCreator.FLD_NAME.getKey();

    /**
     * Description. Stored, not indexed
     */
    public static final String DESCRIPTION = MinimalArtifactInfoIndexCreator.FLD_DESCRIPTION.getKey();

    /**
     * Last modified. Stored, not indexed
     */
    public static final String LAST_MODIFIED = MinimalArtifactInfoIndexCreator.FLD_LAST_MODIFIED.getKey();

    /**
     * SHA1. Stored, indexed untokenized
     */
    public static final String SHA1 = MinimalArtifactInfoIndexCreator.FLD_SHA1.getKey();

    /**
     * Class names Stored compressed, indexed tokeninzed
     */
    public static final String NAMES = JarFileContentsIndexCreator.FLD_CLASSNAMES_KW.getKey();

    /**
     * Plugin prefix. Stored, not indexed
     */
    public static final String PLUGIN_PREFIX = MavenPluginArtifactInfoIndexCreator.FLD_PLUGIN_PREFIX.getKey();

    /**
     * Plugin goals. Stored, not indexed
     */
    public static final String PLUGIN_GOALS = MavenPluginArtifactInfoIndexCreator.FLD_PLUGIN_GOALS.getKey();

    public static final Comparator<ArtifactInfo> VERSION_COMPARATOR = new VersionComparator();

    public static final Comparator<ArtifactInfo> REPOSITORY_VERSION_COMPARATOR = new RepositoryVersionComparator();

    public String fname;

    public String fextension;

    public String groupId;

    public String artifactId;

    public String version;

    @Deprecated
    private transient ArtifactVersion artifactVersion;
    
    private transient float luceneScore;

    public String classifier;

    /**
     * Artifact packaging for the main artifact and extension for secondary artifact (no classifier)
     */
    public String packaging;

    public String name;

    public String description;

    public long lastModified = -1;

    public long size = -1;

    public String md5;

    public String sha1;

    public ArtifactAvailablility sourcesExists = ArtifactAvailablility.NOT_PRESENT;

    public ArtifactAvailablility javadocExists = ArtifactAvailablility.NOT_PRESENT;

    public ArtifactAvailablility signatureExists = ArtifactAvailablility.NOT_PRESENT;

    public String classNames;

    public String repository;

    public String path;

    public String remoteUrl;

    public String context;

    /**
     * Plugin goal prefix (only if packaging is "maven-plugin")
     */
    public String prefix;

    /**
     * Plugin goals (only if packaging is "maven-plugin")
     */
    public List<String> goals;

    private String uinfo = null;

    private final Map<String, String> attributes = new HashMap<String, String>();

    private final List<MatchHighlight> matchHighlights = new ArrayList<MatchHighlight>();

    public ArtifactInfo()
    {
    }

    public ArtifactInfo( String repository, String groupId, String artifactId, String version, String classifier )
    {
        this.repository = repository;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
    }

    @Deprecated
    public ArtifactVersion getArtifactVersion()
    {
        if ( artifactVersion == null )
        {
            artifactVersion = new DefaultArtifactVersion( version );
        }
        return artifactVersion;
    }

    public float getLuceneScore()
    {
        return luceneScore;
    }

    public void setLuceneScore( float score )
    {
        this.luceneScore = score;
    }

    public String getUinfo()
    {
        if ( uinfo == null )
        {
            uinfo = new StringBuilder() //
            .append( groupId ).append( FS ) //
            .append( artifactId ).append( FS ) //
            .append( version ).append( FS ) //
            .append( nvl( classifier ) ) //
            .append( StringUtils.isEmpty( classifier ) || StringUtils.isEmpty( packaging ) ? "" : FS + packaging ) //
            .toString(); // extension is stored in the packaging field when classifier is not used
        }

        return uinfo;
    }

    public String getRootGroup()
    {
        int n = groupId.indexOf( '.' );
        if ( n > -1 )
        {
            return groupId.substring( 0, n );
        }
        return groupId;
    }

    public Gav calculateGav()
        throws IllegalArtifactCoordinateException
    {
        return new Gav( groupId, artifactId, version, classifier, fextension, null, // snapshotBuildNumber
            null, // snapshotTimeStamp
            fname, // name
            VersionUtils.isSnapshot( version ), // isSnapshot
            false, // hash
            null, // hashType
            false, // signature
            null ); // signatureType
    }

    public Map<String, String> getAttributes()
    {
        return attributes;
    }

    public List<MatchHighlight> getMatchHighlights()
    {
        return matchHighlights;
    }

    @Override
    public String toString()
    {
        return new StringBuilder( groupId ).append( ':' ).append( artifactId ) //
        .append( ':' ).append( version ) //
        .append( ':' ).append( classifier ) //
        .append( ':' ).append( packaging ).toString();
    }

    private static final List<Field> DEFAULT_FIELDS = new ArrayList<Field>();
    static
    {
        DEFAULT_FIELDS.add( MAVEN.GROUP_ID );
        DEFAULT_FIELDS.add( MAVEN.ARTIFACT_ID );
        DEFAULT_FIELDS.add( MAVEN.VERSION );
        DEFAULT_FIELDS.add( MAVEN.PACKAGING );
        DEFAULT_FIELDS.add( MAVEN.CLASSIFIER );
        DEFAULT_FIELDS.add( MAVEN.SHA1 );
        DEFAULT_FIELDS.add( MAVEN.NAME );
        DEFAULT_FIELDS.add( MAVEN.DESCRIPTION );
        DEFAULT_FIELDS.add( MAVEN.CLASSNAMES );
        DEFAULT_FIELDS.add( MAVEN.REPOSITORY_ID );
    }

    private List<Field> fields;

    public Collection<Field> getFields()
    {
        if ( fields == null )
        {
            fields = new ArrayList<Field>( DEFAULT_FIELDS.size() );

            fields.addAll( DEFAULT_FIELDS );
        }

        return fields;
    }

    /**
     * This method will dissapear, once we drop ArtifactInfo.
     * 
     * @param field
     * @return
     */
    public String getFieldValue( Field field )
    {
        if ( MAVEN.GROUP_ID.equals( field ) )
        {
            return groupId;
        }
        else if ( MAVEN.ARTIFACT_ID.equals( field ) )
        {
            return artifactId;
        }
        else if ( MAVEN.VERSION.equals( field ) )
        {
            return version;
        }
        else if ( MAVEN.PACKAGING.equals( field ) )
        {
            return packaging;
        }
        else if ( MAVEN.CLASSIFIER.equals( field ) )
        {
            return classifier;
        }
        else if ( MAVEN.SHA1.equals( field ) )
        {
            return sha1;
        }
        else if ( MAVEN.NAME.equals( field ) )
        {
            return name;
        }
        else if ( MAVEN.DESCRIPTION.equals( field ) )
        {
            return description;
        }
        else if ( MAVEN.CLASSNAMES.equals( field ) )
        {
            return classNames;
        }
        else if ( MAVEN.REPOSITORY_ID.equals( field ) )
        {
            return repository;
        }

        // no match
        return null;
    }

    public ArtifactInfo setFieldValue( Field field, String value )
    {
        if ( MAVEN.GROUP_ID.equals( field ) )
        {
            groupId = value;
        }
        else if ( MAVEN.ARTIFACT_ID.equals( field ) )
        {
            artifactId = value;
        }
        else if ( MAVEN.VERSION.equals( field ) )
        {
            version = value;
        }
        else if ( MAVEN.PACKAGING.equals( field ) )
        {
            packaging = value;
        }
        else if ( MAVEN.CLASSIFIER.equals( field ) )
        {
            classifier = value;
        }
        else if ( MAVEN.SHA1.equals( field ) )
        {
            sha1 = value;
        }
        else if ( MAVEN.NAME.equals( field ) )
        {
            name = value;
        }
        else if ( MAVEN.DESCRIPTION.equals( field ) )
        {
            description = value;
        }
        else if ( MAVEN.CLASSNAMES.equals( field ) )
        {
            classNames = value;
        }
        else if ( MAVEN.REPOSITORY_ID.equals( field ) )
        {
            repository = value;
        }

        // no match
        return this;
    }

    // ----------------------------------------------------------------------------
    // Utils
    // ----------------------------------------------------------------------------

    public static String nvl( String v )
    {
        return v == null ? NA : v;
    }

    public static String renvl( String v )
    {
        return NA.equals( v ) ? null : v;
    }

    public static String lst2str( Collection<String> list )
    {
        StringBuilder sb = new StringBuilder();
        for ( String s : list )
        {
            sb.append( s ).append( ArtifactInfo.FS );
        }
        return sb.length() == 0 ? sb.toString() : sb.substring( 0, sb.length() - 1 );
    }

    public static List<String> str2lst( String str )
    {
        return Arrays.asList( ArtifactInfo.FS_PATTERN.split( str ) );
    }

    /**
     * A version comparator
     */
    static class VersionComparator
        implements Comparator<ArtifactInfo>
    {
        @SuppressWarnings( "unchecked" )
        public int compare( ArtifactInfo f1, ArtifactInfo f2 )
        {
            int n = f1.groupId.compareTo( f2.groupId );
            if ( n != 0 )
            {
                return n;
            }

            n = f1.artifactId.compareTo( f2.artifactId );
            if ( n != 0 )
            {
                return n;
            }

            n = -f1.getArtifactVersion().compareTo( f2.getArtifactVersion() );
            if ( n != 0 )
            {
                return n;
            }

            {
                String c1 = f1.classifier;
                String c2 = f2.classifier;
                if ( c1 == null )
                {
                    if ( c2 != null )
                    {
                        return -1;
                    }
                }
                else
                {
                    if ( c2 == null )
                    {
                        return 1;
                    }

                    n = c1.compareTo( c2 );
                    if ( n != 0 )
                    {
                        return n;
                    }
                }
            }

            {
                String p1 = f1.packaging;
                String p2 = f2.packaging;
                if ( p1 == null )
                {
                    return p2 == null ? 0 : -1;
                }
                else
                {
                    return p2 == null ? 1 : p1.compareTo( p2 );
                }
            }
        }
    }

    /**
     * A repository and version comparator
     */
    static class RepositoryVersionComparator
        extends VersionComparator
    {
        @Override
        public int compare( ArtifactInfo f1, ArtifactInfo f2 )
        {
            int n = super.compare( f1, f2 );
            if ( n != 0 )
            {
                return n;
            }

            String r1 = f1.repository;
            String r2 = f2.repository;
            if ( r1 == null )
            {
                return r2 == null ? 0 : -1;
            }
            else
            {
                return r2 == null ? 1 : r1.compareTo( r2 );
            }
        }
    }

}
