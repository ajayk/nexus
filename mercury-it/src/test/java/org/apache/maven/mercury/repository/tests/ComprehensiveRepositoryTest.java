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

package org.apache.maven.mercury.repository.tests;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.mercury.MavenDependencyProcessor;
import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.DefaultArtifact;
import org.apache.maven.mercury.artifact.QualityRange;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.repository.api.MetadataResults;
import org.apache.maven.mercury.repository.api.ArtifactResults;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;
import org.apache.maven.mercury.repository.virtual.VirtualRepositoryReader;
import org.apache.maven.mercury.transport.api.Credentials;
import org.apache.maven.mercury.transport.api.Server;
import org.apache.maven.mercury.util.FileUtil;
import org.codehaus.plexus.PlexusTestCase;

/**
 * This set of UTs covers a comprehensive use case,
 * involving majority of Mercury repository functionality
 *
 * @author Oleg Gusakov
 * @version $Id: ComprehensiveRepositoryTest.java 762963 2009-04-07 21:01:07Z ogusakov $
 *
 */
public class ComprehensiveRepositoryTest
extends PlexusTestCase
{
    WebDavServer _server1;
    File         _base1;
    static final String _context1 = "/webdav1";
    int _port1;
    RemoteRepositoryM2 _rr1;
    
    WebDavServer _server2;
    File         _base2;
    static final String _context2 = "/webdav2";
    int _port2;
    RemoteRepositoryM2 _rr2;
    
    File _lbase1;
    static final String _local1 = "./target/webdav1local";
    LocalRepositoryM2 _lr1;
    
    File _lbase2;
    static final String _local2 = "./target/webdav2local";
    LocalRepositoryM2 _lr2;
    
    static final String _resourceBase = "./target/test-classes";
    
    List<Repository> _rrs;
    List<Repository> _lrs;
    List<Repository> _repos;
    
    private static final boolean isWindows = File.pathSeparatorChar == ';';
    
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        String prefix = "-t-";
        String suffix = "-t";
        File temp = File.createTempFile( prefix, suffix );
        
        DependencyProcessor dp = new MavenDependencyProcessor();
        Credentials user = new Credentials("foo","bar");
        
        _base1 = new File( "./target/webdav1" + temp.getName() );
//        FileUtil.delete( _base1 );
        assertFalse( _base1.exists() );
        _base1.mkdirs();
        _base1.deleteOnExit();
        _server1 = new WebDavServer( 0, _base1, _context1, getContainer(), 9, null, _base1.getCanonicalPath() );
        _server1.start();
        _port1 = _server1.getPort();
        
        Server server = new Server("rr1", new URL("http://localhost:"+_port1+_context1), false, false, user );
        _rr1 = new RemoteRepositoryM2( server, dp );
        
        temp = File.createTempFile( prefix, suffix );
        _base2 = new File( "./target/webdav2" + temp.getName() );
//        FileUtil.delete( _base2 );
        assertFalse( _base2.exists() );
        _base2.mkdirs();
        _base2.deleteOnExit();
        _server2 = new WebDavServer( 0, _base2, _context2, getContainer(), 9, null, _base2.getCanonicalPath() );
        _server2.start();
        _port2 = _server2.getPort();
        
        server = new Server("rr2", new URL("http://localhost:"+_port2+_context2), false, false, user );
        _rr2 = new RemoteRepositoryM2( server, dp );
        
        _rrs = new ArrayList<Repository>(2);
        _rrs.add( _rr1 );
        _rrs.add( _rr2 );
        
        temp = File.createTempFile( prefix, suffix );
        _lbase1 = new File( _local1 + temp.getName() );
//        FileUtil.delete( _lbase1 );
        assertFalse( _lbase1.exists() );
        _lbase1.mkdirs() ;
        _lbase1.deleteOnExit();
        _lr1 = new LocalRepositoryM2( "lr1", _lbase1, dp );
        
        temp = File.createTempFile( prefix, suffix );
        _lbase2 = new File( _local2 + temp.getName() );
//        FileUtil.delete( _lbase2 );
        assertFalse( _lbase2.exists() );
        _lbase2.mkdirs();
        _lbase2.deleteOnExit();
        _lr2 = new LocalRepositoryM2( "lr2", _lbase2, dp );
        
        _lrs = new ArrayList<Repository>(2);
        _lrs.add( _lr1 );
        _lrs.add( _lr2 );
        
        _repos = new ArrayList<Repository>();
        _repos.addAll( _rrs );
        _repos.addAll( _lrs );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
        
        if( _server1 != null )
            try
            {
                _server1.stop();
                _server1.destroy();
            }
            catch( Exception e ) {}
            finally { _server1 = null; }
            
        if( _server2 != null )
            try
            {
                _server2.stop();
                _server2.destroy();
            }
            catch( Exception e ) {}
            finally { _server2 = null; }
            
       File target = new File( "target" );
       File [] files = target.listFiles(
                           new FilenameFilter()
                           {

                            public boolean accept( File dir, String name )
                            {
                                if( name.startsWith( "webdav" ))
                                    return true;
                                return false;
                            }
                               
                           }
                                       );
       
       for( File f : files )
       {
           FileUtil.delete( f );
           System.out.println("dropping "+f.getAbsolutePath() );
       }
    }
    
    public void writeArtifact( String name, File af, File ap, Repository repo, File expectedFile )
    throws Exception
    {
        DefaultArtifact da = new DefaultArtifact( new ArtifactMetadata(name) );
        
        da.setPomBlob( FileUtil.readRawData( ap ) );
        da.setFile( af );
        List<Artifact> al = new ArrayList<Artifact>();
        al.add( da );
        
        repo.getWriter().writeArtifacts( al );
        
        int count = 10;
        
        if( expectedFile != null )
        {
            while( ! expectedFile.exists() && count > 0 )
            {
//                if( isWindows )
                    Thread.sleep( 1000L );
                    count--;
            }
        }
    }
    
    public List<Artifact> readArtifact( String name , List<Repository> repos )
    throws Exception
    {
        ArtifactMetadata bmd = new ArtifactMetadata(name);
        
        List<ArtifactMetadata> al = new ArrayList<ArtifactMetadata>();
        al.add( bmd );
        
        VirtualRepositoryReader vr = new VirtualRepositoryReader( repos );
        
        ArtifactResults  res = vr.readArtifacts( al );
        
        assertNotNull( res );
        
        if( res.hasExceptions() )
            System.out.println( res.getExceptions() );
        
        assertTrue( res.hasResults(bmd) );
        
        return res.getResults( bmd );
    }
    
    public List<ArtifactMetadata> readVersions( String name , List<Repository> repos )
    throws Exception
    {
        ArtifactMetadata bmd = new ArtifactMetadata(name);
        
        List<ArtifactMetadata> al = new ArrayList<ArtifactMetadata>();
        al.add( bmd );
        
        VirtualRepositoryReader vr = new VirtualRepositoryReader( repos );
        
        MetadataResults  res = vr.readVersions( al );
        
        assertNotNull( res );
        
        if( res.hasExceptions() )
            System.out.println( res.getExceptions() );
        
        assertTrue( res.hasResults(bmd) );
        
        return res.getResult( bmd );
    }
    
    public void testWriteReadArtifact()
    throws Exception
    {
        String name = "org.apache.maven:maven-core:2.0.9";
        
        File af = new File( _resourceBase, "maven-core-2.0.9.jar" );
        File ap = new File( _resourceBase, "maven-core-2.0.9.pom" );
        
        File aJar1 = new File( _base1, "org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.jar");
        File aJar2 = new File( _base2, "org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.jar");
        
        assertFalse( aJar1.exists() );
        assertFalse( aJar2.exists() );
        
        writeArtifact( name, af, ap, _rr2, aJar2 );
        
        assertFalse( aJar1.exists() );
        assertTrue( aJar2.exists() );
        
        List<Artifact> al = readArtifact( name, _rrs );
        
        System.out.println(al);
        
        File localRepo1Jar = new File( _lbase1, "org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.jar" );
        File localRepo2Jar = new File( _lbase2, "org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.jar" );
        
        assertFalse( localRepo1Jar.exists() );
        assertFalse( localRepo2Jar.exists() );
        
        al = readArtifact( name, _repos );
        
        assertTrue( localRepo1Jar.exists() );
        assertFalse( localRepo2Jar.exists() );
    }
    
    public void testWriteReadArtifactPom()
    throws Exception
    {
        String name = "org.apache.maven:maven-core:2.0.9::pom";
        
        File af = new File( _resourceBase, "maven-core-2.0.9.pom" );
        File ap = new File( _resourceBase, "maven-core-2.0.9.pom" );
        
        File aJar1 = new File( _base1, "org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.pom");
        File aJar2 = new File( _base2, "org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.pom");
        
        assertFalse( aJar1.exists() );
        assertFalse( aJar2.exists() );
        
        writeArtifact( name, af, ap, _rr2, aJar2 );

        assertFalse( aJar1.exists() );
        assertTrue( aJar2.exists() );
        
        List<Artifact> al = readArtifact( name, _rrs );
        
        System.out.println(al);
        
        File localRepo1Jar = new File( _lbase1, "org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.pom" );
        File localRepo2Jar = new File( _lbase2, "org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.pom" );
        
        assertFalse( localRepo1Jar.exists() );
        assertFalse( localRepo2Jar.exists() );
        
        al = readArtifact( name, _repos );
        
        assertTrue( localRepo1Jar.exists() );
        assertFalse( localRepo2Jar.exists() );
    }
    
    public void testWriteReadTimeStamp()
    throws Exception
    {
        String name = "org.apache.maven:maven-core:2.0.9-20090204.232323-23";
        
        File af = new File( _resourceBase, "maven-core-2.0.9.jar" );
        File ap = new File( _resourceBase, "maven-core-2.0.9.pom" );
        
        File aJar1 = new File( _base1, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20090204.232323-23.jar");
        File aJar2 = new File( _base2, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20090204.232323-23.jar");
        
        assertFalse( aJar1.exists() );
        assertFalse( aJar2.exists() );
        
        writeArtifact( name, af, ap, _rr2, aJar2 );
        
        assertFalse( aJar1.exists() );
        assertTrue( aJar2.exists() );
        
        List<Artifact> al = readArtifact( name, _rrs );
        
        System.out.println(al);
        
        File localRepo1Jar = new File( _lbase1, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20090204.232323-23.jar" );
        File localRepo2Jar = new File( _lbase2, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20090204.232323-23.jar" );
        
        assertFalse( localRepo1Jar.exists() );
        assertFalse( localRepo2Jar.exists() );
        
        al = readArtifact( name, _repos );
        
        assertTrue( localRepo1Jar.exists() );
        assertFalse( localRepo2Jar.exists() );
    }
    
    public void testWriteReadLocalTimeStamp()
    throws Exception
    {
        String name = "org.apache.maven:maven-core:2.0.9-20090204.232323-23";
//        ArtifactBasicMetadata bmd = new ArtifactBasicMetadata( name );
        
        File af = new File( _resourceBase, "maven-core-2.0.9.jar" );
        File ap = new File( _resourceBase, "maven-core-2.0.9.pom" );
        
        File aJar1 = new File( _lbase1, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20090204.232323-23.jar");
        File aJar2 = new File( _lbase2, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20090204.232323-23.jar");
        
        assertFalse( aJar1.exists() );
        assertFalse( aJar2.exists() );
        
        writeArtifact( name, af, ap, _lr2, aJar2 );
        
        assertFalse( aJar1.exists() );
        assertTrue( aJar2.exists() );
        
        List<Artifact> al = readArtifact( name, _repos );
        
        System.out.println(al);
        
        File localRepo1Jar = new File( _lbase1, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20090204.232323-23.jar" );
        File localRepo2Jar = new File( _lbase2, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20090204.232323-23.jar" );
        
        assertTrue( localRepo1Jar.exists() );
        assertTrue( localRepo2Jar.exists() );
    }
    
    public void testWriteReadSnapshot()
    throws Exception
    {
        String name = "org.apache.maven:maven-core:2.0.9-SNAPSHOT";
        
        File af = new File( _resourceBase, "maven-core-2.0.9.jar" );
        File ap = new File( _resourceBase, "maven-core-2.0.9.pom" );
        
        File aJar1 = new File( _base1, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-SNAPSHOT.jar");
        File aJar2 = new File( _base2, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-SNAPSHOT.jar");
        
        assertFalse( aJar1.exists() );
        assertFalse( aJar2.exists() );
        
        writeArtifact( name, af, ap, _rr2, aJar2 );
        
        assertFalse( aJar1.exists() );
        assertTrue( aJar2.exists() );
        
        List<Artifact> al = readArtifact( name, _rrs );
        
        System.out.println(al);
        
        File localRepo1Jar = new File( _lbase1, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-SNAPSHOT.jar" );
        File localRepo2Jar = new File( _lbase2, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-SNAPSHOT.jar" );
        
        assertFalse( localRepo1Jar.exists() );
        assertFalse( localRepo2Jar.exists() );
        
        al = readArtifact( name, _repos );
        
        assertTrue( localRepo1Jar.exists() );
        assertFalse( localRepo2Jar.exists() );
    }
    
    public void testWriteReadSnapshotLocal()
    throws Exception
    {
        String name = "org.apache.maven:maven-core:2.0.9-SNAPSHOT";
        
        File af = new File( _resourceBase, "maven-core-2.0.9.jar" );
        File ap = new File( _resourceBase, "maven-core-2.0.9.pom" );
        
        File aJar1 = new File( _lbase1, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-SNAPSHOT.jar");
        File aJar2 = new File( _lbase2, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-SNAPSHOT.jar");
        
        assertFalse( aJar1.exists() );
        assertFalse( aJar2.exists() );
        
        writeArtifact( name, af, ap, _lr2, aJar2 );
        
        assertFalse( aJar1.exists() );
        assertTrue( aJar2.exists() );
        
        List<Artifact> al = readArtifact( name, _lrs );

        assertTrue( aJar1.exists() );
        assertTrue( aJar2.exists() );
    }
    
    public void testWriteTimestampReadSnapshotSingleRepo()
    throws Exception
    {
        String nameTS1 = "org.apache.maven:maven-core:2.0.9-20090204.232323-23";
        String nameTS2 = "org.apache.maven:maven-core:2.0.9-20090204.232324-24";
        String nameSN = "org.apache.maven:maven-core:2.0.9-SNAPSHOT";
        
        File af = new File( _resourceBase, "maven-core-2.0.9.jar" );
        File ap = new File( _resourceBase, "maven-core-2.0.9.pom" );
        
        File aJar = new File( _base2, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20090204.232323-23.jar");
        
        writeArtifact( nameTS1, af, ap, _rr2, aJar );
        
        aJar = new File( _base2, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20090204.232324-24.jar");
        writeArtifact( nameTS2, af, ap, _rr2, aJar );
        
        List<Artifact> al = readArtifact( nameSN, _rrs );
        
        System.out.println(al);
        
        assertNotNull( al );
        
        assertEquals( 1, al.size() );
        
        Artifact aSN = al.get( 0 );
        
        assertNotNull( aSN.getFile() );
        
        assertTrue( aSN.getFile().exists() );
        
        assertEquals( "2.0.9-20090204.232324-24", aSN.getVersion() );
    }
    
    public void testWriteTimestampReadSnapshotSingleRepoSN()
    throws Exception
    {
        String nameTS1 = "org.apache.maven:maven-core:2.0.9-20090204.232323-23";
        String nameTS2 = "org.apache.maven:maven-core:2.0.9-20090204.232324-24";
        String nameSN = "org.apache.maven:maven-core:2.0.9-SNAPSHOT";
        
        File af = new File( _resourceBase, "maven-core-2.0.9.jar" );
        File ap = new File( _resourceBase, "maven-core-2.0.9.pom" );
        
        File aJar = new File( _base2, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20090204.232323-23.jar");
        writeArtifact( nameTS1, af, ap, _rr2, aJar );
        writeArtifact( nameTS2, af, ap, _rr2, null );
        writeArtifact( nameSN, af, ap, _rr2, null );
        
        List<ArtifactMetadata> vl = readVersions( nameSN, _rrs );
        
        System.out.println(vl);
        
        assertNotNull( vl );
        
        assertEquals( 1, vl.size() );
        
        List<Artifact> al = readArtifact( nameSN, _rrs );
        
        System.out.println(al);
        
        assertNotNull( al );
        
        assertEquals( 1, al.size() );
        
        Artifact aSN = al.get( 0 );
        
        assertNotNull( aSN.getFile() );
        
        assertTrue( aSN.getFile().exists() );
        
        assertEquals( "2.0.9-SNAPSHOT", aSN.getVersion() );
    }
    
    public void testWriteTimestampReadSnapshot2Repos()
    throws Exception
    {
        String nameTS1 = "org.apache.maven:maven-core:2.0.9-20090204.232323-23";
        String nameTS2 = "org.apache.maven:maven-core:2.0.9-20090204.232324-24";
        String nameSN = "org.apache.maven:maven-core:2.0.9-SNAPSHOT";
        
        File af = new File( _resourceBase, "maven-core-2.0.9.jar" );
        File ap = new File( _resourceBase, "maven-core-2.0.9.pom" );
        
        File aJar = new File( _base2, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20090204.232323-23.jar");
        writeArtifact( nameTS1, af, ap, _rr2, aJar );

        aJar = new File( _base1, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20090204.232324-24.jar");
        writeArtifact( nameTS2, af, ap, _rr1, aJar );
        
        List<ArtifactMetadata> vl = readVersions( nameSN, _rrs );
        
        System.out.println(vl);
        
        assertNotNull( vl );
        
        assertEquals( 1, vl.size() );
        
        List<Artifact> al = readArtifact( nameSN, _rrs );
        
        System.out.println(al);
        
        assertNotNull( al );
        
        assertEquals( 1, al.size() );
        
        Artifact aSN = al.get( 0 );
        
        assertNotNull( aSN.getFile() );
        
        assertTrue( aSN.getFile().exists() );
        
        assertEquals( "2.0.9-20090204.232324-24", aSN.getVersion() );
    }
    
    public void testWriteTimestampReadSnapshot2ReposReversed()
    throws Exception
    {
        String nameTS1 = "org.apache.maven:maven-core:2.0.9-20090204.232323-23";
        String nameTS2 = "org.apache.maven:maven-core:2.0.9-20090204.232324-24";
        String nameSN = "org.apache.maven:maven-core:2.0.9-SNAPSHOT";
        
        File af = new File( _resourceBase, "maven-core-2.0.9.jar" );
        File ap = new File( _resourceBase, "maven-core-2.0.9.pom" );
        
        File aJar = new File( _base1, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20090204.232323-23.jar");
        writeArtifact( nameTS1, af, ap, _rr1, aJar );

        aJar = new File( _base2, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20090204.232324-24.jar");
        writeArtifact( nameTS2, af, ap, _rr2, aJar );
        
        List<ArtifactMetadata> vl = readVersions( nameSN, _rrs );
        
        System.out.println(vl);
        
        assertNotNull( vl );
        
        assertEquals( 1, vl.size() );
        
        List<Artifact> al = readArtifact( nameSN, _rrs );
        
        System.out.println(al);
        
        assertNotNull( al );
        
        assertEquals( 1, al.size() );
        
        Artifact aSN = al.get( 0 );
        
        assertNotNull( aSN.getFile() );
        
        assertTrue( aSN.getFile().exists() );
        
        assertEquals( "2.0.9-20090204.232324-24", aSN.getVersion() );
    }
    
    public void testLatest()
    throws Exception
    {
        String nameTS1 = "org.apache.maven:maven-core:2.0.9-20090204.232323-23";
        String nameTS2 = "org.apache.maven:maven-core:2.0.9-20090204.232324-24";
        String nameRL = "org.apache.maven:maven-core:2.0.8";
        String nameLT = "org.apache.maven:maven-core:LATEST";
        
        File af = new File( _resourceBase, "maven-core-2.0.9.jar" );
        File ap = new File( _resourceBase, "maven-core-2.0.9.pom" );
        
        File aJar = new File( _base2, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20090204.232323-23.jar");
        writeArtifact( nameTS1, af, ap, _rr2, aJar );
        
        aJar = new File( _base1, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20090204.232324-24.jar");
        writeArtifact( nameTS2, af, ap, _rr1, aJar );
        
        aJar = new File( _base2, "org/apache/maven/maven-core/2.0.8/maven-core-2.0.8.jar");
        writeArtifact( nameRL,  af, ap, _rr2, aJar );
        
        List<Artifact> al = readArtifact( nameLT, _rrs );
        
        System.out.println(al);
        
        assertNotNull( al );
        
        assertEquals( 1, al.size() );
        
        Artifact aSN = al.get( 0 );
        
        assertNotNull( aSN.getFile() );
        
        assertTrue( aSN.getFile().exists() );
        
        assertEquals( "2.0.9-20090204.232324-24", aSN.getVersion() );
    }
    
    public void testLatestLocal()
    throws Exception
    {
        String nameTS1 = "org.apache.maven:maven-core:2.0.9-20090204.232323-23";
        String nameTS2 = "org.apache.maven:maven-core:2.0.9-20090204.232324-24";
        String nameRL = "org.apache.maven:maven-core:2.0.8";
        String nameLT = "org.apache.maven:maven-core:LATEST";
        
        File af = new File( _resourceBase, "maven-core-2.0.9.jar" );
        File ap = new File( _resourceBase, "maven-core-2.0.9.pom" );
        
        File aJar = new File( _lbase2, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20090204.232323-23.jar");
        writeArtifact( nameTS1, af, ap, _lr2, aJar );
        
        aJar = new File( _lbase1, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20090204.232324-24.jar");
        writeArtifact( nameTS2, af, ap, _lr1, aJar );
        
        aJar = new File( _lbase2, "org/apache/maven/maven-core/2.0.8/maven-core-2.0.8.jar");
        writeArtifact( nameRL, af, ap, _lr2, aJar );
        
        List<Artifact> al = readArtifact( nameLT, _lrs );
        
        System.out.println(al);
        
        assertNotNull( al );
        
        assertEquals( 1, al.size() );
        
        Artifact aSN = al.get( 0 );
        
        assertNotNull( aSN.getFile() );
        
        assertTrue( aSN.getFile().exists() );
        
        assertEquals( "2.0.9-20090204.232324-24", aSN.getVersion() );
    }
    
    public void testRelease()
    throws Exception
    {
        String nameTS1 = "org.apache.maven:maven-core:2.0.9-20090204.232323-23";
        String nameTS2 = "org.apache.maven:maven-core:2.0.9-20090204.232324-24";
        String nameRL = "org.apache.maven:maven-core:2.0.8";
        String name = "org.apache.maven:maven-core:RELEASE";
        
        File af = new File( _resourceBase, "maven-core-2.0.9.jar" );
        File ap = new File( _resourceBase, "maven-core-2.0.9.pom" );
        
        File aJar = new File( _base2, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20090204.232323-23.jar");
        writeArtifact( nameTS1, af, ap, _rr2, aJar );
        
        aJar = new File( _base1, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20090204.232324-24.jar");
        writeArtifact( nameTS2, af, ap, _rr1, aJar );
        
        aJar = new File( _base2, "org/apache/maven/maven-core/2.0.8/maven-core-2.0.8.jar");
        writeArtifact( nameRL, af, ap, _rr2, aJar );
        
        List<Artifact> al = readArtifact( name, _rrs );
        
        System.out.println(al);
        
        assertNotNull( al );
        
        assertEquals( 1, al.size() );
        
        Artifact aSN = al.get( 0 );
        
        assertNotNull( aSN.getFile() );
        
        assertTrue( aSN.getFile().exists() );
        
        assertEquals( "2.0.8", aSN.getVersion() );
    }
    
    public void testReleaseLocal()
    throws Exception
    {
        String nameTS1 = "org.apache.maven:maven-core:2.0.9-20090204.232323-23";
        String nameTS2 = "org.apache.maven:maven-core:2.0.9-20090204.232324-24";
        String nameRL = "org.apache.maven:maven-core:2.0.8";
        String name = "org.apache.maven:maven-core:RELEASE";
        
        File af = new File( _resourceBase, "maven-core-2.0.9.jar" );
        File ap = new File( _resourceBase, "maven-core-2.0.9.pom" );
        
        File aJar = new File( _lbase2, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20090204.232323-23.jar");
        writeArtifact( nameTS1, af, ap, _lr2, aJar );
        
        aJar = new File( _lbase1, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20090204.232324-24.jar");
        writeArtifact( nameTS2, af, ap, _lr1, aJar );
        
        aJar = new File( _lbase2, "org/apache/maven/maven-core/2.0.8/maven-core-2.0.8.jar");
        writeArtifact( nameRL, af, ap, _lr2, aJar );

        List<Artifact> al = readArtifact( name, _lrs );
        
        System.out.println(al);
        
        assertNotNull( al );
        
        assertEquals( 1, al.size() );
        
        Artifact aSN = al.get( 0 );
        
        assertNotNull( aSN.getFile() );
        
        assertTrue( aSN.getFile().exists() );
        
        assertEquals( "2.0.8", aSN.getVersion() );
    }
    
    public void testReadReleasePolicy()
    throws Exception
    {
        String name = "org.apache.maven:maven-core:2.0.9";
        
        File af = new File( _resourceBase, "maven-core-2.0.9.jar" );
        File ap = new File( _resourceBase, "maven-core-2.0.9.pom" );
        
        File aJar1 = new File( _base1, "org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.jar");
        File aJar2 = new File( _base2, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20090204.232324-24.jar");
        
        assertFalse( aJar1.exists() );
        assertFalse( aJar2.exists() );
        
        writeArtifact( "org.apache.maven:maven-core:2.0.9",                    af, ap, _rr1, aJar1 );
        writeArtifact( "org.apache.maven:maven-core:2.0.9-20090204.232324-24", af, ap, _rr2, aJar2 );
        
        _rr1.setRepositoryQualityRange( QualityRange.RELEASES_ONLY );
        _rr2.setRepositoryQualityRange( QualityRange.SNAPSHOTS_ONLY );
        
        List<ArtifactMetadata> al = readVersions( name, _rrs );
        
        assertNotNull( al );

        assertEquals( 1, al.size() );

        assertEquals( "2.0.9", al.get( 0 ).getVersion() );
    }
    
    public void testReadReleasePolicySwapped()
    throws Exception
    {
        String name = "org.apache.maven:maven-core:2.0.9";
        
        File af = new File( _resourceBase, "maven-core-2.0.9.jar" );
        File ap = new File( _resourceBase, "maven-core-2.0.9.pom" );
        
        File aJar1 = new File( _base1, "org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.jar");
        File aJar2 = new File( _base2, "org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20090204.232324-24.jar");
        
        assertFalse( aJar1.exists() );
        assertFalse( aJar2.exists() );
        
        writeArtifact( "org.apache.maven:maven-core:2.0.9",                    af, ap, _rr1, aJar1 );
        writeArtifact( "org.apache.maven:maven-core:2.0.9-20090204.232324-24", af, ap, _rr2, aJar2 );
        
        _rr2.setRepositoryQualityRange( QualityRange.RELEASES_ONLY );
        _rr1.setRepositoryQualityRange( QualityRange.SNAPSHOTS_ONLY );
        
        ArtifactMetadata bmd = new ArtifactMetadata(name);
        
        List<ArtifactMetadata> al = new ArrayList<ArtifactMetadata>();
        al.add( bmd );
        
        VirtualRepositoryReader vr = new VirtualRepositoryReader( _rrs );
        
        MetadataResults  res = vr.readVersions( al );
        
        assertTrue( res  == null || !res.hasResults() );
    }
}
