package org.doubango.imsdroid.Utils;

import org.doubango.imsdroid.IMSDroid;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class GraphicsUtils {
	
	public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
		final int w = bm.getWidth();
		final int h = bm.getHeight();
		final float sw = ((float) newWidth) / w;
		final float sh = ((float) newHeight) / h;

		Matrix matrix = new Matrix();
		matrix.postScale(sw, sh);
		return Bitmap.createBitmap(bm, 0, 0, w, h, matrix, false);
	}
	
	public static int getSizeInPixel(int dp){
		final float scale = IMSDroid.getContext().getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}
}
