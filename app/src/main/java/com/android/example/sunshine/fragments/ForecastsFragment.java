package com.android.example.sunshine.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.example.sunshine.FetchWeatherTask;
import com.android.example.sunshine.R;
import com.android.example.sunshine.activities.ForecastDetailsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastsFragment extends Fragment implements AdapterView.OnItemClickListener {

	ArrayAdapter<String> mForecastAdapter;

	public ForecastsFragment() {
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_forecasts, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ListView forecastsListView = (ListView) view.findViewById(R.id.list_view_forecast);


		mForecastAdapter = new ArrayAdapter<>(getActivity(),
		                                      R.layout.list_item_forecast,
		                                      R.id.list_item_forecast_text_view,
		                                      new ArrayList<String>());

		forecastsListView.setAdapter(mForecastAdapter);
		forecastsListView.setOnItemClickListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();
		refreshForecast();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_forecast, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean eventConsumed;
		switch (item.getItemId()) {
			case R.id.action_refresh:
				refreshForecast();
				eventConsumed = true;
				break;

			default:
				eventConsumed = super.onOptionsItemSelected(item);
				break;
		}
		return eventConsumed;
	}

	private void refreshForecast() {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		final String location = preferences.getString(getString(R.string.pref_location_key),
		                                              getString(R.string.pref_location_default));
		final String units = preferences.getString(getString(R.string.pref_units_key),
		                                           getString(R.string.pref_units_default));

		new FetchWeatherTask(getActivity(), mForecastAdapter).execute(location, units);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent startDetailsActivityIntent = new Intent(getActivity(), ForecastDetailsActivity.class);
		startDetailsActivityIntent.putExtra(ForecastDetailsActivity.EXTRA_FORECAST, mForecastAdapter.getItem(position));
		startActivity(startDetailsActivityIntent);
	}
}
