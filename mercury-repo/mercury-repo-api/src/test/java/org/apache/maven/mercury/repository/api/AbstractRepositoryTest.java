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

package org.apache.maven.mercury.repository.api;

import junit.framework.TestCase;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id: AbstractRepositoryTest.java 747887 2009-02-25 18:28:29Z ogusakov $
 *
 */
public class AbstractRepositoryTest
    extends TestCase
{
    public void testIdHash()
    {
        String res = AbstractRepository.hashId( "central" );
        
        assertEquals( "central", res );

        res = AbstractRepository.hashId( "http://central" );
        
        assertEquals( "3e447f03cb543932ff37403fe937841ff58ff788", res );
    }
}
