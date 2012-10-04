package com.devxperiments.flaglivewallpaper.settings;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.Display;
import android.widget.ImageView;

public class BitmapUtils {

	private static Bitmap userBitmap = null;
	private static int bestFittingScreenPow = 0;

	public static class BitmapDataObject implements Serializable {
		private static final long serialVersionUID = 111696345129311948L;
		public byte[] imageByteArray;
	}

	private static void writeBitmapObject(Bitmap currentImage, ObjectOutputStream out) throws IOException{

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		currentImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
		BitmapDataObject bitmapDataObject = new BitmapDataObject();     
		bitmapDataObject.imageByteArray = stream.toByteArray();

		out.writeObject(bitmapDataObject);
	}

	private static Bitmap readBitmapObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
		BitmapDataObject bitmapDataObject = (BitmapDataObject)in.readObject();
		return BitmapFactory.decodeByteArray(bitmapDataObject.imageByteArray, 0, bitmapDataObject.imageByteArray.length);
	}

	private static Bitmap loadBitmap(Context context) {
		try {
			ObjectInputStream ois = new ObjectInputStream(context.openFileInput("userBitmapFile"));
			return readBitmapObject(ois);
		} catch (Exception e) {
			return null;
		}
	}

	private static void saveBitmap(Context context, Bitmap bitmap) {
		try {
			FileOutputStream fos = context.openFileOutput("userBitmapFile",Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			writeBitmapObject(bitmap, oos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Bitmap getUserBitmap(Context context){
		return loadBitmap(context);
//		if(userBitmap == null)
//			userBitmap = loadBitmap(context);
//		return userBitmap;
	}

	public static void setUserBitmap(Context context, Bitmap bitmap){
//				userBitmap = bitmap;
				if(bitmap!=null)
					saveBitmap(context, bitmap);
	}

	public static void freeBitmaps(){
		if(userBitmap!=null){
			userBitmap.recycle();
			userBitmap = null;
		}
		System.gc();
	}

	public static Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
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

	/**
	 * Compute the sample size as a function of minSideLength
	 * and maxNumOfPixels.
	 * minSideLength is used to specify that minimal width or height of a bitmap.
	 * maxNumOfPixels is used to specify the maximal size in pixels that are tolerable
	 * in terms of memory usage.
	 *
	 * The function returns a sample size based on the constraints.
	 * Both size and minSideLength can be passed in as IImage.UNCONSTRAINED,
	 * which indicates no care of the corresponding constraint.
	 * The functions prefers returning a sample size that
	 * generates a smaller bitmap, unless minSideLength = IImage.UNCONSTRAINED.
	 */
	public static Bitmap transform(Matrix scaler,Bitmap source, int targetWidth, int targetHeight, boolean scaleUp) {
		int deltaX = source.getWidth() - targetWidth;
		int deltaY = source.getHeight() - targetHeight;
		if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
			/*
			 * In this case the bitmap is smaller, at least in one dimension,
			 * than the target.  Transform it by placing as much of the image
			 * as possible into the target and leaving the top/bottom or
			 * left/right (or both) black.
			 */
			Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight,
					Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b2);

			int deltaXHalf = Math.max(0, deltaX / 2);
			int deltaYHalf = Math.max(0, deltaY / 2);
			Rect src = new Rect(
					deltaXHalf,
					deltaYHalf,
					deltaXHalf + Math.min(targetWidth, source.getWidth()),
					deltaYHalf + Math.min(targetHeight, source.getHeight()));
			int dstX = (targetWidth  - src.width())  / 2;
			int dstY = (targetHeight - src.height()) / 2;
			Rect dst = new Rect(dstX, dstY, targetWidth - dstX, targetHeight - dstY);
			c.drawBitmap(source, src, dst, null);
			return b2;
		}
		float bitmapWidthF = source.getWidth();
		float bitmapHeightF = source.getHeight();

		float bitmapAspect = bitmapWidthF / bitmapHeightF;
		float viewAspect   = (float) targetWidth / targetHeight;

		if (bitmapAspect > viewAspect) {
			float scale = targetHeight / bitmapHeightF;
			if (scale < .9F || scale > 1F) 
				scaler.setScale(scale, scale);
			else 
				scaler = null;

		} else {
			float scale = targetWidth / bitmapWidthF;
			if (scale < .9F || scale > 1F) 
				scaler.setScale(scale, scale);
			else 
				scaler = null;

		}

		Bitmap b1;
		if (scaler != null) {
			// this is used for minithumb and crop, so we want to filter here.
			b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), scaler, true);
		} else 
			b1 = source;


		int dx1 = Math.max(0, b1.getWidth() - targetWidth);
		int dy1 = Math.max(0, b1.getHeight() - targetHeight);

		Bitmap b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth, targetHeight);

		if (b1 != source)
			b1.recycle();

		return b2;
	}

	public static int getBestFittingScreenPow(int width, int heigth) {
		if(bestFittingScreenPow == 0){
			int max = Math.max(width, heigth);
			int nearerTwoPower=0;
			while(twoPower(++nearerTwoPower)<=max);
			bestFittingScreenPow = twoPower(nearerTwoPower);
		}
		return bestFittingScreenPow;
	}

	public static int getBestFittingScreenPow(Display display){
		return getBestFittingScreenPow(display.getWidth(), display.getHeight());		
	}

	/**
	 * Ritorna 2^power
	 */
	private static int twoPower(int power) {
		int result = 1;
		for (int i = 0; i < power; i++)
			result *= 2;
		return result;
	}

	public static Bitmap toGrayScale(Bitmap bmpOriginal){        
		int width, height;
		height = bmpOriginal.getHeight();
		width = bmpOriginal.getWidth();    

		Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		Canvas c = new Canvas(bmpGrayscale);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmpOriginal, 0, 0, paint);
		return bmpGrayscale;
	}
	
	public static void toGrayScale(ImageView v){
	    ColorMatrix matrix = new ColorMatrix();
	    matrix.setSaturation(0); //0 means grayscale
	    ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
	    v.setColorFilter(cf);
	}
}
