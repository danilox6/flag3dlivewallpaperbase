package com.devxperiments.flaglivewallpaper;

import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.jbrush.ae.Animator;
import com.jbrush.ae.EditorObject;
import com.jbrush.ae.LightData;
import com.jbrush.ae.Scene;
import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

import net.rbgrn.android.glwallpaperservice.GLWallpaperService;

public class FlagRenderer implements GLWallpaperService.Renderer, OnSharedPreferenceChangeListener{
 
	private FrameBuffer framebuffer;
	private World world;
	private Vector<EditorObject> objects;
	private Vector<LightData> lights; 
	private Context context;
	
	private boolean preference = false;
	
	private Light sun;
	
	static FlagRenderer instance;
	
	public static FlagRenderer getInstance(Context context){
		if (instance==null)
			instance = new FlagRenderer(context);
		return instance;
	}
	
	public static FlagRenderer getInstance(){
		return instance;
	}
	
	public static void setInstance(FlagRenderer renderer){
		instance = renderer;
	}
	
	private FlagRenderer(Context context) {
		this.context = context;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.registerOnSharedPreferenceChangeListener(this);
	}
	
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
		if(world==null || preference){
			preference = false;
			draw(width, height);
		}
		
	}

	public void release() {
		if (framebuffer != null) {
			framebuffer.dispose();
		}
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		preference = true;
	}

	private void draw(int screenWidth, int screenHeight){
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		world = new World();
		
		lights = new Vector<LightData>();
		 
		AssetManager assetManager = context.getAssets();
		objects = Scene.loadSerializedLevel("flag.txt", objects, lights, null,null, world, assetManager);
		
        Object3D flag = Scene.findObject("flag0", objects);
        
        if(screenHeight == 0 && screenWidth == 0){
        	WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        	
        	Display display = wm.getDefaultDisplay();
        	DisplayMetrics metrics = new DisplayMetrics();
        	display.getMetrics(metrics);

        	screenHeight =  metrics.heightPixels;
        	screenWidth =   metrics.widthPixels;
        }
        Animator.Play(flag, "wave", objects);
       
        String texture = prefs.getString(WallpaperChooser.FLAG_IMAGE_SETTING, FlagManager.getDefaultFlag());
        if (screenWidth > screenHeight)
        	texture = FlagManager.toLandscape(texture);
        else
        	texture = FlagManager.toPortrait(texture);
		flag.setTexture(texture);
		
		sun = new Light(world);

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
		
		SimpleVector sv = new SimpleVector();
		sv.set(flag.getTransformedCenter());
		sv.y += 100;
		sv.x -= 100;
		sv.z -= 30;
		sun.setPosition(sv);
//		sun.disable();
		
		MemoryHelper.compact();
	}
	
	public Context getContext() {
		return context;
	}

}
