package org.zeith.cloudflared.core.api;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;

public interface IGameProxy
{
	ExecutorService getBackgroundExecutor();
	
	void addListener(IGameListener listener);
	
	void removeListener(IGameListener listener);
	
	void sendChatMessage(String message);
	
	void createToast(InfoLevel level, String title, String subtitle);
	
	List<IGameListener> getListeners();
	
	default IFileDownload pushFileDownload()
	{
		return IFileDownload.DUMMY;
	}
	
	default File getExtraDataDir()
	{
		File f = new File("asm", "Cloudflared");
		if(f.isFile()) f.delete();
		if(!f.isDirectory()) f.mkdirs();
		return f;
	}
}