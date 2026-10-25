package org.zeith.cloudflared.core.api.channels.dec;

import lombok.SneakyThrows;
import org.zeith.cloudflared.core.api.channels.*;

import java.io.*;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

public class ChannelDecoder
		implements AutoCloseable
{
	protected final DataInputStream input;
	protected final DecodingRegistry registry;
	protected boolean closed;
	protected Thread thread;
	
	public final DecoderListenerManager events = new DecoderListenerManager();
	
	public ChannelDecoder(InputStream input, DecodingRegistry registry)
	{
		this.input = new DataInputStream(input);
		this.registry = registry;
	}
	
	public void start()
	{
		if(thread != null) throw new IllegalStateException("Can not start channel encoder more than once.");
		thread = ThreadAllocator.startVirtualThread(this::run);
	}
	
	@SneakyThrows
	protected void run()
	{
		Map<Byte, OutputChannel> channelMap = new HashMap<>();
		
		int channelCount = VarInt.readVarInt(input);
		for(int i = 0; i < channelCount; i++)
		{
			byte ch = input.readByte();
			DecodedChannelFactory dec = registry.channels.get(ch);
			ChannelDescriptor desc = ChannelDescriptor.read(input);
			
			OutputChannel channel = dec != null ? dec.createChannel(ch, desc) : null;
			
			for(ConfigureListener listener : registry.channelConfigureListeners)
				listener.onConfigured(ch, desc, channel != null);
			
			if(channel != null)
				channelMap.put(ch, channel);
		}
		
		byte channel = 0;
		
		try(OutputChannel.Closer ignored = new OutputChannel.Closer(channelMap.values()))
		{
			do
			{
				byte flagsB = input.readByte();
				if(Flags.CHANNEL_SWITCH.has(flagsB)) channel = input.readByte();
				OutputChannel ch = channelMap.get(channel);
				byte[] buf = new byte[VarInt.readVarInt(input)];
				input.readFully(buf);
				if(ch == null) continue;
				ch.write(buf);
			} while(!closed);
		} catch(EOFException e)
		{
			close();
		} catch(SocketException e)
		{
			if(e.getMessage().equalsIgnoreCase("socket closed"))
				close();
		}
		
		input.close();
		events.fireOnClosed();
	}
	
	@Override
	public void close()
			throws IOException
	{
		closed = true;
	}
	
	public void join()
			throws InterruptedException
	{
		if(thread != null)
			thread.join();
	}
	
	public void setName(String name)
	{
		thread.setName(name);
	}
}