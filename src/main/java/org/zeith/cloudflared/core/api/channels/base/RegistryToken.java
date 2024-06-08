package org.zeith.cloudflared.core.api.channels.base;

import lombok.Data;

@Data
public class RegistryToken<T>
{
	protected final Class<T> type;
	protected final String id;
}