/*
 *  Copyright (c) 2017 Nokia. All rights reserved.
 *
 *  Revision History:
 *
 *  DATE/AUTHOR          COMMENT
 *  ---------------------------------------------------------------------
 *  2017年3月13日/xinfu                            
 */
package boring.pool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * TODO:Write class description
 * @author <a HREF="mailto:xin.1.fu@nokia.com">Fu Xin</a>
 *
 */
public class SocketSwimmer extends Thread
{

    private static int id = 0;

    private Pool pool;

    public SocketSwimmer( Pool pool )
    {
        this.pool = pool;
    }

    public void run()
    {
        int i = ++id;
        while( pool.running() )
        {
            Socket socket = null;
            synchronized( pool )
            {
                while( pool.running() && !pool.accepted() )
                {
                    try
                    {
                        pool.wait();
                    }
                    catch( InterruptedException e )
                    {
                        e.printStackTrace();
                    }
                }
                if( !pool.running() )
                {
                    break;
                }
                socket = pool.take();
            }
            InputStream in = null;
            OutputStream out = null;
            try
            {
                in = socket.getInputStream();
                out = socket.getOutputStream();
                try
                {
                    while( in.available() == 0 )
                    {
                        Thread.sleep( 50 );
                    }
                }
                catch( InterruptedException e )
                {
                    e.printStackTrace();
                }
                byte[] buffer = new byte[ in.available() ];
                in.read( buffer );
                String content = new String( buffer );
                String k =
                    "HTTP/1.1 200 OK\r\nDate: Mon, 13 Mar 2017 10:41:50 GMT\r\nContent-Type: text/html\r\nContent-Length: " +
                        content.getBytes().length + "\r\n\r\n" + content;
                out.write( k.getBytes() );
                out.flush();
                if( content.contains( "close" ) )
                {
                    pool.close();
                }
            }
            catch( IOException e )
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    if( in != null )
                    {
                        in.close();
                    }
                    if( out != null )
                    {
                        out.close();
                    }
                    if( socket != null )
                    {
                        socket.close();
                    }
                }
                catch( IOException e )
                {
                    e.printStackTrace();
                }
            }
        }
        System.out.println( i + "->swimmer get out" );
    }

}
