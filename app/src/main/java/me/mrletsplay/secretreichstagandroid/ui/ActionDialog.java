package me.mrletsplay.secretreichstagandroid.ui;

import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;

public class ActionDialog {

	private MovableFloatingActionButton actionButton;
	private AlertDialog dialog;

	public ActionDialog(MovableFloatingActionButton actionButton, AlertDialog dialog) {
		this.actionButton = actionButton;
		this.dialog = dialog;
	}

	public MovableFloatingActionButton getActionButton() {
		return actionButton;
	}

	public AlertDialog getDialog() {
		return dialog;
	}

	public void dismiss() {
		ViewGroup parent = ((ViewGroup) actionButton.getParent());
		if(parent != null) parent.removeView(actionButton);
		dialog.dismiss();
	}

}
