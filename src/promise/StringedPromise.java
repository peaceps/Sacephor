package promise;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class StringedPromise extends Promise
{
    private List<FunctionWapper> handlers;

    protected StringedPromise( int target, BiConsumer<Consumer<Object>, Consumer<Object>> execution )
    {
        super( target, execution );
        if( handlers == null )
        {
            handlers = new LinkedList<>();
        }
    }

    public StringedPromise( BiConsumer<Consumer<Object>, Consumer<Object>> execution )
    {
        this( 1, execution );
    }

    @Override
    public synchronized Promise then( Function<Object, Object> resolver, Function<Object, Object> rejecter )
    {
        if( state == State.RESOLVED )
        {
            return apply( resolver, data );
        }
        else if( state == State.REJECTED )
        {
            return apply( rejecter, error );
        }
        else if( state == State.PENDING )
        {
            if( resolver != null )
            {
                handlers.add( new FunctionWapper( resolver, false ) );
            }
            if( rejecter != null )
            {
                handlers.add( new FunctionWapper( rejecter, true ) );
            }
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

    @Override
    protected void execute()
    {
        boolean find = false;
        if( handlers == null )
        {
            handlers = new LinkedList<>();
        }
        while( !find && !handlers.isEmpty() )
        {
            FunctionWapper f = handlers.remove( 0 );
            if( state == State.RESOLVED && !f.reject )
            {
                then( f.function );
                find = true;
                break;
            }
            else if( state == State.REJECTED && f.reject )
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

}
