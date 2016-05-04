package com.android.example.sunshine.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.android.example.sunshine.R;
import com.android.example.sunshine.fragments.ForecastDetailsFragment;
import com.android.example.sunshine.fragments.ForecastsFragment;
import com.android.example.sunshine.sync.SunshineSyncAdapter;

public class MainActivity extends AppCompatActivity implements ForecastsFragment.Callback {
	@SuppressWarnings({"unused"})
	private static final String LOG_TAG = MainActivity.class.getSimpleName();
	private static final String FORECAST_DETAILS_FRAGMENT_TAG = "forecast_details_fragment_tag";

	private boolean mTwoPaneLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTwoPaneLayout = findViewById(R.id.weather_detail_container) != null;

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayUseLogoEnabled(true);
			actionBar.setLogo(R.drawable.ic_logo);
			actionBar.setTitle(null);
			if (!mTwoPaneLayout) {
				actionBar.setElevation(0f);
			}
		}

		ForecastsFragment forecastsFragment = (ForecastsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecasts);
		if (mTwoPaneLayout && forecastsFragment != null) {
			forecastsFragment.setTodayLayoutUsed(false);
		}

		SunshineSyncAdapter.initializeSyncAdapter(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean eventConsumed;
		switch (item.getItemId()) {
			case R.id.action_settings:
				startActivity(new Intent(this, SettingsActivity.class));
				eventConsumed = true;
				break;
			default:
				eventConsumed = super.onOptionsItemSelected(item);
		}
		return eventConsumed;
	}

	@Override
	public void onItemClicked(Uri uri) {
		if (mTwoPaneLayout) {
			final Fragment detailsFragment = ForecastDetailsFragment.newInstance(uri);
			getSupportFragmentManager().beginTransaction()
			                           .replace(R.id.weather_detail_container,
			                                    detailsFragment,
			                                    FORECAST_DETAILS_FRAGMENT_TAG)
			                           .commit();
		}
		else {
			startActivity(new Intent(this, ForecastDetailsActivity.class).setData(uri));
		}
	}
}
