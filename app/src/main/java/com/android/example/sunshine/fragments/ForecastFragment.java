package com.android.example.sunshine.fragments;

import com.android.example.sunshine.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

	ArrayAdapter<String> mForecastAdapter;

	public ForecastFragment() {
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_forecast, container, false);
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
		new FetchWeatherTask().execute("50566,pl");
	}

	class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
		private final static String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily";
		private final static String PARAM_QUERY = "q";
		private final static String PARAM_FORMAT = "mode";
		private final static String PARAM_UNITS = "units";
		private final static String PARAM_DAYS = "cnt";
		private final static String PARAM_API_KEY = "appid";
		private final static String API_KEY = "d6c755bc2bccd4dbdc69bd95dec436ae";
		private final String TAG = FetchWeatherTask.class.getSimpleName();

		/* The date/time conversion code is going to be moved outside the asynctask later,
		 * so for convenience we're breaking it out into its own method now.
         */
		private String getReadableDateString(long time) {
			// Because the API returns a unix timestamp (measured in seconds),
			// it must be converted to milliseconds in order to be converted to valid date.
			SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
			return shortenedDateFormat.format(time);
		}

		/**
		 * Prepare the weather high/lows for presentation.
		 */
		private String formatHighLows(double high, double low) {
			// For presentation, assume the user doesn't care about tenths of a degree.
			long roundedHigh = Math.round(high);
			long roundedLow = Math.round(low);

			return roundedHigh + "/" + roundedLow;
		}

		/**
		 * Take the String representing the complete forecast in JSON Format and
		 * pull out the data we need to construct the Strings needed for the wireframes.
		 *
		 * Fortunately parsing is easy:  constructor takes the JSON string and converts it
		 * into an Object hierarchy for us.
		 */
		private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays) throws JSONException {

			// These are the names of the JSON objects that need to be extracted.
			final String OWM_LIST = "list";
			final String OWM_WEATHER = "weather";
			final String OWM_TEMPERATURE = "temp";
			final String OWM_MAX = "max";
			final String OWM_MIN = "min";
			final String OWM_DESCRIPTION = "main";

			JSONObject forecastJson = new JSONObject(forecastJsonStr);
			JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

			// OWM returns daily forecasts based upon the local time of the city that is being
			// asked for, which means that we need to know the GMT offset to translate this data
			// properly.

			// Since this data is also sent in-order and the first day is always the
			// current day, we're going to take advantage of that to get a nice
			// normalized UTC date for all of our weather.

			Time dayTime = new Time();
			dayTime.setToNow();

			// we start at the day returned by local time. Otherwise this is a mess.
			int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

			// now we work exclusively in UTC
			dayTime = new Time();

			String[] resultStrings = new String[numDays];
			for (int i = 0; i < weatherArray.length(); i++) {
				// For now, using the format "Day, description, hi/low"
				String day;
				String description;
				String highAndLow;

				// Get the JSON object representing the day
				JSONObject dayForecast = weatherArray.getJSONObject(i);

				// The date/time is returned as a long.  We need to convert that
				// into something human-readable, since most people won't read "1400356800" as
				// "this saturday".
				long dateTime;
				// Cheating to convert this to UTC time, which is what we want anyhow
				dateTime = dayTime.setJulianDay(julianStartDay + i);
				day = getReadableDateString(dateTime);

				// description is in a child array called "weather", which is 1 element long.
				JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
				description = weatherObject.getString(OWM_DESCRIPTION);

				// Temperatures are in a child object called "temp".  Try not to name variables
				// "temp" when working with temperature.  It confuses everybody.
				JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
				double high = temperatureObject.getDouble(OWM_MAX);
				double low = temperatureObject.getDouble(OWM_MIN);

				highAndLow = formatHighLows(high, low);
				resultStrings[i] = day + " - " + description + " - " + highAndLow;
			}
			return resultStrings;

		}

		@Override
		protected String[] doInBackground(String... params) {
			String[] result = null;
			if (params.length > 0) {
				final String query = params[0];
				final String format = "json";
				final String units = "metric";
				final String days = "7";

				// These two need to be declared outside the try/catch
				// so that they can be closed in the finally block.
				HttpURLConnection urlConnection = null;
				BufferedReader reader = null;

				// Will contain the raw JSON response as a string.
				String forecastJsonStr = null;

				try {
					// Construct the URL for the OpenWeatherMap query
					// Possible parameters are available at OWM's forecast API page, at
					// http://openweathermap.org/API#forecast
					Uri uri = Uri.parse(FORECAST_BASE_URL)
					             .buildUpon()
					             .appendQueryParameter(PARAM_QUERY, query)
					             .appendQueryParameter(PARAM_DAYS, days)
					             .appendQueryParameter(PARAM_UNITS, units)
					             .appendQueryParameter(PARAM_FORMAT, format)
					             .appendQueryParameter(PARAM_API_KEY, API_KEY)
					             .build();

					URL url = new URL(uri.toString());

					// Create the request to OpenWeatherMap, and open the connection
					urlConnection = (HttpURLConnection) url.openConnection();
					urlConnection.setRequestMethod("GET");
					urlConnection.connect();

					// Read the input stream into a String
					InputStream inputStream = urlConnection.getInputStream();
					StringBuilder buffer = new StringBuilder();
					if (inputStream != null) {
						reader = new BufferedReader(new InputStreamReader(inputStream));

						String line;
						while ((line = reader.readLine()) != null) {
							// Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
							// But it does make debugging a *lot* easier if you print out the completed
							// buffer for debugging.
							buffer.append(line).append("\n");
						}

						if (buffer.length() != 0) {
							forecastJsonStr = buffer.toString();
							result = getWeatherDataFromJson(forecastJsonStr,
							                                Integer.parseInt(days));
						}
					}
				} catch (IOException e) {
					Log.e(TAG, "Error while loading weather data!", e);
				} catch (JSONException e) {
					Log.e(TAG, "doInBackground: Error while parsing weather data!", e);
				} finally {
					if (urlConnection != null) {
						urlConnection.disconnect();
					}
					if (reader != null) {
						try {
							reader.close();
						} catch (final IOException e) {
							Log.e("PlaceholderFragment", "Error closing stream", e);
						}
					}
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(String[] forecasts) {
			mForecastAdapter.clear();
			for (String forecast : forecasts) {
				mForecastAdapter.add(forecast);
			}
		}
	}
}
