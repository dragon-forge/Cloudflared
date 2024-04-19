package org.zeith.cloudflared.core.api;

import org.zeith.cloudflared.core.util.UploadProgress;

public interface IFileDownload
		extends UploadProgress, AutoCloseable
{
	IFileDownload DUMMY = new IFileDownload()
	{
		@Override
		public void onStart()
		{
		}
		
		@Override
		public void onUpload(long uploaded, long total)
		{
		}
		
		@Override
		public void onEnd()
		{
		}
	};
	
	void onStart();
	
	@Override
	void onUpload(long uploaded, long total);
	
	void onEnd();
	
	@Override
	default void close()
	{
		onEnd();
	}
}