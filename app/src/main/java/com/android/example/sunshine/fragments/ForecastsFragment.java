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
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.android.example.sunshine.R;
import com.android.example.sunshine.data.WeatherContract;
import com.android.example.sunshine.sync.SunshineSyncAdapter;
import com.android.example.sunshine.utils.RecyclerViewCursorAdapter;
import com.android.example.sunshine.utils.Utility;

public class ForecastsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {
	@SuppressWarnings("unused")
	private static final String LOG_TAG = ForecastsFragment.class.getSimpleName();

	private static final int COL_WEATHER_DATE = 1;
	private static final int COL_WEATHER_DESCRIPTION = 2;
	private static final int COL_TEMP_MAX = 3;
	private static final int COL_TEMP_MIN = 4;
	private static final int COL_WEATHER_ID = 5;
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

	private SwipeRefreshLayout mSwipeRefreshLayout;

	private ForecastAdapter mForecastAdapter;
	private Callback mCallback;
	private boolean mTodayLayoutUsed;
	private String mLocation;
	private boolean mMetricUnits;
	private int mForecastSize;

	public ForecastsFragment() {
		mTodayLayoutUsed = true;
	}

	public void setTodayLayoutUsed(boolean todayLayoutUsed) {
		mTodayLayoutUsed = todayLayoutUsed;
		if (mForecastAdapter != null) {
			mForecastAdapter.setTodayLayoutUsed(todayLayoutUsed);
		}
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
		return inflater.inflate(R.layout.fragment_forecasts, container, false);
	}

	@Override
	public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout);
		mSwipeRefreshLayout.setOnRefreshListener(this);
		final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list_view_forecast);
		mForecastAdapter = new ForecastAdapter(getContext(), null, 0, new ForecastAdapter.ViewHolder.OnClickListener() {
			@Override
			public void onClick(final Uri uri) {
				if (mCallback != null) {
					mCallback.onItemClicked(uri);
				}
			}
		});
		mForecastAdapter.setTodayLayoutUsed(mTodayLayoutUsed);
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
	public void onResume() {
		super.onResume();
		String location = Utility.getPreferredLocation(getContext());
		boolean metricUnits = Utility.isMetric(getContext());
		int forecastSize = Utility.getPreferredForecastSize(getContext());
		if (location != null && !location.equals(mLocation)) {
			mSwipeRefreshLayout.setRefreshing(true);
			refreshForecast();
			mLocation = location;
		}
		if (metricUnits != mMetricUnits) {
//			mSwipeRefreshLayout.setRefreshing(true);
//			refreshForecast();
			getLoaderManager().restartLoader(WEATHER_LOADER_ID, null, this);
			mMetricUnits = metricUnits;
		}
		if (forecastSize != mForecastSize) {
			mForecastSize = forecastSize;
			mSwipeRefreshLayout.setRefreshing(true);
			refreshForecast();
			getLoaderManager().restartLoader(WEATHER_LOADER_ID, null, this);
		}
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

			case R.id.action_refresh:
				mSwipeRefreshLayout.setRefreshing(true);
				refreshForecast();
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

	private void refreshForecast() {
		SunshineSyncAdapter.syncImmediately(getContext());
		mSwipeRefreshLayout.setRefreshing(false);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		CursorLoader loader;
		switch (id) {
			case WEATHER_LOADER_ID:
				final String locationString = Utility.getPreferredLocation(getActivity());
				final String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
				final int forecastSize = Utility.getPreferredForecastSize(getContext());
				Log.d(LOG_TAG, String.format("onCreateLoader: forecastSize: %d", forecastSize));
				final Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
				  locationString,
				  System.currentTimeMillis(),
				  forecastSize);

				loader = new CursorLoader(getContext(),
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
	public void onRefresh() {
		refreshForecast();
	}

	public interface Callback {
		void onItemClicked(Uri uri);
	}

	public static class ForecastAdapter extends RecyclerViewCursorAdapter<ForecastAdapter.ViewHolder> {
		private static final int ITEM_VIEW_TYPE_TODAY = 0;
		private static final int ITEM_VIEW_TYPE_OTHER = 1;
		private ViewHolder.OnClickListener mOnClickListener;

		private boolean mTodayLayoutUsed;

		public ForecastAdapter(final Context context, final Cursor cursor, int flags, ViewHolder.OnClickListener onClickListener) {
			super(context, cursor, flags);
			mOnClickListener = onClickListener;
			mTodayLayoutUsed = true;
		}

		public void setTodayLayoutUsed(boolean useTodayLayout) {
			mTodayLayoutUsed = useTodayLayout;
		}

		@Override
		public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor) {
			final int itemType = viewHolder.getItemViewType();


			final int weatherId = cursor.getInt(COL_WEATHER_ID);
			final int weatherIconResource = (itemType == ITEM_VIEW_TYPE_TODAY) ?
			                                Utility.getArtResourceForWeatherCondition(weatherId) :
			                                Utility.getIconResourceForWeatherCondition(weatherId);

			final long date = cursor.getLong(COL_WEATHER_DATE);
			final String dateString = Utility.getFriendlyDayString(mContext, date);
			final String highTemp = Utility.formatTemperature(mContext, cursor.getDouble(COL_TEMP_MAX));
			final String lowTemp = Utility.formatTemperature(mContext, cursor.getDouble(COL_TEMP_MIN));
			final String weatherDescription = cursor.getString(COL_WEATHER_DESCRIPTION);

			viewHolder.mIconView.setImageResource(weatherIconResource);
			viewHolder.mIconView.setContentDescription(weatherDescription);
			viewHolder.mDateTextView.setText(dateString);
			viewHolder.mHighTempTextView.setText(highTemp);
			viewHolder.mLowTempTextView.setText(lowTemp);
			viewHolder.mWeatherDescriptionTextView.setText(weatherDescription);
			viewHolder.mDataUri = WeatherContract.WeatherEntry
			  .buildWeatherLocationWithDate(Utility.getPreferredLocation(mContext), date);
		}

		@Override
		public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
			final int layoutResource;
			switch (viewType) {
				case ITEM_VIEW_TYPE_TODAY:
					layoutResource = R.layout.list_item_forecast_today;
					break;

				case ITEM_VIEW_TYPE_OTHER:
					layoutResource = R.layout.list_item_forecast;
					break;

				default:
					layoutResource = -1;
			}

			View view = LayoutInflater.from(parent.getContext()).inflate(layoutResource, parent, false);
			return new ViewHolder(view, mOnClickListener);
		}

		@Override
		public int getItemViewType(final int position) {
			return (mTodayLayoutUsed && position == 0) ? ITEM_VIEW_TYPE_TODAY : ITEM_VIEW_TYPE_OTHER;
		}

		public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
			private final ImageView mIconView;
			private final TextView mDateTextView;
			private final TextView mHighTempTextView;
			private final TextView mLowTempTextView;
			private final TextView mWeatherDescriptionTextView;
			private Uri mDataUri;
			private OnClickListener mOnClickListener;

			public ViewHolder(final View itemView, OnClickListener onClickListener) {
				super(itemView);
				mIconView = (ImageView) itemView.findViewById(R.id.list_item_icon);
				mDateTextView = (TextView) itemView.findViewById(R.id.list_item_date_textview);
				mHighTempTextView = (TextView) itemView.findViewById(R.id.list_item_high_textview);
				mLowTempTextView = (TextView) itemView.findViewById(R.id.list_item_low_textview);
				mWeatherDescriptionTextView = (TextView) itemView.findViewById(R.id.list_item_forecast_textview);
				itemView.setOnClickListener(this);
				mOnClickListener = onClickListener;
			}

			@Override
			public void onClick(final View v) {
				if (mOnClickListener != null) {
					mOnClickListener.onClick(mDataUri);
				}
			}

			public interface OnClickListener {
				void onClick(Uri uri);
			}
		}
	}
}
