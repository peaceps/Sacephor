package promise;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class Promise
{
    private static enum State
    {
        PENDING, RESOLVED, REJECTED
    }

    private State state = State.PENDING;
    
    private int target;

    private int done;

    private Object data;

    private Object error;

    private List<FunctionWapper> handlers = new LinkedList<>();
    
    private Consumer<Object> closer;
    
    private Consumer<Object> tomb;

    private Promise( int target, BiConsumer<Consumer<Object>, Consumer<Object>> execution )
    {
        this.target = target;
        execution.accept( this::fulfill, this::decline );
    }

    public Promise( BiConsumer<Consumer<Object>, Consumer<Object>> execution )
    {
        this( 1, execution );
    }

    public synchronized Promise then( Function<Object, Object> resolver )
    {
        if( state == State.RESOLVED )
        {
            return apply( resolver, data );
        }
        else if( state == State.PENDING )
        {
            handlers.add( new FunctionWapper( resolver, false ) );
        }
        return this;
    }

    public synchronized Promise katch( Function<Object, Object> rejecter )
    {
        if( state == State.REJECTED )
        {
            return apply( rejecter, error );
        }
        else if( state == State.PENDING )
        {
            handlers.add( new FunctionWapper( rejecter, true ) );
        }
        return this;
    }

    private Promise apply( Function<Object, Object> func, Object param )
    {
        try
        {
            return attach( resolve( func.apply( param ) ) );
        }
        catch( Throwable e )
        {
            return attach( reject( e ) );
        }
    }

    private Promise attach( Promise p )
    {
        Promise promise = handlers.stream().reduce( p, ( prev, handler ) -> {
            if( handler.reject )
            {
                return prev.katch( handler.function );
            }
            else
            {
                return prev.then( handler.function );
            }
        }, ( u, t ) -> t );

        if( closer != null )
        {
            promise.done( closer, tomb );
        }

        return promise;
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

    public synchronized void done( Consumer<Object> c )
    {
        done( c, null );
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
        boolean find = false;
        while( !find && !handlers.isEmpty() )
        {
            FunctionWapper f = handlers.remove( 0 );
            if( !f.reject )
            {
                then( f.function );
                find = true;
                break;
            }
        }
        
        if( !find )
        {
            done( closer, tomb );
        }
    }

    private synchronized void decline( Object err )
    {
        if( state != State.PENDING )
        {
            return;
        }
        error = err;
        state = State.REJECTED;

        boolean find = false;
        while( !find && !handlers.isEmpty() )
        {
            FunctionWapper f = handlers.remove( 0 );
            if( f.reject )
            {
                katch( f.function );
                find = true;
                break;
            }
        }

        if( !find )
        {
            done( closer, tomb );
        }
    }

    public static Promise resolve( Object obj )
    {
        return obj instanceof Promise ? ( Promise ) obj : new Promise( ( res, rej ) -> res.accept( obj ) );
    }

    public static Promise reject( Object e )
    {
        return new Promise( ( res, rej ) -> rej.accept( e ) );
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
        return new Promise( winners, ( resolve, reject ) -> {
            Stream.of( promises ).forEach(
                promise -> promise.done( data -> resolve.accept( data ), e -> reject.accept( e ) ) );
        } );
    }

    public static Promise setTimeout( int mil )
    {
        return new Promise( ( resolve, reject ) -> {
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
        return new Promise( ( resolve, reject ) -> {
            new Thread( ( )->{
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

    private static class FunctionWapper
    {
        Function<Object, Object> function;

        boolean reject;

        public FunctionWapper( Function<Object, Object> func, boolean rej )
        {
            function = func;
            reject = rej;
        }
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
//        Promise.race( Promise.get( "http://localhost:6000/application_banner.txt" ),
//            Promise.get( "http://localhost:6000/SiteEM.xml" ) ).katch( e -> e ).done( data -> System.out.println( data ) );
//
//        Promise.all( Promise.get( "http://localhost:6000/application_banner.txt" ),
//            Promise.get( "http://localhost:6000/SiteEM.xml" ) ).done( data -> System.out.println( data ),
//            e -> System.out.println( e ) );
        
        System.out.println( "Hello" );
    }
}
