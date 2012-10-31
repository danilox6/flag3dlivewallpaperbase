/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.droid4you.util.cropimage;


import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import com.devxperiments.flaglivewallpaper.FlagWallpaperService;
import com.devxperiments.flaglivewallpaper.R;
import com.devxperiments.flaglivewallpaper.settings.BitmapUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;



/**
 * The activity can crop specific region of interest from an image.
 */
public class CropImage extends Activity {
	private static final String TAG = "CropImage";

	private int mAspectX, mAspectY;
	private final Handler mHandler = new Handler();

	// These options specifiy the output image size and whether we should
	// scale the output to fit it (or just crop it).
	private int mOutputX, mOutputY;

	boolean mSaving;  // Whether the "save" button is already clicked.

	private CropImageView mImageView;
	private ContentResolver mContentResolver;

	private static Bitmap mBitmap;
	HighlightView mCrop;

	private String mImagePath;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		mContentResolver = getContentResolver();

		setContentView(R.layout.cropimage);
		
		
		// Make UI fullscreen.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		findViewById(R.id.btnCancel).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View v) {
						setResult(RESULT_CANCELED);
						finish();
					}
				});

		findViewById(R.id.btnOk).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View v) {
						onSaveClicked();
					}
				});

		mImageView = (CropImageView) findViewById(R.id.image);
//		mImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {

			mImagePath = extras.getString("image-path");

			Display display = getWindowManager().getDefaultDisplay();

				if(display.getHeight()>display.getWidth()){
					mAspectX = display.getWidth();
					mAspectY = display.getHeight();
				}
				else{
					mAspectY = display.getWidth();
					mAspectX = display.getHeight();	
				}

			mOutputX = mOutputY = BitmapUtils.getBestFittingScreenPow(display);
			
		}

		new AsyncTask<Void, Void, Bitmap>(){

			private ProgressDialog dialog = new ProgressDialog(CropImage.this);;
			
		    protected void onPreExecute() {
		        this.dialog.setMessage(getString(R.string.strLoading));
		        this.dialog.show();
		    }
			
			@Override
			protected Bitmap doInBackground(Void... params) {
				FutureTask<Bitmap> task = new FutureTask<Bitmap>(new Callable<Bitmap>() {
					public Bitmap call() throws Exception {
						return getBitmap(mImagePath);
					}
				});
				
				new Thread(task).start();             
                
                try{
                     return task.get(7, TimeUnit.SECONDS);
                }catch (Exception e){
                	return null;
                }
			}
			
			protected void onPostExecute(Bitmap success) {
			        if (dialog.isShowing()) {
			            dialog.dismiss();
			        }
			        mBitmap = success;
			        if (mBitmap == null) {
			        	if(mImagePath.toString().contains("picasa"))
			        		Toast.makeText(CropImage.this, R.string.strPicasaError, Toast.LENGTH_LONG).show();
			        	finish();
			        	return;
			        }
			        startFaceDetection();
			  }
		}.execute();
		

		
//		try {
//			mBitmap =  task.get(10, TimeUnit.SECONDS);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		} catch (ExecutionException e) {
//			e.printStackTrace();
//		} catch (TimeoutException e) {
//			// TODO Auto-generated catch block
//			Toast.makeText(this, R.string.txtPicasaError, Toast.LENGTH_LONG).show();
//			finish();
//			return;
//		}
		
		
		
//		mBitmap = getBitmap(mImagePath);
//
//
//		if (mBitmap == null) {
//			if(mImagePath.toString().contains("picasa"))
//				Toast.makeText(this, R.string.txtPicasaError, Toast.LENGTH_LONG).show();
//			finish();
//			return;
//		}

		
		
	}
	//
	//	private Uri getImageUri(String path) {
	//		return Uri.fromFile(new File(path));
	//	}

	

	private Bitmap getBitmap(String path, int maxSize) {

		Uri uri = Uri.parse(path);
		//		Uri uri = getImageUri(path);
		InputStream in = null;
		try {
			in = mContentResolver.openInputStream(uri);

			//Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;

			BitmapFactory.decodeStream(in, null, o);
			in.close();

			int scale = 1;
			if (o.outHeight > maxSize || o.outWidth > maxSize) 
				scale = (int)Math.pow(2, (int) Math.round(Math.log(maxSize / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));

			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			in = mContentResolver.openInputStream(uri);
			Bitmap b = BitmapFactory.decodeStream(in, null, o2);
			in.close();

			return b;
		} catch (IOException e) {
			Log.e(TAG, "file " + path + " not found");
		}
		return null;
	}

	private Bitmap getBitmap(String path){
		OutOfMemoryError exception;
		int exp = 11;
		do{
			exception = null;
			int maxSize = pow(2, --exp);
			try{
				return getBitmap(path, maxSize);
			}catch (OutOfMemoryError e) {
				exception = e;
			}
		}while(exception!=null);
		return null;
	}

	private void startFaceDetection() {
		if (isFinishing())
			return;
		mImageView.setImageBitmapResetBase(mBitmap, true);
		mRunFaceDetection.run();
	}
	
	
	protected void onSaveClicked() {
		new AsyncTask<Void, Void, Void>(){
			private ProgressDialog dialog = new ProgressDialog(CropImage.this);;
			
		    protected void onPreExecute() {
		    	this.dialog.setMessage(getString(R.string.strCropping));
		        this.dialog.show();
		    }

			protected Void doInBackground(Void... params) {
				startCropping();
				return null;
			}
			
			protected void onPostExecute(Void success) {
		        if (dialog.isShowing()) {
		            dialog.dismiss();
		        }
			}
		}.execute();
	}


	private void startCropping() {
		if (mSaving) return;

		if (mCrop == null) {
			return;
		}

		mSaving = true;

		Rect r = mCrop.getCropRect();

		int width = r.width();
		int height = r.height();

		// If we are circle cropping, we want alpha channel, which is the
		// third param here.
		Bitmap croppedImage = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		{
			Canvas canvas = new Canvas(croppedImage);
			Rect dstRect = new Rect(0, 0, width, height);
			canvas.drawBitmap(mBitmap, r, dstRect, null);
		}


		/* If the output is required to a specific size then scale or fill */

		/* Scale the image to the required dimensions */
		Bitmap old = croppedImage;
		croppedImage = BitmapUtils.transform(new Matrix(), croppedImage, mOutputX, mOutputY, true);
		if (old != croppedImage) 
			old.recycle();


//		/* Don't scale the image crop it to the size requested.
//		 * Create an new image with the cropped image in the center and
//		 * the extra space filled.
//		 */
//		Bitmap b = Bitmap.createBitmap(mOutputX, mOutputY, Bitmap.Config.RGB_565);
//		Canvas canvas = new Canvas(b);
//
//		//		Rect srcRect = mCrop.getCropRect();
//		Rect srcRect = new Rect(0,0,mAspectX,mAspectY);
//		Rect dstRect = new Rect(0, 0, mOutputX, mOutputY);
//
//		int dx = (srcRect.width() - dstRect.width()) / 2;
//		int dy = (srcRect.height() - dstRect.height()) / 2;
//
//		/* If the srcRect is too big, use the center part of it. */
//		srcRect.inset(Math.max(0, dx), Math.max(0, dy));
//
//		/* If the dstRect is too big, use the center part of it. */
//		dstRect.inset(Math.max(0, -dx), Math.max(0, -dy));
//
//		/* Draw the cropped bitmap in the center */
//		canvas.drawBitmap(croppedImage, srcRect, dstRect, null);
//
//		/* Set the cropped bitmap as the new bitmap */
//		croppedImage.recycle();
//		croppedImage = b;

		Log.i("CROP", "Cropped: "+croppedImage.getWidth()+"x"+croppedImage.getHeight() +", " +croppedImage.getRowBytes() * croppedImage.getHeight()+"bytes");
		
		
		BitmapUtils.setUserBitmap(FlagWallpaperService.context, croppedImage);
		setResult(RESULT_OK);
		
		croppedImage.recycle();
		croppedImage =  null;
		mBitmap.recycle();
		mBitmap = null;
		System.gc();
		finish();
	}

	private static int pow(int base, int power) {
		int result = 1;
		for (int i = 0; i < power; i++)
			result *= base;
		return result;
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
//		mBitmap.recycle();
	}


	Runnable mRunFaceDetection = new Runnable() {
		float mScale = 1F;
		Matrix mImageMatrix;

		// Create a default HightlightView if we found no face in the picture.
		private void makeDefault() {
			HighlightView hv = new HighlightView(mImageView);

			int width = mBitmap.getWidth();
			int height = mBitmap.getHeight();

			Rect imageRect = new Rect(0, 0, width, height);

			// make the default size about 4/5 of the width or height
			int cropWidth = Math.min(width, height) * 4 / 5;
			int cropHeight = cropWidth;

			double ratio = 0;
			if (mAspectX != 0 && mAspectY != 0) {
				if (mAspectX > mAspectY) {
					ratio = (mAspectX*1.0) / mAspectY;
					cropHeight = cropWidth;// * mAspectY / mAspectX;
				} else {
					ratio = (mAspectY*1.0) / mAspectX;
					cropWidth = cropHeight; // * mAspectX / mAspectY;
				}
			}
			int x = (width - cropWidth) / 2;
			int y = (height - cropHeight) / 2;

			RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);
			hv.setup(mImageMatrix, imageRect, cropRect, false,	mAspectX != 0 && mAspectY != 0, (float) ratio);

			mImageView.mHighlightViews.clear(); // Thong added for rotate

			mImageView.add(hv);
		}

		// Scale the image down for faster face detection.
		private Bitmap prepareBitmap() {
			if (mBitmap == null) {
				return null;
			}

			// 256 pixels wide is enough.
			if (mBitmap.getWidth() > 256) {
				mScale = 256.0F / mBitmap.getWidth();
			}
			Matrix matrix = new Matrix();
			matrix.setScale(mScale, mScale);
			Bitmap faceBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap
					.getWidth(), mBitmap.getHeight(), matrix, true);
			return faceBitmap;
		}

		public void run() {
			mImageMatrix = mImageView.getImageMatrix();
			Bitmap faceBitmap = prepareBitmap();

			mScale = 1.0F / mScale;

			if (faceBitmap != null && faceBitmap != mBitmap)
				faceBitmap.recycle();

			mHandler.post(new Runnable() {
				public void run() {
					makeDefault();
					mImageView.invalidate();
					if (mImageView.mHighlightViews.size() == 1) {
						mCrop = mImageView.mHighlightViews.get(0);
						mCrop.setFocus(true);
					}
				}
			});
		}
	};


}


