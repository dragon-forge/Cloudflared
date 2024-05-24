package org.zeith.cloudflared.core.api.channels.enc;

import lombok.Getter;
import org.zeith.cloudflared.core.api.channels.ChannelDescriptor;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class InputChannel
{
	final ChannelDescriptor descriptor;
	
	@Getter
	protected boolean closed;
	
	protected final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	protected final List<Runnable> onDataAppeared = new ArrayList<>(1);
	protected final List<Runnable> onClose = new ArrayList<>(1);
	
	public InputChannel(ChannelDescriptor descriptor)
	{
		this.descriptor = new ChannelDescriptor(descriptor); // copy descriptor to prevent mutability.
	}
	
	public synchronized InputChannel addDataAppearedListener(Runnable hook)
	{
		onDataAppeared.add(hook);
		return this;
	}
	
	public void appendData(byte[] bytes)
	{
		appendData(bytes, 0, bytes.length);
	}
	
	public synchronized void appendData(byte[] buf, int offset, int length)
	{
		boolean hadData = hasNewData();
		buffer.write(buf, offset, length);
		if(!hadData && hasNewData()) onDataAppeared.forEach(Runnable::run);
//		synchronized(this)
//		{
//			notifyAll();
//		}
	}
	
	public synchronized boolean hasNewData()
	{
		return buffer.size() > 0;
	}
	
	public synchronized byte[] getData()
	{
		return buffer.toByteArray();
	}
	
	public synchronized void dropData()
	{
		buffer.reset();
	}
	
	public synchronized void close()
	{
		if(closed) return;
		onDataAppeared.clear();
		onClose.forEach(Runnable::run);
		onClose.clear();
		closed = true;
	}
	
	public static InputChannel fromStream(ChannelDescriptor descriptor, InputStream input)
	{
		return new InputChannel(descriptor)
		{
			@Override
			public synchronized void close()
			{
				super.close();
			}
		};
	}
}