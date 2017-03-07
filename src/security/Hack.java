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

import java.lang.reflect.Method;
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
        SaLoader loader = new SaLoader( new URL[]{ new URL( "file:/D:/git/Sacephor/bin" ) } );
        SaLoader loader2 = new SaLoader( new URL[]{ new URL( "http://www.baidu.com" ) } );
        Class<?> h = loader.loadClass( "security.Hack" );
        Class<?> h2 = loader2.loadClass( "security.Hack" );
        Class<?> a = A.class;//loader.loadClass( "security.A" );
        System.out.println( h );
        System.out.println( h2.equals( h ) );
        System.out.println( a );
        Object t = h2.newInstance();
        Method tm = h2.getMethod( "test" );
        tm.invoke( t );
        loader.close();
        loader2.close();
    }

    public void test()
    {
        SecurityManager m = System.getSecurityManager();
        m.checkPermission( new SaPermission( "go" ) );
        System.out.println( 1 );
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

    @Override
    public Class<?> loadClass( String name ) throws ClassNotFoundException
    {
        if( !"security.Hack".equals( name ) )
        {
            throw new ClassNotFoundException( name );
        }
        return super.loadClass( name );
    }

}

class A
{
}