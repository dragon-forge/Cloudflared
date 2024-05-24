package org.zeith.cloudflared.core.api.channels;

import java.io.*;

public class VarInt
{
	public static void writeVarInt(DataOutputStream out, int var)
			throws IOException
	{
		while((var & -128) != 0)
		{
			out.writeByte(var & 127 | 128);
			var >>>= 7;
		}
		
		out.writeByte(var);
	}
	
	public static int readVarInt(DataInputStream in)
			throws IOException
	{
		int var = 0;
		int j = 0;
		
		byte buf;
		do
		{
			buf = in.readByte();
			var |= (buf & 127) << j++ * 7;
			if(j > 5) throw new RuntimeException("VarInt too big.");
		} while((buf & 128) == 128);
		
		return var;
	}
}