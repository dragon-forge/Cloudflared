package org.zeith.cloudflared.core.api.channels.enc;

import lombok.Getter;
import lombok.SneakyThrows;
import org.zeith.cloudflared.core.api.channels.ChannelDescriptor;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public abstract class InputChannel
{
	final ChannelDescriptor descriptor;
	
	@Getter
	protected boolean closed;
	
	protected final List<Runnable> onClose = new ArrayList<>(1);
	
	public InputChannel(ChannelDescriptor descriptor)
	{
		this.descriptor = new ChannelDescriptor(descriptor); // copy descriptor to prevent mutability.
	}
	
	public abstract boolean hasNewData();
	
	public abstract byte[] readNewData();
	
	public synchronized void close()
	{
		if(closed) return;
		onClose.forEach(Runnable::run);
		onClose.clear();
		closed = true;
	}
	
	public static InputChannel fromStream(ChannelDescriptor descriptor, InputStream input)
	{
		return new InputChannel(descriptor)
		{
			@SneakyThrows
			@Override
			public boolean hasNewData()
			{
				return input.available() > 0;
			}
			
			@SneakyThrows
			@Override
			public byte[] readNewData()
			{
				return readNBytes(input, input.available());
			}
			
			@SneakyThrows
			@Override
			public synchronized void close()
			{
				super.close();
				input.close();
			}
		};
	}
	
	public static byte[] readNBytes(InputStream in, int len)
			throws IOException
	{
		if(len < 0) throw new IllegalArgumentException("len < 0");
		List<byte[]> bufs = null;
		byte[] result = null;
		int total = 0;
		int remaining = len;
		int n;
		do
		{
			byte[] buf = new byte[Math.min(remaining, 8192)];
			int nread = 0;
			
			// read to EOF which may read more or less than buffer size
			while((n = in.read(buf, nread, Math.min(buf.length - nread, remaining))) > 0)
			{
				nread += n;
				remaining -= n;
			}
			
			if(nread > 0)
			{
				if(8192 - total < nread)
					throw new OutOfMemoryError("Required array size too large");
				if(nread < buf.length)
				{
					buf = Arrays.copyOfRange(buf, 0, nread);
				}
				total += nread;
				if(result == null)
				{
					result = buf;
				} else
				{
					if(bufs == null)
					{
						bufs = new ArrayList<>();
						bufs.add(result);
					}
					bufs.add(buf);
				}
			}
			// if the last call to read returned -1 or the number of bytes
			// requested have been read then break
		} while(n >= 0 && remaining > 0);
		
		if(bufs == null)
		{
			if(result == null) return new byte[0];
			return result.length == total ?
				   result : Arrays.copyOf(result, total);
		}
		
		result = new byte[total];
		int offset = 0;
		remaining = total;
		for(byte[] b : bufs)
		{
			int count = Math.min(b.length, remaining);
			System.arraycopy(b, 0, result, offset, count);
			offset += count;
			remaining -= count;
		}
		
		return result;
	}
}