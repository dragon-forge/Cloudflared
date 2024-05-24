package org.zeith.cloudflared.core.api.channels.base;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseListenerManager<T extends BaseListenerManager<T>>
{
	protected final List<Runnable> onClosed = new ArrayList<>();
	
	public T onClosed(Runnable task)
	{
		onClosed.add(task);
		return self();
	}
	
	protected abstract T self();
	
	public void fireOnClosed()
	{
		onClosed.forEach(Runnable::run);
	}
}