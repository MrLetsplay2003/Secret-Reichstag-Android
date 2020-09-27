package me.mrletsplay.secretreichstagandroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.content.res.ResourcesCompat;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UIGameSurface extends SurfaceView implements SurfaceHolder.Callback {

	public UIGameSurface(Context context, AttributeSet attrs) {
		super(context, attrs);

		setFocusable(true);

		getHolder().addCallback(this);
	}

	public void update() {
		invalidate();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		setWillNotDraw(false);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);

		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setStyle(Paint.Style.FILL);
		p.setColor(Color.WHITE);

		canvas.drawPaint(p);

		if(GameAsset.COMMUNIST_ARTICLE.getBitmap() != null) {
			DrawUtils.drawBitmapAutoW(canvas, GameAsset.ARTICLE_BACK.getBitmap(), 10, 10, getHeight());
		}

		p.setColor(Color.WHITE);

		Typeface tf = ResourcesCompat.getFont(getContext(), R.font.germania_one_regular);
		p.setTypeface(tf);
		p.setTextSize(100f);

		DrawUtils.drawLinesCentered(canvas, p, (int) (getHeight() / 1.45 / 2) + 10, getHeight() * 3 / 4, String.valueOf(MainActivity.getRoom().getGameState().getDrawPileSize()));

		p.setColor(Color.BLACK);

		String roomID = MainActivity.getRoom().getID();

		List<String> lines = new ArrayList<>(Collections.singletonList("Room ID: " + roomID));
		if(MainActivity.getSelfRole() != null) {
			lines.add("Your role: " + MainActivity.getSelfRole().name());
		}
		DrawUtils.drawLinesCentered(canvas, p, getWidth() / 2, getHeight() / 2, lines.toArray(new String[lines.size()]));
	}
}
