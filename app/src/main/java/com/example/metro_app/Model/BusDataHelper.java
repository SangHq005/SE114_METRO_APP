package com.example.metro_app.Model;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Point;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BusDataHelper {

    public interface OnStopsFetchedListener {
        void onResult(List<BusStop> stops);
        void onError(Exception e);
    }
    public interface OnRouteListFetchedListener {
        void onResult(List<RouteResponse> routeList);
        void onError(Exception e);
    }



    public static void fetchStopsInBounds(double topLat, double topLng, double botLat, double botLng, OnStopsFetchedListener listener) {
        String urlStr = "http://apicms.ebms.vn/businfo/getstopsinbounds/"
                + botLng + "/" + botLat + "/" + topLng + "/" + topLat;

        new AsyncTask<Void, Void, List<BusStop>>() {
            Exception error = null;

            @Override
            protected List<BusStop> doInBackground(Void... voids) {
                List<BusStop> stops = new ArrayList<>();

                try {
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);
                    conn.connect();

                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    String responseString = result.toString();
                    Log.d("BusDataHelper", "JSON Response: " + responseString);

                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<BusStop>>() {}.getType();
                    stops = gson.fromJson(responseString, listType);

                    in.close();
                    conn.disconnect();

                } catch (Exception e) {
                    error = e;
                }

                return stops;
            }

            @Override
            protected void onPostExecute(List<BusStop> stops) {
                if (error != null) {
                    listener.onError(error);
                } else {
                    listener.onResult(stops);
                }
            }
        }.execute();
    }
    public static void fetchPathByStop(double oriLat, double oriLng, double desLat, double desLng, OnRouteListFetchedListener listener) {
        String urlStr = "http://apicms.ebms.vn/pathfinding/getpathbystop/"
                + oriLat + "," + oriLng + "/"
                + desLat + "," + desLng + "/2";

        new AsyncTask<Void, Void, List<RouteResponse>>() {
            Exception error = null;

            @Override
            protected List<RouteResponse> doInBackground(Void... voids) {
                try {
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);
                    conn.connect();

                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    String responseString = result.toString();
                    Log.d("BusDataHelper", "Path JSON Response: " + responseString);

                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<RouteResponse>>() {}.getType();
                    List<RouteResponse> responseList = gson.fromJson(responseString, listType);
                    in.close();
                    conn.disconnect();

                    return responseList;

                } catch (Exception e) {
                    error = e;
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<RouteResponse> routeList) {
                if (error != null) {
                    listener.onError(error);
                } else {
                    listener.onResult(routeList);
                }
            }
        }.execute();
    }


}

