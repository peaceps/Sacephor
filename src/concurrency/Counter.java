package concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Counter
{
	private AtomicInteger atomicI = new AtomicInteger(0);
	private int i = 0;
	
	public static void main(String[] args)
	{
		checkTime(Counter::count);
		checkTime(Counter::safeCount);
	}

	public static void checkTime(Consumer<Counter> consumer)
	{
		final Counter cas = new Counter();
		List<Thread> ts = new ArrayList<>(600);
		long start = System.currentTimeMillis();
		for (int j = 0; j < 100; j++)
		{
			Thread t = new Thread(new Runnable()
			{
				public void run()
				{
					for (int i = 0; i < 10000; i++)
					{
						consumer.accept(cas);
					}
				}
			});
			ts.add(t);
		}
		for (Thread t : ts)
		{
			t.start();
		}
		for (Thread t : ts)
		{
			try
			{
				t.join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		System.out.println(cas.i);
		System.out.println(cas.atomicI.get());
		System.out.println(System.currentTimeMillis() - start);
	}

	private void safeCount()
	{
		for (;;)
		{
			int i = atomicI.get();
			boolean suc = atomicI.compareAndSet(i, ++i);
			if (suc)
			{
				break;
			}
		}
	}

	private void count()
	{
		i++;
	}
}
