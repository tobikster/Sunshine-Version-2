package com.android.example.sunshine.adapters;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.example.sunshine.R;
import com.android.example.sunshine.data.WeatherContract;
import com.android.example.sunshine.utils.RecyclerViewCursorAdapter;
import com.android.example.sunshine.utils.Utility;

public class ForecastAdapter extends RecyclerViewCursorAdapter<ForecastAdapter.ViewHolder> {
	private static final int ITEM_VIEW_TYPE_TODAY = 0;
	private static final int ITEM_VIEW_TYPE_OTHER = 1;
	private ViewHolder.OnClickListener mOnClickListener;

	private int mColumnIndexWeatherDate;
	private int mColumnIndexWeatherDescription;
	private int mColumnIndexWeatherMaxTemp;
	private int mColumnIndexWeatherMinTemp;
	private int mColumnIndexWeatherId;
	private boolean mTodayLayoutUsed;

	public ForecastAdapter(final Context context, final Cursor cursor, int flags, ViewHolder.OnClickListener onClickListener) {
		super(context, cursor, flags);
		mOnClickListener = onClickListener;
		mTodayLayoutUsed = true;
	}

	@Override
	public Cursor swapCursor(final Cursor newCursor) {
		Cursor oldCursor = super.swapCursor(newCursor);
		if (newCursor != null) {
			getColumnsFromCursor(newCursor);
		}
		return oldCursor;
	}

	private void getColumnsFromCursor(Cursor cursor) {
		mColumnIndexWeatherDate = cursor.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_DATE);
		mColumnIndexWeatherDescription = cursor.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC);
		mColumnIndexWeatherMaxTemp = cursor.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
		mColumnIndexWeatherMinTemp = cursor.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
		mColumnIndexWeatherId = cursor.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID);
	}

	@Override
	public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor) {
		final int itemType = viewHolder.getItemViewType();


		final int weatherId = cursor.getInt(mColumnIndexWeatherId);
		final int weatherIconResource = (itemType == ITEM_VIEW_TYPE_TODAY) ?
		                                Utility.getArtResourceForWeatherCondition(weatherId) :
		                                Utility.getIconResourceForWeatherCondition(weatherId);
//		final int weatherIconResource = Utility.getIconResourceForWeatherCondition(weatherId);

		final long date = cursor.getLong(mColumnIndexWeatherDate);
		final String dateString = Utility.getFriendlyDayString(mContext, date);
		final String highTemp = Utility.formatTemperature(mContext,
		                                                  cursor.getDouble(mColumnIndexWeatherMaxTemp));
		final String lowTemp = Utility.formatTemperature(mContext,
		                                                 cursor.getDouble(mColumnIndexWeatherMinTemp));
		final String weatherDescription = cursor.getString(mColumnIndexWeatherDescription);

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

	public void setTodayLayoutUsed(boolean useTodayLayout) {
		mTodayLayoutUsed = useTodayLayout;
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