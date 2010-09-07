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

import org.apache.maven.mercury.crypto.api.StreamObserver;
import org.apache.maven.mercury.crypto.api.StreamObserverException;
import org.apache.maven.mercury.crypto.api.StreamObserverFactory;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id: StupidWagonObserverFactory.java 720564 2008-11-25 18:58:02Z ogusakov $
 *
 */
public class StupidWagonObserverFactory
implements StreamObserverFactory
{
  private MercuryWagon wagon;
  
  public StupidWagonObserverFactory( MercuryWagon wagon )
  {
    this.wagon = wagon;
  }

  public StreamObserver newInstance()
  throws StreamObserverException
  {
    return new StupidWagonObserverAdapter( wagon, wagon.popEvent() );
  }

}
