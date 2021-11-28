package me.mrletsplay.secretreichstagandroid.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import me.mrletsplay.secretreichstagandroid.GameAsset;

public class DrawUtils {

	public static void drawBitmap(Canvas c, Bitmap b, float x, float y, int width, int height) {
		Rect src = new Rect(0, 0, b.getWidth(), b.getHeight());
		RectF dst = new RectF(x, y, x + width, y + height);
		c.drawBitmap(b, src, dst, null);
	}

	public static void drawBitmapAutoH(Canvas c, Bitmap b, float x, float y, int width) {
		drawBitmap(c, b, x, y, width, (int) ((double) b.getHeight() / b.getWidth() * width));
	}

	public static void drawBitmapAutoW(Canvas c, Bitmap b, float x, float y, int height) {
		drawBitmap(c, b, x, y, (int) ((double) b.getWidth() / b.getHeight() * height), height);
	}

	public static void draw(Canvas c, GameAsset b, float x, float y, int width, int height) {
		drawBitmap(c, b.getCachedBitmap(), x, y, width, height);
	}

	public static void drawAutoH(Canvas c, GameAsset b, float x, float y, int width) {
		drawBitmapAutoH(c, b.getCachedBitmap(), x, y, width);
	}

	public static void drawAutoW(Canvas c, GameAsset b, float x, float y, int height) {
		drawBitmapAutoW(c, b.getCachedBitmap(), x, y, height);
	}

	public static void drawLinesCentered(Canvas c, Paint p, int centerX, int centerY, String... lines) {
		float lineHeight = p.getFontMetrics().bottom - p.getFontMetrics().top;
		Rect rect = new Rect();
		for(int i = 0; i < lines.length; i++) {
			p.getTextBounds(lines[i], 0, lines[i].length(), rect);
			c.drawText(lines[i], centerX - rect.exactCenterX(), centerY - lineHeight * (lines.length - 1) / 2 + lineHeight * i - rect.exactCenterY(), p);
		}
	}

}
