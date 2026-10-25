package org.zeith.cloudflared.core.api.channels.enc;

import lombok.Getter;
import lombok.SneakyThrows;
import org.zeith.cloudflared.core.api.channels.ThreadAllocator;
import org.zeith.cloudflared.core.api.channels.VarInt;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;

public class ChannelEncoder
		implements AutoCloseable
{
	protected boolean closed;
	protected final EncodingOutputStream output;
	protected final @Getter EncodingRegistry registry;
	protected final Semaphore flushSync = new Semaphore(0);
	protected Thread thread;
	
	public final EncoderListenerManager events = new EncoderListenerManager();
	
	public ChannelEncoder(EncodingOutputStream output, EncodingRegistry registry)
	{
		this.output = output;
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
		DataOutputStream raw = output.output;
		
		byte[] allChannels = registry.getUsedChannels();
		VarInt.writeVarInt(raw, allChannels.length);
		for(byte channel : allChannels)
		{
			InputChannel input = registry.getInput(channel);
			raw.writeByte(channel);
			input.descriptor.write(raw);
		}
		
		do
		{
			boolean allClosed = true;
			for(byte channel : registry.getUsedChannels())
			{
				InputChannel input = registry.getInput(channel);
				if(!input.isClosed()) allClosed = false;
				if(!input.hasNewData()) continue;
				byte[] data = input.readNewData();
				output.setChannel(channel);
				output.write(data);
			}
			flushSync.release();
			if(allClosed) break;
		} while(!closed);
		registry.close();
		output.close();
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
	
	public void flush()
			throws InterruptedException
	{
		flushSync.acquire();
	}
	
	public void setName(String name)
	{
		thread.setName(name);
	}
}