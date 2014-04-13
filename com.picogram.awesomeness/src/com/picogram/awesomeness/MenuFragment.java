
package com.picogram.awesomeness;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.picogram.awesomeness.DialogMaker.OnDialogResultListener;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class MenuFragment extends Fragment implements
		OnItemClickListener, OnItemLongClickListener {

	private static final String ARG_POSITION = "position";
	private static final String TAG = "SuperAwesomeCardFragment";
	public static final int CREATE_RESULT = 100;
	public static final int GAME_RESULT = 1337;

	public static Date addDaysToDate(final Date date, final int noOfDays) {
		final Date newDate = new Date(date.getTime());

		final GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(newDate);
		calendar.add(Calendar.DATE, noOfDays);
		newDate.setTime(calendar.getTime().getTime());

		return newDate;
	}

	public static MenuFragment newInstance(final int position) {
		final MenuFragment f = new MenuFragment();
		final Bundle b = new Bundle();
		b.putInt(ARG_POSITION, position);
		f.setArguments(b);
		return f;
	}

	private int position;
	PicogramListAdapter myAdapter;
	Handler h = new Handler();

	SQLitePicogramAdapter sql = null;

	SmoothProgressBar pbLoad;

	public void clearAdapter() {
		if (this.myAdapter != null) {
			this.myAdapter.clear();
			this.myAdapter.notifyDataSetChanged();
		}
	}

	public void done(final List<ParseObject> result, final ParseException e) {
		if (e == null)
		{
			for (final ParseObject po : result)
			{
				MenuFragment.this.myAdapter.updateRateById(po.getString("puzzleId"), po.getInt("rate"));
				MenuFragment.this.myAdapter.notifyDataSetChanged();
			}
		} else {
			Log.d(TAG, "Error updating ratings on my puzzles: " + e.getMessage());
		}
		MenuFragment.this.pbLoad.setVisibility(View.INVISIBLE);
	}

	public void generateRandomGame() {
		final FragmentTransaction ft = this.getChildFragmentManager().beginTransaction();
		// Create and show the dialog.
		final Bundle bundle = new Bundle();
		bundle.putInt("layoutId", R.layout.dialog_random_picogram);
		final DialogMaker newFragment = new DialogMaker();
		newFragment.setArguments(bundle);
		newFragment.show(ft, "dialog");
		newFragment.setOnDialogResultListner(new OnDialogResultListener() {

			public void onDialogResult(final Bundle result) {
				// Add to personal.
				String current = "", cols = "";
				for (int i = 0; i != result.getString("solution").length(); ++i) {
					current += "0";
				}
				for (int i = 0; i != result.getInt("numColors"); ++i) {
					if (i == 0) {
						cols += Color.TRANSPARENT + ",";
					} else {
						cols += Color.rgb((int) (Math.random() * 255),
								(int) (Math.random() * 255),
								(int) (Math.random() * 255))
								+ ",";
					}
				}
				cols = cols.substring(0, cols.length() - 1);
				final int w = result.getInt("width"), h = result.getInt("height"), nc = result
						.getInt("numColors");
				final Picogram go = new Picogram(result.getString(
						"solution").hashCode()
						+ "", "0", result.getString("name"),
						((w * h) / 1400) + "", "3", 1, "computer", w
								+ "", h + "", result.getString("solution"),
						current, nc, cols);
				MenuFragment.this.myAdapter.add(go);
				MenuFragment.this.myAdapter.notifyDataSetChanged();
				go.setCurrent(null);
				go.save();
				final PicogramTag gt = new PicogramTag("random");
				gt.setID(go.getID());
				gt.save();
				final SQLiteRatingAdapter sra = new SQLiteRatingAdapter(
						MenuFragment.this.getActivity(), "Rating", null, 2);
				sra.insertCreate(gt.getID());
				sra.close();
				// Start to play.
			}
		});
	}

	public void getMyPuzzles(final FragmentActivity a) {
		this.pbLoad.setVisibility(View.VISIBLE);
		this.myAdapter.myPicograms.clear();
		this.myAdapter.clear();
		this.myAdapter.notifyDataSetChanged();
		this.sql = new SQLitePicogramAdapter(a, "Picograms", null, 1);

		final String[][] picogramsArray = this.sql.getPicograms();
		final String[] ids = new String[picogramsArray.length];
		final SharedPreferences prefs = Util.getPreferences(a);
		Log.d(TAG, "PREF: " + (prefs.getInt("mySetting", 0)));
		for (int i = 0; i < picogramsArray.length; i++) {
			final String temp[] = picogramsArray[i];
			final String id = temp[0];
			ids[i] = id;
			final String name = temp[2];
			final String rate = temp[3];
			final String width = temp[7];
			final String height = temp[8];
			final String current = temp[5];
			final String solution = temp[4];
			final String diff = temp[6];
			final String author = temp[1];
			final String status = temp[9];
			int numColors = 0;
			String colors = null;
			if ((temp[10] != null) && (temp[11] != null)) {
				numColors = Integer.parseInt(temp[10]);
				colors = temp[11];
			}
			boolean isAdd = true;

			if (prefs != null) {
				if (prefs.getBoolean("wonvisible", false)
						|| (prefs.getInt("mySetting", 0) == 1)) {
					Log.d(TAG, name + " - " + status);
					if (status.equals("1") || solution.equals(current.replaceAll("x|X", "0"))) {
						isAdd = false;
					}
				}
			}
			if (isAdd || status.equals("2")) {
				final Picogram tempPicogram = new Picogram(id, status,
						name, diff, rate, 0, author, width, height, solution,
						current, numColors, colors);
				a.runOnUiThread(new Runnable() {

					public void run() {
						MenuFragment.this.myAdapter.myPicograms
								.add(tempPicogram);
					}

				});
			}
		}
		this.myAdapter.picograms = this.myAdapter.myPicograms;
		MenuFragment.this.myAdapter.notifyDataSetChanged();
		// Update ratings only every hour.
		final long last = Util.getPreferences(this.getActivity()).getLong("lastMyUpdate", 0);
		final long currentTime = System.currentTimeMillis();

		if ((currentTime - last) < 3600000) {
			MenuFragment.this.pbLoad.setVisibility(View.INVISIBLE);
			return;
		}
		Util.getPreferences(this.getActivity()).edit().putLong("lastMyUpdate", currentTime).commit();
		// Update ratings.
		final ParseQuery<ParseObject> query = ParseQuery.getQuery("Picogram");
		query.whereContainedIn("puzzleId", Arrays.asList(ids));
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(final List<ParseObject> result, final ParseException e) {
				if (e == null)
				{
					for (final ParseObject po : result)
					{
						MenuFragment.this.myAdapter.updateRateById(po.getString("puzzleId"), po.getInt("rate"));
						MenuFragment.this.myAdapter.notifyDataSetChanged();
					}
				} else {
					Log.d(TAG, "Error updating ratings on my puzzles: " + e.getMessage());
				}
				MenuFragment.this.pbLoad.setVisibility(View.INVISIBLE);
			}

		});

	}

	public void getPackPuzzles() {
		this.myAdapter.clear();
		final boolean isHideDownloaded = Util.getPreferences(this.getActivity())
				.getInt("packsSetting", 0) == 0;
		Picogram go = new Picogram();
		go.setName("Easy Pack 1 (10 puzzles)");
		go.setStatus("2");
		if (isHideDownloaded
				|| !Util.getPreferences(this.getActivity()).getBoolean(
						"hasDownloadedEasyOne", false)) {
			this.myAdapter.add(go);
			this.myAdapter.notifyDataSetChanged();
		}
		go = new Picogram();
		go.setName("Easy Pack 2 (10 puzzles)");
		go.setStatus("2");
		if (isHideDownloaded
				|| !Util.getPreferences(this.getActivity()).getBoolean(
						"hasDownloadedEasyTwo", false)) {
			this.myAdapter.add(go);
			this.myAdapter.notifyDataSetChanged();
		}
		go = new Picogram();
		go.setName("Medium Pack 1 (10 puzzles)");
		go.setStatus("2");
		if (isHideDownloaded
				|| !Util.getPreferences(this.getActivity()).getBoolean(
						"hasDownloadedMediumOne", false)) {
			this.myAdapter.add(go);
			this.myAdapter.notifyDataSetChanged();
		}
	}

	public void getRecentPuzzles(final Activity a) {
		this.pbLoad.setVisibility(View.VISIBLE);
		// Update ratings only every hour.
		final long last = Util.getPreferences(this.getActivity()).getLong("lastRecentUpdate", 0);
		final long currentTime = System.currentTimeMillis();

		// 10 Minutes / if it's saved.
		if ((currentTime - last) < 600000) {
			this.myAdapter.picograms = this.myAdapter.recentPicograms;
			if (this.myAdapter.picograms.size() != 0)
			{
				this.myAdapter.notifyDataSetChanged();
				MenuFragment.this.pbLoad.setVisibility(View.INVISIBLE);
				return;
			}
		}
		Util.getPreferences(this.getActivity()).edit().putLong("lastRecentUpdate", currentTime).commit();
		final ParseQuery<ParseObject> query = ParseQuery.getQuery("Picogram");
		query.orderByDescending("createdAt");
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(final List<ParseObject> pos, final ParseException e) {
				if (e == null) {
					if (pos != null)
					{
						MenuFragment.this.myAdapter.recentPicograms.clear();
						for (final ParseObject po : pos) {
							if (!MenuFragment.this.myAdapter.existsById(po.getString("puzzleId")))
							{
								MenuFragment.this.myAdapter.recentPicograms.add(new Picogram(po));
							}
						}
						MenuFragment.this.myAdapter.picograms = MenuFragment.this.myAdapter.recentPicograms;
						MenuFragment.this.myAdapter.notifyDataSetChanged();
						MenuFragment.this.pbLoad.setVisibility(View.INVISIBLE);
					}
				}
				else
				{
					Log.d(TAG, "ERROR in Recent Puzzles: " + e.getMessage());
					MenuFragment.this.pbLoad.setVisibility(View.INVISIBLE);
				}
			}
		});
	}

	public void getTagPuzzles(final Activity a, final String tag) {
		this.pbLoad.setVisibility(View.VISIBLE);
		this.myAdapter.clear();
		ParseQuery<ParseObject> query = ParseQuery.getQuery("PicogramTag");
		query.whereEqualTo("tag", tag.trim().toLowerCase());
		Log.d(TAG, "puzzles   : " + tag);
		List<ParseObject> queryResult = null;
		try {
			queryResult = query.find();
		} catch (final ParseException e) {
			Log.d(TAG, "Error with tags 1: " + e.getMessage());
		}
		Log.d(TAG, "PuzzleIds here");
		if (queryResult != null) {
			Log.d(TAG, "puzzles : " + queryResult.size());
			final String ids[] = new String[queryResult.size()];
			for (int i = 0; i != queryResult.size(); ++i) {
				ids[i] = queryResult.get(i).getString("puzzleId");
			}
			query = ParseQuery.getQuery("Picogram");
			query.whereContainedIn("puzzleId", Arrays.asList(ids));

			// Filter according to the actionbar list.
			final int searchSetting = Util.getPreferences(this.getActivity()).getInt("searchSetting", 0);
			if ((searchSetting == 0) || (searchSetting == 3)) {
				// Weekly.
				final Date date = addDaysToDate(new Date(), -7);
				query.whereGreaterThan("createdAt", date);
			} else if ((searchSetting == 1) || (searchSetting == 4)) {
				// Monthly.
				final Date date = addDaysToDate(new Date(), -30);
				query.whereGreaterThan("createdAt", date);
			}
			if (searchSetting < 3) {
				// Rate sort.
				query.orderByDescending("Rate");

			} else {
				// Date sort.
				query.orderByDescending("createdAt");
			}
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(final List<ParseObject> result, final ParseException e) {
					if (e == null)
					{
						Log.d(TAG, "Got puzzles : " + result.size());
						for (final ParseObject po : result)
						{
							MenuFragment.this.myAdapter.add(new Picogram(po));
							MenuFragment.this.myAdapter.notifyDataSetChanged();
						}
						MenuFragment.this.pbLoad.setVisibility(View.INVISIBLE);
					}
					else
					{
						Log.d(TAG, "Error with tags 2: " + e.getMessage());
						MenuFragment.this.pbLoad.setVisibility(View.INVISIBLE);
					}
				}

			});

		}
	}

	public void getTopPuzzles(final Activity a) {
		this.pbLoad.setVisibility(View.VISIBLE);
		this.myAdapter.topPicograms.clear();
		this.myAdapter.clear();
		final ParseQuery<ParseObject> query = ParseQuery.getQuery("Picogram");
		// Weekly, Monthly, All Time. Rate only matters, createddate is in
		// general, gonna retreive most recent.
		if (Util.getPreferences(this.getActivity()).getInt("topSetting", 0) == 0) {
			final Date date = addDaysToDate(new Date(), -7);
			query.whereGreaterThan("createdAt", date);
		} else if (Util.getPreferences(this.getActivity()).getInt("topSetting",
				0) == 1) {
			final Date date = addDaysToDate(new Date(), -30);
			query.whereGreaterThan("createdAt", date);
		}
		query.orderByDescending("rate");
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(final List<ParseObject> list, final ParseException e) {
				if (e == null)
				{
					if (list != null) {
						MenuFragment.this.myAdapter.topPicograms.clear();
						for (final ParseObject po : list)
						{
							if (!MenuFragment.this.myAdapter.existsById(po.getString("puzzleId")))
							{
								MenuFragment.this.myAdapter.topPicograms.add(new Picogram(po));
							}
						}
						MenuFragment.this.myAdapter.picograms = MenuFragment.this.myAdapter.topPicograms;
						MenuFragment.this.myAdapter.notifyDataSetChanged();
						MenuFragment.this.pbLoad.setVisibility(View.INVISIBLE);
					}
				}
				else
				{
					Log.d(TAG, "Error getting Top: " + e.getMessage());
					MenuFragment.this.pbLoad.setVisibility(View.INVISIBLE);
				}
			}
		});
	}

	private void loadEasyPackOne() {
		this.sql = new SQLitePicogramAdapter(this.getActivity(), "Picograms",
				null, 1);
		// These shouldn't change for these packs.
		Picogram go = new Picogram();
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		// Change.
		go.setName("Smile");
		go.setSolution("01010000001000101110");
		go.setWidth("5");
		go.setHeight("4");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("0");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		go.setName("Sad");
		go.setSolution("01010000000111010001");
		go.setWidth("5");
		go.setHeight("4");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("0");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		go.setName("Person");
		go.setSolution("010111010101");
		go.setWidth("3");
		go.setHeight("4");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("0");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		go.setName("Umbrella Man");
		go.setSolution("1010000110111111011010100");
		go.setWidth("5");
		go.setHeight("5");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("0");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		go.setName("Football Man");
		go.setSolution("0010001111000101010100100");
		go.setWidth("5");
		go.setHeight("5");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("0");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		go.setName("Scorpion");
		go.setSolution("0110010000100110111000011");
		go.setWidth("5");
		go.setHeight("5");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("0");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		go.setName("Drummer");
		go.setSolution("0100010010101001101101011");
		go.setWidth("5");
		go.setHeight("5");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("0");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		go.setName("Peace");
		go.setSolution("001110001010101001001101010101000100011100");
		go.setWidth("7");
		go.setHeight("6");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("0");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		go.setName("Shopping");
		go.setSolution("0001111110100100110010010");
		go.setWidth("5");
		go.setHeight("5");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("0");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		go.setName("Two Prayers");
		go.setSolution("1010001010111100101001101");
		go.setWidth("5");
		go.setHeight("5");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("0");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		this.sql.close();
	}

	private void loadEasyPackTwo() {
		this.sql = new SQLitePicogramAdapter(this.getActivity(), "Picograms",
				null, 1);
		// These shouldn't change for these packs.
		Picogram go = new Picogram();
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("0");
		// Change.
		go.setName("Key");
		go.setSolution("000001111111110100100111");
		go.setWidth("8");
		go.setHeight("3");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("0");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		go.setName("Cat");
		go.setSolution("00111111010000111011111000010100");
		go.setWidth("8");
		go.setHeight("4");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("0");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		go.setName("Heart");
		go.setSolution("01010111110111000100");
		go.setWidth("5");
		go.setHeight("4");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("0");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		go.setName("Hourglass");
		go.setSolution("1111111111111110000000001111111111111110100000000010010000000001001001010100100100010100010011000100011000110000011000001101011000000011011000000001101100000001100011000001100100110001100000001100100001000010010001010001001001010100100101010101010111111111111111000000000111111111111111");
		go.setWidth("13");
		go.setHeight("22");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("0");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		go.setName("Cube");
		go.setSolution("00111111110100000101111111100110000010011000001001100000100110000010101111111100");
		go.setWidth("10");
		go.setHeight("8");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("0");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		go.setName("Eye");
		go.setSolution("0001111000001100110001001100101000110001010011001000110011000001111000");
		go.setWidth("10");
		go.setHeight("7");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("0");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		go.setName("Note");
		go.setSolution("011111010001010001110011110011");
		go.setWidth("6");
		go.setHeight("5");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("0");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		go.setName("Shot");
		go.setSolution("111010010010111101101101111111111010010");
		go.setWidth("3");
		go.setHeight("13");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("0");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		go.setName("Flower");
		go.setSolution("001000101010101010100010010101011100010000100");
		go.setWidth("5");
		go.setHeight("9");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("0");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		go.setName("Bomb");
		go.setSolution("00000001100000000100100000010000100000100000000111100000011111100001110111100111011111101101111111011011111110011111111000011111100000011110000");
		go.setWidth("11");
		go.setHeight("13");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("0");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		this.sql.close();
	}

	private void loadMediumPackOne() {
		this.sql = new SQLitePicogramAdapter(this.getActivity(), "Picograms",
				null, 1);
		Picogram go = new Picogram();
		go = new Picogram();
		go.setName("House");
		go.setSolution("00000111000000001110111000011100000111011000000000111111111111111100000000000110111110000011010101011111101111101000110101010100011011111010011100000001000110000000100011111111111111");
		go.setWidth("13");
		go.setHeight("14");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("1");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		go.setName("X Wins");
		go.setSolution("1001100001000001101000010110011010000101101001100001000011111111111111100110000100000110101101000001101011010000100110000100001111111111111110011000011001011010110101100110101101011010011000011001");
		go.setWidth("14");
		go.setHeight("14");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("1");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		go.setName("Ship");
		go.setSolution("0000000000001111000000000000011000000011100000000000011100000000010000000000000010000011111111100000011111111101000000100000001000000100100000100000000100000100001000011000000001000011001111111110000001111111110000010000000000000010000011111111100000011111111111000000100000000100000101000000010000000110000100100000011100000010000010100000001011000001000001010000001000110000010000101000000100001100111110010100000011000010001101111001000000100111100011000100100111110111111000111111011110111111111001111111101000011111111100001111111000011111111100000111111111111111111110000001111111111111111111000000001111111111111100000");
		go.setWidth("25");
		go.setHeight("25");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("1");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		go.setName("Bird");
		go.setSolution("0000000111000000000000000000001110110000000000000000000011111000000000000000000000111111000000000000000000011111111000000000000000001111111111000000000000000111111111110000000000000011111111111110000000000001111111111111111110011110011111111111111100011111100011111111100000011111110000010011111100001000000110010010000111000001111101111110100000000001111100011101101111100000111001111001101001111111110001111101110111001100010000111100111011100011001000111110111101111011110000011110011110111101111000001110001110011110111100000110000111001111001110000011000011100111100111000001000000110001100001100000100000001000100000100");
		go.setWidth("25");
		go.setHeight("25");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("1");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		go.setName("His Master's Voice");
		go.setSolution("0000000111100000000000000011111111000000000000111110011100001100001011000001100011100000100000111000111100111010011110000111001100110111100010111010000001101000110110110000010010000110110111100001110000110110011111110100001111100100000001100110111011001000001011000000101010000010100000001010101100111000000010101010000111000000101010100001111000011110111110011111100101001000000111111001111111111111");
		go.setWidth("20");
		go.setHeight("20");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("1");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		go.setName("Singing");
		go.setSolution("00000000010000000000000000000000011100000001110000000000011111000001111100000000001010100000111111100000000101010000001111111000000011011000000100111100000000111000011010001110000000011001111111100110000011111100000110011110000011111100000011000111100001100000000001111110011000110000000000100000001100011100000001111000001110000110000000100111111111011111110000110000111111111111111100010110001111111111111110011011100011110111111111001011011000111001111111001111000110011100011111001111100001111100");
		go.setWidth("20");
		go.setHeight("25");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("1");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		go = new Picogram();
		go.setName("Teddy");
		go.setSolution("0011111011111111011110000001100111000000110011000000110010000000001101100000011001000000000010110000001101000000000001110000000011100011000110010000000000010001001101001000000000001000000000000100000000000110000000000010000000000001000000000011000000011100011111001111000111010011111111111111111110011000000011111111110000001100000100011001101000000111111110011100110111111110000001001110001101000000000111110111000110101111000110001111100011011100110010111011110001101000111001011101011000110100011100101110100000000010001110010111011100000011100111001011101011111111011011100111111100000000000111110001111100000000000000000");
		go.setWidth("25");
		go.setHeight("25");
		go.setColors(Color.TRANSPARENT + "," + Color.BLACK);
		go.setDiff("1");
		go.nullsToValue(this.getActivity());
		this.sql.addUserPicogram(go);
		this.sql.close();
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Check for an incoming deep link from Facebook
		final Uri targetUri = this.getActivity().getIntent().getData();
		if (targetUri != null) {
			if (this.sql == null)
			{
				this.sql = new SQLitePicogramAdapter(
						this.getActivity(), "Picograms", null, 1);
			}
			final Picogram p = this.sql.getPicogram(targetUri.getLastPathSegment());
			this.startGame(p);
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.position = this.getArguments().getInt(ARG_POSITION);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		this.pbLoad = (SmoothProgressBar) this.getActivity().findViewById(R.id.spbLoad);
		this.myAdapter = new PicogramListAdapter(this.getActivity(),
				R.layout.picogram_menu_choice_item);

		final LayoutParams params = new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT);

		final FrameLayout fl = new FrameLayout(this.getActivity());
		fl.setLayoutParams(params);

		final int margin = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 8, this.getResources()
						.getDisplayMetrics());
		if (!Util.isOnline())
		{
			if (this.position != 0) {
				if (this.position != 1) {
					String randomFacts[] = this.getActivity().getResources().getStringArray(R.array.picogramFacts);
					final TextView v = new TextView(this.getActivity());
					params.setMargins(margin, margin, margin, margin);
					v.setLayoutParams(params);
					v.setLayoutParams(params);
					v.setGravity(Gravity.CENTER);
					v.setBackgroundResource(R.drawable.background_card);
					v.setText(randomFacts[(int) (Math.random() * randomFacts.length)] + "\n\nYou're currently offline, and this functionality is online only.  We apologize.\nIf you think this is a mistake, please email us.");

					fl.addView(v);
					return fl;
				}
			}
		}
		this.pbLoad.setVisibility(View.INVISIBLE);
		final BounceListView v = new BounceListView(this.getActivity());
		fl.setBackgroundColor(Color.TRANSPARENT);
		v.setBackgroundDrawable(this.getActivity().getResources().getDrawable(R.drawable.item_square));
		v.setDivider(new ColorDrawable(Color.parseColor("#00000000")));
		v.setDivider(this.getActivity().getResources().getDrawable(R.drawable.item_square));
		v.setCacheColorHint(Color.TRANSPARENT);
		v.setSelector(this.getActivity().getResources().getDrawable(R.drawable.item_selector_bad));
		v.setDividerHeight(20);
		v.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		v.setItemsCanFocus(true);
		v.setVerticalScrollBarEnabled(false);
		params.setMargins(margin, margin, margin, margin);
		v.setLayoutParams(params);
		v.setBackgroundResource(R.drawable.background_card);
		if (this.position == MenuActivity.TITLES.indexOf("My")) {
			this.getMyPuzzles(this.getActivity());
		} else if (this.position == MenuActivity.TITLES.indexOf("Top")) {
			this.getTopPuzzles(this.getActivity());
		} else if (this.position == MenuActivity.TITLES.indexOf("Recent")) {
			this.getRecentPuzzles(this.getActivity());
		} else if (this.position == MenuActivity.TITLES.indexOf("Search")) {
			// Don't load anything on start.
			// this.getTagPuzzles(this.getActivity(), "", true);
		} else if (this.position == MenuActivity.TITLES.indexOf("Packs")) {
			this.getPackPuzzles();
		} else {
			for (int i = 0; i != 20; ++i) {
				final Picogram obj = new Picogram("=/", "0",
						"Had an error. You shouldn't see this " + i, "0", "0",
						1, "Justin", "1", "1", "1", "0", 2, Color.BLACK + " "
								+ Color.RED);
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

		v.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
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
			final Picogram picogram = this.myAdapter.get(pos);
			if (this.position == MenuActivity.TITLES.indexOf("Packs")) {
				// We load the pack to My and prompt user.
				if (picogram.getName().contains("Easy Pack 1")) {
					this.loadEasyPackOne();
					Util.getPreferences(this.getActivity()).edit()
							.putBoolean("hasDownloadedEasyOne", true).commit();
				} else if (picogram.getName().contains("Easy Pack 2")) {
					this.loadEasyPackTwo();
					Util.getPreferences(this.getActivity()).edit()
							.putBoolean("hasDownloadedEasyTwo", true).commit();
				} else if (picogram.getName().contains("Medium Pack 1")) {
					this.loadMediumPackOne();
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
				this.sql.close();
				Crouton.makeText(this.getActivity(),
						picogram.getName() + " loaded, go back to My tab.",
						Style.INFO).show();

			} else {
				if ((this.position == MenuActivity.TITLES.indexOf("My"))
						&& ((pos == 0) || (pos == 1))) {
					// Can this be the Creating or Random?
					this.sql.close();
					if (pos == 0) {
						final Intent createIntent = new Intent(
								this.getActivity(),
								CreateActivity.class);
							this.getActivity().startActivityForResult(createIntent,
									MenuActivity.CREATE_CODE);
							this.getActivity().overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
					} else if (pos == 1) {
						this.generateRandomGame();
					}
				} else {
					// If this Picogram doesn't exists for the person, add it.
					if (this.sql == null) {
						this.sql = new SQLitePicogramAdapter(
								this.getActivity(), "Picograms", null, 1);
					}
					if (this.sql.doesPuzzleExist(picogram) == -1) {
						this.sql.addUserPicogram(picogram);
						// Add to the ranking table.
						final SQLiteRatingAdapter sra = new SQLiteRatingAdapter(
								this.getActivity(), "Rating", null, 2);
						sra.insertOnOpenOnlineGame(picogram.getID());
						sra.close();
					}
					this.startGame(picogram);
				}
			}
		}
	}

	public boolean onItemLongClick(final AdapterView<?> parent, final View view,
			final int position, final long id) {
		if (!this.myAdapter.get(0).getName().contains("Create")) {
			// We only want long click support on My tab.
			return false;
		}
		if ((position == 0) || (position == 1)) {
			// Create or Random, should just ignore this?
			return false;
		} else {
			view.getFocusables(position);
			view.setSelected(true);
			((MenuActivity) this.getActivity()).actionMode = ((MenuActivity) this.getActivity()).startActionMode(new CustomActionMode(this.myAdapter.get(position), this.sql, this.myAdapter, this, view));

			return true;
		}

	}

	private void startGame(final Picogram go) {
		// Intent gameIntent = new Intent(this, AdvancedGameActivity.class);
		final Intent gameIntent = new Intent(this.getActivity(),
				PreGameActivity.class);
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
		getActivity().overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}
}
