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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;

/**
 * TODO:Write class description
 * @author <a HREF="mailto:xin.1.fu@nokia.com">Fu Xin</a>
 *
 */
public class Pool
{
    public static void main( String[] args )
    {
        new Pool().start();
    }

    private LinkedList<Socket> sockets = new LinkedList<>();

    ServerSocket serverSocket ;
    boolean running;

    public Pool()
    {
        try
        {
            serverSocket = new ServerSocket( 8080 );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    public void start()
    {
        running = true;
        for( int i = 0; i < 5; i++ )
        {
            new SocketSwimmer( this ).start();
        }
        while( running )
        {
            try
            {
                Socket socket = serverSocket.accept();
                synchronized( this )
                {
                    sockets.add( socket );
                    notify();
                }
            }
            catch( SocketException e )
            {
                if( !e.getMessage().contains( "socket closed" ) )
                {
                    e.printStackTrace();
                }
            }
            catch( IOException e )
            {
                e.printStackTrace();
            }
        }
        System.out.println( "server closed" );
    }

    public Socket take()
    {
        return sockets.removeFirst();
    }

    public boolean running()
    {
        return running;
    }

    public boolean accepted()
    {
        return sockets.size() > 0;
    }

    public void close()
    {
        synchronized( this )
        {
            running = false;
            sockets = null;
            notifyAll();
        }
        try
        {
            serverSocket.close();
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
    }
}
