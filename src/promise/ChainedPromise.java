package promise;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ChainedPromise extends Promise
{
    private ChainedPromise next;

    private Function<Object, Object> resolver;

    private Function<Object, Object> rejecter;

    protected ChainedPromise( int target, BiConsumer<Consumer<Object>, Consumer<Object>> execution )
    {
        super( target, execution );
    }

    public ChainedPromise( BiConsumer<Consumer<Object>, Consumer<Object>> execution )
    {
        super( execution );
    }

    @Override
    public synchronized Promise then( Function<Object, Object> res, Function<Object, Object> rej )
    {
        if( state == State.PENDING )
        {
            resolver = res;
            rejecter = rej;
            if( next == null )
            {
                next = new ChainedPromise( ( reslove, reject ) -> {
                } );
            }
            return next;
        }
        else
        {
            ChainedPromise p = this;
            if( state == State.RESOLVED && res != null )
            {
                p = apply( res, data );
            }
            else if( state == State.REJECTED && rej != null )
            {
                p = apply( rej, error );
            }
            if( next != null )
            {
                ChainedPromise n = next;
                p.next = n.next;
                if( n.resolver != null || n.rejecter != null )
                {
                    p.then( n.resolver, n.rejecter );
                }
                else if( n.closer != null || n.tomb != null )
                {
                    p.done( n.closer, n.tomb );
                }
            }
            return p;
        }
    }

    private ChainedPromise apply( Function<Object, Object> func, Object p )
    {
        try
        {
            return resolve( func.apply( p ) );
        }
        catch( Throwable e )
        {
            return reject( e );
        }
    }

    @Override
    protected void execute()
    {
        if( resolver != null || rejecter != null )
        {
            then( resolver, rejecter );
        }
        else if( closer != null || tomb != null )
        {
            done( closer, tomb );
        }
    }
}
