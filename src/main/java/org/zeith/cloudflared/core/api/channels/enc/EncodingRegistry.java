package org.zeith.cloudflared.core.api.channels.enc;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class EncodingRegistry
{
	final Semaphore wakeupLock = new Semaphore(0);
	
	private boolean frozen = false;
	
	private final ByteArrayOutputStream usedChannels = new ByteArrayOutputStream();
	private byte[] channelsCache;
	
	private final Map<Byte, InputChannel> channels = new HashMap<>();
	protected final Runnable wakeup = () ->
	{
		synchronized(wakeupLock)
		{
			wakeupLock.release();
		}
	};
	
	synchronized void freeze()
	{
		this.frozen = true;
	}
	
	public synchronized void registerChannel(byte channel, InputChannel input)
	{
		if(frozen) throw new IllegalStateException("Channel registry is frozen and can not accept registration on channel " + channel + ".");
		if(channels.containsKey(channel)) throw new IllegalArgumentException("Channel " + channel + " is already in use.");
		channels.put(channel, input.addDataAppearedListener(wakeup));
		usedChannels.write(channel);
	}
	
	public byte[] getUsedChannels()
	{
		if(channelsCache == null) channelsCache = usedChannels.toByteArray();
		return channelsCache;
	}
	
	public InputChannel getInput(byte channel)
	{
		return channels.get(channel);
	}
	
	public void close()
	{
		for(InputChannel value : channels.values())
			value.close();
		channels.clear();
		usedChannels.reset();
		wakeup.run();
	}
	
	public ChannelEncoder createThread(OutputStream out)
	{
		if(frozen) throw new IllegalStateException("Channel registry is frozen and can not create any more threads.");
		freeze();
		return new ChannelEncoder(new EncodingOutputStream(out), this);
	}
}