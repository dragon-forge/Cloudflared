package org.zeith.cloudflared.core.util;

public interface UploadProgress {

    UploadProgress DEFAULT = (uploaded, total) -> {};

    void onUpload(long paramLong1, long paramLong2);
}
