import org.zeith.cloudflared.core.api.channels.ChannelDescriptor;
import org.zeith.cloudflared.core.api.channels.ThreadAllocator;
import org.zeith.cloudflared.core.api.channels.base.RegistryToken;
import org.zeith.cloudflared.core.api.channels.dec.*;
import org.zeith.cloudflared.core.api.channels.enc.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class TestMultichannel
{
	public static final RegistryToken<PipedOutputStream> OUTPUT_15 = new RegistryToken<>(PipedOutputStream.class, "Output15");
	public static final RegistryToken<PipedOutputStream> OUTPUT_100 = new RegistryToken<>(PipedOutputStream.class, "Output100");
	public static final RegistryToken<PipedOutputStream> OUTPUT_3 = new RegistryToken<>(PipedOutputStream.class, "Output3");
	
	public static void main(String[] args)
	{
		enc();
		dec();
	}
	
	public static void enc()
	{
		try(FileOutputStream out = new FileOutputStream("enc.bin"))
		{
			EncodingRegistry reg = new EncodingRegistry();
			
			PipedInputStream pip15 = new PipedInputStream();
			PipedInputStream pip100 = new PipedInputStream();
			PipedInputStream pip3 = new PipedInputStream();
			
			try
			{
				reg.registerToken(OUTPUT_15, new PipedOutputStream(pip15));
				reg.registerToken(OUTPUT_100, new PipedOutputStream(pip100));
				reg.registerToken(OUTPUT_3, new PipedOutputStream(pip3));
			} catch(IOException e)
			{
				e.printStackTrace();
			}
			
			{
				ChannelDescriptor desc1 = new ChannelDescriptor();
				desc1.put("session", "erfphioerjhporejh");
				InputChannel ic1 = InputChannel.fromStream(desc1, pip15);
				reg.getToken(OUTPUT_15).write("[TEST PACKET]".getBytes(StandardCharsets.UTF_8));
				reg.registerChannel((byte) 15, ic1);
			}
			
			// This channel is unknown to the decoder, and thus will get dropped
			{
				ChannelDescriptor desc1 = new ChannelDescriptor();
				desc1.put("what is this", "huh?!");
				InputChannel ic1 = InputChannel.fromStream(desc1, pip100);
				reg.getToken(OUTPUT_100).write("[TEST 3 PACKET]".getBytes(StandardCharsets.UTF_8));
				reg.registerChannel((byte) 100, ic1);
			}
			
			{
				ChannelDescriptor desc1 = new ChannelDescriptor();
				desc1.put("sess", "erfphioerjhporejh");
				InputChannel ic1 = InputChannel.fromStream(desc1, pip3);
				reg.getToken(OUTPUT_3).write("[TEST 2 PACKET]".getBytes(StandardCharsets.UTF_8));
				reg.registerChannel((byte) 104, ic1);
			}
			
			ChannelEncoder t = reg.createThread(out);
			t.start();
			t.close();
			t.join();
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		
		System.out.println("Has VT: " + ThreadAllocator.HAS_VIRTUAL_THREADS);
	}
	
	public static void dec()
	{
		try(FileInputStream in = new FileInputStream("enc.bin"))
		{
			DecodingRegistry reg = new DecodingRegistry();
			
			reg.channelConfigureListener((channel, descriptor, satisfied) ->
			{
				System.out.println("Configure channel " + channel + ": " + descriptor + " OK:" + satisfied);
			});
			
			reg.registerChannel((byte) 15, (channel, descriptor) -> OutputChannel.forStream(new FileOutputStream("ch" + channel + ".txt")));
			reg.registerChannel((byte) 104, (channel, descriptor) -> OutputChannel.forStream(new FileOutputStream("ch" + channel + ".txt")));
			
			ChannelDecoder dec = reg.createThread(in);
			dec.start();
			dec.join();
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}