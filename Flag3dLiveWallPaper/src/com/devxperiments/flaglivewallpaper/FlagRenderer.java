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
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

import net.rbgrn.android.glwallpaperservice.GLWallpaperService;

public class FlagRenderer implements GLWallpaperService.Renderer, OnSharedPreferenceChangeListener{
 
	private FrameBuffer fb;
	private World world;
//	private RGBColor backgroundColor = RGBColor.BLACK;
	private Vector<EditorObject> objects;
	private Vector<LightData> lights; 
//	private Context master;
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
		
//		int color = prefs.getInt(Settings.BACKGROUND_COLOR, 0xff000000);
//		backgroundColor = new RGBColor(Color.red(color), Color.green(color), Color.blue(color));
//		 if (master != null) {
//				copy(master);
//		}
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {}

	@Override
	public void onDrawFrame(GL10 gl) {
		fb.clear();
		world.renderScene(fb);
		Animator.EnableAnimations();
		world.draw(fb);
		fb.display();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if (fb != null) {
			fb.dispose();
		}
		
		fb = new FrameBuffer(gl, width, height);
		if(world==null || preference){
			preference = false;
			draw(width, height);
		}
		
		
		
//		String orientationPref = PreferenceManager.getDefaultSharedPreferences(context).getString(Settings.ORIENTATION, "auto");
//		if(!orientationPref.equals("auto") &&( 
//				!(orientationPref.equals("landscape") && width > height) ||
//				!(orientationPref.equals("portrait") && height > width))){
//				adjustCamera(orientationPref.equals("landscape"));
//		}
			
//		if (master == null) {

				
//			if (master == null) {
//				Logger.log("Saving master Activity!");
//				master = context;
//			}
//		}

	}

	public void release() {
		if (fb != null) {
			fb.dispose();
		}
	}
	
//	private void copy(Object src) {
//		try {
//			Logger.log("Copying data from master Activity!");
//			Field[] fs = src.getClass().getDeclaredFields();
//			for (Field f : fs) {
//				f.setAccessible(true);
//				f.set(this, f.get(src));
//			}
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		preference = true;
	}

	private void draw(int screenWidth, int screenHeight){
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		world = new World();
		
		lights = new Vector<LightData>();
		
//		int color = prefs.getInt(Settings.BACKGROUND_COLOR, 0xff000000);
//		backgroundColor = new RGBColor(Color.red(color), Color.green(color), Color.blue(color));
        
//		world.setAmbientLight(Color.red(color), Color.green(color), Color.blue(color));
		
		AssetManager assetManager = context.getAssets();
		objects = Scene.loadSerializedLevel("flagh.txt", objects, lights, null,null, world, assetManager);
//		objects = Scene.loadSerializedLevel("flagh.txt", objects, world, assetManager);
        
		
        Object3D flag = Scene.findObject("flagh0", objects);
        
        
        if(screenHeight == 0 && screenWidth == 0){
        	WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        	

        	Display display = wm.getDefaultDisplay();
        	DisplayMetrics metrics = new DisplayMetrics();
        	display.getMetrics(metrics);

        	screenHeight =  metrics.heightPixels;
        	screenWidth =   metrics.widthPixels;
        }
        Animator.Play(flag, "wave", objects);
       
        String texture = prefs.getString(Settings.FLAG_IMAGE, FlagManager.getDefaultFlag());
        if (screenWidth > screenHeight)
        	texture = FlagManager.toLandscape(texture);
        else
        	texture = FlagManager.toPortrait(texture);
		flag.setTexture(texture);
		
		sun = new Light(world);

		float[] bb = flag.getMesh().getBoundingBox();

//		float height = Math.abs(bb[2]-bb[3]);
		float width = Math.abs(bb[0]-bb[1]);

		float moveout = 35; 
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
		sv.z -= 5;
		sun.setPosition(sv);
//		sun.disable();
		
		MemoryHelper.compact();
	}
	

	
	public Context getContext() {
		return context;
	}

//	class FrameBuffer extends com.threed.jpct.FrameBuffer{
//		
//		
//		private static final long serialVersionUID = 1L;
//		private GL10 glContext;
//		private int width, heigh;
//		
//		
//		public FrameBuffer(GL10 glContext, int width, int height) {
//			super(glContext, width, height);
//			this.glContext = glContext;
//			this.width = width;
//			this.heigh = height;
//		}
//
//		public GL10 getGlContext() {
//			return glContext;
//		}
//
//		public int getWidth() {
//			return width;
//		}
//
//		public int getHeigh() {
//			return heigh;
//		}
//		
//	}
	
}
