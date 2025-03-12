package org.zeith.cloudflared.core.api;

import java.util.Objects;
import java.util.UUID;

public abstract class MCGameSession implements IGameSession {

    public final int port;
    public final UUID host;

    public MCGameSession(int port, UUID host) {
        this.port = port;
        this.host = host;
    }

    public boolean equals(Object object) {
        if (object == this) return true;

        if (!(object instanceof MCGameSession other)) return false;

        if (!other.canEqual(this)) return false;
        if (getPort() != other.getPort()) return false;

        Object this$host = getHost(), other$host = other.getHost();
        return Objects.equals(this$host, other$host);
    }

    protected boolean canEqual(Object other) {
        return other instanceof MCGameSession;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = PRIME + getPort();
        Object $host = getHost();
        return result * PRIME + (($host == null) ? 43 : $host.hashCode());
    }

    public String toString() {
        return "MCGameSession1710(serverPort=" + getPort() + ", host=" + getHost() + ")";
    }

    public UUID getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }
}
