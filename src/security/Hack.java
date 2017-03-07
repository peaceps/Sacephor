/*
 *  Copyright (c) 2017 Nokia. All rights reserved.
 *
 *  Revision History:
 *
 *  DATE/AUTHOR          COMMENT
 *  ---------------------------------------------------------------------
 *  2017年2月23日/xinfu                            
 */
package security;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * TODO:Write class description
 * @author <a HREF="mailto:xin.1.fu@nokia.com">Fu Xin</a>
 *
 */
public class Hack
{
    public static void main( String[] args ) throws Exception
    {
//        SecurityManager m = System.getSecurityManager();
//        m.checkPermission( new SaPermission( "go" ) );
        SaLoader loader = new SaLoader( new URL[]{ new URL( "file:/D:/git/Sacephor/bin" ) } );
        SaLoader loader2 = new SaLoader( new URL[]{ new URL( "file:/D:/git/Sacephor/bin" ) } );
        Class<?> h = loader.loadClass( "security.Hack" );
        Class<?> h2 = loader2.loadClass( "security.Hack" );
        System.out.println( h );
        System.out.println( h2.equals( h ) );
        loader.close();
        loader2.close();
    }
}

class SaLoader extends URLClassLoader
{

    /**
     * @param urls
     */
    public SaLoader( URL[] urls )
    {
        super( urls );
    }

}