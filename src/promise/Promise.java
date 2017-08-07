package promise;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class Promise
{
    protected static enum State
    {
        PENDING, RESOLVED, REJECTED
    }

    protected State state = State.PENDING;

    private int target;

    private int done;

    protected Object data;

    protected Object error;

    protected Consumer<Object> closer;

    protected Consumer<Object> tomb;

    protected Promise( int target, BiConsumer<Consumer<Object>, Consumer<Object>> execution )
    {
        this.target = target;
        execution.accept( this::fulfill, this::decline );
    }

    public Promise( BiConsumer<Consumer<Object>, Consumer<Object>> execution )
    {
        this( 1, execution );
    }

    public abstract Promise then( Function<Object, Object> resolver, Function<Object, Object> rejecter );

    public Promise then( Function<Object, Object> res )
    {
        return then( res, null );
    }

    public Promise katch( Function<Object, Object> rej )
    {
        return then( null, rej );
    }

    public synchronized void done( Consumer<Object> c, Consumer<Object> t )
    {
        try
        {
            if( state == State.RESOLVED && c != null )
            {
                c.accept( data );
            }
            else if( state == State.REJECTED && t != null )
            {
                t.accept( error );
            }
            else
            {
                closer = c;
                tomb = t;
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    public void done( Consumer<Object> c )
    {
        done( c, null );
    }

    public void bury( Consumer<Object> t )
    {
        done( null, t );
    }

    private synchronized void fulfill( Object result )
    {
        if( state != State.PENDING )
        {
            return;
        }
        done++;
        if( done < target )
        {
            if( data == null )
            {
                data = new ArrayList<>();
            }
            ( ( List<Object> ) data ).add( result );
            return;
        }
        else if( target > 1 )
        {
            ( ( List<Object> ) data ).add( result );
        }
        else
        {
            data = result;
        }
        state = State.RESOLVED;

        execute();
    }

    private synchronized void decline( Object err )
    {
        if( state != State.PENDING )
        {
            return;
        }
        error = err;
        state = State.REJECTED;

        execute();
    }

    protected abstract void execute();

    public static <T extends Promise> T resolve( Object obj )
    {
        return obj instanceof Promise ? ( T ) obj : createPromise( ( res, rej ) -> res.accept( obj ) );
    }

    public static <T extends Promise> T reject( Object e )
    {
        return createPromise( ( res, rej ) -> rej.accept( e ) );
    }

    public static Promise all( Promise... promises )
    {
        return pickUp( promises.length, promises );
    }

    public static Promise race( Promise... promises )
    {
        return pickUp( 1, promises );
    }

    private static Promise pickUp( int winners, Promise... promises )
    {
        if( promises == null )
        {
            return resolve( "" );
        }
        return createPromise(
            winners,
            ( resolve, reject ) -> {
                Stream.of( promises ).forEach(
                    promise -> promise.done( data -> resolve.accept( data ), e -> reject.accept( e ) ) );
            } );
    }

    public static Promise setTimeout( int mil )
    {
        return createPromise( ( resolve, reject ) -> {
            new Thread( ( ) -> {
                try
                {
                    TimeUnit.MILLISECONDS.sleep( mil );
                }
                catch( Exception e )
                {
                    e.printStackTrace();
                }
                resolve.accept( "Resolved after " + mil + "!" );
            } ).start();
        } );
    }

    public static Promise get( String url )
    {
        return createPromise( ( resolve, reject ) -> {
            new Thread( ( ) -> {
                try
                {
                    URLConnection connection = new URL( url ).openConnection();
                    connection.setConnectTimeout( 5000 );
                    connection.setReadTimeout( 5000 );
                    connection.connect();
                    BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );
                    String result = "";
                    String line = null;
                    while( ( line = reader.readLine() ) != null )
                    {
                        result += line;
                    }
                    resolve.accept( result );
                }
                catch( Exception e )
                {
                    reject.accept( e );
                }
            } ).start();
        } );
    }

    private static <T extends Promise> T createPromise( BiConsumer<Consumer<Object>, Consumer<Object>> execution )
    {
        return createPromise( 1, execution );
    }

    private static <T extends Promise> T createPromise( int target,
                                                        BiConsumer<Consumer<Object>, Consumer<Object>> execution )
    {
        return ( T ) new StringedPromise( target, execution );
    }

    public static void main( String[] args )
    {
//        Promise.setTimeout( 3000 ).then( data -> {
//            System.out.println( data );
//            return Promise.setTimeout( 2000 );
//        } ).then( data -> {
//            System.out.println( data );
//            throw new NullPointerException( "Error" );
//        } ).katch( e -> e ).done( data -> System.out.println( data ) );
//        
//        Promise.get( "http://localhost:6000/SiteEM.xml" ).then( data -> {
//            System.out.println( data );
//            return Promise.get( "http://localhost:6000/application_banner.txt" );
//        } ).done( data -> System.out.println( data ), e -> System.out.println( e ) );
//
//        for( int i = 0; i < 5; i++ )
//        {
//            Promise.race( Promise.get( "http://localhost:6000/application_banner.txt" ),
//                Promise.get( "http://localhost:6000/SiteEM.xml" ) ).katch( e -> e ).done(
//                data -> System.out.println( data ) );
//        }
//
//        Promise.all( Promise.get( "http://localhost:6000/application_banner.txt" ),
//            Promise.get( "http://localhost:6000/SiteEM.xml" ) ).done( data -> System.out.println( data ),
//            e -> System.out.println( e ) );

        System.out.println( "Hello" );
    }
}
