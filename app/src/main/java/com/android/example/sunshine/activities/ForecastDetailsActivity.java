package com.android.example.sunshine.activities;

import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.android.example.sunshine.R;
import com.android.example.sunshine.fragments.ForecastDetailsFragment;

public class ForecastDetailsActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forecast_details);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		if (savedInstanceState == null) {
			Fragment detailsFragment = ForecastDetailsFragment.newInstance(getIntent().getData());
			getSupportFragmentManager().beginTransaction()
			                           .add(R.id.weather_detail_container, detailsFragment)
			                           .commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_forecast_details, menu);
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
}
