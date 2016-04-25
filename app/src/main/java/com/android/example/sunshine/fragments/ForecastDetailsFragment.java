package com.android.example.sunshine.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.example.sunshine.R;
import com.android.example.sunshine.activities.ForecastDetailsActivity;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastDetailsFragment extends Fragment {
	TextView mForecastTextView;

	public ForecastDetailsFragment() {
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
			mForecastTextView.setText(startActivityIntent.getStringExtra(ForecastDetailsActivity.EXTRA_FORECAST));
		}
	}
}
