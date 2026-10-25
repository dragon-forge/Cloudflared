package org.zeith.cloudflared.core.util;

import java.util.function.Supplier;

public class MemoizingSupplier<T>
		implements Supplier<T>
{
	protected Supplier<T> src;
	protected T value;
	
	public MemoizingSupplier(Supplier<T> src)
	{
		this.src = src;
	}
	
	@Override
	public T get()
	{
		if(src != null)
		{
			value = src.get();
			src = null;
		}
		return value;
	}
	
	public static <T> MemoizingSupplier<T> of(Supplier<T> supplier)
	{
		if(supplier instanceof MemoizingSupplier) return (MemoizingSupplier<T>) supplier;
		return new MemoizingSupplier<>(supplier);
	}
}