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
package org.apache.maven.mercury.util;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Configurable;

/**
 * general utility helpers
 *
 * @author Oleg Gusakov
 * @version $Id: Util.java 762963 2009-04-07 21:01:07Z ogusakov $
 *
 */
public class Util
{
    public static final boolean bWindows = File.pathSeparatorChar == ';';
    
    public static final boolean isWindows()
    {
      return bWindows;
    }
    
   @SuppressWarnings("unchecked")
  public static final boolean isEmpty( Collection o )
   {
     return o == null || o.isEmpty();
   }
   
   public static final boolean isEmpty( String o )
   {
     return o == null || o.length() < 1;
   }
   
   public static final boolean isEmpty( File o )
   {
     return o == null || !o.exists() || o.length() < 1L;
   }

   public static final boolean isEmpty( Object [] o )
   {
     return o == null || o.length < 1;
   }

   public static final boolean isEmpty( Map o )
   {
     return o == null || o.isEmpty();
   }

   public static final boolean isEmpty( Object o )
   {
     return o == null;
   }

   public static final String nvlS( String s, String dflt )
   {
     return isEmpty(s) ? dflt : s;
   }
   
   public static final void say( String msg, Monitor monitor )
   {
       if( monitor != null )
           monitor.message( msg );
   }
   
   public static final String convertLength( long sz )
   {
       if( sz < 5000L )
           return sz+" bytes";
       
       return (int)(Math.round( sz / 1024.))+" kb";
   }
   
   public static Map<String,Object> mapOf( Object [][] entries )
   {
       if( entries == null )
           return null;
       
       Map<String,Object> map = new HashMap<String, Object>( entries.length );
       
       for( Object [] kv : entries )
           map.put( (String)kv[0], kv[1] );
       
       return map;
   }
}
