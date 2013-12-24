package com.picogram.awesomeness;

import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment.NumberPickerDialogHandler;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DialogMaker extends DialogFragment implements View.OnClickListener {

	int randomHeight, randomWidth, randomColors; // Used in random.
	TextView tvHeight, tvWidth, tvColors; // Used in random.

	public DialogMaker() {
		super();
	}

	public interface OnDialogResultListener {
		public void onDialogResult(Bundle result);
	}

	OnDialogResultListener listener;

	public void setOnDialogResultListner(OnDialogResultListener odrl) {
		listener = odrl;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		savedInstanceState = getArguments();
		int li = 0;
		if (savedInstanceState != null) {
			li = savedInstanceState.getInt("layoutId");
		}
		final int layoutId = li;
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View v = inflater.inflate(layoutId, null);

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(v);
		builder
		// Add action buttons
		.setPositiveButton("Done", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Bundle bundle = new Bundle();
				if (layoutId == R.layout.dialog_random_griddler) {
					bundle.putInt("width", randomWidth);
					bundle.putInt("height", randomHeight);
					bundle.putInt("numColors", randomColors);
					bundle.putString("solution", randomPuzzle);
					bundle.putString("name",
							"Random #" + randomPuzzle.hashCode());
					bundle.putString("tags", "random");
				}
				listener.onDialogResult(bundle);
			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		// Setup listeners and attributes of dialog UI elements.
		if (layoutId == R.layout.dialog_listview_contextmenu) {
			setupLongClick(v);
		} else if (layoutId == R.layout.dialog_random_griddler) {
			setupRandom(v);
		} else if (layoutId == R.layout.dialog_ranking) {
			setupRating(v);
		} else if (layoutId == R.layout.dialog_save_griddler) {
			setupCreate(v);
		}

		return builder.create();
	}

	public void setupCreate(View v) {
	}

	public void setupLongClick(View v) {
		Button bClear = (Button) v.findViewById(R.id.bClearPuzzle);
		Button bDelete = (Button) v.findViewById(R.id.bDeletePuzzle);
		Button bNewRate = (Button) v.findViewById(R.id.bSubmitNewRank);
		bClear.setOnClickListener(this);
		bDelete.setOnClickListener(this);
		bNewRate.setOnClickListener(this);
	}

	public void setupRating(View v) {
	}

	public void setupRandom(View v) {
		Button bHeight = (Button) v.findViewById(R.id.bRandomHeight);
		Button bWidth = (Button) v.findViewById(R.id.bRandomWidth);
		Button bColors = (Button) v.findViewById(R.id.bRandomNumColors);

		bHeight.setOnClickListener(this);
		bWidth.setOnClickListener(this);
		bColors.setOnClickListener(this);

		tvHeight = (TextView) v.findViewById(R.id.tvRandomHeight);
		tvWidth = (TextView) v.findViewById(R.id.tvRandomWidth);
		tvColors = (TextView) v.findViewById(R.id.tvRandomNumColors);

		randomHeight = (int) Math.floor((Math.random() * 24)) + 2;
		randomWidth = (int) Math.floor((Math.random() * 24)) + 2;
		randomColors = (int) Math.floor((Math.random() * 8)) + 2;

		etName = (EditText) v.findViewById(R.id.etRandomName);
		etTag = (EditText) v.findViewById(R.id.etRandomTags);
		etTag.setText("Random");
		etName.setOnKeyListener(null);
		etTag.setOnKeyListener(null);

		updateViews();
	}

	public void updateViews() {
		tvHeight.setText(randomHeight + "");
		tvWidth.setText(randomWidth + "");
		tvColors.setText(randomColors + "");
		generateRandom();
		etName.setText("Random: #" + randomPuzzle.hashCode());
	}

	String randomPuzzle = "";

	private void generateRandom() {
		randomPuzzle = "";
		for (int i = 0; i != randomHeight; ++i) {
			for (int j = 0; j != randomWidth; ++j) {
				randomPuzzle += ((int) (Math.random() * randomColors));
			}
		}
	}

	EditText etName, etTag;

	public void onClick(View v) {
		Bundle bundle = new Bundle();
		switch (v.getId()) {
		case R.id.bSubmitNewRank:
			bundle.putInt("resultInt", 0);
			listener.onDialogResult(bundle);
			this.getDialog().dismiss();
			break;
		case R.id.bClearPuzzle:
			bundle.putInt("resultInt", 1);
			listener.onDialogResult(bundle);
			this.getDialog().dismiss();
			break;
		case R.id.bDeletePuzzle:
			bundle.putInt("resultInt", 2);
			listener.onDialogResult(bundle);
			this.getDialog().dismiss();
			break;
		case R.id.bRandomHeight:
		case R.id.bRandomWidth:
		case R.id.bRandomNumColors:
			showNumberDialog(v.getId());
			break;
		default:
			// Ignore if doesn't exist.
			break;
		}
	}

	Handler handler = new Handler();

	private void showNumberDialog(final int id) {
		NumberPickerBuilder npb = new NumberPickerBuilder()
				.setStyleResId(R.style.MyCustomBetterPickerTheme)
				.setFragmentManager(this.getFragmentManager())
				.setPlusMinusVisibility(View.INVISIBLE)
				.setDecimalVisibility(View.INVISIBLE);
		if (id == R.id.bRandomNumColors) {
			npb.setMinNumber(2);
			npb.setMaxNumber(10);
		} else {
			if (id == R.id.bRandomWidth)
				Crouton.makeText(this.getActivity(), "Give us a new width.",
						Style.INFO).show();
			else
				Crouton.makeText(this.getActivity(), "Give us a new height.",
						Style.INFO).show();
			npb.setMaxNumber(25);
			npb.setMinNumber(1);
		}
		npb.addNumberPickerDialogHandler(new NumberPickerDialogHandler() {

			public void onDialogNumberSet(int reference, final int number,
					double decimal, boolean isNegative, double fullNumber) {
				handler.post(new Runnable() {
					public void run() {
						if (id == R.id.bRandomNumColors) {
							randomColors = number;
						} else if (id == R.id.bRandomWidth) {
							randomWidth = number;
						} else if (id == R.id.bRandomHeight) {
							randomHeight = number;
						}
						updateViews();
					}
				});
			}
		});
		npb.show();
	}

	private static final String TAG = "DialogMaker";

}
