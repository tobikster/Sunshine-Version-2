package com.android.example.sunshine.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.example.sunshine.R;
import com.android.example.sunshine.fragments.ForecastDetailsFragment;
import com.android.example.sunshine.fragments.ForecastsFragment;
import com.android.example.sunshine.sync.SunshineSyncAdapter;
import com.android.example.sunshine.utils.Utility;

public class MainActivity extends AppCompatActivity implements ForecastsFragment.Callback {
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final String FORECAST_DETAILS_FRAGMENT_TAG = "forecast_details_fragment_tag";

	String mLocation;
	boolean mTwoPaneLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLocation = Utility.getPreferredLocation(this);
		setContentView(R.layout.activity_main);
		mTwoPaneLayout = findViewById(R.id.weather_detail_container) != null;

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayUseLogoEnabled(true);
			actionBar.setLogo(R.drawable.ic_logo);
			actionBar.setTitle(null);
			if(!mTwoPaneLayout) {
				actionBar.setElevation(0f);
			}
		}
		SunshineSyncAdapter.initializeSyncAdapter(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		String currentLocation = Utility.getPreferredLocation(this);
		ForecastsFragment forecastsFragment = (ForecastsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecasts);
		if (mTwoPaneLayout && forecastsFragment != null) {
			forecastsFragment.setUseTodayLayout(false);
		}
		if (currentLocation != null && !currentLocation.equals(mLocation)) {
			if (forecastsFragment != null) {
				forecastsFragment.onLocationChanged();
			}
			ForecastDetailsFragment detailsFragment = (ForecastDetailsFragment) getSupportFragmentManager().findFragmentByTag(
					FORECAST_DETAILS_FRAGMENT_TAG);
			if (detailsFragment != null) {
				detailsFragment.onLocationChanged(currentLocation);
			}
			((ForecastsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecasts)).onLocationChanged();
			mLocation = currentLocation;
		}
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
			case R.id.action_map:
				openPreferredLocationOnMap();
				eventConsumed = true;
				break;
			default:
				eventConsumed = super.onOptionsItemSelected(item);
		}
		return eventConsumed;
	}

	private void openPreferredLocationOnMap() {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		final String location = preferences.getString(getString(R.string.pref_location_key),
		                                              getString(R.string.pref_location_default));

		Uri geoLocation = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q", location).build();
		Intent openMapIntent = new Intent(Intent.ACTION_VIEW);
		openMapIntent.setData(geoLocation);
		if (openMapIntent.resolveActivity(getPackageManager()) != null) {
			startActivity(openMapIntent);
		}
		else {
			Log.d(TAG,
			      "openPreferredLocationOnMap: Couldn't call " + location + ", no activities can handle the Intent");
		}
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
