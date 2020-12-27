package me.mrletsplay.secretreichstagandroid.fragment;

import android.graphics.text.LineBreaker;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragmentCompat;

import me.mrletsplay.secretreichstagandroid.R;

public class CreditsFragment extends Fragment {

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.credits, container, false);
		TextView t = v.findViewById(R.id.credits_text_view);
		t.setMovementMethod(LinkMovementMethod.getInstance());
		return v;
	}

}
