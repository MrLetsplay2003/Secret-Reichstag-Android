package me.mrletsplay.secretreichstagandroid.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import androidx.core.content.res.ResourcesCompat;

import me.mrletsplay.secretreichstagandroid.GameAsset;
import me.mrletsplay.secretreichstagandroid.MainActivity;
import me.mrletsplay.secretreichstagandroid.R;

public class UICardStack extends SurfaceView implements SurfaceHolder.Callback {

	public UICardStack(Context context, AttributeSet attrs) {
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

		ViewGroup.LayoutParams p = getLayoutParams();
		p.width = (int) (getHeight() / 1.45);
		p.height = getHeight();
		setLayoutParams(p);
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

		if(GameAsset.COMMUNIST_ARTICLE.getSVG() != null) {
			DrawUtils.drawAutoW(canvas, GameAsset.ARTICLE_BACK, 0, 0, getHeight());
		}

		p.setColor(Color.WHITE);

		Typeface tf = ResourcesCompat.getFont(getContext(), R.font.germania_one_regular);
		p.setTypeface(tf);
		p.setTextSize(30f * Resources.getSystem().getDisplayMetrics().density);

		DrawUtils.drawLinesCentered(canvas, p, getWidth() / 2, getHeight() * 3 / 4 - 10, String.valueOf(MainActivity.getRoom().getGameState().getDrawPileSize()));
	}
}
