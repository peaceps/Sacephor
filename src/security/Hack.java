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

/**
 * TODO:Write class description
 * @author <a HREF="mailto:xin.1.fu@nokia.com">Fu Xin</a>
 *
 */
public class Hack
{
    public static void main( String[] args )
    {
        SecurityManager m = System.getSecurityManager();
        m.checkPermission( new SaPermission( "go" ) );
        System.out.println( 1 );
    }
}
