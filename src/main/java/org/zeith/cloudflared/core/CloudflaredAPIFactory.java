package org.zeith.cloudflared.core;

import com.zeitheron.hammercore.utils.base.Cast;
import lombok.*;
import org.zeith.cloudflared.core.api.IGameProxy;
import org.zeith.cloudflared.core.exceptions.CloudflaredNotFoundException;

import java.util.function.Supplier;

@Getter
@ToString
@Builder
public class CloudflaredAPIFactory
{
	@Builder.Default
	protected Supplier<String> executable = Cast.constant("cloudflared");
	
	@Builder.Default
	protected Supplier<String> hostname = Cast.constant(null);
	
	protected boolean autoDownload;
	
	protected final IGameProxy gameProxy;
	
	public CloudflaredAPI createApi()
			throws CloudflaredNotFoundException
	{
		return CloudflaredAPI.create(this);
	}
}