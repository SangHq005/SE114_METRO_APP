package com.example.metro_app.Activity.User;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.metro_app.Activity.MapBoxFragment;
import com.example.metro_app.Model.BusDataHelper;
import com.example.metro_app.Model.BusStop;
import com.example.metro_app.Model.RawCoor;
import com.example.metro_app.Model.RouteResponse;
import com.example.metro_app.Model.Stop;
import com.example.metro_app.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CoordinateBounds;
import com.mapbox.maps.MapboxMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FindPathActivity extends AppCompatActivity {

    private static final int REQUEST_SEARCH_FROM = 1;
    private static final int REQUEST_SEARCH_TO = 2;

    private TextView tvSearchFrom, tvSearchTo;
    private Point pointFrom, pointTo;
    private FloatingActionButton imgMyLocation;
    private MapBoxFragment mapFragment;
    private Point currentLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private boolean isFirstUpdate = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_path);

        tvSearchFrom = findViewById(R.id.tvSearchFrom);
        tvSearchTo = findViewById(R.id.tvSearchTo);
        imgMyLocation = findViewById(R.id.btnMyLocation);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mapFragment = new MapBoxFragment();

        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, mapFragment)
                .commit();

        mapFragment.setOnMapReadyCallback(mapboxMap -> {
            mapFragment.zoomToLocation(Point.fromLngLat(106.7009,10.7769 ));
        });


        tvSearchFrom.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchPlaceActivity.class);
            startActivityForResult(intent, REQUEST_SEARCH_FROM);
        });

        tvSearchTo.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchPlaceActivity.class);
            startActivityForResult(intent, REQUEST_SEARCH_TO);
        });

        imgMyLocation.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mapFragment.zoomToLocation(currentLocation);
            }
        });

        // Yêu cầu quyền nếu chưa cấp
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            double lat = location.getLatitude();
                            double lon = location.getLongitude();
                            currentLocation = Point.fromLngLat(lon, lat);

                            mapFragment.updateCurrentLocationMarker(currentLocation); // vẽ avatar trong fragment
                            mapFragment.zoomToLocation(currentLocation);
                        }
                    });

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    if (locationResult == null) return;
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        currentLocation = Point.fromLngLat(location.getLongitude(), location.getLatitude());

                        if (isFirstUpdate) {
                            mapFragment.zoomToLocation(currentLocation);
                            isFirstUpdate = false;
                        }

                        mapFragment.updateCurrentLocationMarker(currentLocation);
                    }
                }

            };
        }
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("goto_lat") && intent.hasExtra("goto_lng")) {
            String name = intent.getStringExtra("goto_name");
            double lat = intent.getDoubleExtra("goto_lat", 0);
            double lng = intent.getDoubleExtra("goto_lng", 0);
            pointTo = Point.fromLngLat(lng, lat);

            mapFragment.setOnMapReadyCallback(mapboxMap -> {
                mapFragment.addMarkerAt(pointTo, R.drawable.location_pin, 50);
                mapFragment.zoomToLocation(pointTo);
                if (name != null) {
                    tvSearchTo.setText(name);
                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                        if(location != null){
                            pointFrom = Point.fromLngLat(location.getLongitude(),location.getLatitude());
                            tvSearchFrom.setText("vị trí của tôi");
                            if (pointFrom != null) {
                                BusDataHelper.fetchPathByStop(pointFrom.latitude(), pointFrom.longitude(),
                                        pointTo.latitude(), pointTo.longitude(),
                                        new BusDataHelper.OnRouteListFetchedListener() {
                                            @Override
                                            public void onResult(List<RouteResponse> routes) {
                                                showRouteDialog(routes);
                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                Toast.makeText(FindPathActivity.this, "Không tìm được đường", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    });
                }
            });



        }
    }
    private void showRouteDialog(List<RouteResponse> routeResponses) {
        List<String> titles = new ArrayList<>();
        for (RouteResponse r : routeResponses) {
            titles.add(r.Title + " - " + r.Desc);
        }

        new AlertDialog.Builder(this)
                .setTitle("Chọn tuyến đường")
                .setItems(titles.toArray(new String[0]), (dialog, which) -> {
                    RouteResponse selectedRoute = routeResponses.get(which);

                    Bitmap rawBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_bus_stop);
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(rawBitmap, 80, 80, false);
                    drawRouteOnMap(selectedRoute);
                    mapFragment.clearAllMarkers();
                    for (Stop entry : selectedRoute.stops) {
                        Point stop = Point.fromLngLat(entry.Lng, entry.Lat);
                        mapFragment.addMarkerAt(stop,R.drawable.ic_bus_stop,70);
                    }
                })
                .show();
        mapFragment.addMarkerAt(pointFrom, R.drawable.location_pin,50);
        mapFragment.addMarkerAt(pointTo, R.drawable.location_pin,50);
    }

    private void drawRouteOnMap(RouteResponse route) {
        mapFragment.clearPolylines();
        for (Map.Entry<String, List<RawCoor>> entry : route.coordRoute.entrySet()) {
            List<RawCoor> coords = entry.getValue();
            List<Point> points = new ArrayList<>();

            for (RawCoor rc : coords) {
                points.add(Point.fromLngLat(rc.Longitude, rc.Latitude));
            }

            if (mapFragment != null) {
                mapFragment.drawRouteFromPoints(points);
                mapFragment.zoomToFit(points);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            double lat = data.getDoubleExtra("lat", 0);
            double lon = data.getDoubleExtra("lon", 0);
            String name = data.getStringExtra("name");

            Point selectedPoint = Point.fromLngLat(lon, lat);

            if (requestCode == REQUEST_SEARCH_FROM) {
                tvSearchFrom.setText(name);
                pointFrom = selectedPoint;
                mapFragment.addMarkerAt(pointFrom, R.drawable.location_pin,50);
                mapFragment.zoomToLocation(pointFrom);
            } else if (requestCode == REQUEST_SEARCH_TO) {
                tvSearchTo.setText(name);
                pointTo = selectedPoint;
                mapFragment.addMarkerAt(pointTo, R.drawable.location_pin,50);
                mapFragment.zoomToLocation(pointTo);
            }
            if (pointFrom != null && pointTo != null) {
                BusDataHelper.fetchPathByStop(pointFrom.latitude(), pointFrom.longitude(),
                        pointTo.latitude(), pointTo.longitude(),
                        new BusDataHelper.OnRouteListFetchedListener() {
                            @Override
                            public void onResult(List<RouteResponse> routes) {
                                showRouteDialog(routes);
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(FindPathActivity.this, "Không tìm được đường", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = LocationRequest.create()
                    .setInterval(5000) // 5 giây
                    .setFastestInterval(2000)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }


}
