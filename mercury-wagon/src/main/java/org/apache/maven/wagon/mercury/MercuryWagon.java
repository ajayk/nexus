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
package org.apache.maven.wagon.mercury;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.mercury.crypto.api.StreamObserverFactory;
import org.apache.maven.mercury.crypto.api.StreamVerifierAttributes;
import org.apache.maven.mercury.crypto.api.StreamVerifierException;
import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.crypto.pgp.PgpStreamVerifierFactory;
import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.MercuryLoggerManager;
import org.apache.maven.mercury.spi.http.client.HttpClientException;
import org.apache.maven.mercury.spi.http.client.deploy.DefaultDeployRequest;
import org.apache.maven.mercury.spi.http.client.deploy.DefaultDeployer;
import org.apache.maven.mercury.spi.http.client.deploy.DeployResponse;
import org.apache.maven.mercury.spi.http.client.retrieve.DefaultRetrievalRequest;
import org.apache.maven.mercury.spi.http.client.retrieve.DefaultRetriever;
import org.apache.maven.mercury.spi.http.client.retrieve.RetrievalResponse;
import org.apache.maven.mercury.transport.api.Binding;
import org.apache.maven.mercury.transport.api.Credentials;
import org.apache.maven.mercury.transport.api.Server;
import org.apache.maven.wagon.AbstractWagon;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.resource.Resource;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 *  plexus.component
 *    role-hint="http"
 *    instantiation-strategy="per-lookup"
 *    description="Mercury based wagon implementation"
 *
 * @author Oleg Gusakov
 * @version $Id: MercuryWagon.java 743599 2009-02-12 02:15:51Z ogusakov $
 *
 */
public class MercuryWagon
extends AbstractWagon
implements Wagon
{
  private static final IMercuryLogger LOG = MercuryLoggerManager.getLogger( MercuryWagon.class ); 
  private static final Language LANG = new DefaultLanguage( MercuryWagon.class );

  public static final String SYSTEM_PARAMETER_DEBUG_TRANSFER = "maven.mercury.wagon.debug.transfer";
  private boolean debugTransfer = Boolean.parseBoolean( System.getProperty( SYSTEM_PARAMETER_DEBUG_TRANSFER, "false" ) );

  public static final String SYSTEM_PARAMETER_PGP_CONGIG = "maven.mercury.wagon.pgp.config";
  private String pgpConfig = System.getProperty( SYSTEM_PARAMETER_PGP_CONGIG, null );
  
  // TODO: Oleg, 2008-09-26: move to mercury-transport-api 
  public static final String [][] protocolConversions = new String [][]
                                                                      {
      {"http:",      "http:"}
    , {"https:",     "https:"}
    , {"dav:",       "http:"}
    , {"davs:",      "https:"}
    , {"dav:http:",  "http:"}
    , {"dav:https:", "https:"}
    , {"mttp:",      "http:"}
    , {"mttps:",     "https:"}
                                                                      };
  
  private Server server;
  private DefaultRetriever retriever;
  private DefaultDeployer deployer;
  
  private List<TransferEvent> events = new ArrayList<TransferEvent>(8);
  private String userAgent;
  
  /**
   * 
   */
  public MercuryWagon()
  {
    if( LOG.isDebugEnabled() )
      LOG.debug( "MercuryWagon instantiated" );
  }
  
  public MercuryWagon( Server server )
  throws IllegalArgumentException
  {
    init( server );
  }

  private void init( Server server )
  throws IllegalArgumentException
  {
    if( server == null )
      throw new IllegalArgumentException( LANG.getMessage( "null.read.server" ) );
    
    if( server.getURL() == null )
      throw new IllegalArgumentException( LANG.getMessage( "null.read.server.url", server.getId() ) );
    
    this.server = server;
    
    Set<StreamVerifierFactory>[] pgpFac = null;
    
    try
    {
      pgpFac = readPgpConfig();
      this.server.setReaderStreamVerifierFactories( pgpFac[0] );
      this.server.setWriterStreamVerifierFactories( pgpFac[1] );
    }
    catch( Exception ex )
    {
      throw new IllegalArgumentException(ex);
    }

    Set<StreamObserverFactory> rf = server.getReaderStreamObserverFactories();
    if( rf == null )
    {
      rf = new HashSet<StreamObserverFactory>(1);
      this.server.setReaderStreamObserverFactories( rf );
    }
    rf.add( new StupidWagonObserverFactory( this ) );
    
    HashSet<Server> servers = new HashSet<Server>(1);
    servers.add( this.server );
    
    Set<StreamObserverFactory> wf = this.server.getWriterStreamObserverFactories();
    if( wf == null )
    {
      wf = new HashSet<StreamObserverFactory>(1);
      this.server.setReaderStreamObserverFactories( wf );
    }
    wf.add( new StupidWagonObserverFactory( this ) );

    try
    {
      retriever = new DefaultRetriever();
    }
    catch( HttpClientException e )
    {
      throw new IllegalArgumentException(e);
    }
    retriever.setServers( servers );
    
    try
    {
      deployer = new DefaultDeployer();
    }
    catch( HttpClientException e )
    {
      throw new IllegalArgumentException(e);
    }
    deployer.setServers( servers );
  }

  public void get( String resourceName, File destination )
  throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
  {
    if( LOG.isDebugEnabled() )
      LOG.debug("MercuryWagon getting "+resourceName+" into "+destination);

    Binding binding = null;
    
    try
    {
      binding = new Binding( new URL(server.getURL().toString()+'/'+resourceName), destination );
    }
    catch( MalformedURLException e )
    {
      throw new TransferFailedException( e.getMessage() );
    }
    
    DefaultRetrievalRequest request = new DefaultRetrievalRequest();
    request.addBinding( binding );
    
    Resource resource = new Resource( resourceName );

    fireGetInitiated( resource, destination );

    resource.setLastModified( 0l );

    server.setUserAgent( userAgent );

    fireGetStarted( resource, destination );
    
    pushEvent( new TransferEvent(this, resource, TransferEvent.TRANSFER_PROGRESS, TransferEvent.REQUEST_GET) );
    
    long start = System.currentTimeMillis();
    
    RetrievalResponse response = retriever.retrieve( request );
    
    fireGetCompleted( resource, destination );

    if( response.hasExceptions() )
    {
      LOG.error( response.getExceptions().toString() );
      throw new ResourceDoesNotExistException( response.getExceptions().toString() );
    }

    if( LOG.isDebugEnabled() )
      LOG.debug("MercuryWagon got ["+resourceName+" ==> "+destination + "], time " + (System.currentTimeMillis()-start) + " millis");
    
  }

  public boolean getIfNewer( String resourceName, File destination, long timestamp )
  throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
  {
    get( resourceName, destination );
    return true;
  }

  public void put( File source, String destination )
  throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
  {
    if( LOG.isDebugEnabled() )
      LOG.debug( "put request for: "+source+" => "+destination );

    Resource resource = new Resource( destination );
    
    firePutInitiated( resource, source );
    
    resource.setContentLength( source.length() );
    
    resource.setLastModified( source.lastModified() );

    Binding binding = null;
    try
    {
      binding = new Binding( new URL( this.server.getURL().toString()+'/'+destination), source );
    }
    catch( MalformedURLException e )
    {
      throw new TransferFailedException( e.getMessage() );
    }
    
    HashSet<Binding> bindings = new HashSet<Binding>(1);
    bindings.add( binding );
    
    DefaultDeployRequest request = new DefaultDeployRequest();
    request.setBindings( bindings );

    firePutStarted( resource, source );
    
    server.setUserAgent( userAgent );

    pushEvent( new TransferEvent(this, resource, TransferEvent.TRANSFER_PROGRESS, TransferEvent.REQUEST_PUT) );
    
    long start = System.currentTimeMillis();

    DeployResponse response = deployer.deploy( request );

    firePutCompleted( resource, source );

    if( response.hasExceptions() )
    {
      throw new TransferFailedException( response.getExceptions().toString() );
    }

    if( LOG.isDebugEnabled() )
      LOG.debug("MercuryWagon put ["+source+" ==> "+destination + "], time " + (System.currentTimeMillis()-start) + " millis");
  }

  protected void closeConnection()
  throws ConnectionException
  {
  }
  
  @Override
  public void openConnection()
  throws ConnectionException, AuthenticationException
  {
    openConnectionInternal();
  }
  
  private final String convertProtocol( String pin )
  throws ConnectionException
  {
    for( String [] pc : protocolConversions )
    {
      if( pc[0].equals( pin ) )
        return pc[1];
    }
    
    throw new ConnectionException("bad protocol: "+pin);
  }
  
  @Override
  protected void openConnectionInternal()
  throws ConnectionException, AuthenticationException
  {
    if( LOG.isDebugEnabled() )
      LOG.debug( "opening connection to repository "+repository );

    try
    {
      String repUrl = repository.getUrl();
      int index = repUrl.indexOf( '/' );
      String protocol =  convertProtocol( repUrl.substring( 0, index  ) );

      String theRest = repUrl.substring( index );
      if( theRest.endsWith( "/" ) )
        theRest = theRest.substring( 0, theRest.length()-1 );

      String url = protocol + theRest;

      if(LOG.isDebugEnabled())
        LOG.debug( "converted url "+repository.getUrl()+" ==> "+url );

      Server server = new Server( repository.getId(), new URL( url ) );

      if( authenticationInfo != null && authenticationInfo.getUserName() != null )
      {
        Credentials user = new Credentials( authenticationInfo.getUserName(), authenticationInfo.getPassword() );
        
        server.setServerCredentials( user );
        
        if( LOG.isDebugEnabled() )
          LOG.debug( "user ceredentials: "+user.getUser()+"/..." );
      }
      
      
      ProxyInfo pi = getProxyInfo("http", getRepository().getHost());

      String httpProxyType = ProxyInfo.PROXY_HTTP.toLowerCase(); 

      if( pi != null && pi.getHost() != null )
      {
        String proxyType = pi.getType();
        
        if( !httpProxyType.equalsIgnoreCase( proxyType ) )
        {
          throw new ConnectionException( "Mercury wagon does not support "+proxyType+" proxies at this point. Only "+httpProxyType+" proxy is supported" );
        }

        server.setProxy( new URL("http://"+pi.getHost()+":" + (pi.getPort()>0 ? pi.getPort() : 80) ) );

        if( pi.getUserName() != null )
        {
          Credentials proxyUser = new Credentials( pi.getUserName(), pi.getPassword() );
          
          server.setProxyCredentials( proxyUser );

          if(LOG.isDebugEnabled())
            LOG.debug( "proxy credentials set to : " + proxyUser.getUser()+"/..." );
        }
      }

      if(LOG.isDebugEnabled())
        LOG.debug( "proxy url set to : " + server.getProxy() );

      init( server );
      
      if( debugTransfer )
        transferEventSupport.addTransferListener( new TransferEventDebugger() );
    }
//    catch( MalformedURLException e )
//    {
//      throw new ConnectionException( e.getMessage() );
//    }
    catch( Throwable e )
    {
      e.printStackTrace();
      LOG.error( e.getMessage() );
      throw new ConnectionException( e.getMessage() );
    }
  }

  void bytesReady( TransferEvent transferEvent, byte [] buf, int len )
  {
    fireTransferProgress( transferEvent, buf, len );
  }

  /**
   * @return
   */
  public final TransferEvent popEvent()
  {
    if( events.isEmpty() )
      return null;
    
    TransferEvent event = events.get( 0 );
    events.remove( 0 );
    
    return event;
  }

  public final void pushEvent( final TransferEvent event)
  {
    events.add( 0, event );
  }
  
  private final Set<StreamVerifierFactory>[] readPgpConfig()
  throws FileNotFoundException, IOException, StreamVerifierException
  {
    Set<StreamVerifierFactory> [] res = new Set [] { null, null };
    
    if( pgpConfig == null )
      return res;
    
    if( LOG.isDebugEnabled() )
      LOG.debug( "PGP signature configuration will be read from "+pgpConfig );
    
    Properties pgpProps = new Properties();
    pgpProps.load( new FileInputStream(pgpConfig) );

    String readerKeyring = pgpProps.getProperty( "reader.keyring" );
    
    if( readerKeyring != null )
    {
      StreamVerifierAttributes readerAttr = new StreamVerifierAttributes(
          PgpStreamVerifierFactory.DEFAULT_EXTENSION
        , Boolean.parseBoolean( pgpProps.getProperty( "reader.lenient", "true" ) )
        , false
                                                                      );

      StreamVerifierFactory rf = new PgpStreamVerifierFactory( readerAttr, new FileInputStream(readerKeyring) );

      if( LOG.isDebugEnabled() )
        LOG.debug( "public key file: "+new File(readerKeyring).getAbsolutePath() );
      
      Set<StreamVerifierFactory> rs = new HashSet<StreamVerifierFactory>(1);
      rs.add( rf );
      res[0] = rs;
    }

    String writerKeyring = pgpProps.getProperty( "writer.keyring" );
    String writerKeyId = pgpProps.getProperty( "writer.key.id" );
    String writerKeyringPass = pgpProps.getProperty( "writer.key.pass" );

    if( writerKeyring != null && writerKeyId != null && writerKeyringPass != null )
    {
      if( LOG.isDebugEnabled() )
        LOG.debug( "secret key file: "+new File(writerKeyring).getAbsolutePath() );

      StreamVerifierAttributes writerAttr = new StreamVerifierAttributes(
          PgpStreamVerifierFactory.DEFAULT_EXTENSION
        , Boolean.parseBoolean( pgpProps.getProperty( "writer.lenient", "true" ) )
        , false
                                                                      );

      StreamVerifierFactory wf = new PgpStreamVerifierFactory( writerAttr, new FileInputStream(writerKeyring)
                                                              , writerKeyId, writerKeyringPass );
      
      Set<StreamVerifierFactory> ws = new HashSet<StreamVerifierFactory>(1);
      ws.add( wf );
      res[1] = ws;
    }
    
    return res;
  }
  
  public void setHttpHeaders( Properties httpHeaders )
  {
      this.userAgent = httpHeaders.getProperty( "User-Agent", null );
      
      if( LOG.isDebugEnabled() )
        LOG.debug( "userAgent set to : "+this.userAgent );
      
  }

  @Override
  public boolean resourceExists( String resourceName )
  throws TransferFailedException, AuthorizationException
  {
    if( LOG.isDebugEnabled() )
      LOG.debug( "check if resourceExists: "+resourceName+" on server "+(server == null ? "null" : server.getURL() ) );

    File temp;
    try
    {
      temp = File.createTempFile( "mercury-", ".temp" );
    }
    catch( IOException e )
    {
      throw new TransferFailedException( e.getMessage() );
    }
    temp.delete();
    
    Binding binding = null;
    
    try
    {
      binding = new Binding( new URL(server.getURL().toString()+'/'+resourceName), temp );
    }
    catch( MalformedURLException e )
    {
      throw new TransferFailedException( e.getMessage() );
    }
    
    DefaultRetrievalRequest request = new DefaultRetrievalRequest();
    request.addBinding( binding );
    
    server.setUserAgent( userAgent );

    RetrievalResponse response = retriever.retrieve( request );
    
    temp.delete();
    
    if( response.hasExceptions() )
    {
      LOG.error( response.getExceptions().toString() );
      return false;
    }
    return true;
  }
  
  

}
