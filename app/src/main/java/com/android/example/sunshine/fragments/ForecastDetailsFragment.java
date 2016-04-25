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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.example.sunshine.R;
import com.android.example.sunshine.data.WeatherContract;
import com.android.example.sunshine.utils.Utility;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	public static final String ARG_WEATHER_URI = "arg_weather_uri";
	static final int COL_WEATHER_ID = 0;
	static final int COL_WEATHER_DATE = 1;
	static final int COL_WEATHER_DESC = 2;
	static final int COL_WEATHER_MAX_TEMP = 3;
	static final int COL_WEATHER_MIN_TEMP = 4;
	static final int COL_WEATHER_HUMIDITY = 5;
	static final int COL_WEATHER_WIND_SPEED = 6;
	static final int COL_WEATHER_DEGREES = 7;
	static final int COL_WEATHER_PRESSURE = 8;
	static final int COL_WEATHER_CONDITION_ID = 9;
	static final int DETAILED_WEATHER_LOADER_ID = 461;
	static final String[] FORECAST_COLUMNS = {
			WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
			WeatherContract.WeatherEntry.COLUMN_DATE,
			WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
			WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
			WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
			WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
			WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
			WeatherContract.WeatherEntry.COLUMN_DEGREES,
			WeatherContract.WeatherEntry.COLUMN_PRESSURE,
			WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
	};
	@SuppressWarnings("unused")
	private static final String TAG = ForecastDetailsFragment.class.getSimpleName();
	TextView mDateView;
	TextView mHighTemperatureView;
	TextView mLowTemperatureView;
	TextView mHumidityView;
	TextView mWindSpeedView;
	TextView mPressureView;
	ImageView mWeatherIconView;
	TextView mWeatherDescriptionView;

	ShareActionProvider mShareActionProvider;

	String mSharingText;
	Uri mWeatherUri;

	public ForecastDetailsFragment() {
	}

	public static ForecastDetailsFragment newInstance(Uri weatherUri) {

		Bundle args = new Bundle();
		args.putString(ARG_WEATHER_URI, weatherUri.toString());

		ForecastDetailsFragment fragment = new ForecastDetailsFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(DETAILED_WEATHER_LOADER_ID, null, this);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if (args != null) {
			mWeatherUri = Uri.parse(args.getString(ARG_WEATHER_URI));
		}
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_forecast_details, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		mDateView = (TextView) view.findViewById(R.id.date_line_1_text_view);
		mHighTemperatureView = (TextView) view.findViewById(R.id.temperature_high_text_view);
		mLowTemperatureView = (TextView) view.findViewById(R.id.temperature_low_text_view);
		mHumidityView = (TextView) view.findViewById(R.id.humidity_text_view);
		mWindSpeedView = (TextView) view.findViewById(R.id.wind_speed_text_view);
		mPressureView = (TextView) view.findViewById(R.id.pressure_text_view);
		mWeatherIconView = (ImageView) view.findViewById(R.id.icon_view);
		mWeatherDescriptionView = (TextView) view.findViewById(R.id.weather_description_text_view);

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_forecast_details, menu);
		final MenuItem shareMenuItem = menu.findItem(R.id.action_share);
		mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareMenuItem);
		if (mSharingText != null) {
			mShareActionProvider.setShareIntent(createShareForecastIntent());
		}
	}

	private Intent createShareForecastIntent() {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
		}
		else {
			//noinspection deprecation
			shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		}
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, mSharingText);
		return shareIntent;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		CursorLoader loader;
		switch (id) {
			case DETAILED_WEATHER_LOADER_ID:
				loader = new CursorLoader(getActivity(),
				                          mWeatherUri,
				                          FORECAST_COLUMNS,
				                          null,
				                          null,
				                          null);
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

			final int weatherIconResource = Utility.getArtResourceForWeatherCondition(data.getInt(
					COL_WEATHER_CONDITION_ID));
			final String dateString = Utility.formatDate(data.getLong(COL_WEATHER_DATE));
			final String weatherDescription = data.getString(COL_WEATHER_DESC);
			final String temperatureMin = Utility.formatTemperature(getContext(),
			                                                        data.getDouble(COL_WEATHER_MIN_TEMP),
			                                                        isMetric);
			final String temperatureMax = Utility.formatTemperature(getContext(),
			                                                        data.getDouble(COL_WEATHER_MAX_TEMP),
			                                                        isMetric);
			final String humidity = Utility.getFormattedHumidity(getContext(), data.getDouble(COL_WEATHER_HUMIDITY));
			final String windSpeed = Utility.getFormattedWind(getContext(),
			                                                  data.getFloat(COL_WEATHER_WIND_SPEED),
			                                                  data.getFloat(COL_WEATHER_DEGREES));
			final String pressure = Utility.getFormattedPressure(getContext(), data.getFloat(COL_WEATHER_PRESSURE));

			mSharingText = String.format("%s - %s - %s/%s %s",
			                             dateString,
			                             weatherDescription,
			                             temperatureMin,
			                             temperatureMax,
			                             getString(R.string.sharing_hash_tag));

			mWeatherIconView.setImageResource(weatherIconResource);
			mWeatherIconView.setContentDescription(weatherDescription);
			mDateView.setText(dateString);
			mHighTemperatureView.setText(temperatureMax);
			mLowTemperatureView.setText(temperatureMin);
			mHumidityView.setText(humidity);
			mWindSpeedView.setText(windSpeed);
			mPressureView.setText(pressure);
			mWeatherDescriptionView.setText(weatherDescription);

			if (mShareActionProvider != null) {
				mShareActionProvider.setShareIntent(createShareForecastIntent());
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}

	public void onLocationChanged(String newLocation) {
		Uri uri = mWeatherUri;
		if(mWeatherUri != null) {
			long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
			mWeatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
			getLoaderManager().restartLoader(DETAILED_WEATHER_LOADER_ID, null, this);
		}
	}
}
