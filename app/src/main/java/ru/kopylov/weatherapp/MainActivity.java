package ru.kopylov.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GetWeatherData.AsyncResponse {

    private static final String TAG = "MainActivity";

    private Button searchButton;
    private EditText searchField;
    private TextView cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchField = findViewById(R.id.searchField);
        cityName = findViewById(R.id.cityName);
        searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(this);
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
            temp.setText(tempCelsiusString);

            TextView pressure = findViewById(R.id.pressureValue);
            pressure.setText(weather.getString("pressure"));

            TextView sunrise = findViewById(R.id.timeSunriseValue);
            String sunriseTime = systemData.getString("sunrise");
            Locale locale = new Locale("ru", "RU");
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", locale);
            String dateString = dateFormat.format(new Date(Long.parseLong(sunriseTime) * 1000 + (60 * 60 * 1000) * 3));
            sunrise.setText(dateString);

            TextView sunset = findViewById(R.id.timeSunsetValue);
            String sunsetTime = systemData.getString("sunset");
            dateString = dateFormat.format(new Date(Long.parseLong(sunsetTime) * 1000 + (60 * 60 * 1000) * 3));
            sunset.setText(dateString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}