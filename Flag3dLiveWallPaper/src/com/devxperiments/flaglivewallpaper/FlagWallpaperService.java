package com.devxperiments.flaglivewallpaper;

import android.content.Context;
import android.view.SurfaceHolder;
import net.rbgrn.android.glwallpaperservice.*;

public class FlagWallpaperService extends GLWallpaperService {
	Context context;
	
	public FlagWallpaperService() {
		super();
		 context = this;
	}

	public Engine onCreateEngine() {
//		android.os.Debug.waitForDebugger(); 
		MyEngine engine = new MyEngine();
		return engine;
	}
	
	class MyEngine extends GLEngine {
		FlagRenderer renderer;
		
		public MyEngine() {
			super();
			
			FlagManager.inizialize(context);
			
			FlagRenderer.setInstance(null);
			renderer = FlagRenderer.getInstance(context);
			setRenderer(renderer);
			setRenderMode(RENDERMODE_CONTINUOUSLY);
		}
		
		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,	int width, int height) {
			super.onSurfaceChanged(holder, format, width, height);
		}
		
		public void onDestroy() {
			super.onDestroy();
			if (renderer != null) {
				renderer.release();
			}
			renderer = null;
		}
	}
	
}
