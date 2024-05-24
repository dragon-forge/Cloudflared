import org.zeith.cloudflared.core.api.channels.ChannelDescriptor;
import org.zeith.cloudflared.core.api.channels.ThreadAllocator;
import org.zeith.cloudflared.core.api.channels.dec.DecodingRegistry;
import org.zeith.cloudflared.core.api.channels.dec.OutputChannel;
import org.zeith.cloudflared.core.api.channels.enc.EncodingRegistry;
import org.zeith.cloudflared.core.api.channels.enc.InputChannel;
import org.zeith.cloudflared.core.api.channels.soc.SocketConfigurations;
import org.zeith.cloudflared.core.api.channels.soc.SocketWithChannels;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * An example of server and client screaming at each other on random channels.
 * <p>
 * This is a test case for random nature of channel switching.
 */
public class SocketMultichannelExample
{
	public static void main(String[] args)
	{
		SocketConfigurations sessionConfigs = SocketConfigurations.builder()
				.encoder(soc ->
				{
					EncodingRegistry reg = new EncodingRegistry();
					
					reg.registerChannel((byte) 1, new InputChannel(
							new ChannelDescriptor()
									.with("settings", "sysout x 1")
					));
					
					reg.registerChannel((byte) 2, new InputChannel(
							new ChannelDescriptor()
									.with("settings", "sysout x 1")
					));
					
					return reg;
				})
				.decoder(soc ->
				{
					DecodingRegistry reg = new DecodingRegistry();
					
					reg.channelConfigureListener((channel, descriptor, satisfied) ->
					{
						String t = Thread.currentThread().getName();
						System.out.println("[" + t + "] [DEC] Configure channel #" + channel + " with data " + descriptor + " (satisfied: " + satisfied + ")");
					});
					
					// Channel 1 is always to console
					reg.registerChannel((byte) 1, (channel, descriptor) ->
							OutputChannel.uncloseable(bytes ->
							{
								String t = Thread.currentThread().getName();
								System.out.println("[" + t + "] " + new String(bytes));
							})
					);
					
					reg.registerChannel((byte) 2, (channel, descriptor) ->
							OutputChannel.uncloseable(bytes ->
							{
								String t = Thread.currentThread().getName();
								System.out.println("[" + t + "-1] " + new String(bytes));
								System.out.println("[" + t + "-2] " + new String(bytes));
							})
					);
					
					return reg;
				})
				.build();
		
		try
		{
			ServerSocket host = new ServerSocket(0);
			
			// Client
			ThreadAllocator.startVirtualThread("ClientThread", new Client(host.getLocalPort(), sessionConfigs)).setName("ClientThread");
			Thread.currentThread().setName("ServerThread");
			
			Socket acceptedClient = host.accept();
			
			SocketWithChannels swc = SocketWithChannels.wrap(sessionConfigs, acceptedClient).start();
			
			long start = System.currentTimeMillis();
			while(System.currentTimeMillis() - start < 10000L)
			{
				Thread.sleep(450L);
				if(Math.random() > 0.5)
				{
					swc.write((byte) 1, "Hello on channel 1 from server!".getBytes(StandardCharsets.UTF_8));
					swc.flush();
				} else
				{
					swc.write((byte) 2, "Hello on channel 2 from server!!".getBytes(StandardCharsets.UTF_8));
					swc.flush();
				}
			}
			
			swc.close();
			
			swc.join();
			System.out.println("Server ended.");
		} catch(IOException | InterruptedException e)
		{
			e.printStackTrace();
		} catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static class Client
			implements Runnable
	{
		protected final int port;
		protected final SocketConfigurations sessionConfigs;
		
		public Client(int port, SocketConfigurations sessionConfigs)
		{
			this.port = port;
			this.sessionConfigs = sessionConfigs;
		}
		
		@Override
		public void run()
		{
			try(Socket so = new Socket("127.0.0.1", port))
			{
				SocketWithChannels swc = SocketWithChannels.wrap(sessionConfigs, so).start();
				ThreadAllocator.startVirtualThread(() ->
				{
					try
					{
						Thread.sleep(225L);
						long start = System.currentTimeMillis();
						while(System.currentTimeMillis() - start < 10000L)
						{
							Thread.sleep(450L);
							if(Math.random() > 0.5)
							{
								swc.write((byte) 1, "Hello on channel 1 from client!".getBytes(StandardCharsets.UTF_8));
								swc.flush();
							} else
							{
								swc.write((byte) 2, "Hello on channel 2 from client!!".getBytes(StandardCharsets.UTF_8));
								swc.flush();
							}
						}
					} catch(InterruptedException e)
					{
						throw new RuntimeException(e);
					}
				});
				swc.join();
				System.out.println("Client ended.");
			} catch(IOException | InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}