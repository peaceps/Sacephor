package concurrency;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockExample
{
	int i =0;
	Lock lock = new ReentrantLock(true);

	public static void main(String[] args)
	{
		final LockExample example = new LockExample();
		Thread t1 = new Thread(example::lock);
		Thread t2 = new Thread(example::lock);
		t1.start();
		t2.start();
	}

	public void lock()
	{
		lock.lock();
		try
		{
			System.out.println(i++);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			lock.unlock();
		}
	}
}
