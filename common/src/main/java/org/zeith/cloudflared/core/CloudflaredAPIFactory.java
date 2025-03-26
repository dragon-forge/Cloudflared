package org.zeith.cloudflared.core;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.zeith.cloudflared.core.api.IGameProxy;
import org.zeith.cloudflared.core.exceptions.CloudflaredNotFoundException;

import java.util.function.Supplier;

@Getter
@ToString
@Builder
public class CloudflaredAPIFactory {
    protected final IGameProxy gameProxy;
    @Builder.Default
    protected Supplier<String> hostname = () -> null;

    public CloudflaredAPI createApi()
            throws CloudflaredNotFoundException {
        return CloudflaredAPI.create(this);
    }
}