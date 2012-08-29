package com.devxperiments.flaglivewallpaper;

import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.res.AssetManager;
import android.preference.PreferenceManager;

import com.devxperiments.flaglivewallpaper.settings.Settings;
import com.jbrush.ae.Animator;
import com.jbrush.ae.EditorObject;
import com.jbrush.ae.LightData;
import com.jbrush.ae.Scene;
import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.BitmapHelper;
import com.threed.jpct.util.MemoryHelper;

import net.rbgrn.android.glwallpaperservice.GLWallpaperService;

public class FlagRenderer implements GLWallpaperService.Renderer{
 
	private FrameBuffer framebuffer = null;
	private World world = null;
	private Object3D flag = null;
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {}

	@Override
	public void onDrawFrame(GL10 gl) {
		framebuffer.clear();
		world.renderScene(framebuffer);
		Animator.EnableAnimations();
		world.draw(framebuffer);
		framebuffer.display();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if (framebuffer != null) {
			framebuffer.dispose();
		}
		
		framebuffer = new FrameBuffer(gl, width, height);
		if(world==null)
			draw(width, height);

		String texture = PreferenceManager.getDefaultSharedPreferences(FlagWallpaperService.context)
				.getString(Settings.FLAG_IMAGE_SETTING, FlagManager.getDefaultFlag());
		if (width > height)
			texture = FlagManager.toLandscape(texture);
		else
			texture = FlagManager.toPortrait(texture);
		
		if(!TextureManager.getInstance().containsTexture(texture)){
			TextureManager.getInstance().flush();
			TextureManager.getInstance().addTexture(texture, new Texture(BitmapHelper.convert(FlagWallpaperService.context.getResources().getDrawable(FlagManager.getFlagId(texture)))));
			TextureManager.getInstance().compress();
		}
		flag.setTexture(texture);
		
		FlagManager.release();
	}

	public void release() {
		if (framebuffer != null) {
			framebuffer.dispose();
		}
	}

	private void draw(int screenWidth, int screenHeight){
		world = new World();
		
		Vector<EditorObject> objects = new Vector<EditorObject>();
		Vector<LightData> lights = new Vector<LightData>();
		 
		AssetManager assetManager = FlagWallpaperService.context.getAssets();
		objects = Scene.loadSerializedLevel("flag.txt", objects, lights, null,null, world, assetManager);
		
        flag = Scene.findObject("flag0", objects);
        Animator.Play(flag, "wave", objects);
       
		float[] bb = flag.getMesh().getBoundingBox();
//		float height = Math.abs(bb[2]-bb[3]);
		float width = Math.abs(bb[0]-bb[1]);

		float moveout = 30; 
		Camera cam = world.getCamera();
		cam.setPositionToCenter(flag);
		cam.moveCamera(Camera.CAMERA_MOVEOUT, moveout);
//		cam.setYFOV(cam.convertRADAngleIntoFOV((float) Math.atan(height/(2*moveout))));
		cam.setFOV(cam.convertRADAngleIntoFOV((float) Math.atan(width/(2*moveout))));
		cam.lookAt(flag.getTransformedCenter());
		
		Light sun = new Light(world);
		SimpleVector sv = new SimpleVector();
		sv.set(flag.getTransformedCenter());
		sv.y += 100;
		sv.x -= 100;
		sv.z -= 30;
		sun.setPosition(sv);
//		sun.disable();
		
		MemoryHelper.compact();
	}
}
