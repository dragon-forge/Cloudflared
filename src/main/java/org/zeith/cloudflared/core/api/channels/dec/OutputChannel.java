package org.zeith.cloudflared.core.api.channels.dec;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public interface OutputChannel
{
	void write(byte[] buf)
			throws IOException;
	
	void close()
			throws IOException;
	
	static OutputChannel uncloseable(IoConsumer<byte[]> writer)
	{
		return new OutputChannel()
		{
			@Override
			public void write(byte[] buf)
					throws IOException
			{
				writer.accept(buf);
			}
			
			@Override
			public void close() {}
		};
	}
	
	static OutputChannel forStream(OutputStream out)
	{
		return new OutputChannel()
		{
			@Override
			public void write(byte[] buf)
					throws IOException
			{
				out.write(buf);
			}
			
			@Override
			public void close()
					throws IOException
			{
				out.close();
			}
		};
	}
	
	interface IoConsumer<T>
	{
		void accept(T t)
				throws IOException;
	}
	
	class Closer
			extends ArrayList<OutputChannel>
			implements AutoCloseable
	{
		public Closer(@NotNull Collection<? extends OutputChannel> c)
		{
			super(c);
		}
		
		@Override
		public void close()
				throws IOException
		{
			List<Exception> errors = null;
			for(OutputChannel o : this)
				try
				{
					o.close();
				} catch(Exception e)
				{
					if(errors == null) errors = new ArrayList<>();
					errors.add(e);
				}
			if(errors != null && !errors.isEmpty())
			{
				IOException e = new IOException();
				errors.forEach(e::addSuppressed);
				throw e;
			}
		}
	}
}