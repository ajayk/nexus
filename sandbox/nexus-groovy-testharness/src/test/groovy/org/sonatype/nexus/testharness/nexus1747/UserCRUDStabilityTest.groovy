/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testharness.nexus1747

import static org.testng.Assert.*
import org.testng.annotations.Test
import org.testng.annotations.BeforeClass
import org.testng.annotations.AfterClass
import org.sonatype.nexus.testharness.util.MessageUtil
import org.sonatype.nexus.groovytest.NexusCompatibilityimport groovy.util.XmlSlurperimport groovy.xml.StreamingMarkupBuilderimport org.codehaus.plexus.component.annotations.Component;

@Component(role = UserCRUDStabilityTest.class)
class UserCRUDStabilityTest
{

     def static msgUtil;
     
     def static users = new Vector();

     @BeforeClass
     static void init() {
        msgUtil = new MessageUtil(messageUrl:"users/", messageIdUrl:"users/\$id");
     }

     @Test(threadPoolSize = 10, invocationCount = 10, timeOut = 2000L )
     @NexusCompatibility (minVersion = "1.3")
     void userCreationStability() {
        def mark = Long.toHexString( System.nanoTime() );
        users.add( "velo" + mark );
        
        def s_xml=new StringWriter()
        def builder=new groovy.xml.MarkupBuilder(s_xml);
        builder.'user-request'(){
            data(){
              userId( "velo" + mark )
              name("velo" + mark)
              email("velo@sonatype.org")
              status("active")
              password("velo" + mark)
              roles {
                role("admin")
              }
            }
          }

        def result = msgUtil.doPost( s_xml.toString() );
        assertEquals result.code, 201

        //read and check
        result = msgUtil.doGet( "velo" + mark );
        assertEquals result.code, 200
        
        assertEquals result.content.data.name.text(), "velo" + mark;
     }

     @AfterClass
     static void deleteUsers() {
//         def result = msgUtil.doGet();
//         result.content.data.'users-list-item'.each {
//             def id = it.userId.text();
//             if(id.startsWith("velo")) {
//                 println "Delete $id";
//                 def delResult = msgUtil.doDelete( id );
//                 assertEquals delResult.code, 204
//             }
//         }
         users.each{
             //println "Delete $it";
             if(it != null) {
                 def delResult = msgUtil.doDelete( it );
                 if( delResult.code !=  204 // was deleted 
                                 && delResult.code != 404) { // was not inserted
                     fail "Unable to delete user $it"; 
                 }
             }
         }
     }
}
