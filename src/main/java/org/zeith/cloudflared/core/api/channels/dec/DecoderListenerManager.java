package org.zeith.cloudflared.core.api.channels.dec;

import org.zeith.cloudflared.core.api.channels.base.BaseListenerManager;

public class DecoderListenerManager
		extends BaseListenerManager<DecoderListenerManager>
{
	@Override
	protected DecoderListenerManager self()
	{
		return this;
	}
}