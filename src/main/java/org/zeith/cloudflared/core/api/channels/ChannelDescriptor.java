package org.zeith.cloudflared.core.api.channels;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ChannelDescriptor
		extends HashMap<String, String>
{
	public ChannelDescriptor(int initialCapacity)
	{
		super(initialCapacity);
	}
	
	public ChannelDescriptor()
	{
	}
	
	public ChannelDescriptor(ChannelDescriptor m)
	{
		super(m);
	}
	
	public ChannelDescriptor with(String key, String value)
	{
		put(key, value);
		return this;
	}
	
	public void write(DataOutputStream out)
			throws IOException
	{
		VarInt.writeVarInt(out, size());
		for(Map.Entry<String, String> property : entrySet())
		{
			out.writeUTF(property.getKey());
			out.writeUTF(property.getValue());
		}
	}
	
	public static ChannelDescriptor read(DataInputStream in)
			throws IOException
	{
		int size = VarInt.readVarInt(in);
		ChannelDescriptor desc = new ChannelDescriptor(size);
		for(int i = 0; i < size; i++)
		{
			String key = in.readUTF();
			String val = in.readUTF();
			desc.put(key, val);
		}
		return desc;
	}
}