package org.zeith.cloudflared.core.exceptions;

public class CloudflaredNotFoundException
		extends Exception
{
	public CloudflaredNotFoundException()
	{
	}
	
	public CloudflaredNotFoundException(String message)
	{
		super(message);
	}
	
	public CloudflaredNotFoundException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public CloudflaredNotFoundException(Throwable cause)
	{
		super(cause);
	}
	
	public CloudflaredNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}