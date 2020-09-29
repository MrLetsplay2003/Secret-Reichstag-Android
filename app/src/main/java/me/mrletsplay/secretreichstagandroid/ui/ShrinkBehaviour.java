package me.mrletsplay.secretreichstagandroid.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;

public class ShrinkBehaviour<V extends View> extends CoordinatorLayout.Behavior<V> {

	public ShrinkBehaviour(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull View dependency) {
		return dependency instanceof Snackbar.SnackbarLayout;
	}

	@Override
	public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull View dependency) {
		ViewGroup.LayoutParams layoutParams = child.getLayoutParams();
		layoutParams.height = parent.getHeight() - dependency.getHeight();
		child.setLayoutParams(layoutParams);
		return true;
	}

	@Override
	public void onDependentViewRemoved(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull View dependency) {
		ViewGroup.LayoutParams layoutParams = child.getLayoutParams();
		layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
		child.setLayoutParams(layoutParams);
	}

	@Override
	public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
		return true;
	}

}
