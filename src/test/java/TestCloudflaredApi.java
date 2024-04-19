import org.zeith.cloudflared.core.CloudflaredAPI;
import org.zeith.cloudflared.core.CloudflaredAPIFactory;
import org.zeith.cloudflared.core.api.*;
import org.zeith.cloudflared.core.exceptions.CloudflaredNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestCloudflaredApi
		implements IGameProxy
{
	public static final ExecutorService EXE = Executors.newCachedThreadPool();
	
	public static void main(String[] args)
			throws CloudflaredNotFoundException
	{
		CloudflaredAPI api = CloudflaredAPIFactory.builder()
				.gameProxy(new TestCloudflaredApi())
				.build()
				.createApi();
		
		System.out.println(api.getVersion());
		
		System.exit(0);
	}
	
	@Override
	public ExecutorService getBackgroundExecutor()
	{
		return EXE;
	}
	
	@Override
	public void addListener(IGameListener listener)
	{
	
	}
	
	@Override
	public void removeListener(IGameListener listener)
	{
	
	}
	
	@Override
	public void sendChatMessage(String message)
	{
		System.out.println("[JOIN MSG] " + message);
	}
	
	@Override
	public void createToast(InfoLevel level, String title, String subtitle)
	{
		System.out.println("[" + level.name() + "] " + title + "\n" + subtitle + "\n");
	}
	
	@Override
	public List<IGameListener> getListeners()
	{
		return Collections.emptyList();
	}
}