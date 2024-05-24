package org.zeith.cloudflared.core.api.channels.dec;

import java.io.InputStream;
import java.util.*;

public class DecodingRegistry
{
	protected boolean frozen;
	protected final Map<Byte, DecodedChannelFactory> channels = new HashMap<>();
	protected final List<ConfigureListener> channelConfigureListeners = new ArrayList<>();
	
	synchronized void freeze()
	{
		this.frozen = true;
	}
	
	public synchronized void registerChannel(byte channel, DecodedChannelFactory input)
	{
		if(frozen) throw new IllegalStateException("Channel registry is frozen and can not accept registration on channel " + channel + ".");
		if(channels.containsKey(channel)) throw new IllegalArgumentException("Channel " + channel + " is already in use.");
		channels.put(channel, input);
	}
	
	public DecodingRegistry channelConfigureListener(ConfigureListener listener)
	{
		if(frozen) throw new IllegalStateException("Channel registry is frozen and can not accept new configuration listeners.");
		channelConfigureListeners.add(listener);
		return this;
	}
	
	public ChannelDecoder createThread(InputStream out)
	{
		if(frozen) throw new IllegalStateException("Channel registry is frozen and can not create any more threads.");
		freeze();
		return new ChannelDecoder(out, this);
	}
}