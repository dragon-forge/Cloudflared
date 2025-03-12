package org.zeith.cloudflared.core.util;

import java.util.Objects;
import java.util.regex.Pattern;

@SuppressWarnings("ClassCanBeRecord")
public class CloudflaredVersion {

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof CloudflaredVersion other)) return false;
        if (!other.canEqual(this)) return false;
        if (!Objects.equals(this.version, other.version)) return false;
        return Objects.equals(this.buildTime, other.buildTime);
    }

    protected boolean canEqual(Object other) {
        return other instanceof CloudflaredVersion;
    }

    public int hashCode() {
        int result = 59 + ((this.version == null) ? 43 : this.version.hashCode());
        return result * 59 + ((this.buildTime == null) ? 43 : this.buildTime.hashCode());
    }

    public static final Pattern CFD_VER_REGEX = Pattern.compile("(?<version>\\d\\S+).+\\s(?<built>\\d[^)]+)");

    public final String version;

    public final String buildTime;

    public CloudflaredVersion(String version, String buildTime) {
        this.version = version;
        this.buildTime = buildTime;
    }

    public String toString() {
        return String.format("cloudflared version %s (built %s)", this.version, this.buildTime);
    }
}
