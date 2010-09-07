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

import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.MercuryLoggerManager;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id: TransferEventDebugger.java 726706 2008-12-15 14:49:51Z hboutemy $
 *
 */
public class TransferEventDebugger
implements TransferListener
{
  public static final String SYSTEM_PARAMETER_DEBUG_TRANSFER_BYTES = "maven.mercury.wagon.debug.transfer.bytes";
  private boolean debugTransferBytes = Boolean.parseBoolean( System.getProperty( SYSTEM_PARAMETER_DEBUG_TRANSFER_BYTES, "false" ) );

  private static final IMercuryLogger LOG = MercuryLoggerManager.getLogger( TransferEventDebugger.class ); 

  public void debug( String message )
  {
  }

  public void transferCompleted(
      TransferEvent transferEvent )
  {
    LOG.info("|=============>   completed: "+transferEvent.getResource().getName() );
  }

  public void transferError( TransferEvent transferEvent )
  {
    LOG.info("|=============>   error: "+transferEvent.getResource().getName() );
  }

  public void transferInitiated( TransferEvent transferEvent )
  {
    LOG.info("|=============>   initialized: "+transferEvent.getResource().getName() );
  }

  public void transferProgress(
      TransferEvent transferEvent,
      byte[] buffer,
      int length )
  {
    if( debugTransferBytes )
      LOG.info("|=============>   ready "+length+" bytes : "+transferEvent.getResource().getName() );
  }

  public void transferStarted(
      TransferEvent transferEvent )
  {
    LOG.info("|=============>   started: "+transferEvent.getResource().getName() );
  }

}
