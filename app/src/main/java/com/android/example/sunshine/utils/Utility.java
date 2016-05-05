/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.example.sunshine.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.util.Log;

import com.android.example.sunshine.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utility {
	@SuppressWarnings("unused")
	private static final String LOG_TAG = Utility.class.getSimpleName();
	// Format used for storing dates in the database.  ALso used for converting those strings
	// back into date objects for comparison/processing.
	public static final String DATE_FORMAT = "yyyyMMdd";

	/**
	 * Helper method to convert the database representation of the date into something to display to users.  As classy
	 * and polished a user experience as "20140102" is, we can do better.
	 *
	 * @param context      Context to use for resource localization
	 * @param dateInMillis The date in milliseconds
	 *
	 * @return a user-friendly representation of the date.
	 */
	public static String getFriendlyDayString(final Context context, final long dateInMillis) {
		// The day string for forecast uses the following logic:
		// For today: "Today, June 8"
		// For tomorrow:  "Tomorrow"
		// For the next 5 days: "Wednesday" (just the day name)
		// For all days after that: "Mon Jun 8"

		String dateString;
		Calendar calendar = Calendar.getInstance();
		final int currentDay = calendar.get(Calendar.DAY_OF_YEAR);
		calendar.setTimeInMillis(dateInMillis);
		final int day = calendar.get(Calendar.DAY_OF_YEAR);

		// If the date we're building the String for is today's date, the format
		// is "Today, June 24"
		if (day == currentDay) {
			dateString = getFormattedMonthDay(dateInMillis);
		}
		else if (day < currentDay + 7) {
			dateString = getDayName(context, dateInMillis);
		}
		else {
			DateFormat formatter = DateFormat.getDateInstance();
			dateString = formatter.format(dateInMillis);
		}
		return dateString;
	}

	/**
	 * Converts db date format to the format "Month day", e.g "June 24".
	 *
	 * @param dateInMillis The db formatted date string, expected to be of the form specified in Utility.DATE_FORMAT
	 *
	 * @return The day in the form of a string formatted "December 6"
	 */
	public static String getFormattedMonthDay(final long dateInMillis) {
		return SimpleDateFormat.getDateInstance(SimpleDateFormat.LONG).format(dateInMillis);
	}

	/**
	 * Given a day, returns just the name to use for that day. E.g "today", "tomorrow", "wednesday".
	 *
	 * @param context      Context to use for resource localization
	 * @param dateInMillis The date in milliseconds
	 *
	 * @return
	 */
	public static String getDayName(final Context context, final long dateInMillis) {
		// If the date is today, return the localized version of "Today" instead of the actual
		// day name.

		String dayName;
		Calendar calendar = Calendar.getInstance();
		final int currentDay = calendar.get(Calendar.DAY_OF_YEAR);
		calendar.setTimeInMillis(dateInMillis);
		final int day = calendar.get(Calendar.DAY_OF_YEAR);
		if (day == currentDay) {
			dayName = context.getString(R.string.today);
		}
		else if (day == currentDay + 1) {
			dayName = context.getString(R.string.tomorrow);
		}
		else {
			dayName = new SimpleDateFormat("cccc", Locale.getDefault()).format(dateInMillis);
		}
		return dayName;
	}

	public static String getPreferredLocation(final Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(context.getString(R.string.pref_key_location),
		                       context.getString(R.string.pref_default_location));
	}

	public static int getPreferredForecastSize(final Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		int forecastSize = Integer.parseInt(preferences.getString(context.getString(R.string.pref_key_forecast_size),
		                                              context.getString(R.string.pref_default_forecast_size)));
		Log.d(LOG_TAG, String.format("getPreferredForecastSize: forecastSize: %d", forecastSize));
		return forecastSize;
	}

	public static String formatTemperature(final Context context, final double temperature) {
		double temp = (isMetric(context)) ? temperature : 9 * temperature / 5 + 32;
		return context.getString(R.string.format_temperature, temp);
	}

	public static boolean isMetric(final Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(context.getString(R.string.pref_key_units),
		                       context.getString(R.string.pref_value_units_metric))
		            .equals(context.getString(R.string.pref_value_units_metric));
	}

	public static String formatDate(final long dateInMillis) {
		Date date = new Date(dateInMillis);
		return DateFormat.getDateInstance().format(date);
	}

	public static String getFormattedWind(final Context context, float windSpeed, final float degrees) {
		int windFormat;
		if (Utility.isMetric(context)) {
			windFormat = R.string.format_wind_kmh;
		}
		else {
			windFormat = R.string.format_wind_mph;
			windSpeed = .621371192237334f * windSpeed;
		}

		final String[] directionsText = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
		final int DEGREES_TOTAL = 360;
		final int DIR_TOTAL = directionsText.length;

		String direction = directionsText[Math.round(degrees / (DEGREES_TOTAL / DIR_TOTAL)) % DIR_TOTAL];
		return String.format(context.getString(windFormat), windSpeed, direction);
	}

	public static String getFormattedHumidity(final Context context, final double humidity) {
		return context.getString(R.string.format_humidity, humidity);
	}

	public static String getFormattedPressure(final Context context, final float pressure) {
		return context.getString(R.string.format_pressure, pressure);
	}

	/**
	 * Helper method to provide the icon resource id according to the weather condition id returned by the
	 * OpenWeatherMap call.
	 *
	 * @param weatherId from OpenWeatherMap API response
	 *
	 * @return resource id for the corresponding icon. -1 if no relation is found.
	 */
	public static
	@DrawableRes
	int getIconResourceForWeatherCondition(final int weatherId) {
		// Based on weather code data found at:
		// http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
		if (weatherId >= 200 && weatherId <= 232) {
			return R.drawable.ic_storm;
		}
		else if (weatherId >= 300 && weatherId <= 321) {
			return R.drawable.ic_light_rain;
		}
		else if (weatherId >= 500 && weatherId <= 504) {
			return R.drawable.ic_rain;
		}
		else if (weatherId == 511) {
			return R.drawable.ic_snow;
		}
		else if (weatherId >= 520 && weatherId <= 531) {
			return R.drawable.ic_rain;
		}
		else if (weatherId >= 600 && weatherId <= 622) {
			return R.drawable.ic_snow;
		}
		else if (weatherId >= 701 && weatherId <= 761) {
			return R.drawable.ic_fog;
		}
		else if (weatherId == 761 || weatherId == 781) {
			return R.drawable.ic_storm;
		}
		else if (weatherId == 800) {
			return R.drawable.ic_clear;
		}
		else if (weatherId == 801) {
			return R.drawable.ic_light_clouds;
		}
		else if (weatherId >= 802 && weatherId <= 804) {
			return R.drawable.ic_cloudy;
		}
		return -1;
	}

	/**
	 * Helper method to provide the art resource id according to the weather condition id returned by the OpenWeatherMap
	 * call.
	 *
	 * @param weatherId from OpenWeatherMap API response
	 *
	 * @return resource id for the corresponding image. -1 if no relation is found.
	 */
	public static
	@DrawableRes
	int getArtResourceForWeatherCondition(final int weatherId) {
		// Based on weather code data found at:
		// http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
		if (weatherId >= 200 && weatherId <= 232) {
			return R.drawable.art_storm;
		}
		else if (weatherId >= 300 && weatherId <= 321) {
			return R.drawable.art_light_rain;
		}
		else if (weatherId >= 500 && weatherId <= 504) {
			return R.drawable.art_rain;
		}
		else if (weatherId == 511) {
			return R.drawable.art_snow;
		}
		else if (weatherId >= 520 && weatherId <= 531) {
			return R.drawable.art_rain;
		}
		else if (weatherId >= 600 && weatherId <= 622) {
			return R.drawable.art_rain;
		}
		else if (weatherId >= 701 && weatherId <= 761) {
			return R.drawable.art_fog;
		}
		else if (weatherId == 761 || weatherId == 781) {
			return R.drawable.art_storm;
		}
		else if (weatherId == 800) {
			return R.drawable.art_clear;
		}
		else if (weatherId == 801) {
			return R.drawable.art_light_clouds;
		}
		else if (weatherId >= 802 && weatherId <= 804) {
			return R.drawable.art_clouds;
		}
		return -1;
	}
}