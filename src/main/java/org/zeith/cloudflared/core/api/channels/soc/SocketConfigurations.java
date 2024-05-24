package org.zeith.cloudflared.core.api.channels.soc;

import lombok.Builder;
import org.zeith.cloudflared.core.api.channels.dec.DecodingRegistry;
import org.zeith.cloudflared.core.api.channels.enc.EncodingRegistry;

import java.util.function.Function;

@Builder(toBuilder = true)
public class SocketConfigurations
{
	protected final Function<SocketWithChannels, EncodingRegistry> encoder;
	protected final Function<SocketWithChannels, DecodingRegistry> decoder;
}