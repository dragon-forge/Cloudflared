package org.zeith.cloudflared.core.api;

import org.zeith.cloudflared.core.util.UploadProgress;

public interface IFileDownload extends UploadProgress, AutoCloseable {

    IFileDownload DUMMY = new IFileDownload() {

        public void onStart() {}

        public void onUpload(long uploaded, long total) {}

        public void onEnd() {}
    };

    void onStart();

    void onUpload(long uploaded, long total);

    void onEnd();

    default void close() {
        onEnd();
    }
}
