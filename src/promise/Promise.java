package promise;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class Promise
{
    private static enum State
    {
        PENDING, RESOLVED, REJECTED
    }

    private State state = State.PENDING;
    
    private Object data;

    private Throwable error;

    private List<FunctionWapper> handlers = new LinkedList<>();
    
    private Consumer<Object> closer;

    public Promise( BiConsumer<Consumer<Object>, Consumer<Throwable>> run )
    {
        run.accept( this::fullfill, this::decline );
    }

    public synchronized Promise then( Function<Object, Object> resolver )
    {
        if( state == State.RESOLVED )
        {
            return runIt( resolver, data );
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
            return runIt( rejecter, error );
        }
        else if( state == State.PENDING )
        {
            handlers.add( new FunctionWapper( rejecter, true ) );
        }
        return this;
    }

    private Promise runIt( Function<Object, Object> func, Object param )
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
            promise.done( closer );
        }

        return promise;
    }

    public void done( Consumer<Object> consumer )
    {
        if( state == State.RESOLVED )
        {
            consumer.accept( data );
        }
        else
        {
            closer = consumer;
        }
    }

    private synchronized void fullfill( Object ret )
    {
        data = ret;
        state = State.RESOLVED;
        FunctionWapper f = null;
        while( f == null && !handlers.isEmpty() )
        {
            f = handlers.remove( 0 );
            if( !f.reject )
            {
                then( f.function );
                break;
            }
        }
        
        if( f == null && closer != null )
        {
            done( closer );
        }
    }

    private synchronized void decline( Throwable err )
    {
        error = err;
        state = State.REJECTED;

        FunctionWapper f = null;
        while( f == null && !handlers.isEmpty() )
        {
            f = handlers.remove( 0 );
            if( f.reject )
            {
                katch( f.function );
                break;
            }
        }
    }

    public static Promise resolve( Object obj )
    {
        return obj instanceof Promise ? ( Promise ) obj : new Promise( ( res, rej ) -> res.accept( obj ) );
    }

    public static Promise reject( Throwable e )
    {
        return new Promise( ( res, rej ) -> rej.accept( e ) );
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
                resolve.accept( "Resolved!" );
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
        Promise.setTimeout( 3000 ).then( data -> {
            System.out.println( "1" );
            return Promise.setTimeout( 2000 );
        } ).then( data -> {
            System.out.println( "2" );
            throw new NullPointerException( "Error" );
        } ).katch( e -> e ).done( data -> System.out.println( data ) );
        System.out.println( "Hei" );
    }
}
