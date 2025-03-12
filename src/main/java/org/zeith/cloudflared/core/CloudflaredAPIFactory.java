package org.zeith.cloudflared.core;

import java.util.function.Supplier;

import org.zeith.cloudflared.core.api.IGameProxy;
import org.zeith.cloudflared.core.exceptions.CloudflaredNotFoundException;

public class CloudflaredAPIFactory {

    protected Supplier<String> hostname;
    protected final IGameProxy gameProxy;

    public String toString() {
        return "CloudflaredAPIFactory(hostname=" + getHostname() + ", gameProxy=" + getGameProxy() + ")";
    }

    private static Supplier<String> $default$hostname() {
        return () -> null;
    }

    CloudflaredAPIFactory(Supplier<String> hostname, IGameProxy gameProxy) {
        this.hostname = hostname;
        this.gameProxy = gameProxy;
    }

    public static CloudflaredAPIFactoryBuilder builder() {
        return new CloudflaredAPIFactoryBuilder();
    }

    public static class CloudflaredAPIFactoryBuilder {

        private boolean hostname$set;

        public CloudflaredAPIFactoryBuilder hostname(Supplier<String> hostname) {
            this.hostname$value = hostname;
            this.hostname$set = true;
            return this;
        }

        private Supplier<String> hostname$value;
        private IGameProxy gameProxy;

        public CloudflaredAPIFactoryBuilder gameProxy(IGameProxy gameProxy) {
            this.gameProxy = gameProxy;
            return this;
        }

        public CloudflaredAPIFactory build() {
            Supplier<String> hostname$value = this.hostname$value;
            if (!this.hostname$set) hostname$value = CloudflaredAPIFactory.$default$hostname();
            return new CloudflaredAPIFactory(hostname$value, this.gameProxy);
        }

        public String toString() {
            return "CloudflaredAPIFactory.CloudflaredAPIFactoryBuilder(hostname$value=" + this.hostname$value
                + ", gameProxy="
                + this.gameProxy
                + ")";
        }
    }

    public Supplier<String> getHostname() {
        return this.hostname;
    }

    public IGameProxy getGameProxy() {
        return this.gameProxy;
    }

    public CloudflaredAPI createApi() throws CloudflaredNotFoundException {
        return CloudflaredAPI.create(this);
    }
}
