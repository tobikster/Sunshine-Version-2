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
import android.widget.ImageView;
import android.widget.TextView;

import com.android.example.sunshine.R;
import com.android.example.sunshine.data.WeatherContract;
import com.android.example.sunshine.utils.RecyclerViewCursorAdapter;
import com.android.example.sunshine.utils.Utility;

/**
 * Created by pti on 27.04.16.
 */
public class ForecastsFragment_new extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	@SuppressWarnings("unused")
	private static final String LOG_TAG = ForecastsFragment_new.class.getSimpleName();

	private static final int COL_WEATHER_DATE = 1;
	private static final int COL_WEATHER_DESC = 2;
	private static final int COL_WEATHER_MAX_TEMP = 3;
	private static final int COL_WEATHER_MIN_TEMP = 4;
	private static final int COL_WEATHER_CONDITION_ID = 5;
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

	private RecyclerView mRecyclerView;
	private ForecastAdapter mForecastAdapter;
	private Callback mCallback;

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
		mRecyclerView = (RecyclerView) view.findViewById(R.id.list_view_forecast);
		mForecastAdapter = new ForecastAdapter(getContext(), null, 0);
		mRecyclerView.setAdapter(mForecastAdapter);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
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

	public interface Callback {
		void onItemClicked(Uri uri);
	}

	static class ForecastAdapter extends RecyclerViewCursorAdapter<ForecastAdapter.ViewHolder> {

		public ForecastAdapter(final Context context, final Cursor cursor, int flags) {
			super(context, cursor, flags);
		}

		@Override
		public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor) {
//			final int itemType = getItemViewType(cursor.getPosition());

			final int weatherId = cursor.getInt(ForecastsFragment_new.COL_WEATHER_CONDITION_ID);
//			final int weatherIconResource = (itemType == VIEW_TYPE_TODAY) ?
//			                                Utility.getArtResourceForWeatherCondition(weatherId) :
//			                                Utility.getIconResourceForWeatherCondition(weatherId);
			final int weatherIconResource = Utility.getIconResourceForWeatherCondition(weatherId);

			final long date = cursor.getLong(ForecastsFragment_new.COL_WEATHER_DATE);
			final String dateString = Utility.getFriendlyDayString(mContext, date);
			final String highTemp = Utility.formatTemperature(mContext,
			                                                  cursor.getDouble(ForecastsFragment_new.COL_WEATHER_MAX_TEMP));
			final String lowTemp = Utility.formatTemperature(mContext,
			                                                 cursor.getDouble(ForecastsFragment_new.COL_WEATHER_MIN_TEMP));
			final String weatherDescription = cursor.getString(ForecastsFragment_new.COL_WEATHER_DESC);

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
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_forecast, parent, false);
			return new ViewHolder(view);
		}

		static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
			private final ImageView mIconView;
			private final TextView mDateTextView;
			private final TextView mHighTempTextView;
			private final TextView mLowTempTextView;
			private final TextView mWeatherDescriptionTextView;
			private Uri mDataUri;

			public ViewHolder(final View itemView) {
				super(itemView);
				mIconView = (ImageView) itemView.findViewById(R.id.list_item_icon);
				mDateTextView = (TextView) itemView.findViewById(R.id.list_item_date_textview);
				mHighTempTextView = (TextView) itemView.findViewById(R.id.list_item_high_textview);
				mLowTempTextView = (TextView) itemView.findViewById(R.id.list_item_low_textview);
				mWeatherDescriptionTextView = (TextView) itemView.findViewById(R.id.list_item_forecast_textview);
				itemView.setOnClickListener(this);
			}

			@Override
			public void onClick(final View v) {
			}

		}
	}
}
