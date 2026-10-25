package org.zeith.cloudflared.core.util;

import lombok.EqualsAndHashCode;

import java.util.regex.Pattern;

@EqualsAndHashCode
public class CloudflaredVersion
{
	public static final Pattern CFD_VER_REGEX = Pattern.compile("(?<version>\\d\\S+).+\\s(?<built>\\d[^)]+)");
	
	public final String version;
	public final String buildTime;
	
	public CloudflaredVersion(String version, String buildTime)
	{
		this.version = version;
		this.buildTime = buildTime;
	}
	
	@Override
	public String toString()
	{
		return String.format("cloudflared version %s (built %s)", version, buildTime);
	}
}