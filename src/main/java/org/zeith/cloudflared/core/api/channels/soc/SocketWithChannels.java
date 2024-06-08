package org.zeith.cloudflared.core.api.channels.soc;

import org.zeith.cloudflared.core.api.channels.base.RegistryToken;
import org.zeith.cloudflared.core.api.channels.dec.ChannelDecoder;
import org.zeith.cloudflared.core.api.channels.enc.ChannelEncoder;

import java.io.IOException;
import java.net.Socket;

public class SocketWithChannels
		implements AutoCloseable
{
	protected final Socket socket;
	protected final SocketConfigurations configurations;
	
	protected ChannelEncoder encoder;
	protected ChannelDecoder decoder;
	
	public SocketWithChannels(Socket socket, SocketConfigurations configurations)
	{
		this.socket = socket;
		this.configurations = configurations;
	}
	
	public <T> T getEncoderToken(RegistryToken<T> token)
	{
		return encoder.getRegistry().getToken(token);
	}
	
	public <T> T getDecoderToken(RegistryToken<T> token)
	{
		return encoder.getRegistry().getToken(token);
	}
	
	public static SocketWithChannels wrap(SocketConfigurations configurations, Socket socket)
	{
		return new SocketWithChannels(socket, configurations);
	}
	
	public SocketWithChannels start()
			throws IOException
	{
		String name = Thread.currentThread().getName();
		encoder = configurations.encoder.apply(this).createThread(socket.getOutputStream());
		decoder = configurations.decoder.apply(this).createThread(socket.getInputStream());
		decoder.events.onClosed(() ->
		{
			try
			{
				encoder.close();
			} catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		});
		encoder.start();
		decoder.start();
		encoder.setName(name + "-Encoder");
		decoder.setName(name + "-Decoder");
		return this;
	}
	
	@Override
	public void close()
			throws IOException
	{
		encoder.close();
		decoder.close();
		try
		{
			join();
		} catch(InterruptedException e)
		{
			throw new IOException(e);
		}
		socket.close();
	}
	
	public void join()
			throws InterruptedException
	{
		encoder.join();
		decoder.join();
	}
	
	public void flush()
			throws InterruptedException
	{
		encoder.flush();
	}
}