package org.apache.maven.mercury.ant.tasks;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.maven.mercury.transport.api.Credentials;
import org.apache.maven.mercury.util.FileUtil;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 * @author Oleg Gusakov
 * @version $Id: Auth.java 747226 2009-02-24 00:02:44Z ogusakov $
 */
public class Auth
    extends AbstractDataType
{
    private static final Language LANG = new DefaultLanguage( Auth.class );

    private static final String DEFAULT_AUTH_ID =
        System.getProperty( "mercury.default.auth.id", "mercury.default.auth.id." + System.currentTimeMillis() );
    
    public static final String METHOD_BASIC = "basic";

    String _method;

    String _name;

    String _pass;

    String _certfile;
    
    public Auth()
    {
    }
    
    public Auth( String auth )
    {
        setSource( auth );
    }

    public void setSource( String auth )
    {
        if( auth == null )
            throw new IllegalArgumentException( LANG.getMessage( "auth.null.auth" ) );
        
        int colon = auth.indexOf( ':' );
        
        String paramStr = null;
        
        if( colon == -1 )
        {
            _method = METHOD_BASIC;
            
            paramStr = auth;
        }
        else
            paramStr = auth.substring( colon+1 );
        
        if( METHOD_BASIC.regionMatches( 0, _method, 0, METHOD_BASIC.length() ) )
        {
            StringTokenizer st = new StringTokenizer( paramStr, "/" );
            
            if( st.countTokens() != 2 )
                throw new IllegalArgumentException( LANG.getMessage( "auth.bad.basic.params" ) );
            
            _name = st.nextToken();
            
            _pass = st.nextToken();
        }
    }

    public void setName( String name )
    {
        this._name = name;
    }

    // compatibility with old syntax
    public void setUsername( String name )
    {
        setName( name );
    }

    public void setPass( String pass )
    {
        this._pass = pass;
    }

    // compatibility with old syntax
    public void setPassword( String pass )
    {
        setPass( pass );
    }

    // compatibility with old syntax
    public void setPassphrase( String pass )
    {
        setPass( pass );
    }

    public void setCertfile( String certfile )
    {
        this._certfile = certfile;
    }

    // compatibility with old syntax
    public void setPrivateKey( String certfile )
    {
        setCertfile( certfile );
    }

    // compatibility with old syntax + case independence
    public void setPrivatekey( String certfile )
    {
        setCertfile( certfile );
    }

    protected static Auth findAuth( Project project, String authId )
    {
        Object ao = ( authId == null ) ? project.getReference( DEFAULT_AUTH_ID ) : project.getReference( authId );

        if ( ao == null )
        {
            return null;
        }

        return (Auth) ao;
    }

    protected Credentials createCredentials()
    {
        Credentials cred = null;

        if ( _certfile != null )
        {
            File cf = new File( _certfile );
            if ( !cf.exists() )
            {
                throw new BuildException( LANG.getMessage( "config.no.cert.file", _certfile ) );
            }

            try
            {
                cred = new Credentials( FileUtil.readRawData( cf ), _name, _pass );
            }
            catch ( IOException e )
            {
                throw new BuildException( e );
            }
        }
        else
        {
            cred = new Credentials( _name, _pass );
        }

        return cred;
    }
}
