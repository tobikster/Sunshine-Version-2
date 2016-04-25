package com.android.example.sunshine.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.example.sunshine.R;
import com.android.example.sunshine.fragments.ForecastsFragment;
import com.android.example.sunshine.utils.Utility;

/**
 * {@link ForecastsAdapter} exposes a list of weather forecasts from a {@link android.database.Cursor} to a {@link
 * android.widget.ListView}.
 */
public class ForecastsAdapter extends CursorAdapter {
	@SuppressWarnings("unused")
	private static final String TAG = ForecastsAdapter.class.getSimpleName();
	static final int VIEW_TYPE_TODAY = 0;
	static final int VIEW_TYPE_FUTURE_DAY = 1;

	boolean mTodayLayoutUsed;

	public ForecastsAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		mTodayLayoutUsed = true;
	}

	public void setTodayLayoutUsed(boolean todayLayoutUsed) {
		mTodayLayoutUsed = todayLayoutUsed;
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
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final int viewType = getItemViewType(cursor.getPosition());
		final int layoutId = (viewType ==
		                      VIEW_TYPE_TODAY) ? R.layout.list_item_forecast_today : R.layout.list_item_forecast;
		View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
		ViewHolder viewHolder = new ViewHolder(view);
		view.setTag(viewHolder);
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final boolean isMetric = Utility.isMetric(mContext);

		final int itemType = getItemViewType(cursor.getPosition());

		ViewHolder viewHolder = (ViewHolder) view.getTag();

		final int weatherId = cursor.getInt(ForecastsFragment.COL_WEATHER_CONDITION_ID);
		final int weatherIconResource = (itemType == VIEW_TYPE_TODAY) ?
				Utility.getArtResourceForWeatherCondition(weatherId) :
				Utility.getIconResourceForWeatherCondition(weatherId);

		final String date = Utility
				.getFriendlyDayString(mContext, cursor.getLong(ForecastsFragment.COL_WEATHER_DATE));
		final String highTemp = Utility
				.formatTemperature(mContext, cursor.getDouble(ForecastsFragment.COL_WEATHER_MAX_TEMP), isMetric);
		final String lowTemp = Utility
				.formatTemperature(mContext, cursor.getDouble(ForecastsFragment.COL_WEATHER_MIN_TEMP), isMetric);
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