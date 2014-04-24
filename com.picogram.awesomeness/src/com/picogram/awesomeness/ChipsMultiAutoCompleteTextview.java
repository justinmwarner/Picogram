
package com.picogram.awesomeness;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

public class ChipsMultiAutoCompleteTextview extends MultiAutoCompleteTextView implements OnItemClickListener {

	private final String TAG = "ChipsMultiAutoCompleteTextview";

	private final TextWatcher textWather = new TextWatcher() {

		public void afterTextChanged(final Editable s) {
		}

		public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
		}

		public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
			if (count >= 1) {
				if (s.charAt(start) == ' ')
				{
					ChipsMultiAutoCompleteTextview.this.setChips(); // generate chips
				}
			}
		}
	};

	// Constructor
	public ChipsMultiAutoCompleteTextview(final Context context) {
		super(context);
		this.init(context);
	}

	// Constructor
	public ChipsMultiAutoCompleteTextview(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		this.init(context);
	}

	// Constructor
	public ChipsMultiAutoCompleteTextview(final Context context, final AttributeSet attrs,
			final int defStyle) {
		super(context, attrs, defStyle);
		this.init(context);
	}

	// set listeners for item click and text change
	public void init(final Context context) {
		this.setOnItemClickListener(this);
		this.addTextChangedListener(this.textWather);
	}

	public void onItemClick(final AdapterView parent, final View view, final int position, final long id) {
		this.setChips(); // call generate chips when user select any item from auto complete
	}

	// This function has whole logic for chips generate
	public void setChips() {
		if (this.getText().toString().contains(" ")) // check comman in string
		{

			final SpannableStringBuilder ssb = new SpannableStringBuilder(this.getText());
			// split string wich comma
			final String chips[] = this.getText().toString().split(" ");
			int x = 0;
			// loop will generate ImageSpan for every country name separated by comma
			for (final String c : chips) {
				// inflate chips_edittext layout
				final LayoutInflater lf = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				final TextView textView = (TextView) lf.inflate(R.layout.chips_edittext, null);
				textView.setText(c); // set text
				this.setFlags(textView, c); // set flag image
				// capture bitmapt of genreated textview
				final int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
				textView.measure(spec, spec);
				textView.layout(0, 0, textView.getMeasuredWidth(), textView.getMeasuredHeight());
				final Bitmap b = Bitmap.createBitmap(textView.getWidth(), textView.getHeight(), Bitmap.Config.ARGB_8888);
				final Canvas canvas = new Canvas(b);
				canvas.translate(-textView.getScrollX(), -textView.getScrollY());
				textView.draw(canvas);
				textView.setDrawingCacheEnabled(true);
				final Bitmap cacheBmp = textView.getDrawingCache();
				final Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
				textView.destroyDrawingCache(); // destory drawable
				// create bitmap drawable for imagespan
				final BitmapDrawable bmpDrawable = new BitmapDrawable(viewBmp);
				bmpDrawable.setBounds(0, 0, bmpDrawable.getIntrinsicWidth(), bmpDrawable.getIntrinsicHeight());
				// create and set imagespan
				ssb.setSpan(new ImageSpan(bmpDrawable), x, x + c.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				x = x + c.length() + 1;
			}
			// set chips span
			this.setText(ssb);
			// move cursor to last
			this.setSelection(this.getText().length());
		}

	}

	// this method set country flag image in textview's drawable component, this logic is not optimize, you need to change as per your requirement
	public void setFlags(final TextView textView, String country) {
		country = country.trim();

	}

}
