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


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.devxperiments.flaglivewallpaper.R;
import com.devxperiments.flaglivewallpaper.settings.BitmapUtils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;



/**
 * The activity can crop specific region of interest from an image.
 */
public class CropImage extends Activity {
	private static final String TAG = "CropImage";

	// These are various options can be specified in the intent.
//	private Bitmap.CompressFormat mOutputFormat = Bitmap.CompressFormat.JPEG; // only used with mSaveUri
//	private Uri mSaveUri = null;
	private int mAspectX, mAspectY;
	private final Handler mHandler = new Handler();

	// These options specifiy the output image size and whether we should
	// scale the output to fit it (or just crop it).
	private int mOutputX, mOutputY;
	private boolean mScale;
	private boolean mScaleUp = true;

	boolean mSaving;  // Whether the "save" button is already clicked.

	private CropImageView mImageView;
	private ContentResolver mContentResolver;

	private Bitmap mBitmap;
	HighlightView mCrop;


	private String mImagePath;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		mContentResolver = getContentResolver();

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.cropimage);

		mImageView = (CropImageView) findViewById(R.id.image);

		showStorageToast(this);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {

			mImagePath = extras.getString("image-path");
			Log.e("URI", mImagePath);
			
//			mSaveUri = getImageUri(mImagePath);
			mBitmap = getBitmap(mImagePath);

//			mAspectX = extras.getInt("aspectX");
//			mAspectY = extras.getInt("aspectY");
			mOutputX = extras.getInt("outputX");
			mOutputY = extras.getInt("outputY");
			mAspectX = 1;
			mAspectY = 1;
//			mOutputX = 512;
//			mOutputY = 512;
			mScale = extras.getBoolean("scale", true);
			mScaleUp = extras.getBoolean("scaleUpIfNeeded", true);
		}



		if (mBitmap == null) {
			Log.d(TAG, "finish!!!");
			finish();
			return;
		}

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


		startFaceDetection();
	}
//
//	private Uri getImageUri(String path) {
//		return Uri.fromFile(new File(path));
//	}

	private Bitmap getBitmap(String path) {
		
		Uri uri = Uri.parse(path);
//		Uri uri = getImageUri(path);
		InputStream in = null;
		try {
			final int IMAGE_MAX_SIZE = 1024;
			in = mContentResolver.openInputStream(uri);

			//Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;

			BitmapFactory.decodeStream(in, null, o);
			in.close();

			int scale = 1;
			if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
				scale = (int)Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
			}

			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			in = mContentResolver.openInputStream(uri);
			Bitmap b = BitmapFactory.decodeStream(in, null, o2);
			in.close();

			return b;
		} catch (FileNotFoundException e) {
			Log.e(TAG, "file " + path + " not found");
		} catch (IOException e) {
			Log.e(TAG, "file " + path + " not found");
		}
		return null;
	}


	private void startFaceDetection() {
		if (isFinishing())
			return;

		mImageView.setImageBitmapResetBase(mBitmap, true);
		mRunFaceDetection.run();
//		Util.startBackgroundJob(this, null, "Please wait\u2026", new Runnable() {
//			public void run() {
//				final CountDownLatch latch = new CountDownLatch(1);
//				final Bitmap b = mBitmap;
//				mHandler.post(new Runnable() {
//					public void run() {
//						if (b != mBitmap && b != null) {
//							mImageView.setImageBitmapResetBase(b, true);
//							mBitmap.recycle();
//							mBitmap = b;
//						}
//						if (mImageView.getScale() == 1F) {
//							mImageView.center(true, true);
//						}
//						latch.countDown();
//					}
//				});
//				try {
//					latch.await();
//				} catch (InterruptedException e) {
//					throw new RuntimeException(e);
//				}
//				mRunFaceDetection.run();
//			}
//		}, mHandler);
	}



	private void onSaveClicked() {
		// TODO this code needs to change to use the decode/crop/encode single
		// step api so that we don't require that the whole (possibly large)
		// bitmap doesn't have to be read into memory
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
		
		int nearerTwoPowerDown = 10;
		int nearerTwoPowerUp = 0;
		
		while(pow(2, --nearerTwoPowerDown)<=croppedImage.getWidth());
		while(pow(2, ++nearerTwoPowerUp)<=croppedImage.getWidth());
		
		if(nearerTwoPowerUp>9)
			nearerTwoPowerUp = 9;
		
		int size = Math.max(croppedImage.getWidth()-pow(2,nearerTwoPowerDown), pow(2,nearerTwoPowerUp)-croppedImage.getWidth());
		
//		mOutputX = mOutputY = pow(2,nearerTwoPowerDown);
		
		mOutputX = mOutputY = size;
		
		Log.e("CROP", mOutputX+"");

		/* If the output is required to a specific size then scale or fill */
		if (mOutputX != 0 && mOutputY != 0) {
			if (mScale) {
				/* Scale the image to the required dimensions */
				Bitmap old = croppedImage;
				croppedImage = BitmapUtils.transform(new Matrix(), croppedImage, mOutputX, mOutputY, mScaleUp);
				if (old != croppedImage) {
					old.recycle();
				}
			} else {

				/* Don't scale the image crop it to the size requested.
				 * Create an new image with the cropped image in the center and
				 * the extra space filled.
				 */

				// Don't scale the image but instead fill it so it's the
				// required dimension
				Bitmap b = Bitmap.createBitmap(mOutputX, mOutputY,
						Bitmap.Config.RGB_565);
				Canvas canvas = new Canvas(b);

				Rect srcRect = mCrop.getCropRect();
				Rect dstRect = new Rect(0, 0, mOutputX, mOutputY);

				int dx = (srcRect.width() - dstRect.width()) / 2;
				int dy = (srcRect.height() - dstRect.height()) / 2;

				/* If the srcRect is too big, use the center part of it. */
				srcRect.inset(Math.max(0, dx), Math.max(0, dy));

				/* If the dstRect is too big, use the center part of it. */
				dstRect.inset(Math.max(0, -dx), Math.max(0, -dy));

				/* Draw the cropped bitmap in the center */
				canvas.drawBitmap(mBitmap, srcRect, dstRect, null);

				/* Set the cropped bitmap as the new bitmap */
				croppedImage.recycle();
				croppedImage = b;
			}
		}

		// Return the cropped image directly or save it to the specified URI.
		Bundle myExtras = getIntent().getExtras();
		if (myExtras != null && (myExtras.getParcelable("data") != null|| myExtras.getBoolean("return-data"))) {
			Bundle extras = new Bundle();
			extras.putParcelable("data", croppedImage);
			setResult(RESULT_OK,(new Intent()).setAction("inline-data").putExtras(extras));
			finish();
		} else {
//			final Bitmap b = croppedImage;
//			Util.startBackgroundJob(this, null,"Saving image",	new Runnable() {
//				public void run() {
//					saveOutput(b);
//				}
//			}, mHandler);
		}
	}
	
	private static int pow(int base, int power) {
	    int result = 1;
	    for (int i = 0; i < power; i++)
	        result *= base;
	    return result;
	}

//	private void saveOutput(Bitmap croppedImage) {
//		if (mSaveUri != null) {
//			OutputStream outputStream = null;
//			try {
//				outputStream = mContentResolver.openOutputStream(mSaveUri);
//				if (outputStream != null) {
//					croppedImage.compress(mOutputFormat, 75, outputStream);
//				}
//			} catch (IOException ex) {
//				// TODO: report error to caller
//				Log.e(TAG, "Cannot open file: " + mSaveUri, ex);
//			} finally {
//				Util.closeSilently(outputStream);
//			}
//			Bundle extras = new Bundle();
//			setResult(RESULT_OK, new Intent(mSaveUri.toString())
//			.putExtras(extras));
//		} else {
//			Log.e(TAG, "not defined image url");
//		}
//		croppedImage.recycle();
//		finish();
//	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mBitmap.recycle();
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

			if (mAspectX != 0 && mAspectY != 0) {
				if (mAspectX > mAspectY) {
					cropHeight = cropWidth * mAspectY / mAspectX;
				} else {
					cropWidth = cropHeight * mAspectX / mAspectY;
				}
			}

			int x = (width - cropWidth) / 2;
			int y = (height - cropHeight) / 2;

			RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);
			hv.setup(mImageMatrix, imageRect, cropRect, false,	mAspectX != 0 && mAspectY != 0);

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

			if (faceBitmap != null && faceBitmap != mBitmap) {
				faceBitmap.recycle();
			}

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

	public static final int NO_STORAGE_ERROR = -1;
	public static final int CANNOT_STAT_ERROR = -2;

	public static void showStorageToast(Activity activity) {
		showStorageToast(activity, calculatePicturesRemaining());
	}

	public static void showStorageToast(Activity activity, int remaining) {
		String noStorageText = null;

		if (remaining == NO_STORAGE_ERROR) {
			String state = Environment.getExternalStorageState();
			if (state == Environment.MEDIA_CHECKING) {
				noStorageText = "Preparing card";
			} else {
				noStorageText = "No storage card";
			}
		} else if (remaining < 1) {
			noStorageText = "Not enough space";
		}

		if (noStorageText != null) {
			Toast.makeText(activity, noStorageText, Toast.LENGTH_LONG).show();
		}
	}

	public static int calculatePicturesRemaining() {
		try {
			/*if (!ImageManager.hasStorage()) {
                return NO_STORAGE_ERROR;
            } else {*/
			String storageDirectory =
					Environment.getExternalStorageDirectory().toString();
			StatFs stat = new StatFs(storageDirectory);
			float remaining = ((float) stat.getAvailableBlocks()
					* (float) stat.getBlockSize()) / 400000F;
			return (int) remaining;
			//}
		} catch (Exception ex) {
			// if we can't stat the filesystem then we don't know how many
			// pictures are remaining.  it might be zero but just leave it
			// blank since we really don't know.
			return CANNOT_STAT_ERROR;
		}
	}



}


class CropImageView extends ImageViewTouchBase {
	ArrayList<HighlightView> mHighlightViews = new ArrayList<HighlightView>();
	HighlightView mMotionHighlightView = null;
	float mLastX, mLastY;
	int mMotionEdge;

	private Context mContext;

	@Override
	protected void onLayout(boolean changed, int left, int top,
			int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (mBitmapDisplayed.getBitmap() != null) {
			for (HighlightView hv : mHighlightViews) {
				hv.mMatrix.set(getImageMatrix());
				hv.invalidate();
				if (hv.mIsFocused) {
					centerBasedOnHighlightView(hv);
				}
			}
		}
	}

	public CropImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
	}

	@Override
	protected void zoomTo(float scale, float centerX, float centerY) {
		super.zoomTo(scale, centerX, centerY);
		for (HighlightView hv : mHighlightViews) {
			hv.mMatrix.set(getImageMatrix());
			hv.invalidate();
		}
	}

	@Override
	protected void zoomIn() {
		super.zoomIn();
		for (HighlightView hv : mHighlightViews) {
			hv.mMatrix.set(getImageMatrix());
			hv.invalidate();
		}
	}

	@Override
	protected void zoomOut() {
		super.zoomOut();
		for (HighlightView hv : mHighlightViews) {
			hv.mMatrix.set(getImageMatrix());
			hv.invalidate();
		}
	}

	@Override
	protected void postTranslate(float deltaX, float deltaY) {
		super.postTranslate(deltaX, deltaY);
		for (int i = 0; i < mHighlightViews.size(); i++) {
			HighlightView hv = mHighlightViews.get(i);
			hv.mMatrix.postTranslate(deltaX, deltaY);
			hv.invalidate();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		CropImage cropImage = (CropImage) mContext;
		if (cropImage.mSaving) {
			return false;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:

			for (int i = 0; i < mHighlightViews.size(); i++) {
				HighlightView hv = mHighlightViews.get(i);
				int edge = hv.getHit(event.getX(), event.getY());
				if (edge != HighlightView.GROW_NONE) {
					mMotionEdge = edge;
					mMotionHighlightView = hv;
					mLastX = event.getX();
					mLastY = event.getY();
					mMotionHighlightView.setMode(
							(edge == HighlightView.MOVE)
							? HighlightView.ModifyMode.Move
									: HighlightView.ModifyMode.Grow);
					break;
				}

			}
			break;
		case MotionEvent.ACTION_UP:
			if (mMotionHighlightView != null) {
				centerBasedOnHighlightView(mMotionHighlightView);
				mMotionHighlightView.setMode(
						HighlightView.ModifyMode.None);
			}
			mMotionHighlightView = null;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mMotionHighlightView != null) {
				mMotionHighlightView.handleMotion(mMotionEdge,
						event.getX() - mLastX,
						event.getY() - mLastY);
				mLastX = event.getX();
				mLastY = event.getY();

				if (true) {
					// This section of code is optional. It has some user
					// benefit in that moving the crop rectangle against
					// the edge of the screen causes scrolling but it means
					// that the crop rectangle is no longer fixed under
					// the user's finger.
					ensureVisible(mMotionHighlightView);
				}
			}
			break;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			center(true, true);
			break;
		case MotionEvent.ACTION_MOVE:
			// if we're not zoomed then there's no point in even allowing
			// the user to move the image around.  This call to center puts
			// it back to the normalized location (with false meaning don't
			// animate).
			if (getScale() == 1F) {
				center(true, true);
			}
			break;
		}

		return true;
	}

	// Pan the displayed image to make sure the cropping rectangle is visible.
	private void ensureVisible(HighlightView hv) {
		Rect r = hv.mDrawRect;

		int panDeltaX1 = Math.max(0, mLeft - r.left);
		int panDeltaX2 = Math.min(0, mRight - r.right);

		int panDeltaY1 = Math.max(0, mTop - r.top);
		int panDeltaY2 = Math.min(0, mBottom - r.bottom);

		int panDeltaX = panDeltaX1 != 0 ? panDeltaX1 : panDeltaX2;
		int panDeltaY = panDeltaY1 != 0 ? panDeltaY1 : panDeltaY2;

		if (panDeltaX != 0 || panDeltaY != 0) {
			panBy(panDeltaX, panDeltaY);
		}
	}

	// If the cropping rectangle's size changed significantly, change the
	// view's center and scale according to the cropping rectangle.
	private void centerBasedOnHighlightView(HighlightView hv) {
		Rect drawRect = hv.mDrawRect;

		float width = drawRect.width();
		float height = drawRect.height();

		float thisWidth = getWidth();
		float thisHeight = getHeight();

		float z1 = thisWidth / width * .6F;
		float z2 = thisHeight / height * .6F;

		float zoom = Math.min(z1, z2);
		zoom = zoom * this.getScale();
		zoom = Math.max(1F, zoom);
		if ((Math.abs(zoom - getScale()) / zoom) > .1) {
			float [] coordinates = new float[] {hv.mCropRect.centerX(),
					hv.mCropRect.centerY()};
			getImageMatrix().mapPoints(coordinates);
			zoomTo(zoom, coordinates[0], coordinates[1], 300F);
		}

		ensureVisible(hv);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		for (int i = 0; i < mHighlightViews.size(); i++) {
			mHighlightViews.get(i).draw(canvas);
		}
	}

	public void add(HighlightView hv) {
		mHighlightViews.add(hv);
		invalidate();
	}
}