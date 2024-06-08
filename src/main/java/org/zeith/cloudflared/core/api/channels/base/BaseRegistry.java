package org.zeith.cloudflared.core.api.channels.base;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseRegistry
{
	protected boolean frozen;
	protected Map<RegistryToken<?>, Object> tokens = new ConcurrentHashMap<>();
	
	public <T> void registerToken(RegistryToken<T> token, T value)
	{
		checkFrozen();
		tokens.putIfAbsent(token, value);
	}
	
	protected void checkFrozen()
	{
		if(frozen) throw new IllegalStateException("Channel registry is frozen and can not be modified.");
	}
	
	protected synchronized void freeze()
	{
		this.frozen = true;
		this.tokens = ImmutableMap.copyOf(tokens);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getToken(RegistryToken<T> token)
	{
		return (T) tokens.get(token);
	}
}