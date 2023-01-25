package ru.kopylov.weatherapp;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class GetWeatherData extends AsyncTask<URL, Void, String> {
    private static final String TAG = "GetWeatherData";

    public interface AsyncResponse {
        void processFinish(String output);
    }

    public AsyncResponse delegate;

    public GetWeatherData(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    protected String getResponse(URL url) throws IOException {

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        try {
            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean isInput = scanner.hasNext();
            String result;

            if (isInput) {
                result = scanner.next();
                return result;
            }

            return null;
        } finally {
            urlConnection.disconnect();
        }
    }

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "onPreExecute: called");
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d(TAG, "onPostExecute: called");
        Log.d(TAG, "onPostExecute: " + result);
        delegate.processFinish(result);
    }

    @Override
    protected String doInBackground(URL[] url) {
        Log.d(TAG, "doInBackground: called");

        String result = null;
        URL urlQuery = url[0];

        try {
            result = getResponse(urlQuery);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
