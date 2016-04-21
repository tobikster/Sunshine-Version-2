package com.android.example.sunshine.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.example.sunshine.R;
import com.android.example.sunshine.activities.ForecastDetailsActivity;
import com.android.example.sunshine.adapters.ForecastAdapter;
import com.android.example.sunshine.data.WeatherContract;
import com.android.example.sunshine.utils.FetchWeatherTask;
import com.android.example.sunshine.utils.Utility;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {
	private static final int WEATHER_LOADER_ID = 674;

	public static final int COL_WEATHER_ID = 0;
	public static final int COL_WEATHER_DATE = 1;
	public static final int COL_WEATHER_DESC = 2;
	public static final int COL_WEATHER_MAX_TEMP = 3;
	public static final int COL_WEATHER_MIN_TEMP = 4;
	public static final int COL_LOCATION_SETTING = 5;
	public static final int COL_WEATHER_CONDITION_ID = 6;
	public static final int COL_COORD_LAT = 7;
	public static final int COL_COORD_LONG = 8;

	public static final String[] FORECAST_COLUMNS = {
			WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
			WeatherContract.WeatherEntry.COLUMN_DATE,
			WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
			WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
			WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
			WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
			WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
			WeatherContract.LocationEntry.COLUMN_COORD_LAT,
			WeatherContract.LocationEntry.COLUMN_COORD_LONG
	};

	CursorAdapter mForecastAdapter;

	public ForecastsFragment() {
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(WEATHER_LOADER_ID, null, this);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_forecasts, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ListView forecastsListView = (ListView) view.findViewById(R.id.list_view_forecast);

		mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
		forecastsListView.setAdapter(mForecastAdapter);
		forecastsListView.setOnItemClickListener(this);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_forecast, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean eventConsumed;
		switch (item.getItemId()) {
			case R.id.action_refresh:
				refreshForecast();
				eventConsumed = true;
				break;

			default:
				eventConsumed = super.onOptionsItemSelected(item);
				break;
		}
		return eventConsumed;
	}

	private void refreshForecast() {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		final String location = preferences.getString(getString(R.string.pref_location_key),
		                                              getString(R.string.pref_location_default));
		final String units = preferences.getString(getString(R.string.pref_units_key),
		                                           getString(R.string.pref_units_default));

		new FetchWeatherTask(getContext()).execute(location, units);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		CursorLoader loader;
		switch (id) {
			case WEATHER_LOADER_ID:
				final String locationString = Utility.getPreferredLocation(getActivity());
				final String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
				final Uri weatherForLocationUri = WeatherContract.WeatherEntry
						.buildWeatherLocationWithStartDate(locationString, System.currentTimeMillis());

				loader = new CursorLoader(getActivity(),
				                          weatherForLocationUri,
				                          FORECAST_COLUMNS,
				                          null,
				                          null,
				                          sortOrder);
				break;
			default:
				throw new UnsupportedOperationException("Unknown loader id: " + id);
		}
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mForecastAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mForecastAdapter.swapCursor(null);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Cursor cursor = (Cursor) mForecastAdapter.getItem(position);
		if (cursor != null) {
			String locationString = Utility.getPreferredLocation(getActivity());
			Intent startDetailsViewIntent = new Intent(getActivity(), ForecastDetailsActivity.class)
					.setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationString,
					                                                                   cursor.getLong(COL_WEATHER_DATE)));
			startActivity(startDetailsViewIntent);
		}
	}

	public void onLocationChanged() {
		refreshForecast();
		getLoaderManager().restartLoader(WEATHER_LOADER_ID, null, this);
	}
}
