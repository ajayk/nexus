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
package org.apache.maven.mercury.logging;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id: MercuryLoggingLevelEnum.java 750267 2009-03-05 01:09:25Z ogusakov $
 *
 */
public enum MercuryLoggingLevelEnum
{
    debug(0)
  , info(1)
  , warn(2)
  , error(3)
  , fatal(4)
  , disabled(5)
  ;

  public static final MercuryLoggingLevelEnum DEFAULT_LEVEL = info;

  private int id;

  // Constructor
  MercuryLoggingLevelEnum( int id )
  {
      this.id = id;
  }

  int getId()
  {
      return id;
  }
  
}
