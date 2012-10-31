package com.devxperiments.flaglivewallpaper.settings;

import java.util.List;

import com.devxperiments.flaglivewallpaper.FlagManager;
import com.devxperiments.flaglivewallpaper.FlagWallpaperService;
import com.devxperiments.flaglivewallpaper.R;
import com.droid4you.util.cropimage.CropImage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;

public class WallpaperChooser extends Activity implements OnClickListener, OnItemSelectedListener{

	private List<Integer> pics;
	private ImageView imageView;
	private Button btnOk;
	private TextView txtInfo;
	private SharedPreferences prefs;
	private int thumbHeight;
	private Bitmap[] thumbCache;
	private boolean skyBackground;
	private OnClickListener listener = null;
	private final int PICKED_IMAGE = 1;
	private final int CROPPED_IMAGE = 2;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wallpaper_chooser);


		skyBackground = getIntent().getBooleanExtra(Settings.SKY_MODE_BACKGROUND_IMAGE, false);

		if(skyBackground){
			setTitle(R.string.prefBackground);
			pics = FlagManager.getSkyBackgroundIds();
			listener = new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.setType("image/*");
					startActivityForResult(intent, PICKED_IMAGE);
				}
			};
		}
		else{
			listener = new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(WallpaperChooser.this, PreviewActivity.class);
					intent.putExtra("flagId", (Integer) imageView.getTag());
					startActivity(intent);
				}
			};
			pics = FlagManager.getPortraitFlagIds();
		}


		txtInfo = (TextView) findViewById(R.id.txtImageInfo);

		Gallery gallery = (Gallery) findViewById(R.id.gallery);
		gallery.setAdapter(new FlagAdapter(this));
		gallery.setCallbackDuringFling(false);

		gallery.setOnItemSelectedListener(this);

		imageView = (ImageView)findViewById(R.id.imgFlag);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		String texture = prefs.getString(Settings.SINGLE_FLAG_IMAGE_SETTING, FlagManager.getDefaultFlag());
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
			texture = FlagManager.toLandscape(texture);
		imageView.setImageResource(FlagManager.getFlagId(texture));
		imageView.setTag(FlagManager.getFlagId(texture));

		btnOk = (Button) findViewById(R.id.btnOk);
		btnOk.setOnClickListener(this);
		((Button) findViewById(R.id.btnCancel)).setOnClickListener(this);

		Display display = getWindowManager().getDefaultDisplay();
		thumbHeight = (display.getHeight()*30/100) - 20;

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try{
			if(resultCode == RESULT_OK){
				switch (requestCode) {
				case PICKED_IMAGE:
					Uri selectedImage = data.getData();
					Intent intent = new Intent(this, CropImage.class);
					Log.i("WallpaperChooser", "Picked image URI: "+selectedImage.toString());
					intent.putExtra("image-path", selectedImage.toString());
					startActivityForResult(intent, CROPPED_IMAGE);
					break;

				case CROPPED_IMAGE:
					Bitmap bitmap = BitmapUtils.getUserBitmap(FlagWallpaperService.context);
					imageView.setImageBitmap(bitmap);
					break;
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onClick(View v) {
		if(((Button) v).equals(btnOk)){
			int flagId = (Integer) imageView.getTag();
			String texture;
			if(flagId == R.drawable.sys_btn_add){
				prefs.edit().putString(Settings.SKY_MODE_BACKGROUND_IMAGE, "update").commit();
				texture = Settings.SKY_USER_BACKGROUND;
			}else
				texture = FlagManager.getFlagNameById(flagId);

			if(!skyBackground && !FlagWallpaperService.PRO && !(texture.startsWith(FlagManager.DEFAULT) || texture.startsWith(FlagManager.FREE))){
				Toast.makeText(this, R.string.strOnlyInProVersion, Toast.LENGTH_LONG).show();
				return;
			}
			prefs.edit().putString(skyBackground?Settings.SKY_MODE_BACKGROUND_IMAGE : Settings.SINGLE_FLAG_IMAGE_SETTING, texture).commit();
		}
		finish();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		int flagId = pics.get(position);

		boolean portrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

		if(skyBackground && flagId == R.drawable.sys_btn_add){

			txtInfo.setVisibility(View.VISIBLE);
			txtInfo.setText(R.string.strPickFromGallery);

			Bitmap bitmap = BitmapUtils.getUserBitmap(FlagWallpaperService.context);
			if(bitmap == null)
				imageView.setImageResource(flagId);
			else
				imageView.setImageBitmap(bitmap);

			imageView.setScaleType(ScaleType.CENTER_INSIDE);
			imageView.setBackgroundResource(android.R.color.transparent); 
			imageView.setOnClickListener(listener);
			txtInfo.setOnClickListener(listener);

		}else{
			imageView.setBackgroundResource(R.drawable.sys_holo_border);
			imageView.setScaleType(ScaleType.CENTER_CROP);
			String selectedTexture = null;
			if(!skyBackground){
				selectedTexture = FlagManager.getFlagNameById(flagId);
				if(!portrait)
					flagId = FlagManager.toLandscape(flagId);
			}
			imageView.setImageResource(flagId);
			if(!skyBackground){
				txtInfo.setVisibility(View.VISIBLE);
				if(!FlagWallpaperService.PRO && !(selectedTexture.startsWith(FlagManager.DEFAULT) || selectedTexture.startsWith(FlagManager.FREE))){
					//				BitmapUtils.toGrayScale(imageView);
					txtInfo.setText(R.string.strOnlyInProVersion);
				}else{
					txtInfo.setText(R.string.strOpenPreview);
					imageView.setOnClickListener(listener);
					txtInfo.setOnClickListener(listener);
				}
			}else{
				txtInfo.setVisibility(View.GONE);
				txtInfo.setOnClickListener(null);
				imageView.setOnClickListener(null);
			}
		}

		imageView.setTag(flagId);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {}



	public class FlagAdapter extends BaseAdapter {

		private Context context;
		int imageBackground;

		public FlagAdapter(Context c) {
			thumbCache = new Bitmap[pics.size()];
			context = c;
			TypedArray array = obtainStyledAttributes(R.styleable.GalleryStyle);
			imageBackground = array.getResourceId(R.styleable.GalleryStyle_android_galleryItemBackground, 1);
			array.recycle();
		}

		@Override
		public int getCount() {
			return pics.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;
			if(convertView == null)
				imageView = new ImageView(context);
			else
				imageView = (ImageView) convertView;


			if (thumbCache[position] == null){
				Bitmap bitmap  = BitmapUtils.scaleCenterCrop(BitmapFactory.decodeResource(getResources(), pics.get(position)),thumbHeight,thumbHeight);
				if(!skyBackground && !FlagWallpaperService.PRO){
					String texture = FlagManager.getFlagNameById(pics.get(position));
					if (!(texture.startsWith(FlagManager.DEFAULT) || texture.startsWith(FlagManager.FREE)))
						bitmap = BitmapUtils.toGrayScale(bitmap);
				}
				thumbCache[position] = bitmap;
			}
			imageView.setImageBitmap(thumbCache[position]);
			imageView.setAdjustViewBounds(true);
			//			imageView.setLayoutParams(new Gallery.LayoutParams(thumbHeight, thumbHeight));
			//			imageView.setBackgroundResource(imageBackground);
			if(skyBackground && position == thumbCache.length-1)
				imageView.setBackgroundResource(android.R.color.transparent); 
			else
				imageView.setBackgroundResource(R.drawable.sys_holo_border);
			return imageView;
		}

	}


}