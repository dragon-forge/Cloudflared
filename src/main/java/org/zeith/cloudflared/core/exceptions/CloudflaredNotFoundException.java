package org.zeith.cloudflared.core.exceptions;

public class CloudflaredNotFoundException extends Exception {

    public CloudflaredNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
