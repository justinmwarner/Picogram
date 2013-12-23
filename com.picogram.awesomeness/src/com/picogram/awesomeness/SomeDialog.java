package com.picogram.awesomeness;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SomeDialog extends DialogFragment {
	public SomeDialog() {
		super();
	}

	public interface OnDialogResultListener {
		public void onDialogResult(int result);
	}

	OnDialogResultListener listener;

	public void setOnDialogResultListner(OnDialogResultListener odrl) {
		listener = odrl;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity()).setTitle("Title")
				.setTitle("Sure you wanna do this!")
				.setNegativeButton("Delete Puzzle", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						listener.onDialogResult(2);
					}
				}).setPositiveButton("Clear Puzzle", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						listener.onDialogResult(1);
					}
				}).setNeutralButton("Cancel", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Log.d("dd", "Cancellllll");
						listener.onDialogResult(0);
					}
				}).create();
	}
}
