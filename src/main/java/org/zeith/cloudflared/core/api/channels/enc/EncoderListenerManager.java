package org.zeith.cloudflared.core.api.channels.enc;

import org.zeith.cloudflared.core.api.channels.base.BaseListenerManager;

public class EncoderListenerManager
		extends BaseListenerManager<EncoderListenerManager>
{
	@Override
	protected EncoderListenerManager self()
	{
		return this;
	}
}