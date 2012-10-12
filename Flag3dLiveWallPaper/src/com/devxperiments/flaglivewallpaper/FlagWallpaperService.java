package com.devxperiments.flaglivewallpaper;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import net.rbgrn.android.glwallpaperservice.*;

public class FlagWallpaperService extends GLWallpaperService {
	public static Context context;
	public static boolean PRO = true; 
	private String defPackcage;
	
//	public FlagWallpaperService() {
//		super();
//		 context = this;
//		 defPackcage = "com.devxperiments.flaglivewallpaper";
//	}
	
	public FlagWallpaperService(String defPackage){
		super();
		context = this;
		this.defPackcage = defPackage;
	}

	public Engine onCreateEngine() {
//		android.os.Debug.waitForDebugger(); 
		return new MyEngine();
	}
	
	
	class MyEngine extends GLEngine {
		FlagRenderer renderer;
		
		public MyEngine() {
			super();
			
			FlagManager.inizialize(defPackcage);
			
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
			Log.i("OnDestroy", "destroyed");
			if (renderer != null) {
				renderer.release();
			}
			DayTimeAlarmManager.stop();
			renderer = null;
		}
		
		
	}
	
}
