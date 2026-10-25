package org.zeith.cloudflared.core.api.channels.enc;

import lombok.Getter;
import lombok.Setter;
import org.zeith.cloudflared.core.api.channels.VarInt;

import java.io.*;

import static org.zeith.cloudflared.core.api.channels.Flags.*;

public class EncodingOutputStream
		extends OutputStream
{
	protected final DataOutputStream output;
	
	protected Byte prevChannel;
	
	@Setter
	protected byte channel;
	
	@Setter
	@Getter
	protected byte flags;
	
	public EncodingOutputStream(OutputStream output)
	{
		this.output = new DataOutputStream(output);
	}
	
	@Override
	public void write(int b)
			throws IOException
	{
		write(new byte[] { (byte) b });
	}
	
	@Override
	public void write(byte[] b, int off, int len)
			throws IOException
	{
		boolean differentChannel = prevChannel == null || prevChannel != channel;
		if(differentChannel) flags = add(flags, CHANNEL_SWITCH);
		output.writeByte(flags);
		
		if(differentChannel)
		{
			output.writeByte(channel);
			prevChannel = channel;
		}
		
		VarInt.writeVarInt(output, len);
		output.write(b, off, len);
		output.flush();
		
		flags = 0;
	}
	
	@Override
	public void close()
			throws IOException
	{
		output.close();
	}
	
	@Override
	public void flush()
			throws IOException
	{
		output.flush();
	}
}