package com.android.example.sunshine.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.android.example.sunshine.activities.ForecastDetailsActivity;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastDetailsFragment extends Fragment {
	@SuppressWarnings("unused")
	private static final String TAG = ForecastDetailsFragment.class.getSimpleName();

	TextView mForecastTextView;
	ShareActionProvider mShareActionProvider;

	String mForecastString;

	public ForecastDetailsFragment() {
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
			mForecastString = startActivityIntent.getStringExtra(ForecastDetailsActivity.EXTRA_FORECAST);
			mForecastTextView.setText(mForecastString);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_forecast_details, menu);
		final MenuItem shareMenuItem = menu.findItem(R.id.action_share);
		mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareMenuItem);
		if(mShareActionProvider != null) {
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
		shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastString + " " + getString(R.string.sharing_hash_tag));
		return shareIntent;
	}
}
