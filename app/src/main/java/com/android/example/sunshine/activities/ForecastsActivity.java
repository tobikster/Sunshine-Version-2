package com.android.example.sunshine.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.example.sunshine.R;
import com.android.example.sunshine.fragments.ForecastsFragment;
import com.android.example.sunshine.utils.Utility;

public class ForecastsActivity extends AppCompatActivity {
	private static final String TAG = ForecastsActivity.class.getSimpleName();
	private static final String FORECAST_FRAGMENT_TAG = "forecast_fragment_tag";

	String mLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forecasts);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		mLocation = Utility.getPreferredLocation(this);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			                           .add(R.id.container, new ForecastsFragment(), FORECAST_FRAGMENT_TAG)
			                           .commit();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		String currentLocation = Utility.getPreferredLocation(this);
		if (!currentLocation.equals(mLocation)) {
			((ForecastsFragment) getSupportFragmentManager().findFragmentByTag(FORECAST_FRAGMENT_TAG)).onLocationChanged();
			mLocation = currentLocation;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_forecasts, menu);
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
}
