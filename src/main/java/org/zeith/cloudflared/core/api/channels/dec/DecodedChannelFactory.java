package org.zeith.cloudflared.core.api.channels.dec;

import org.zeith.cloudflared.core.api.channels.ChannelDescriptor;

import java.io.IOException;

public interface DecodedChannelFactory
{
	OutputChannel createChannel(byte channel, ChannelDescriptor descriptor)
			throws IOException;
}