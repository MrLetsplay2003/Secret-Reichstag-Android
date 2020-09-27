package me.mrletsplay.secretreichstagandroid;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ResizablePlayerList extends ScrollView {

	public ResizablePlayerList(Context context) {
		super(context);
	}

	public ResizablePlayerList(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public ResizablePlayerList(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

}
