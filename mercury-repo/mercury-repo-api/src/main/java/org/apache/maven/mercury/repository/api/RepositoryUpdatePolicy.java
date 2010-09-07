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
package org.apache.maven.mercury.repository.api;

import org.apache.maven.mercury.artifact.Quality;

/**
 * abstraction of a repository update policy calculator
 * 
 * @author Oleg Gusakov
 * @version $Id: RepositoryUpdatePolicy.java 762963 2009-04-07 21:01:07Z ogusakov $
 */
public interface RepositoryUpdatePolicy
{
    public static final String SYSTEM_PROPERTY_UPDATE_POLICY = "mercury.repository.update.policy";
    /**
     * initialize this calculator
     * 
     * @param policy as a string somewhere in configuration
     */
    void init( String policy );

    /**
     * perform the calculation and decide if it's time to update
     * 
     * @param timestamp - UTC-based timestamp as long milliseconds
     * @return
     */
    boolean timestampExpired( long timestampMillis, Quality quality );
}
