package org.zeith.cloudflared.core.util;

import java.math.BigInteger;
import java.security.*;

class Hashers
{
	public static final Hashers SHA1 = new Hashers("SHA1", 40);
	
	final String algorithm;
	final int hexLength;
	
	public Hashers(String algorithm, int hexLength)
	{
		this.algorithm = algorithm;
		this.hexLength = hexLength;
	}
	
	protected MessageDigest newDigest()
	{
		try
		{
			return MessageDigest.getInstance(algorithm);
		} catch(NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public byte[] hashifyRaw(byte[] data)
	{
		MessageDigest messageDigest = newDigest();
		messageDigest.reset();
		messageDigest.update(data);
		return messageDigest.digest();
	}
	
	public String hashifyHex(byte[] data)
	{
		byte[] digest = hashifyRaw(data);
		BigInteger bigInt = new BigInteger(1, digest);
		StringBuilder hex = new StringBuilder(bigInt.toString(16));
		while(hex.length() < hexLength) hex.insert(0, "0");
		return hex.toString();
	}
	
	public String hashifyHex(String line)
	{
		return hashifyHex(line.getBytes());
	}
}