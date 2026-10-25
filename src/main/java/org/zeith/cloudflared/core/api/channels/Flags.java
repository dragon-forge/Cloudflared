package org.zeith.cloudflared.core.api.channels;

public enum Flags
{
	CHANNEL_SWITCH,
	CHANNEL_CLOSE,
	UNUSED_2,
	UNUSED_3,
	UNUSED_4,
	UNUSED_5,
	UNUSED_6,
	UNUSED_7;
	
	public boolean has(byte flags)
	{
		return ((flags >> ordinal()) & 1) > 0;
	}
	
	public static byte of(Flags... flags)
	{
		byte b = 0;
		for(Flags flag : flags) b |= (byte) (1 << flag.ordinal());
		return b;
	}
	
	public static byte add(byte b, Flags flag)
	{
		return (byte) (b | (1 << flag.ordinal()));
	}
	
	public static byte addAll(byte b, Flags... flags)
	{
		for(Flags flag : flags) b |= (byte) (1 << flag.ordinal());
		return b;
	}
}