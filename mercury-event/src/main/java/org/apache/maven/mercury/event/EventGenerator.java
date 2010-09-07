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
 * this component generates events 
 *
 * @author Oleg Gusakov
 * @version $Id: EventGenerator.java 720564 2008-11-25 18:58:02Z ogusakov $
 *
 */
public interface EventGenerator
{
  /**
   * register event listener
   * 
   * @param listener
   */
  void register( MercuryEventListener listener );
  
  /**
   * remove particular event listener
   * 
   * @param listener
   */
  void unRegister( MercuryEventListener listener );
  
  /**
   * set entire event manager
   * 
   * @param eventManager
   */
  void setEventManager( EventManager eventManager );
  
}
