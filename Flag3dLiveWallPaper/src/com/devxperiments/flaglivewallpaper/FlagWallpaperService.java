package com.devxperiments.flaglivewallpaper;

import android.content.Context;
import android.view.SurfaceHolder;
import net.rbgrn.android.glwallpaperservice.*;

// Original code provided by Robert Green
// http://www.rbgrn.net/content/354-glsurfaceview-adapted-3d-live-wallpapers
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
			// handle prefs, other initialization
			
//			if(!TextureManager.getInstance().containsTexture("resolution"))
//				TextureManager.getInstance().addTexture("resolution", new Texture(new BitmapDrawable(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.resolution)))));
//			if(!TextureManager.getInstance().containsTexture("ruler"))
//				TextureManager.getInstance().addTexture("ruler", new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.ruler)), 512, 512)));
			
			FlagManager.inizialize(context);
			
			FlagRenderer.setInstance(null);
			renderer = FlagRenderer.getInstance(context);
			setRenderer(renderer);
			setRenderMode(RENDERMODE_CONTINUOUSLY);
//			getApplication().get;
		}
		
		
		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,	int width, int height) {
//			renderer.draw(width, height);
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
