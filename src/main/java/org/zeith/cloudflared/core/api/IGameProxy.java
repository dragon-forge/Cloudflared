package org.zeith.cloudflared.core.api;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;

public interface IGameProxy {

    ExecutorService getBackgroundExecutor();

    void addListener(IGameListener listener);

    void sendChatMessage(String string);

    List<IGameListener> getListeners();

    default IFileDownload pushFileDownload() {
        return IFileDownload.DUMMY;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    default File getExtraDataDir() {
        File f = new File("asm", "CloudflaredForge");
        if (f.isFile()) f.delete();
        if (!f.isDirectory()) f.mkdirs();
        return f;
    }
}
