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
package org.apache.maven.mercury.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.maven.mercury.crypto.api.StreamObserverException;
import org.apache.maven.mercury.crypto.api.StreamObserverFactory;
import org.apache.maven.mercury.crypto.api.StreamVerifier;
import org.apache.maven.mercury.crypto.api.StreamVerifierAttributes;
import org.apache.maven.mercury.crypto.api.StreamVerifierException;
import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.crypto.pgp.PgpStreamVerifierFactory;
import org.apache.maven.mercury.crypto.sha.SHA1VerifierFactory;
import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.MercuryLoggerManager;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 * File related utilities: copy, write, sign, verify, etc.
 *
 * @author Oleg Gusakov
 * @version $Id: FileUtil.java 744626 2009-02-15 08:06:47Z ogusakov $
 *
 */
public class FileUtil
{
  public static final String SEP = "/";
  public static final char   SEP_CHAR = SEP.charAt( 0 );

  public static final String DASH = "-";
  public static final char   DASH_CHAR = DASH.charAt( 0 );

  public static final String LOCK_FILE = ".lock";
  public static final String DEFAULT_CHARSET = "utf-8";

  public static final int    K = 1024;
  public static final int    DEFAULT_BUFFER_SIZE = 10 * K;
  
  public static final String PROTOCOL_DELIM = "://";
  public static final int    PROTOCOL_DELIM_LEN = PROTOCOL_DELIM.length();
  public static final String [] URL_PROTOCOLS = new String [] {"http","https","dav","file","davs","webdav","webdavs","dav+http","dav+https"};
  public static final String [] LOCAL_PROTOCOLS = new String [] {"file"};
  //---------------------------------------------------------------------------------------------------------------
  private static final IMercuryLogger LOG = MercuryLoggerManager.getLogger( FileUtil.class );
  private static final Language LANG = new DefaultLanguage( FileUtil.class );

  private static final OverlappingFileLockException FILE_LOCKED = new OverlappingFileLockException();
  //---------------------------------------------------------------------------------------------------------------
  public static void delete( File f )
  {
    if( ! f.exists()  )
      return;

    if( f.isDirectory() )
    {
      File [] kids = f.listFiles();
      for( File kid : kids )
        delete( kid );
    }

    f.delete();
  }
  //---------------------------------------------------------------------------------------------------------------
  public static void copy( File fromFile, File toFile, boolean clean )
  throws IOException
  {
    if( toFile.exists() && clean )
      delete( toFile );

    if( fromFile.isFile() )
    {
      copyFile( fromFile, toFile );
      return;
    }

    File [] kids = fromFile.listFiles();
    if( kids != null )
    {
      for( File kid : kids )
      {
        if( kid.isDirectory() )
        {
          File newDir = new File( toFile, kid.getName() );
          newDir.mkdirs();

          copy( kid, newDir, false );
        }
        else
          copyFile( kid, toFile );
      }
    }

  }
  //---------------------------------------------------------------------------------------------------------------
  private static void copyFile( File f, File toFile )
  throws IOException
  {
    File fOut = null;
    
    if( toFile.isDirectory() )
      fOut = new File(toFile, f.getName() );
    else
      fOut = toFile;
    
    FileInputStream fis = new FileInputStream(f);
    
    writeRawData( fis, fOut );
  }
  //---------------------------------------------------------------------------------------------------------------
  /**
   * Read the content of a file and converts it with UTF-8 encoding to a String.
   */
  public static String readRawDataAsString( File file )
  throws IOException
  {
    return new String( readRawData( file ), DEFAULT_CHARSET );
  }
  //---------------------------------------------------------------------------------------------------------------
  public static byte[] readRawData( File file )
  throws IOException
  {
    if( ! file.exists() )
      return null;

    FileInputStream fis = null;

    try
    {
      fis = new FileInputStream( file );
      int len = (int)file.length();
      if( len == 0 )
      {
        LOG.info( LANG.getMessage( "reading.empty.file", file.getAbsolutePath() ) );
        return null;
      }

      byte [] pom = new byte [ len ];
      while( fis.available() < 1 )
        try { Thread.sleep( 8L ); } catch( InterruptedException e ){}

      fis.read( pom, 0, len );

      return pom;
    }
    catch( IOException e )
    {
      throw  e;
    }
    finally
    {
      if( fis != null ) try { fis.close(); } catch( Exception any ) {}
    }
  }
  //---------------------------------------------------------------------------------------------------------------
  public static byte[] readRawData( File file, Collection<StreamVerifierFactory> vFacs  )
  throws IOException, FileUtilException, StreamVerifierException
  {
    if( file == null || ! file.exists() )
      return null;

    boolean verify = vFacs != null && vFacs.size() > 0;

    String fileName = file.getAbsolutePath();

    HashSet<StreamVerifier> vs = new HashSet<StreamVerifier>( verify ? vFacs.size() : 1 );

    for( StreamVerifierFactory svf : vFacs )
    {
      StreamVerifier sv = svf.newInstance();
      String ext = sv.getAttributes().getExtension();
      String sigFileName = fileName + ( ext.startsWith( "." ) ? "" : "." ) + ext;
      File sigFile = new File( sigFileName );
      if( sigFile.exists() )
      {
        try
        {
          sv.initSignature( FileUtil.readRawDataAsString( sigFile ) );
        }
        catch( IOException e )
        {
          throw new FileUtilException( LANG.getMessage( "cannot.read.signature.file", sigFileName, e.getMessage() ) );
        }
        vs.add( sv );
      }
      else if( ! sv.getAttributes().isLenient() )
      {
        throw new FileUtilException( LANG.getMessage( "no.signature.file", ext, sigFileName ) );
      }
      // otherwise ignore absence of signature file, if verifier is lenient
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    FileInputStream fin = null;
    try
    {
      fin = new FileInputStream( file );
      byte [] buf = new byte[ DEFAULT_BUFFER_SIZE ];
      int n = -1;
      while( (n = fin.read( buf )) != -1 )
      {
        if( verify )
        {
          for( StreamVerifier sv : vs )
            try
            {
              sv.bytesReady( buf, 0, n );
            }
            catch( StreamObserverException e )
            {
              if( ! sv.getAttributes().isLenient() )
                throw new FileUtilException(e);
            }
        }

        baos.write( buf, 0, n );
      }

      if( verify )
      {
        for( StreamVerifier sv : vs )
        {
          if( sv.verifySignature() )
          {
            if( sv.getAttributes().isSufficient() )
              break;
          }
          else
          {
            if( !sv.getAttributes().isLenient() )
              throw new StreamVerifierException( LANG.getMessage( "signature.failed", sv.getAttributes().getExtension(), fileName ) );
          }
        }
      }

      return baos.toByteArray();
    }
    catch( IOException e )
    {
      throw new FileUtilException( e );
    }
    finally
    {
      if( fin != null ) try { fin.close(); } catch( Exception any ) {}
    }
  }
  //---------------------------------------------------------------------------------------------------------------
  public static byte[] readRawData( InputStream in )
  throws IOException
  {
    byte [] bytes = new byte [ DEFAULT_BUFFER_SIZE ];
    int n = -1;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    while( ( n = in.read( bytes ) ) != -1 )
      baos.write( bytes, 0, n );

    return baos.toByteArray();
  }
  //---------------------------------------------------------------------------------------------------------------
  /**
   * Write UTF-8 representation of a String to a File. 
   */
  public static void writeRawData( File file, String sBytes )
  throws IOException
  {
    writeRawData( file, sBytes.getBytes( DEFAULT_CHARSET ) );
  }
  //---------------------------------------------------------------------------------------------------------------
  public static void writeRawData( InputStream in, File f )
  throws IOException
  {
    OutputStream out = new FileOutputStream( f );
    try
    {
      byte [] buf = new byte[ DEFAULT_BUFFER_SIZE ];
      int n;

      while( (n = in.read( buf ) ) > 0 )
          out.write( buf, 0, n );
      
      out.flush();
    }
    finally
    {
      out.close();
    }

    in.close();
  }
  //---------------------------------------------------------------------------------------------------------------
  public static File writeTempData( byte [] bytes )
  throws IOException
  {
      File f = File.createTempFile( "temp-", "-mercury-util" );
      
      writeRawData( f, bytes );
      
      return f;
  }
  //---------------------------------------------------------------------------------------------------------------
  public static void writeRawData( File file, byte [] bytes )
  throws IOException
  {
    if( file.exists() )
      file.delete();

    File parentDir = file.getParentFile();

    if( !parentDir.exists() )
      parentDir.mkdirs();

    FileOutputStream fos = null;

    try
    {
      fos = new FileOutputStream( file );
      fos.write( bytes );
      fos.flush();
    }
    catch( IOException e )
    {
      throw  e;
    }
    finally
    {
      if( fos != null ) try { fos.close(); } catch( Exception any ) {}
    }
  }
  //---------------------------------------------------------------------------------------------------------------
  public static void writeAndSign( String fName, byte [] bytes, Set<StreamVerifierFactory> vFacs )
  throws IOException, StreamObserverException
  {
    ByteArrayInputStream bais = new ByteArrayInputStream( bytes );
    writeAndSign( fName, bais, vFacs );
  }
  //---------------------------------------------------------------------------------------------------------------
  public static void writeAndSign( String fName, InputStream in, Set<StreamVerifierFactory> vFacs )
  throws IOException, StreamObserverException
  {
    byte [] buf = new byte[ DEFAULT_BUFFER_SIZE ];
    int n = -1;
    HashSet<StreamVerifier> vSet = new HashSet<StreamVerifier>( vFacs.size() );

    for( StreamVerifierFactory vf : vFacs )
      vSet.add( vf.newInstance() );

    FileOutputStream fout = null;

    try
    {
      File f = new File( fName );

      f.getParentFile().mkdirs();

      fout = new FileOutputStream( f );

      while( ( n = in.read( buf ) ) != -1 )
      {
        for( StreamVerifier sv : vSet )
          sv.bytesReady( buf, 0, n );

        fout.write( buf, 0, n );
      }

      fout.flush();
      fout.close();
      fout = null;

      for( StreamVerifier sv : vSet )
      {
        String sig = sv.getSignature();
        FileUtil.writeRawData( new File( fName + sv.getAttributes().getExtension() ), sig );
      }

    }
    finally
    {
      if( fout != null ) try { fout.close(); } catch( Exception any ) {}
    }

  }

  public List<String> dirToList( File dir, boolean includeDirs, boolean includeFiles )
  {
    if( ! dir.exists() )
      return null;

    File [] files = dir.listFiles();

    List<String> res = new ArrayList<String>( files.length );

    for( File f : files )
      if( f.isDirectory() )
      {
        if( includeDirs )
          res.add( f.getName() );
      }
      else
        if( includeFiles )
          res.add( f.getName() );

    return res;
  }

  /**
   * 
   * @param f
   * @param vFacs
   * @param recurse
   * @param force
   * @throws IOException
   * @throws StreamObserverException
   */
  public static void sign( File f, Set<StreamVerifierFactory> vFacs, boolean recurse, boolean force )
  throws IOException, StreamObserverException
  {
    if( vFacs == null || vFacs.size() < 1 )
      return;

    if( f.isDirectory() )
    {
      if( ! recurse )
        return;

      File [] kids = f.listFiles();
      for( File kid : kids )
        sign( kid, vFacs, recurse, force );
      return;
    }

    String fName = f.getAbsolutePath();

    HashSet<StreamVerifier> vs = new HashSet<StreamVerifier>( vFacs.size() );
    for( StreamVerifierFactory vf : vFacs )
    {
      StreamVerifier sv = vf.newInstance();
      String ext = sv.getAttributes().getExtension();

      // don't sign signature files
      if( fName.endsWith( ext ) )
        return;

      File sf = new File( fName + ext );
      if( sf.exists() )
      {
        if( force )
          sf.delete();
        else
          continue;
      }
      vs.add( sv );
    }

    byte [] buf = new byte[ DEFAULT_BUFFER_SIZE ];
    FileInputStream fis = null;
    try
    {
      fis = new FileInputStream( f );
      int n = -1;

      while( ( n = fis.read( buf ) ) != -1 )
      {
        for( StreamVerifier sv : vs )
        {
          sv.bytesReady( buf, 0, n );
        }
      }

      for( StreamVerifier sv : vs )
      {
        String sig = sv.getSignature();
        String ext = sv.getAttributes().getExtension();
        File sf = new File( fName + ext );
        writeRawData( sf, sig );
      }
    }
    finally
    {
      if( fis != null ) try { fis.close(); } catch( Exception any ) {}
    }

  }
  //---------------------------------------------------------------------------------------------------------------
  public static void verify( File f, Set<StreamVerifierFactory> vFacs, boolean recurse, boolean force )
  throws IOException, StreamObserverException
  {
    if( vFacs == null || vFacs.size() < 1 )
      return;

    if( f.isDirectory() )
    {
      if( !recurse )
        return;

      File [] kids = f.listFiles();
      for( File kid : kids )
        verify( kid, vFacs, recurse, force );
      return;
    }

    String fName = f.getAbsolutePath();
    HashSet<StreamVerifier> vs = new HashSet<StreamVerifier>( vFacs.size() );
    for( StreamVerifierFactory vf : vFacs )
    {
      StreamVerifier sv = vf.newInstance();
      String ext = sv.getAttributes().getExtension();

      // don't verify signature files
      if( fName.endsWith( ext ) )
        return;

      File sf = new File( fName + ext );
      if( !sf.exists() )
      {
        if( force )
          throw new StreamVerifierException( LANG.getMessage( "no.mandatory.signature", f.getAbsolutePath(), sf.getAbsolutePath() ) );
        else
          continue;
      }
      else
      {
        String sig = readRawDataAsString( sf );
        sv.initSignature( sig );
      }
      vs.add( sv );
    }

    byte [] buf = new byte[ DEFAULT_BUFFER_SIZE ];
    FileInputStream fis = null;
    try
    {
      fis = new FileInputStream( f );
      int n = -1;

      while( ( n=fis.read( buf ) ) != -1 )
      {
        for( StreamVerifier sv : vs )
        {
          sv.bytesReady( buf, 0, n );
        }
      }

      List<String> fl = null;
      char comma = ' ';

      for( StreamVerifier sv : vs )
      {
        if( sv.verifySignature() )
          continue;

        if( fl == null )
          fl = new ArrayList<String>( 4 );

        fl.add( sv.getAttributes().getExtension().replace( '.', comma ) );
        comma = ',';
      }

      if( fl != null )
      {
        throw new StreamVerifierException( LANG.getMessage( "file.failed.verification", f.getAbsolutePath(), fl.toString() ) );
      }
    }
    finally
    {
      if( fis != null ) try { fis.close(); } catch( Exception any ) {}
    }

  }
  //---------------------------------------------------------------------------------------------------------------
  @SuppressWarnings("static-access")
  public static void main( String[] args )
  throws IOException, StreamObserverException
  {
    Option sign      = new Option( "sign", LANG.getMessage( "option.sign" ) );
    Option verify    = new Option( "verify", LANG.getMessage( "option.verify" ) );
    OptionGroup  cmd = new OptionGroup();
    cmd.addOption( sign );
    cmd.addOption( verify );

    Option recurce   = new Option( "r", LANG.getMessage( "option.r" ) );
    Option force     = new Option( "force", LANG.getMessage( "option.force" ) );

    OptionGroup  sig = new OptionGroup();
    Option sha1      = new Option( "sha1", LANG.getMessage( "option.sha1" ) );
    Option pgp       = new Option( "pgp", LANG.getMessage( "option.pgp" ) );
    sig.addOption( sha1 );
    sig.addOption( pgp );

    Option keyring   = OptionBuilder.withArgName( "file" )
                                    .hasArg()
                                    .withType( java.io.File.class )
                                    .withDescription( LANG.getMessage( "option.keyring" ) )
                                    .create( "keyring" )
                                    ;
    Option keyid     = OptionBuilder.withArgName( "hexstring" )
                                    .hasArg()
                                    .withDescription( LANG.getMessage( "option.keyid" ) )
                                    .create( "keyid" )
                                    ;
    Option keypass   = OptionBuilder.withArgName( "string" )
                                    .hasArg()
                                    .withDescription( LANG.getMessage( "option.keypass" ) )
                                    .create( "keypass" )
                                    ;

    Options options = new Options();
    options.addOptionGroup( cmd );
    options.addOptionGroup( sig );

    options.addOption( recurce );
    options.addOption( force );

    options.addOption( keyring );
    options.addOption( keyid );
    options.addOption( keypass );

    CommandLine commandLine = null;
    CommandLineParser parser = new GnuParser();

    if( args == null || args.length < 2 )
    {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp( "mercury-util", options );
      return;
    }

    try
    {
        commandLine = parser.parse( options, args );
    }
    catch( ParseException e )
    {
        System.err.println( "Command line parsing eror: " + e.getMessage() );
        return;
    }

    Set<StreamVerifierFactory> vFacs = new HashSet<StreamVerifierFactory>( 4 );

    if( commandLine.hasOption( "pgp" ) )
    {
      if( commandLine.hasOption( "sign" ) && commandLine.hasOption( "keyring" ) && commandLine.hasOption( "keyid" ) )
      {
        BufferedReader r = new BufferedReader( new InputStreamReader( System.in ) );
        String pass = commandLine.hasOption( "keypass" ) ? commandLine.getOptionValue( "keypass" ) : r.readLine();

        vFacs.add( 
            new PgpStreamVerifierFactory(
                    new StreamVerifierAttributes( PgpStreamVerifierFactory.DEFAULT_EXTENSION, false, true )
                    , new FileInputStream( commandLine.getOptionValue( "keyring" ) )
                    , commandLine.getOptionValue( "keyid" )
                    , pass
                                        )
                    );
      }
      else if( commandLine.hasOption( "verify" ) && commandLine.hasOption( "keyring" ) )
      {

        vFacs.add(
            new PgpStreamVerifierFactory(
                    new StreamVerifierAttributes( PgpStreamVerifierFactory.DEFAULT_EXTENSION, false, true )
                    , new FileInputStream( commandLine.getOptionValue( "keyring" ) )
                                        )
                    );
      }
      else
      {
        System.err.println( LANG.getMessage( "bad.pgp.args" ) );
        return;
      }
    }

    if( commandLine.hasOption("sha1") )
    {
      vFacs.add( new SHA1VerifierFactory( true,false ) );
    }

    try
    {
      signAll( commandLine.getArgList(), vFacs, commandLine.hasOption( "r" ), commandLine.hasOption( "force" ), commandLine.hasOption( "sign" ) );
    }
    catch( Exception e )
    {
      System.err.println( "Bummer: " + e.getMessage() );
      return;
    }
    System.out.println( "Done" );

  }
  //---------------------------------------------------------------------------------------------------------------
  private static void signAll( List<String> fileNames, Set<StreamVerifierFactory> vFacs, boolean recurse, boolean force, boolean sign )
  throws IOException, StreamObserverException
  {
    if( vFacs == null || vFacs.size() < 1 )
    {
      System.err.println( "no.verifiers" );
      return;
    }

    File f = null;

    for( String fName : fileNames )
    {
      f = new File( fName );
      if( ! f.exists() )
      {
        System.out.println( LANG.getMessage( "file.not.exists", fName ) );
        continue;
      }
      if( f.isDirectory() && ! recurse )
      {
        System.out.println( LANG.getMessage( "file.is.directory", fName ) );
        continue;
      }
      if( sign )
        sign( f, vFacs, recurse, force );
      else
        verify( f, vFacs, recurse, force );
    }
  }
  //---------------------------------------------------------------------------------------------------------------
  /**
   * try to acquire lock on specified directory for <code>millis<code> milliseconds
   * 
   * @param dir directory to lock
   * @param millis how long to wait for the lock before surrendering
   * @param sleepFor how long to sleep between attempts
   * 
   * @return obtained FileLock or null
   * @throws IOException if there were problems obtaining the lock
   */
  public static FileLockBundle lockDir( String dir, long millis, long sleepFor )
  throws IOException
  {

    File df = new File( dir );

    boolean exists = df.exists();

    for( int i=0; i<10 && !exists; i++ )
    {
      try{ Thread.sleep( 1l ); } catch( InterruptedException e ){}
      df.mkdirs();
      exists = df.exists();
      LOG.info( LANG.getMessage( "had.to.create.directory", dir, exists + "" ) );
    }

    if( !exists )
      throw new IOException( LANG.getMessage( "cannot.create.directory", dir ) );

    if( !df.isDirectory() )
      throw new IOException( LANG.getMessage( "file.is.not.directory", dir, df.exists() + "", df.isDirectory() + "", df.isFile() + "" ) );

    File lockFile = new File( dir,LOCK_FILE );

    long start = System.currentTimeMillis();

    for( long now = start; ( now - start ) < millis; now = System.currentTimeMillis() )
      try
      {
        synchronized( FileUtil.class )
        {
          if( !lockFile.exists() )
          {
            writeRawData( lockFile, "lock" );
            lockFile.deleteOnExit();
            return new FileLockBundle( dir );
          }
        }
        Thread.sleep( sleepFor );

      }
      catch( InterruptedException ie )
      {
      }

      // too long a wait
      return null;
  }
  //---------------------------------------------------------------------------------------------------------------
  /**
   * try to acquire lock on specified directory for <code>millis<code> milliseconds
   * 
   * @param dir directory to lock
   * @param millis how long to wait for the lock before surrendering
   * @param sleepFor how long to sleep between attempts
   * 
   * @return obtained FileLock or null
   * @throws IOException if there were problems obtaining the lock
   */
  public static FileLockBundle lockDirNio( String dir, long millis, long sleepFor )
  throws IOException
  {
    File df = new File( dir );

    boolean exists = df.exists(); 

    for( int i = 0; i < 10 && !exists; i++ )
    {
      try{ Thread.sleep( 1l ); } catch( InterruptedException e ){}
      df.mkdirs();
      exists = df.exists();
      LOG.info( LANG.getMessage( "had.to.create.directory", dir, exists + "" ) );
    }

    if( !exists )
      throw new IOException( LANG.getMessage( "cannot.create.directory", dir ) );

    if( !df.isDirectory() )
      throw new IOException( LANG.getMessage( "file.is.not.directory", dir, df.exists() + "", df.isDirectory() + "", df.isFile() + "" ) );

    File lockFile = new File( dir,LOCK_FILE );
    if( !lockFile.exists() )
      writeRawData( lockFile, "lock" );
    lockFile.deleteOnExit();

    FileChannel ch = new RandomAccessFile( lockFile, "rw" ).getChannel();
    FileLock lock = null;

    long start = System.currentTimeMillis();

    for(;;)
      try
      {
        lock = ch.tryLock( 0L, 4L, false );

        if( lock == null )
          throw FILE_LOCKED;

        return new FileLockBundle( dir, ch, lock );
      }
      catch( OverlappingFileLockException oe )
      {
        try { Thread.sleep( sleepFor ); } catch( InterruptedException e ){}
        if( System.currentTimeMillis() - start > millis )
          return null;
      }

  }
  //---------------------------------------------------------------------------------------------------------------
  public static synchronized void unlockDir( String dir )
  {
    try
    {
      File df = new File( dir );
      if( !df.isDirectory() )
        throw new IOException( LANG.getMessage( "file.is.not.directory", dir ) );

      File lock = new File( dir,LOCK_FILE );
      if( lock.exists() )
        lock.delete();
    }
    catch( IOException e )
    {
      LOG.error( e.getMessage() );
    }
  }
  //---------------------------------------------------------------------------------------------------------------
  public static final Set<StreamVerifierFactory> vSet( StreamVerifierFactory... facs )
  {
    if( facs == null || facs.length < 1 )
      return null;

    HashSet<StreamVerifierFactory> res = new HashSet<StreamVerifierFactory>( facs.length );
    for( StreamVerifierFactory f : facs )
    {
      res.add( f );
    }

    return res;
  }
  //---------------------------------------------------------------------------------------------------------------
  public static final Set<StreamObserverFactory> oSet( StreamObserverFactory... facs )
  {
    if( facs == null || facs.length < 1 )
      return null;

    HashSet<StreamObserverFactory> res = new HashSet<StreamObserverFactory>( facs.length );
    for( StreamObserverFactory f : facs )
    {
      res.add( f );
    }

    return res;
  }
  //---------------------------------------------------------------------------------------------------------------
  public static void renameFile( File dir, String from, String to )
  {
    if( dir == null )
      return;

    File [] files = dir.listFiles();

    if( files == null || files.length < 1 )
      return;

    for( File f : files )
    {
      if( f.isDirectory() )
        renameFile( f, from, to );
      else if( from.equals( f.getName() ) )
      {
        f.renameTo( new File( f.getParent(), to ) );
      }
    }
  }
  //---------------------------------------------------------------------------------------------------------------
  public static int depth( File file )
  {
    if( file == null || !file.exists() )
      throw new IllegalArgumentException( LANG.getMessage( "file.not.exists.error", file == null ? "null" : file.getAbsolutePath() ) );

    if( file.isFile() )
      return 0;

    File [] files = file.listFiles();

    int max = 0;

    for( File f : files )
    {
      if( f.isDirectory() )
      {
        int res = depth( f );
        if( res > max )
          max = res;
      }
    }

    return max + 1;
  }
  //---------------------------------------------------------------------------------------------------------------
  public static InputStream toStream( String resource )
  throws MalformedURLException, IOException
  {
    if( resource == null )
      return null;
    
    int ind = resource.indexOf( PROTOCOL_DELIM );
    
    if( ind > 1 )
    {
        String protocol = resource.substring( 0, ind );
        resource = resource.substring( ind + PROTOCOL_DELIM_LEN );

        for( int i=0; i<URL_PROTOCOLS.length; i++ )
        {
            String p = URL_PROTOCOLS[i];
            
            if( protocol.regionMatches( true, 0, p, 0, p.length() ) )
              return new URL( p+PROTOCOL_DELIM+resource).openStream();
        }
    }

    return new FileInputStream( new File(resource) );
  }
  //---------------------------------------------------------------------------------------------------------------
  public static boolean isLocalResource( String resource )
  {
    if( resource == null )
      return true;
    
    int ind = resource.indexOf( PROTOCOL_DELIM );
    
    if( ind > 1 )
    {
        String protocol = resource.substring( 0, ind );

        for( int i=0; i<LOCAL_PROTOCOLS.length; i++ )
        {
            String p = LOCAL_PROTOCOLS[i];
            
            if( protocol.regionMatches( true, 0, p, 0, p.length() ) )
              return true;
        }
    }
    else
        return true;

    return false;
  }
  // -----------------------------------------------------
    public static final void unZip( InputStream zipInputStream, File destDir )
        throws FileNotFoundException, IOException
    {
        ZipInputStream zis = new ZipInputStream( zipInputStream );

        BufferedOutputStream dest = null;
        ZipEntry entry;

        while ( ( entry = zis.getNextEntry() ) != null )
        {
            String entryName = entry.getName();
            
            File fo = new File( destDir, entryName );
            
            if( LOG.isDebugEnabled() )
                LOG.debug( "Extracting: " + fo.getCanonicalPath() );

            int count;
            byte data[] = new byte[ DEFAULT_BUFFER_SIZE ];

            // write the files to the disk
            if ( entry.isDirectory() )
            {
                fo.mkdirs();
                continue;
            }
            
            fo.getParentFile().mkdirs();

            FileOutputStream fos = new FileOutputStream( fo );
            
            dest = new BufferedOutputStream( fos, DEFAULT_BUFFER_SIZE );
            
            while ( ( count = zis.read( data, 0, DEFAULT_BUFFER_SIZE ) ) != -1 )
            {
                dest.write( data, 0, count );
            }
            
            dest.flush();
            
            dest.close();
        }
        
        zis.close();

        if( LOG.isDebugEnabled() )
            LOG.debug( destDir + " - done." );
    }
  //---------------------------------------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------------------------------------
}
