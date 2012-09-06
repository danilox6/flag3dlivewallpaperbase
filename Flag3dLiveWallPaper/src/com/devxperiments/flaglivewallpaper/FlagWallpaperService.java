package com.devxperiments.flaglivewallpaper;

import android.content.Context;
import android.view.SurfaceHolder;
import net.rbgrn.android.glwallpaperservice.*;

public class FlagWallpaperService extends GLWallpaperService {
	public static Context context;
	
	
	public FlagWallpaperService() {
		super();
		 context = this;
	}

	public Engine onCreateEngine() {
//		android.os.Debug.waitForDebugger(); 
		return new MyEngine();
	}
	
	
	class MyEngine extends GLEngine {
		FlagRenderer renderer;
		
		public MyEngine() {
			super();
			
			FlagManager.inizialize();
			
			renderer = new FlagRenderer(context);
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
			DayTimeAlarmManager.stop();
			renderer = null;
		}
		
		
	}
	
}
