package me.mrletsplay.secretreichstagandroid.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.res.ResourcesCompat;

import me.mrletsplay.secretreichstagandroid.GameAsset;
import me.mrletsplay.secretreichstagandroid.MainActivity;
import me.mrletsplay.secretreichstagandroid.R;
import me.mrletsplay.srweb.game.state.GameParty;
import me.mrletsplay.srweb.game.state.board.GameBoard;
import me.mrletsplay.srweb.game.state.board.GameBoardActionField;

public class UIGameBoard extends SurfaceView implements SurfaceHolder.Callback {

	private GameParty party;

	public UIGameBoard(Context context, AttributeSet attrs) {
		this(context, GameParty.LIBERAL);
	}

	public UIGameBoard(Context context, GameParty party) {
		super(context);

		getHolder().addCallback(this);

		this.party = party;
	}

	public void update() {
		invalidate();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		setWillNotDraw(false);

		ViewGroup.LayoutParams p = getLayoutParams();
		p.height = ((View) getParent()).getWidth() / 3; // Ratio width:height = 3:1
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

		float density = Resources.getSystem().getDisplayMetrics().density;

		Paint p = new Paint();
		p.setStyle(Paint.Style.FILL);
		p.setColor(getResources().getColor(R.color.board_background));
		canvas.drawPaint(p);

		p.setColor(getColor("board_" + party.name().toLowerCase() + "_outer_fill"));
		canvas.drawRect(3 * density, 3 * density, getWidth() - 3 * density, getHeight() - 3 * density, p);

		int numCards = party == GameParty.LIBERAL ? 5 : 6;
		int cardHeight = getHeight() * 3 / 5;
		int cardWidth = (int) (cardHeight / 1.45);
		float cardSpacing = 6 * density;

		GameBoard board = null;
		switch(party) {
			case LIBERAL:
				board = MainActivity.getRoom().getGameState().getLiberalBoard();
				break;
			case COMMUNIST:
				board = MainActivity.getRoom().getGameState().getCommunistBoard();
				break;
			case FASCIST:
				board = MainActivity.getRoom().getGameState().getFascistBoard();
				break;
		}

		if(party != GameParty.LIBERAL) {
			// Draw "unsafe" cards boundary
			int i = 3;
			float x = getWidth() / 2 - ((cardWidth + cardSpacing) * numCards - cardSpacing) / 2 + (cardWidth + cardSpacing) * i;
			int y = getHeight() / 2 - cardHeight / 2;
			p.setColor(getColor("board_" + party.name().toLowerCase() + "_unsafe_fill"));
			canvas.drawRect(x - 3 * density, y - 3 * density, x + cardWidth * 3 + cardSpacing * 2 + 3 * density, y + cardHeight + 3 * density, p);
		}

		p.setColor(getColor("board_" + party.name().toLowerCase() + "_card_background"));
		for(int i = 0; i < numCards; i++) {
			float x = getWidth() / 2 - ((cardWidth + cardSpacing) * numCards - cardSpacing) / 2 + (cardWidth + cardSpacing) * i;
			int y = getHeight() / 2 - cardHeight / 2;
			canvas.drawRect(x, y, x + cardWidth, y + cardHeight, p);

			if(board != null) {
				GameBoardActionField af = null;
				for (GameBoardActionField f : board.getActionFields()) {
					if(f.getFieldIndex() == i) {
						af = f;
						break;
					}
				}

				if(af != null) {
					GameAsset icon = GameAsset.valueOf("ACTION_" + af.getAction().name() + "_" + party.name());
					DrawUtils.drawBitmapAutoH(canvas, icon.getBitmap(), x, y + cardHeight / 2 - cardWidth / 2, cardWidth);
				}

				if(party != GameParty.LIBERAL && i == numCards - 1) {
					GameAsset icon = GameAsset.valueOf("ICON_WIN_" + party.name());
					DrawUtils.drawBitmapAutoH(canvas, icon.getBitmap(), x, y + cardHeight / 2 - cardWidth / 2, cardWidth);
				}

				if (board.getNumCards() > i)
					DrawUtils.drawBitmap(canvas, GameAsset.valueOf(party.name() + "_ARTICLE").getBitmap(), x, y, cardWidth, cardHeight);
			}
		}

		if(party == GameParty.LIBERAL) {
			// Election tracker
			int numPoints = 4;
			float size = 10 * density;
			float spacing = 30 * density;
			float y = getHeight() - 15 * density;

			for(int i = 0; i < numPoints; i++) {
				p.setColor(ResourcesCompat.getColor(getResources(), MainActivity.getRoom().getGameState().getFailedElections() == i ? R.color.board_liberal_election_tracker_active : R.color.board_liberal_election_tracker_inactive, null));

				float x = getWidth() / 2f - (spacing * numPoints) / 2 + i * spacing;
				RectF r = new RectF(x - size / 2f + spacing / 2f, y - size / 2f, x - size / 2f + spacing / 2f + size, y - size / 2f + size);
				canvas.drawRoundRect(r, size, size, p);
			}
		}

		p.setColor(getColor("board_" + party.name().toLowerCase() + "_title"));
		p.setTextSize(15 * density);
		p.setTypeface(ResourcesCompat.getFont(getContext(), R.font.germania_one_regular));

		DrawUtils.drawLinesCentered(canvas, p, getWidth() / 2, (getHeight() / 2 - cardHeight / 2 - 10) / 2 + 10, party.getFriendlyNameSingular());
	}

	private int getColor(String name) {
		return getResources().getColor(getResources().getIdentifier(name, "color", "me.mrletsplay.secretreichstagandroid"));
	}

}
