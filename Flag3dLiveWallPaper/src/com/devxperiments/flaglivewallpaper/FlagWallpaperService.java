package com.devxperiments.flaglivewallpaper;

import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.util.BitmapHelper;

import android.app.WallpaperManager;
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
			
			if(!TextureManager.getInstance().containsTexture("flag"))
				TextureManager.getInstance().addTexture("flag", new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.flag)), 512, 512)));
			if(!TextureManager.getInstance().containsTexture("whiteflag"))
				TextureManager.getInstance().addTexture("whiteflag", new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.whiteflag)), 512, 512)));
			if(!TextureManager.getInstance().containsTexture("riga"))
				TextureManager.getInstance().addTexture("riga", new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.riga)), 512, 512)));
			if(!TextureManager.getInstance().containsTexture("flagt"))
				TextureManager.getInstance().addTexture("flagt", new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.flagt)), 512, 1024)));
			
			
			FlagRenderer.setInstance(null);
			renderer = FlagRenderer.getInstance(context);
			setRenderer(renderer);
			setRenderMode(RENDERMODE_CONTINUOUSLY);
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format, 	int width, int height) {
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
