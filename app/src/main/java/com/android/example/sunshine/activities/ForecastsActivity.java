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

public class ForecastsActivity extends AppCompatActivity {
	private static final String TAG = ForecastsActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forecasts);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Log.d(TAG, "onCreate");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
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
		if(openMapIntent.resolveActivity(getPackageManager()) != null) {
			startActivity(openMapIntent);
		}
		else {
			Log.d(TAG, "openPreferredLocationOnMap: Couldn't call " + location + ", no activities can handle the Intent");
		}
	}
}
