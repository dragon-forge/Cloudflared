package org.zeith.cloudflared.core.api.channels.dec;

import org.zeith.cloudflared.core.api.channels.ChannelDescriptor;

public interface ConfigureListener
{
	void onConfigured(byte channel, ChannelDescriptor descriptor, boolean satisfied);
}