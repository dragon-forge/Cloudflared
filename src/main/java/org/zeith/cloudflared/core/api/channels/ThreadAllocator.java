package org.zeith.cloudflared.core.api.channels;

import java.lang.reflect.Method;
import java.util.function.Function;

public class ThreadAllocator
{
	public static final boolean HAS_VIRTUAL_THREADS;
	private static final Function<Runnable, Thread> VIRTUAL_THREAD;
	
	static
	{
		boolean hasVT = false;
		Function<Runnable, Thread> virtualThread = task ->
		{
			Thread t = new Thread(task);
			t.start();
			return t;
		};
		
		try
		{
			Method method = Thread.class.getDeclaredMethod("startVirtualThread", Runnable.class);
			hasVT = true;
			virtualThread = task ->
			{
				try
				{
					return (Thread) method.invoke(task);
				} catch(Exception e)
				{
				}
				
				Thread t = new Thread(task);
				t.start();
				return t;
			};
		} catch(Throwable e)
		{
		}
		
		HAS_VIRTUAL_THREADS = hasVT;
		VIRTUAL_THREAD = virtualThread;
	}
	
	public static Thread startVirtualThread(Runnable task)
	{
		return VIRTUAL_THREAD.apply(task);
	}
	
	public static Thread startVirtualThread(String name, Runnable task)
	{
		return VIRTUAL_THREAD.apply(() ->
		{
			Thread.currentThread().setName(name);
			task.run();
		});
	}
}