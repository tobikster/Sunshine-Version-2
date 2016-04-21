package com.android.example.sunshine.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.example.sunshine.R;
import com.android.example.sunshine.data.WeatherContract;
import com.android.example.sunshine.utils.Utility;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	static final int COL_WEATHER_ID = 0;
	static final int COL_WEATHER_DATE = 1;
	static final int COL_WEATHER_DESC = 2;
	static final int COL_WEATHER_MAX_TEMP = 3;
	static final int COL_WEATHER_MIN_TEMP = 4;
	private static final int DETAILED_WEATHER_LOADER_ID = 461;
	private static final String[] FORECAST_COLUMNS = {
			WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
			WeatherContract.WeatherEntry.COLUMN_DATE,
			WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
			WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
			WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
			};

	@SuppressWarnings("unused")
	private static final String TAG = ForecastDetailsFragment.class.getSimpleName();

	TextView mForecastTextView;
	ShareActionProvider mShareActionProvider;

	String mForecast;

	public ForecastDetailsFragment() {
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(DETAILED_WEATHER_LOADER_ID, null, this);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_forecast_details, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

		mForecastTextView = (TextView) view.findViewById(R.id.forecast);

		final Intent startActivityIntent = getActivity().getIntent();
		if (startActivityIntent != null) {
			mForecastTextView.setText(mForecast);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_forecast_details, menu);
		final MenuItem shareMenuItem = menu.findItem(R.id.action_share);
		mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareMenuItem);
		if (mForecast != null) {
			mShareActionProvider.setShareIntent(createShareForecastIntent());
		}
		else {
			Log.d(TAG, "onCreateOptionsMenu: ShareActionProvider is null!");
		}
	}

	private Intent createShareForecastIntent() {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
		}
		else {
			shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		}
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + " " + getString(R.string.sharing_hash_tag));
		return shareIntent;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		CursorLoader loader = null;
		switch (id) {
			case DETAILED_WEATHER_LOADER_ID:
				Intent intent = getActivity().getIntent();
				if (intent != null) {
					loader = new CursorLoader(getActivity(),
					                          Uri.parse(intent.getDataString()),
					                          FORECAST_COLUMNS,
					                          null,
					                          null,
					                          null);
				}
				break;
			default:
				throw new UnsupportedOperationException("Unknown loader id: " + id);
		}
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (data.moveToFirst()) {
			boolean isMetric = Utility.isMetric(getActivity());

			final String dateString = Utility.formatDate(data.getLong(COL_WEATHER_DATE));
			final String weatherDescription = data.getString(COL_WEATHER_DESC);
			final String temperatureMin = Utility.formatTemperature(data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
			final String temperatureMax = Utility.formatTemperature(data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);

			mForecast = String.format("%s - %s - %s/%s",
			                          dateString,
			                          weatherDescription,
			                          temperatureMin,
			                          temperatureMax);
			mForecastTextView.setText(mForecast);

			if(mShareActionProvider != null) {
				mShareActionProvider.setShareIntent(createShareForecastIntent());
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}
}
