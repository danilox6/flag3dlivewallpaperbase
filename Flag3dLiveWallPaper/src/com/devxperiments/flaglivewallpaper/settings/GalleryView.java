package com.devxperiments.flaglivewallpaper.settings;

import com.devxperiments.flaglivewallpaper.FlagManager;
import com.devxperiments.flaglivewallpaper.R;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;

public class GalleryView extends Activity implements OnClickListener{
    Integer[] flags;
    ImageView imageView;
    Button btnOk, btnCancel;
    SharedPreferences prefs;
    
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        flags = FlagManager.getFlagIds();
        
        setContentView(R.layout.gallery);
        
        Gallery gallery = (Gallery) findViewById(R.id.Gallery01);
        gallery.setAdapter(new ImageAdapter(this));
        
        imageView = (ImageView)findViewById(R.id.imgFlag);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String texture = prefs.getString(Settings.FLAG_IMAGE, FlagManager.getDefaultFlag());
        imageView.setImageResource(FlagManager.getFlagId(texture));
        imageView.setTag(FlagManager.getFlagId(texture));
        
        gallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,	long arg3) {
				imageView.setImageResource(flags[position]);
				imageView.setTag(flags[position]);
			}
        	
        });
        
        btnOk = (Button) findViewById(R.id.btnOk);
        btnOk.setOnClickListener(this);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);
        
        
    }
    
    
    public class ImageAdapter extends BaseAdapter {

    	private Context ctx;
    	int imageBackground;
    	
    	public ImageAdapter(Context c) {
			ctx = c;
			TypedArray array = obtainStyledAttributes(R.styleable.Gallery1);
			imageBackground = array.getResourceId(R.styleable.Gallery1_android_galleryItemBackground, 1);
			array.recycle();
		}

		@Override
    	public int getCount() {
    		return flags.length;
    	}

		@Override
		public Object getItem(int position) {
			return position;
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		
		//FIXME dimensioni assolute
    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		ImageView imageView = new ImageView(ctx);
//    		imageView.setImageResource(flags[position]);
//    		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
    		imageView.setImageBitmap(scaleCenterCrop(BitmapFactory.decodeResource(getResources(), flags[position]),120,150));
    		imageView.setLayoutParams(new Gallery.LayoutParams(150,120));
    		imageView.setBackgroundResource(imageBackground);
    		return imageView;
    	}
    	
    	private Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
    	    int sourceWidth = source.getWidth();
    	    int sourceHeight = source.getHeight();

    	    // Compute the scaling factors to fit the new height and width, respectively.
    	    // To cover the final image, the final scaling will be the bigger 
    	    // of these two.
    	    float xScale = (float) newWidth / sourceWidth;
    	    float yScale = (float) newHeight / sourceHeight;
    	    float scale = Math.max(xScale, yScale);

    	    // Now get the size of the source bitmap when scaled
    	    float scaledWidth = scale * sourceWidth;
    	    float scaledHeight = scale * sourceHeight;

    	    // Let's find out the upper left coordinates if the scaled bitmap
    	    // should be centered in the new size give by the parameters
    	    float left = (newWidth - scaledWidth) / 2;
    	    float top = (newHeight - scaledHeight) / 2;

    	    // The target rectangle for the new, scaled version of the source bitmap will now
    	    // be
    	    RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

    	    // Finally, we create a new bitmap of the specified size and draw our new,
    	    // scaled bitmap onto it.
    	    Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
    	    Canvas canvas = new Canvas(dest);
    	    canvas.drawBitmap(source, null, targetRect, null);

    	    return dest;
    	}

    }


	@Override
	public void onClick(View v) {
		if(((Button) v).equals(btnOk)){
			
			String texture = FlagManager.getFlagNameById((Integer) imageView.getTag());
			Log.w("AAA", texture);
			prefs.edit().putString(Settings.FLAG_IMAGE, texture).commit();
		}
		finish();
	}
}