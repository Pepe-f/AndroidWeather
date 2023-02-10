package ru.kopylov.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GetWeatherData.AsyncResponse, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "MainActivity";
    private Button searchButton;
    private EditText searchField;
    private TextView cityName;
    protected static boolean showWind = true;
    protected static boolean showPressure = true;
    protected static String color = "red";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchField = findViewById(R.id.searchField);
        cityName = findViewById(R.id.cityName);
        searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(this);

        setupSharedPreferences();
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        showPressure = sharedPreferences.getBoolean(getString(R.string.show_pressure_settings_key), true);
        showWind = sharedPreferences.getBoolean(getString(R.string.show_wind_settings_key), true);
        color = sharedPreferences.getString(getString(R.string.pref_color_key), getString(R.string.pref_color_red_value));

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.show_pressure_settings_key))) {
            showPressure = sharedPreferences.getBoolean(getString(R.string.show_pressure_settings_key), true);
        } else if (key.equals(getString(R.string.show_wind_settings_key))) {
            showWind = sharedPreferences.getBoolean(getString(R.string.show_wind_settings_key), true);
        } else if (key.equals(getString(R.string.pref_color_key))) {
            color = sharedPreferences.getString(getString(R.string.pref_color_key), getString(R.string.pref_color_red_value));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        URL url = buildUrl(searchField.getText().toString());
        cityName.setText(searchField.getText().toString());
        new GetWeatherData(this).execute(url);
    }

    private URL buildUrl(String city) {
        String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
        String PARAM_CITY = "q";
        String PARAM_APP_ID = "appid";
        String appIdValue = "d0fe89df954130c8a25b9164e8bbfbef";

        Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendQueryParameter(PARAM_CITY, city).appendQueryParameter(PARAM_APP_ID, appIdValue).build();
        URL url = null;

        try {
            url = new URL(buildUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    @Override
    public void processFinish(String output) {
        Log.d(TAG, "processFinish: " + output);
        try {
            JSONObject resultJSON = new JSONObject(output);
            JSONObject weather = resultJSON.getJSONObject("main");
            JSONObject systemData = resultJSON.getJSONObject("sys");

            TextView temp = findViewById(R.id.tempValue);
            String tempKelvin = weather.getString("temp");
            float tempCelsius = Float.parseFloat(tempKelvin) - 273.15f;
            String tempCelsiusString = Float.toString(tempCelsius);

            if (showWind) {
                temp.setText(tempCelsiusString);
            } else {
                temp.setText("");
            }

            TextView pressure = findViewById(R.id.pressureValue);

            if (showPressure) {
                pressure.setText(weather.getString("pressure"));
            } else {
                pressure.setText("");
            }

            TextView sunrise = findViewById(R.id.timeSunriseValue);
            String sunriseTime = systemData.getString("sunrise");
            Locale locale = new Locale("ru", "RU");
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", locale);
            String dateString = dateFormat.format(new Date(Long.parseLong(sunriseTime) * 1000 + (60 * 60 * 1000) * 3));

            sunrise.setTextColor(Color.parseColor(color));
            sunrise.setText(dateString);

            TextView sunset = findViewById(R.id.timeSunsetValue);
            String sunsetTime = systemData.getString("sunset");
            dateString = dateFormat.format(new Date(Long.parseLong(sunsetTime) * 1000 + (60 * 60 * 1000) * 3));

            sunset.setTextColor(Color.parseColor(color));
            sunset.setText(dateString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}