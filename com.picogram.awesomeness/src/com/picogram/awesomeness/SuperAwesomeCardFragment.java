
package com.picogram.awesomeness;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;

import com.flurry.android.FlurryAgent;
import com.picogram.awesomeness.DialogMaker.OnDialogResultListener;
import com.stackmob.sdk.api.StackMobQuery;
import com.stackmob.sdk.callback.StackMobCallback;
import com.stackmob.sdk.callback.StackMobModelCallback;
import com.stackmob.sdk.callback.StackMobQueryCallback;
import com.stackmob.sdk.exception.StackMobException;
import com.stackmob.sdk.model.StackMobModel;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class SuperAwesomeCardFragment extends Fragment implements
		OnItemClickListener, OnItemLongClickListener {

	private static final String ARG_POSITION = "position";
	private static final String TAG = "SuperAwesomeCardFragment";
	public static final int CREATE_RESULT = 100;
	public static final int GAME_RESULT = 1337;

	public static SuperAwesomeCardFragment newInstance(final int position) {
		final SuperAwesomeCardFragment f = new SuperAwesomeCardFragment();
		final Bundle b = new Bundle();
		b.putInt(ARG_POSITION, position);
		f.setArguments(b);
		return f;
	}

	private int position;

	PicogramListAdapter myAdapter;
	Handler h = new Handler();
	SQLitePicogramAdapter sql = null;

	public void clearAdapter() {
		if (myAdapter != null) {
			this.myAdapter.clear();
			this.myAdapter.notifyDataSetChanged();
		}
	}

	public void getMyPuzzles(final FragmentActivity a) {
		this.sql = new SQLitePicogramAdapter(a, "Picograms", null, 1);

		final String[][] PicogramsArray = this.sql.getPicograms();
		final SharedPreferences prefs = Util.getPreferences(a);
		for (int i = 0; i < PicogramsArray.length; i++) {
			final String temp[] = PicogramsArray[i];
			final String id = temp[0];
			final String name = temp[2];
			final String rate = temp[3];
			final String width = temp[7];
			final String height = temp[8];
			final String current = temp[5];
			final String solution = temp[4];
			final String diff = temp[6];
			final String author = temp[1];
			final String status = temp[9];
			final String personalRank = temp[12];
			final String isUploaded = temp[13];
			int numColors = 0;
			String colors = null;
			if ((temp[10] != null) && (temp[11] != null)) {
				numColors = Integer.parseInt(temp[10]);
				colors = temp[11];
			}
			boolean isAdd = true;

			if (prefs != null) {
				if (prefs.getBoolean("wonvisible", false)
						|| prefs.getInt("mySetting", 0) == 1) {
					if (status.equals("1")) {
						isAdd = false;
					}
				}
			}
			if (isAdd) {
				final GriddlerOne tempPicogram = new GriddlerOne(id, status,
						name, diff, rate, 0, author, width, height, solution,
						current, numColors, colors, isUploaded, personalRank);
				if (status.equals("2") || !Util.isOnline()) {
					a.runOnUiThread(new Runnable() {

						public void run() {
							SuperAwesomeCardFragment.this.myAdapter
									.add(tempPicogram);
							SuperAwesomeCardFragment.this.myAdapter
									.notifyDataSetChanged();
						}

					});

				} else {
					// Get data from online about the Picogram for its updated
					// rating.
					// These variables should be removed.
					final String cols = colors;
					final int nc = numColors;
					final String oldStatus = status; // Don't get rid of this.
					final String oldCurrent = current;
					final GriddlerOne g = new GriddlerOne();
					g.setID(id);
					// Add the Puzzle, then update in the adapter later on.
					myAdapter.add(tempPicogram);

					g.fetch(new StackMobModelCallback() {
						@Override
						public void failure(final StackMobException arg0) {
							// Don't do anything, we already added it.
						}

						@Override
						public void success() {
							if (!SuperAwesomeCardFragment.this.myAdapter
									.existsById(g.getID())) {
								// TODO Update the ranking.
								a.runOnUiThread(new Runnable() {
									public void run() {
										// TODO Test this, should update rating.
										for (int i = 0; i != myAdapter
												.getCount(); ++i) {

											if (myAdapter.get(i).getID() == g
													.getID()) {
												myAdapter.remove(tempPicogram);
												g.setStatus(oldStatus);
												g.setCurrent(oldCurrent);
												myAdapter.add(g);
												return;
											}
										}
										SuperAwesomeCardFragment.this.myAdapter
												.notifyDataSetChanged();
									}
								});
							}
						}
					});
				}
			}
		}

	}

	public void getRecentPuzzles(final Activity a) {
		StackMobModel.query(
				GriddlerOne.class,
				new StackMobQuery()
						.isInRange(0, 9)
						.fieldIsNotEqual("griddlerone_id", "random")
						.fieldIsOrderedBy("createddate",
								StackMobQuery.Ordering.DESCENDING),
				new StackMobQueryCallback<GriddlerOne>() {

					@Override
					public void failure(final StackMobException arg0) {
						Crouton.makeText(a,
								"Error fetching data: " + arg0.toString(),
								Style.ALERT);

					}

					@Override
					public void success(final List<GriddlerOne> gs) {
						for (final GriddlerOne g : gs) {
							SuperAwesomeCardFragment.this.myAdapter.add(g);
							SuperAwesomeCardFragment.this.myAdapter
									.notifyDataSetChanged();
						}
					}
				});
	}

	public void getSortedPuzzles(final Activity a, final String sort) {
		this.myAdapter.clear();
		StackMobQuery smq = new StackMobQuery().isInRange(0, 9)
				.fieldIsOrderedBy(sort, StackMobQuery.Ordering.DESCENDING);
		if (sort.equals("rate")) {
			// Weekly, Monthly, All Time. Rate only matters, createddate is in
			// general, gonna retreive most recent.
			Calendar date = Calendar.getInstance();
			if (Util.getPreferences(getActivity()).getInt("topSetting", 0) == 0) {
				Log.d(TAG, "Rate: Doing a week");
				date.add(Calendar.DAY_OF_MONTH, -7);
				smq.fieldIsGreaterThan(
						"createddate",
						TimeUnit.MILLISECONDS
								.toMillis(date.getTime().getTime()) + "");
			} else if (Util.getPreferences(getActivity()).getInt("topSetting",
					0) == 1) {
				Log.d(TAG, "Rate: Doing a Month");
				date.add(Calendar.MONTH, -1);
				smq.fieldIsGreaterThan(
						"createddate",
						TimeUnit.MILLISECONDS
								.toMillis(date.getTime().getTime()) + "");
			}
		}
		StackMobModel.query(
				GriddlerOne.class,
				smq,
				new StackMobQueryCallback<GriddlerOne>() {

					@Override
					public void failure(final StackMobException arg0) {
						Crouton.makeText(a,
								"Error fetching data: " + arg0.toString(),
								Style.ALERT);
						SuperAwesomeCardFragment.this.myAdapter
								.notifyDataSetChanged();
					}

					@Override
					public void success(final List<GriddlerOne> gs) {
						for (final GriddlerOne g : gs) {
							a.runOnUiThread(new Runnable() {

								public void run() {
									SuperAwesomeCardFragment.this.myAdapter
											.add(g);
									SuperAwesomeCardFragment.this.myAdapter
											.notifyDataSetChanged();
								}

							});
						}
					}
				});
	}

	public void getPackPuzzles() {
		this.myAdapter.clear();
		boolean isHideDownloaded = Util.getPreferences(this.getActivity())
				.getInt("packsSetting", 0) == 0;
		Log.d(TAG,
				"Setting "
						+ " Got: "
						+ Util.getPreferences(this.getActivity()).getInt(
								"packsSetting", 0)
						+ " "
						+ isHideDownloaded
						+ " "
						+ !Util.getPreferences(this.getActivity()).getBoolean(
								"hasDownloadedEasyTwo", false));
		GriddlerOne go = new GriddlerOne();
		go.setName("Easy Pack 1 (10 puzzles)");
		go.setStatus("2");
		if (isHideDownloaded
				|| !Util.getPreferences(this.getActivity()).getBoolean(
						"hasDownloadedEasyOne", false)) {
			myAdapter.add(go);
			myAdapter.notifyDataSetChanged();
		}
		go = new GriddlerOne();
		go.setName("Easy Pack 2 (10 puzzles)");
		go.setStatus("2");
		if (isHideDownloaded
				|| !Util.getPreferences(this.getActivity()).getBoolean(
						"hasDownloadedEasyTwo", false)) {
			myAdapter.add(go);
			myAdapter.notifyDataSetChanged();
		}
		go = new GriddlerOne();
		go.setName("Medium Pack 1 (10 puzzles)");
		go.setStatus("2");
		if (isHideDownloaded
				|| !Util.getPreferences(this.getActivity()).getBoolean(
						"hasDownloadedMediumOne", false)) {
			myAdapter.add(go);
			myAdapter.notifyDataSetChanged();
		}
	}

	public void getTagPuzzles(final Activity a, final String tag,
			final boolean isSortByRate) {
		this.myAdapter.clear();
		final StackMobQuery smq = new StackMobQuery().fieldIsEqualTo("tag",
				tag.toLowerCase(Locale.ENGLISH));
		StackMobModel.query(GriddlerTag.class, smq,
				new StackMobQueryCallback<GriddlerTag>() {

					@Override
					public void failure(final StackMobException arg0) {
						Crouton.makeText(a,
								"Error fetching data: " + arg0.toString(),
								Style.ALERT);
					}

					@Override
					public void success(final List<GriddlerTag> gts) {
						final ArrayList<String> ids = new ArrayList();
						for (final GriddlerTag gt : gts) {
							ids.add(gt.getID());
						}
						final StackMobQuery smqInner = new StackMobQuery()
								.isInRange(0, 9).fieldIsIn("griddlerone_id",
										ids);

						if (isSortByRate) {
							smqInner.fieldIsOrderedBy("rate",
									StackMobQuery.Ordering.DESCENDING);
						} else {
							smqInner.fieldIsOrderedBy("createddate",
									StackMobQuery.Ordering.DESCENDING);
						}
						GriddlerOne.query(GriddlerOne.class, smqInner,
								new StackMobQueryCallback<GriddlerOne>() {

									@Override
									public void failure(
											final StackMobException arg0) {
										Crouton.makeText(
												a,
												"Error fetching data: "
														+ arg0.toString(),
												Style.ALERT);
									}

									@Override
									public void success(
											final List<GriddlerOne> gs) {
										for (final GriddlerOne g : gs) {
											a.runOnUiThread(new Runnable() {

												public void run() {
													SuperAwesomeCardFragment.this.myAdapter
															.add(g);
													SuperAwesomeCardFragment.this.myAdapter
															.notifyDataSetChanged();
												}
											});
										}
									}
								});

					}
				});
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.position = this.getArguments().getInt(ARG_POSITION);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {

		final LayoutParams params = new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT);

		final FrameLayout fl = new FrameLayout(this.getActivity());
		fl.setLayoutParams(params);

		final int margin = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 8, this.getResources()
						.getDisplayMetrics());

		final ListView v = new ListView(this.getActivity());
		params.setMargins(margin, margin, margin, margin);
		v.setLayoutParams(params);
		v.setLayoutParams(params);
		v.setBackgroundResource(R.drawable.background_card);
		// final List<String> items = new ArrayList();
		this.myAdapter = new PicogramListAdapter(this.getActivity(),
				R.id.tvName);
		if (this.position == MenuActivity.TITLES.indexOf("My")) {
			this.getMyPuzzles(this.getActivity());
		} else if (this.position == MenuActivity.TITLES.indexOf("Top")) {
			this.getSortedPuzzles(this.getActivity(), "rate");
		} else if (this.position == MenuActivity.TITLES.indexOf("Recent")) {
			this.getSortedPuzzles(this.getActivity(), "createddate");
		} else if (this.position == MenuActivity.TITLES.indexOf("Search")) {
			// Don't load anything on start.
			// this.getTagPuzzles(this.getActivity(), "", true);
		} else if (this.position == MenuActivity.TITLES.indexOf("Prefs")) {
			return new View(this.getActivity());
		} else if (this.position == MenuActivity.TITLES.indexOf("Packs")) {
			this.getPackPuzzles();
		} else {
			for (int i = 0; i != 20; ++i) {
				final GriddlerOne obj = new GriddlerOne("=/", "0",
						"Had an error. You shouldn't see this " + i, "0", "0",
						1, "Justin", "1", "1", "1", "0", 2, Color.BLACK + " "
								+ Color.RED, "1", "0");
				obj.setID(i + "" + this.position);
				this.myAdapter.add(obj);
				// items.add(MenuActivity.TITLES.get(this.position) + " " +
				// this.position + " " + i);
			}
		}
		v.setAdapter(this.myAdapter);
		v.setOnItemClickListener(this);
		v.setLongClickable(true);
		v.setOnItemLongClickListener(this);

		fl.addView(v);
		return fl;
	}

	@Override
	public void onDestroy() {
		if (this.sql != null) {
			this.sql.close();
		}
		super.onDestroy();
	}

	public void onItemClick(final AdapterView<?> parent, final View v,
			final int pos, final long id) {
		if (pos >= 0) // If valid position to select.
		{
			GriddlerOne picogram = this.myAdapter.get(pos);
			if (this.position == MenuActivity.TITLES.indexOf("Packs")) {
				// We load the pack to My and prompt user.
				if (picogram.getName().contains("Easy Pack 1")) {
					loadEasyPackOne();
					Util.getPreferences(this.getActivity()).edit()
							.putBoolean("hasDownloadedEasyOne", true).commit();
				} else if (picogram.getName().contains("Easy Pack 2")) {
					loadEasyPackTwo();
					Util.getPreferences(this.getActivity()).edit()
							.putBoolean("hasDownloadedEasyTwo", true).commit();
				} else if (picogram.getName().contains("Medium Pack 1")) {
					loadMediumPackOne();
					Util.getPreferences(this.getActivity()).edit()
							.putBoolean("hasDownloadedMediumOne", true)
							.commit();
				} else {
					Crouton.makeText(
							this.getActivity(),
							picogram.getName()
									+ " is not currently supported.  Report a bug.",
							Style.INFO).show();
					return;
				}
				sql.close();
				Crouton.makeText(this.getActivity(),
						picogram.getName() + " loaded, go back to My tab.",
						Style.INFO).show();

			} else {
				if ((this.position == MenuActivity.TITLES.indexOf("My"))
						&& ((pos == 0) || pos == 1)) {
					// Can this be the Creating or Random?
					this.sql.close();
					if (pos == 0) {
						final Intent createIntent = new Intent(
								this.getActivity(),
								CreatePicogramActivity.class);
						this.getActivity().startActivityForResult(createIntent,
								MenuActivity.CREATE_CODE);
					} else if (pos == 1) {
						generateRandomGame();
					}
				} else {
					// If this Picogram doesn't exists for the person, add it.
					if (sql == null)
						this.sql = new SQLitePicogramAdapter(
								this.getActivity(), "Picograms", null, 1);
					if (sql.doesPuzzleExist(picogram) == -1) {
						sql.addUserPicogram(picogram);
						// Add to the ranking table.
						SQLiteRatingAdapter sra = new SQLiteRatingAdapter(
								this.getActivity(), "Rating", null, 2);
						sra.insertOnOpenOnlineGame(picogram.getID());
						sra.close();
					}
					this.startGame(picogram);
				}
			}
		}
	}

	private void loadMediumPackOne() {
		this.sql = new SQLitePicogramAdapter(this.getActivity(), "Picograms",
				null, 1);
		GriddlerOne go = new GriddlerOne();
		// go.setName("Rose");
		// go.setSolution("000111101110000001001110011100010011001011010110110110111011111010110011010101111101111111011011111100110111101111011110011110000111100001110011111000000001111000000000010000000000111001000011100011111001111000001111011111000000110111110000000001111000000000001000000000000000100000000");
		// go.setWidth("15");
		// go.setHeight("20");
		// go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		// go.nullsToValue(this.getActivity());
		// sql.addUserPicogram(go);
		go = new GriddlerOne();
		go.setName("House");
		go.setSolution("00000111000000001110111000011100000111011000000000111111111111111100000000000110111110000011010101011111101111101000110101010100011011111010011100000001000110000000100011111111111111");
		go.setWidth("13");
		go.setHeight("14");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		// go.setName("Water Drop");
		// go.setSolution("000001000000011100000110100000100110001100010011000011110000001100000001100000001100000001110000011011100110000111100");
		// go.setWidth("9");
		// go.setHeight("15");
		// go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		// go.nullsToValue(this.getActivity());
		// sql.addUserPicogram(go);
		// go = new GriddlerOne();
		// go.setName("Arrow Heart");
		// go.setSolution("000000000111000000000011000100010101001010101000010001010100010000100100001001001000000110010000000110100000001001000000110000000000010000000000");
		// go.setWidth("12");
		// go.setHeight("12");
		// go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		// go.nullsToValue(this.getActivity());
		// sql.addUserPicogram(go);
		go = new GriddlerOne();
		go.setName("X Wins");
		go.setSolution("1001100001000001101000010110011010000101101001100001000011111111111111100110000100000110101101000001101011010000100110000100001111111111111110011000011001011010110101100110101101011010011000011001");
		go.setWidth("14");
		go.setHeight("14");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		go.setName("Ship");
		go.setSolution("0000000000001111000000000000011000000011100000000000011100000000010000000000000010000011111111100000011111111101000000100000001000000100100000100000000100000100001000011000000001000011001111111110000001111111110000010000000000000010000011111111100000011111111111000000100000000100000101000000010000000110000100100000011100000010000010100000001011000001000001010000001000110000010000101000000100001100111110010100000011000010001101111001000000100111100011000100100111110111111000111111011110111111111001111111101000011111111100001111111000011111111100000111111111111111111110000001111111111111111111000000001111111111111100000");
		go.setWidth("25");
		go.setHeight("25");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		go.setName("Bird");
		go.setSolution("0000000111000000000000000000001110110000000000000000000011111000000000000000000000111111000000000000000000011111111000000000000000001111111111000000000000000111111111110000000000000011111111111110000000000001111111111111111110011110011111111111111100011111100011111111100000011111110000010011111100001000000110010010000111000001111101111110100000000001111100011101101111100000111001111001101001111111110001111101110111001100010000111100111011100011001000111110111101111011110000011110011110111101111000001110001110011110111100000110000111001111001110000011000011100111100111000001000000110001100001100000100000001000100000100");
		go.setWidth("25");
		go.setHeight("25");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		go.setName("His Master's Voice");
		go.setSolution("0000000111100000000000000011111111000000000000111110011100001100001011000001100011100000100000111000111100111010011110000111001100110111100010111010000001101000110110110000010010000110110111100001110000110110011111110100001111100100000001100110111011001000001011000000101010000010100000001010101100111000000010101010000111000000101010100001111000011110111110011111100101001000000111111001111111111111");
		go.setWidth("20");
		go.setHeight("20");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		go.setName("Singing");
		go.setSolution("00000000010000000000000000000000011100000001110000000000011111000001111100000000001010100000111111100000000101010000001111111000000011011000000100111100000000111000011010001110000000011001111111100110000011111100000110011110000011111100000011000111100001100000000001111110011000110000000000100000001100011100000001111000001110000110000000100111111111011111110000110000111111111111111100010110001111111111111110011011100011110111111111001011011000111001111111001111000110011100011111001111100001111100");
		go.setWidth("20");
		go.setHeight("25");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		go.setName("Teddy");
		go.setSolution("0011111011111111011110000001100111000000110011000000110010000000001101100000011001000000000010110000001101000000000001110000000011100011000110010000000000010001001101001000000000001000000000000100000000000110000000000010000000000001000000000011000000011100011111001111000111010011111111111111111110011000000011111111110000001100000100011001101000000111111110011100110111111110000001001110001101000000000111110111000110101111000110001111100011011100110010111011110001101000111001011101011000110100011100101110100000000010001110010111011100000011100111001011101011111111011011100111111100000000000111110001111100000000000000000");
		go.setWidth("25");
		go.setHeight("25");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		sql.close();
	}

	private void loadEasyPackTwo() {
		this.sql = new SQLitePicogramAdapter(this.getActivity(), "Picograms",
				null, 1);
		// These shouldn't change for these packs.
		GriddlerOne go = new GriddlerOne();
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		// Change.
		go.setName("Key");
		go.setSolution("000001111111110100100111");
		go.setWidth("8");
		go.setHeight("3");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		go.setName("Cat");
		go.setSolution("00111111010000111011111000010100");
		go.setWidth("8");
		go.setHeight("4");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		go.setName("Heart");
		go.setSolution("01010111110111000100");
		go.setWidth("5");
		go.setHeight("4");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		go.setName("Hourglass");
		go.setSolution("1111111111111110000000001111111111111110100000000010010000000001001001010100100100010100010011000100011000110000011000001101011000000011011000000001101100000001100011000001100100110001100000001100100001000010010001010001001001010100100101010101010111111111111111000000000111111111111111");
		go.setWidth("13");
		go.setHeight("22");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		go.setName("Cube");
		go.setSolution("00111111110100000101111111100110000010011000001001100000100110000010101111111100");
		go.setWidth("10");
		go.setHeight("8");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		go.setName("Eye");
		go.setSolution("0001111000001100110001001100101000110001010011001000110011000001111000");
		go.setWidth("10");
		go.setHeight("7");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		go.setName("Note");
		go.setSolution("011111010001010001110011110011");
		go.setWidth("6");
		go.setHeight("5");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		go.setName("Shot");
		go.setSolution("111010010010111101101101111111111010010");
		go.setWidth("3");
		go.setHeight("13");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		go.setName("Flower");
		go.setSolution("001000101010101010100010010101011100010000100");
		go.setWidth("5");
		go.setHeight("9");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		go.setName("Bomb");
		go.setSolution("00000001100000000100100000010000100000100000000111100000011111100001110111100111011111101101111111011011111110011111111000011111100000011110000");
		go.setWidth("11");
		go.setHeight("13");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		sql.close();
	}

	private void loadEasyPackOne() {
		this.sql = new SQLitePicogramAdapter(this.getActivity(), "Picograms",
				null, 1);
		// These shouldn't change for these packs.
		GriddlerOne go = new GriddlerOne();
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		// Change.
		go.setName("Smile");
		go.setSolution("01010000001000101110");
		go.setWidth("5");
		go.setHeight("4");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		go.setName("Sad");
		go.setSolution("01010000000111010001");
		go.setWidth("5");
		go.setHeight("4");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		go.setName("Person");
		go.setSolution("010111010101");
		go.setWidth("3");
		go.setHeight("4");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		go.setName("Umbrella Man");
		go.setSolution("1010000110111111011010100");
		go.setWidth("5");
		go.setHeight("5");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		go.setName("Football Man");
		go.setSolution("0010001111000101010100100");
		go.setWidth("5");
		go.setHeight("5");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		go.setName("Scorpion");
		go.setSolution("0110010000100110111000011");
		go.setWidth("5");
		go.setHeight("5");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		go.setName("Drummer");
		go.setSolution("0100010010101001101101011");
		go.setWidth("5");
		go.setHeight("5");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		go.setName("Peace");
		go.setSolution("001110001010101001001101010101000100011100");
		go.setWidth("7");
		go.setHeight("6");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		go.setName("Shopping");
		go.setSolution("0001111110100100110010010");
		go.setWidth("5");
		go.setHeight("5");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		go.setName("Two Prayers");
		go.setSolution("1010001010111100101001101");
		go.setWidth("5");
		go.setHeight("5");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.nullsToValue(this.getActivity());
		sql.addUserPicogram(go);
		go = new GriddlerOne();
		sql.close();
	}

	private void startGame(GriddlerOne go) {
		FlurryAgent.logEvent("UserPlayGame");
		// Intent gameIntent = new Intent(this, AdvancedGameActivity.class);
		final Intent gameIntent = new Intent(this.getActivity(),
				AdvancedGameActivity.class);
		gameIntent.putExtra("name", go.getName());
		gameIntent.putExtra("solution", go.getSolution());
		gameIntent.putExtra("current", go.getCurrent());
		gameIntent.putExtra("width", go.getWidth());
		gameIntent.putExtra("height", go.getHeight());
		gameIntent.putExtra("id", go.getID());
		gameIntent.putExtra("name", go.getName());
		gameIntent.putExtra("colors", go.getColors());
		this.getActivity().startActivityForResult(gameIntent,
				MenuActivity.GAME_CODE);
	}

	public void generateRandomGame() {
		FragmentTransaction ft = getChildFragmentManager().beginTransaction();
		// Create and show the dialog.
		Bundle bundle = new Bundle();
		bundle.putInt("layoutId", R.layout.dialog_random_picogram);
		DialogMaker newFragment = new DialogMaker();
		newFragment.setArguments(bundle);
		newFragment.show(ft, "dialog");
		newFragment.setOnDialogResultListner(new OnDialogResultListener() {

			public void onDialogResult(Bundle result) {
				// Add to personal.
				String current = "", cols = "";
				for (int i = 0; i != result.getString("solution").length(); ++i)
					current += "0";
				for (int i = 0; i != result.getInt("numColors"); ++i)
					if (i == 0)
						cols += Color.TRANSPARENT + ",";
					else
						cols += Color.rgb((int) (Math.random() * 255),
								(int) (Math.random() * 255),
								(int) (Math.random() * 255))
								+ ",";
				cols = cols.substring(0, cols.length() - 1);
				int w = result.getInt("width"), h = result.getInt("height"), nc = result
						.getInt("numColors");
				final GriddlerOne go = new GriddlerOne(result.getString(
						"solution").hashCode()
						+ "", "0", result.getString("name"),
						((int) ((w * h) / 1400)) + "", "3", 1, "computer", w
								+ "", h + "", result.getString("solution"),
						current, nc, cols, "0", "5");
				// TODO make sure go gets saved.
				myAdapter.add(go);
				myAdapter.notifyDataSetChanged();
				go.setCurrent(null);
				go.save(new StackMobCallback() {

					@Override
					public void failure(StackMobException arg0) {
						sql.addUserPicogram(go);
					}

					@Override
					public void success(String arg0) {
						go.setIsUploaded("1");
						sql.addUserPicogram(go);
					}
				});
				GriddlerTag gt = new GriddlerTag("random");
				gt.setID(go.getID());
				gt.save();
				SQLiteRatingAdapter sra = new SQLiteRatingAdapter(
						getActivity(), "Rating", null, 2);
				sra.insertCreate(gt.getID());
				sra.close();
				// Start to play.
			}
		});
	}

	public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
			final int position, long arg3) {
		if (!myAdapter.get(0).getName().contains("Create")) {
			// We only want long click support on My tab.
			return false;
		}
		if (position == 0 || position == 1) {
			// Create or Random, should just ignore this?
			return false;
		} else {
			FragmentTransaction ft = getChildFragmentManager()
					.beginTransaction();
			// Create and show the dialog.
			Bundle bundle = new Bundle();
			bundle.putInt("layoutId", R.layout.dialog_listview_contextmenu);
			DialogMaker newFragment = new DialogMaker();
			newFragment.setArguments(bundle);
			newFragment.show(ft, "dialog");
			newFragment.setOnDialogResultListner(new OnDialogResultListener() {

				public void onDialogResult(Bundle res) {
					if (res == null) {
						return;
					}
					int result = res.getInt("resultInt");
					if (result == 0) {
						// Nothing
						// TODO
					} else if (result == 1) {
						// Clear.
						String newCurrent = "";
						for (int i = 0; i != myAdapter.get(position)
								.getCurrent().length(); ++i) {
							newCurrent += "0";
						}
						myAdapter.updateCurrentById(myAdapter.get(position)
								.getID(), newCurrent, "0");
						sql.updateCurrentPicogram(myAdapter.get(position)
								.getID(), "0", newCurrent);
					} else if (result == 2) {
						// Delete.
						sql.deletePicogram(myAdapter.get(position).getID());
						myAdapter.removeById(myAdapter.get(position).getID());
						// TODO Remove from personal ranking table.
					}
					myAdapter.notifyDataSetChanged();
				}
			});

		}
		return true;
	}
}
