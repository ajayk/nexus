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
package org.apache.maven.mercury.event;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.apache.maven.mercury.event.MercuryEvent.EventMask;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id: EventFrameworkTest.java 726880 2008-12-16 00:04:14Z ogusakov $
 *
 */
public class EventFrameworkTest
extends TestCase
{
  static final int THREAD_COUNT = 5;
  static final int EVENT_COUNT  = 10;
  
  ExecutorService es;
  
  EventManager em;
  
  Listener listener;
  
  @Override
  protected void setUp()
  throws Exception
  {
    es = Executors.newFixedThreadPool( THREAD_COUNT );
  }
  
  public void testListenAllEvents()
  throws Exception
  {
    runTest( null, null, THREAD_COUNT * EventFrameworkTest.EVENT_COUNT,  THREAD_COUNT * EventFrameworkTest.EVENT_COUNT );
  }

  public void testListenMaskedListenerEvents()
  throws Exception
  {
    runTest(  null
            , new MercuryEvent.EventMask(EventTypeEnum.localRepository)
            , THREAD_COUNT * EventFrameworkTest.EVENT_COUNT
            , 0
           );
  }

  public void testListenMaskedManagerEvents()
  throws Exception
  {
    runTest( new MercuryEvent.EventMask(EventTypeEnum.remoteRepository)
            , null
            , 0
            , THREAD_COUNT * EventFrameworkTest.EVENT_COUNT
       );
  }

  public void testListenMismatchedMaskEvents()
  throws Exception
  {
    runTest( new MercuryEvent.EventMask(EventTypeEnum.remoteRepository)
            , new MercuryEvent.EventMask(EventTypeEnum.localRepository)
            , 0
            , 0
          );
  }
  //-------------------------------------------------------------------------------------------------------------------------------
  private void runTest( MercuryEvent.EventMask emMask, MercuryEvent.EventMask listenerMask, int expectedLocal, int expectedRemote )
  throws Exception
  {
    em = new EventManager( emMask );
    
    listener = new Listener( listenerMask  );
    
    em.register( listener );

    for( int i=0; i<THREAD_COUNT; i++ )
    {
      es.execute( new Generator( em, EventTypeEnum.localRepository, ""+i ) );
    }

    for( int i=0; i<THREAD_COUNT; i++ )
    {
      es.execute( new Generator( em, EventTypeEnum.remoteRepository, ""+i ) );
    }
    
    es.awaitTermination( 2, TimeUnit.SECONDS );
    
    assertEquals( expectedLocal, listener.localRepoCount );
    assertEquals( expectedRemote, listener.remoteRepoCount );
  }
}

//=====================  helper classes  =====================
class Listener
implements MercuryEventListener
{
  MercuryEvent.EventMask _mask;
  
  int localRepoCount = 0;
  
  int remoteRepoCount = 0;
  
  public Listener( MercuryEvent.EventMask mask )
  {
    _mask = mask;
  }

  public void fire( MercuryEvent event )
  {
//    System.out.println( EventManager.toString( event ) );
//    System.out.flush();
    
    if( event.getType().equals( EventTypeEnum.localRepository ) )
      ++localRepoCount;
    else
      if( event.getType().equals( EventTypeEnum.remoteRepository ) )
        ++remoteRepoCount;
  }

  public EventMask getMask()
  {
    return _mask;
  }
  
}

class Generator
implements Runnable, EventGenerator
{
  
  EventManager _eventManager;
  
  String _msg;
  
  EventTypeEnum _myType;
  
  public Generator( EventManager em, EventTypeEnum type, String msg  )
  {
    _eventManager = em;
    _msg = msg;
    _myType = type;
  }
  
  public void run()
  {
    for( int i=0; i< EventFrameworkTest.EVENT_COUNT; i++ )
      try
      {
        GenericEvent event = new GenericEvent( _myType, _msg );
        Thread.sleep( (int)(100.0*Math.random()) );
        event.stop();
        _eventManager.fireEvent( event );
      }
      catch( InterruptedException e )
      {
        return;
      }
  }

  public void register( MercuryEventListener listener )
  {
    if( _eventManager == null )
      _eventManager = new EventManager();
      
    _eventManager.register( listener );
  }

  public void unRegister( MercuryEventListener listener )
  {
    if( _eventManager != null )
      _eventManager.unRegister( listener );
  }
  
  public void setEventManager( EventManager eventManager )
  {
    if( _eventManager == null )
      _eventManager = eventManager;
    else
      _eventManager.getListeners().addAll( eventManager.getListeners() );
      
  }
}

