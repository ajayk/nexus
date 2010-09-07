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

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id: MercuryEventListener.java 720564 2008-11-25 18:58:02Z ogusakov $
 *
 */
public interface MercuryEventListener
{
  /**
   * identifies what events this listrener is interested in. 
   * 
   * @return the mask - BitSet of event type bits, or null, if this listener wants to be notified of all events 
   */
  MercuryEvent.EventMask getMask();
  
  /**
   * this is called when an event matching the listener mask is generated
   * 
   * @param event
   */
  void fire( MercuryEvent event );
}
