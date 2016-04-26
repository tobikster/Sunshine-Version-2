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
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.example.sunshine.R;
import com.android.example.sunshine.data.WeatherContract;
import com.android.example.sunshine.sync.SunshineSyncAdapter;
import com.android.example.sunshine.utils.Utility;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
                                                           AdapterView.OnItemClickListener {
	@SuppressWarnings("Unused")
	private static final String LOG_TAG = ForecastsFragment.class.getSimpleName();
	private static final int COL_WEATHER_ID = 0;
	private static final int COL_WEATHER_DATE = 1;
	private static final int COL_WEATHER_DESC = 2;
	private static final int COL_WEATHER_MAX_TEMP = 3;
	private static final int COL_WEATHER_MIN_TEMP = 4;
	private static final int COL_LOCATION_SETTING = 5;
	private static final int COL_WEATHER_CONDITION_ID = 6;
	private static final int COL_COORD_LAT = 7;
	private static final int COL_COORD_LONG = 8;
	private static final String[] FORECAST_COLUMNS = {
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
	private static final int WEATHER_LOADER_ID = 674;
	private static final String BUNDLE_KEY_SELECTED_POSITION = "selected_position";

	private ListView mForecastsListView;

	private Callback mCallback;
	private ForecastsAdapter mForecastAdapter;
	private int mSelectedPosition;
	private boolean mTodayLayoutUsed;

	public ForecastsFragment() {
		mSelectedPosition = ListView.INVALID_POSITION;
		mTodayLayoutUsed = true;
	}

	public void setUseTodayLayout(boolean todayLayoutUsed) {
		mTodayLayoutUsed = todayLayoutUsed;
		if (mForecastAdapter != null) {
			mForecastAdapter.setTodayLayoutUsed(todayLayoutUsed);
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		try {
			mCallback = (Callback) context;
		}
		catch (ClassCastException e) {
			throw new RuntimeException(String.format("%s class must implements %s interface!",
			                                         context.getClass().getCanonicalName(),
			                                         Callback.class.getCanonicalName()));
		}
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if (savedInstanceState != null) {
			mSelectedPosition = savedInstanceState.getInt(BUNDLE_KEY_SELECTED_POSITION);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_forecasts, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mForecastsListView = (ListView) view.findViewById(R.id.list_view_forecast);

		mForecastAdapter = new ForecastsAdapter(getActivity(), null, 0);
		mForecastAdapter.setTodayLayoutUsed(mTodayLayoutUsed);
		mForecastsListView.setAdapter(mForecastAdapter);
		mForecastsListView.setOnItemClickListener(this);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(WEATHER_LOADER_ID, null, this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (mSelectedPosition != ListView.INVALID_POSITION) {
			outState.putInt(BUNDLE_KEY_SELECTED_POSITION, mSelectedPosition);
		}
		super.onSaveInstanceState(outState);
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

	private void refreshForecast() {
		SunshineSyncAdapter.syncImmediately(getContext());
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

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mForecastAdapter.swapCursor(data);
		if (mSelectedPosition != ListView.INVALID_POSITION) {
			mForecastsListView.smoothScrollToPosition(mSelectedPosition);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mForecastAdapter.swapCursor(null);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mCallback != null) {
			Cursor cursor = (Cursor) mForecastAdapter.getItem(position);
			if (cursor != null) {
				String locationString = Utility.getPreferredLocation(getActivity());
				Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationString,
				                                                                    cursor.getLong(COL_WEATHER_DATE));
				mCallback.onItemClicked(uri);
				mSelectedPosition = position;
			}
		}
	}

	public void onLocationChanged() {
		refreshForecast();
		getLoaderManager().restartLoader(WEATHER_LOADER_ID, null, this);
	}

	public interface Callback {
		void onItemClicked(Uri uri);
	}

	/**
	 * {@link ForecastsAdapter} exposes a list of weather forecasts from a {@link android.database.Cursor} to a {@link
	 * android.widget.ListView}.
	 */
	private static class ForecastsAdapter extends CursorAdapter {
		@SuppressWarnings("unused")
		private static final String TAG = ForecastsAdapter.class.getSimpleName();
		private static final int VIEW_TYPE_TODAY = 0;
		private static final int VIEW_TYPE_FUTURE_DAY = 1;

		private boolean mTodayLayoutUsed;

		public ForecastsAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
			mTodayLayoutUsed = true;
		}

		public void setTodayLayoutUsed(boolean todayLayoutUsed) {
			mTodayLayoutUsed = todayLayoutUsed;
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final int viewType = getItemViewType(cursor.getPosition());
			final int layoutId = (viewType == VIEW_TYPE_TODAY) ?
			                     R.layout.list_item_forecast_today :
			                     R.layout.list_item_forecast;
			View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
			ViewHolder viewHolder = new ViewHolder(view);
			view.setTag(viewHolder);
			return view;
		}

		@Override
		public int getItemViewType(int position) {
			return (position == 0 && mTodayLayoutUsed) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			final int itemType = getItemViewType(cursor.getPosition());

			ViewHolder viewHolder = (ViewHolder) view.getTag();

			final int weatherId = cursor.getInt(ForecastsFragment.COL_WEATHER_CONDITION_ID);
			final int weatherIconResource = (itemType == VIEW_TYPE_TODAY) ?
			                                Utility.getArtResourceForWeatherCondition(weatherId) :
			                                Utility.getIconResourceForWeatherCondition(weatherId);

			final String date = Utility.getFriendlyDayString(mContext,
			                                                 cursor.getLong(ForecastsFragment.COL_WEATHER_DATE));
			final String highTemp = Utility.formatTemperature(mContext,
			                                                  cursor.getDouble(ForecastsFragment.COL_WEATHER_MAX_TEMP));
			final String lowTemp = Utility.formatTemperature(mContext,
			                                                 cursor.getDouble(ForecastsFragment.COL_WEATHER_MIN_TEMP));
			final String weatherDescription = cursor.getString(ForecastsFragment.COL_WEATHER_DESC);

			viewHolder.mIconView.setImageResource(weatherIconResource);
			viewHolder.mIconView.setContentDescription(weatherDescription);
			viewHolder.mDateTextView.setText(date);
			viewHolder.mHighTempTextView.setText(highTemp);
			viewHolder.mLowTempTextView.setText(lowTemp);
			viewHolder.mWeatherDescriptionTextView.setText(weatherDescription);
		}

		static class ViewHolder {
			final ImageView mIconView;
			final TextView mDateTextView;
			final TextView mHighTempTextView;
			final TextView mLowTempTextView;
			final TextView mWeatherDescriptionTextView;

			public ViewHolder(View view) {
				mIconView = (ImageView) view.findViewById(R.id.list_item_icon);
				mDateTextView = (TextView) view.findViewById(R.id.list_item_date_textview);
				mHighTempTextView = (TextView) view.findViewById(R.id.list_item_high_textview);
				mLowTempTextView = (TextView) view.findViewById(R.id.list_item_low_textview);
				mWeatherDescriptionTextView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
			}
		}
	}
}
