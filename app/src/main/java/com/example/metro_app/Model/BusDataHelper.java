package com.example.metro_app.Model;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Point;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusDataHelper {
    public interface OnPathFetchedListener {
        void onResult(List<Point> points);
        void onError(Exception e);
    }


    public interface OnStopsFetchedListener {
        void onResult(List<BusStop> stops);

        void onError(Exception e);
    }
    public interface OnRouteVarsFetchedListener {
        void onResult(List<RouteVariant> routeVars);
        void onError(Exception e);
    }


    public interface OnRouteListFetchedListener {
        void onResult(List<RouteResponse> routeList);

        void onError(Exception e);
    }
    public interface OnStopsByRouteFetchedListener {
        void onResult(List<BusStop> stops);
        void onError(Exception e);
    }

    public interface OnAllRoutesFetchedListener {
        void onResult(List<RawRoute> routes);
        void onError(Exception e);
    }
    public interface OnPathsByRouteFetchedListener {
        void onResult(List<RouteResponse> routeList);
        void onError(Exception e);
    }
    public static void fetchAllRoutes(OnAllRoutesFetchedListener listener) {
        String urlStr = "http://apicms.ebms.vn/businfo/getallroute";

        new AsyncTask<Void, Void, List<RawRoute>>() {
            Exception error = null;

            @Override
            protected List<RawRoute> doInBackground(Void... voids) {
                List<RawRoute> routeList = new ArrayList<>();

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
                    Log.d("BusDataHelper", "All Routes: " + responseString);

                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<RawRoute>>() {}.getType();
                    routeList = gson.fromJson(responseString, listType);

                    in.close();
                    conn.disconnect();

                } catch (Exception e) {
                    error = e;
                }

                return routeList;
            }

            @Override
            protected void onPostExecute(List<RawRoute> routes) {
                if (error != null) {
                    listener.onError(error);
                } else {
                    listener.onResult(routes);
                }
            }
        }.execute();
    }
    public static void fetchRouteVars(int routeId, OnRouteVarsFetchedListener listener) {
        String urlStr = "http://apicms.ebms.vn/businfo/getvarsbyroute/" + routeId;

        new AsyncTask<Void, Void, List<RouteVariant>>() {
            Exception error = null;

            @Override
            protected List<RouteVariant> doInBackground(Void... voids) {
                List<RouteVariant> list = new ArrayList<>();

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
                    Log.d("BusDataHelper", "RouteVars: " + responseString);

                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<RouteVariant>>() {}.getType();
                    list = gson.fromJson(responseString, listType);

                    in.close();
                    conn.disconnect();
                } catch (Exception e) {
                    error = e;
                }

                return list;
            }

            @Override
            protected void onPostExecute(List<RouteVariant> result) {
                if (error != null) {
                    listener.onError(error);
                } else {
                    listener.onResult(result);
                }
            }
        }.execute();
    }
    public static void fetchStopsByRouteVar(int stopId, int routeVarId, OnStopsFetchedListener listener) {
        String urlStr = "http://apicms.ebms.vn/businfo/getstopsbyvar/" + stopId + "/" + routeVarId;

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

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<BusStop>>() {}.getType();
                    stops = gson.fromJson(result.toString(), listType);

                    reader.close();
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
                    Type listType = new TypeToken<List<RouteResponse>>() {
                    }.getType();
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
    public static void fetchPathFromStopId(int stopId, int routeVarId, OnPathFetchedListener listener) {
        String urlStr = "http://apicms.ebms.vn/businfo/getpathsbyvar/" + stopId + "/" + routeVarId;

        new AsyncTask<Void, Void, List<Point>>() {
            Exception error = null;

            @Override
            protected List<Point> doInBackground(Void... voids) {
                try {
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);
                    conn.connect();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    reader.close();
                    conn.disconnect();

                    return parsePointsFromJson(result.toString());

                } catch (Exception e) {
                    error = e;
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<Point> result) {
                if (error != null) {
                    listener.onError(error);
                } else {
                    listener.onResult(result);
                }
            }
        }.execute();
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
                    Type listType = new TypeToken<List<BusStop>>() {
                    }.getType();
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
    public void uploadStationsToFirestore(Context context) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        try {
            InputStream is = context.getAssets().open("stations.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                Station station = new Station();
                station.StopId = obj.getInt("StopId");
                station.Code = obj.getString("Code");
                station.Name = obj.getString("Name");
                station.StopType = obj.getString("StopType");
                station.Zone = obj.getString("Zone");
                station.Ward = obj.getString("Ward");
                station.AddressNo = obj.getString("AddressNo");
                station.Street = obj.getString("Street");
                station.SupportDisability = obj.getString("SupportDisability");
                station.Status = obj.getString("Status");
                station.Lng = obj.getDouble("Lng");
                station.Lat = obj.getDouble("Lat");
                station.Search = obj.getString("Search");
                station.Routes = obj.getString("Routes");

                // Upload lên Firestore, theo từng StopId
                db.collection("stations")
                        .document(String.valueOf(station.StopId))
                        .set(station)
                        .addOnSuccessListener(aVoid -> Log.d("UPLOAD", "Success: " + station.Code))
                        .addOnFailureListener(e -> Log.e("UPLOAD", "Error: ", e));
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
    public void uploadGeoPointListToFirestore(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        try {
            InputStream is = context.getAssets().open("MetroWay1.json"); // File JSON chứa mảng lat/lng
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONObject jsonObject = new JSONObject(json);
            JSONArray latArray = jsonObject.getJSONArray("lat");
            JSONArray lngArray = jsonObject.getJSONArray("lng");

            int len = Math.min(latArray.length(), lngArray.length());

            List<GeoPoint> geoPoints = new ArrayList<>();

            for (int i = 0; i < len; i++) {
                double lat = latArray.getDouble(i);
                double lng = lngArray.getDouble(i);
                geoPoints.add(new GeoPoint(lat, lng));
            }

            Map<String, Object> data = new HashMap<>();
            data.put("points", geoPoints);

            db.collection("MetroWay")
                    .document("LuotDi")
                    .set(data)
                    .addOnSuccessListener(aVoid -> Log.d("UPLOAD", "Tải thành công"))
                    .addOnFailureListener(e -> Log.e("UPLOAD", "Lỗi khi tải lên", e));

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        try {
            InputStream is = context.getAssets().open("MetroWay2.json"); // File JSON chứa mảng lat/lng
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONObject jsonObject = new JSONObject(json);
            JSONArray latArray = jsonObject.getJSONArray("lat");
            JSONArray lngArray = jsonObject.getJSONArray("lng");

            int len = Math.min(latArray.length(), lngArray.length());

            List<GeoPoint> geoPoints = new ArrayList<>();

            for (int i = 0; i < len; i++) {
                double lat = latArray.getDouble(i);
                double lng = lngArray.getDouble(i);
                geoPoints.add(new GeoPoint(lat, lng));
            }

            Map<String, Object> data = new HashMap<>();
            data.put("points", geoPoints);

            db.collection("MetroWay")
                    .document("LuotVe")
                    .set(data)
                    .addOnSuccessListener(aVoid -> Log.d("UPLOAD", "Tải thành công"))
                    .addOnFailureListener(e -> Log.e("UPLOAD", "Lỗi khi tải lên", e));

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
    public static List<Point> parsePointsFromJson(String json) {
        List<Point> pointList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray latArray = jsonObject.getJSONArray("lat");
            JSONArray lngArray = jsonObject.getJSONArray("lng");

            int len = Math.min(latArray.length(), lngArray.length());
            for (int i = 0; i < len; i++) {
                double lat = latArray.getDouble(i);
                double lng = lngArray.getDouble(i);
                Point point = Point.fromLngLat(lng, lat);
                pointList.add(point);

                // Ghi log
                Log.d("ParsePoint", "Point " + i + ": " + point.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return pointList;
    }

}

