package com.android.example.sunshine.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.example.sunshine.R;
import com.android.example.sunshine.data.WeatherContract;
import com.android.example.sunshine.adapters.ForecastAdapter;
import com.android.example.sunshine.sync.SunshineSyncAdapter;
import com.android.example.sunshine.utils.Utility;

public class ForecastsFragment_new extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ForecastAdapter.ViewHolder.OnClickListener {
	@SuppressWarnings("unused")
	private static final String LOG_TAG = ForecastsFragment_new.class.getSimpleName();

	private static final int COL_COORD_LAT = 6;
	private static final int COL_COORD_LONG = 7;

	private static final String[] FORECAST_COLUMNS = {
	  WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
	  WeatherContract.WeatherEntry.COLUMN_DATE,
	  WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
	  WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
	  WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
	  WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
	  WeatherContract.LocationEntry.COLUMN_COORD_LAT,
	  WeatherContract.LocationEntry.COLUMN_COORD_LONG
	};

	private static final int WEATHER_LOADER_ID = 0;

	private ForecastAdapter mForecastAdapter;
	private Callback mCallback;
	private boolean mTodayLayoutUsed;

	public ForecastsFragment_new() {
		mTodayLayoutUsed = true;
	}

	@Override
	public void onAttach(final Context context) {
		super.onAttach(context);
		try {
			mCallback = (Callback) context;
		}
		catch (ClassCastException e) {
			throw new RuntimeException(String.format("%s must implement %s interface!",
			                                         context.getClass().getCanonicalName(),
			                                         Callback.class.getCanonicalName()));
		}
	}

	@Override
	public void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Nullable
	@Override
	public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_forecasts_new, container, false);
	}

	@Override
	public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list_view_forecast);
		mForecastAdapter = new ForecastAdapter(getContext(), null, 0, this);
		recyclerView.setAdapter(mForecastAdapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		recyclerView.setItemAnimator(new DefaultItemAnimator());
	}

	@Override
	public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(WEATHER_LOADER_ID, null, this);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallback = null;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_forecast, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean eventConsumed;
		switch (item.getItemId()) {
			case R.id.action_map:
				openPreferredLocationOnMap();
				eventConsumed = true;
				break;

			default:
				eventConsumed = super.onOptionsItemSelected(item);
				break;
		}
		return eventConsumed;
	}

	private void openPreferredLocationOnMap() {
		Cursor c = mForecastAdapter.getCursor();
		if (c != null && c.moveToFirst()) {
			String positionLatitude = c.getString(COL_COORD_LAT);
			String positionLongitude = c.getString(COL_COORD_LONG);
			Uri geoLocation = Uri.parse(String.format("geo:%s,%s", positionLatitude, positionLongitude));
			Intent openMapIntent = new Intent(Intent.ACTION_VIEW);
			openMapIntent.setData(geoLocation);
			if (openMapIntent.resolveActivity(getContext().getPackageManager()) != null) {
				startActivity(openMapIntent);
			}
			else {
				Log.d(LOG_TAG,
				      "openPreferredLocationOnMap: Couldn't call " + geoLocation.toString() + ", no activities can handle the Intent");
			}
		}
	}

	public void setTodayLayoutUsed(boolean todayLayoutUsed) {
		mTodayLayoutUsed = todayLayoutUsed;
		if (mForecastAdapter != null) {
			mForecastAdapter.setTodayLayoutUsed(todayLayoutUsed);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		CursorLoader loader;
		switch (id) {
			case WEATHER_LOADER_ID:
				final String locationString = Utility.getPreferredLocation(getActivity());
				final String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
				final Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
				  locationString,
				  System.currentTimeMillis());

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
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
		mForecastAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		mForecastAdapter.swapCursor(null);
	}

	@Override
	public void onClick(final Uri uri) {
		if(mCallback != null) {
			mCallback.onItemClicked(uri);
		}
	}

	private void refreshForecast() {
		SunshineSyncAdapter.syncImmediately(getContext());
	}

	public void onLocationChanged() {
		refreshForecast();
		getLoaderManager().restartLoader(WEATHER_LOADER_ID, null, this);
	}

	public interface Callback {
		void onItemClicked(Uri uri);
	}
}
