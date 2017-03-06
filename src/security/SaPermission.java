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

import java.security.BasicPermission;
import java.security.Permission;

/**
 * TODO:Write class description
 * @author <a HREF="mailto:xin.1.fu@nokia.com">Fu Xin</a>
 *
 */
public class SaPermission extends BasicPermission
{

    /**
     * @param name
     */
    public SaPermission( String name, String action )
    {
        super( name, action );
    }

    /**
     * @param name
     */
    public SaPermission( String action )
    {
        super( "sa.pesdfasd", action );
    }

    /** 
     * @param permission
     * @return
     * @see java.security.Permission#implies(java.security.Permission)
     */
    @Override
    public boolean implies( Permission permission )
    {
        // TODO Auto-generated method stub
        return true;
    }

    /** 
     * @param obj
     * @return
     * @see java.security.Permission#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object obj )
    {
        return obj == this;
    }

    /** 
     * @return
     * @see java.security.Permission#hashCode()
     */
    @Override
    public int hashCode()
    {
        // TODO Auto-generated method stub
        return getName().hashCode();
    }

    /** 
     * @return
     * @see java.security.Permission#getActions()
     */
    @Override
    public String getActions()
    {
        return null;
    }

}
