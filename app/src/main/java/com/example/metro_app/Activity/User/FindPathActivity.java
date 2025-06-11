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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
    private FloatingActionButton btnMyLocation;
    private MapBoxFragment mapFragment;
    private Point currentLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private boolean isFirstUpdate = true;

    // Bottom card components
    private CardView routeInfoCard;
    private TextView tvRouteTitle;
    private TextView tvStationName;
    private TextView tvDistanceTime;
    private Button btnDirection;
    private Button btnRoute;

    // Current selected route
    private RouteResponse currentSelectedRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_path);

        initializeViews();
        setupMapFragment();
        setupClickListeners();
        setupLocationServices();
        handleIntentData();
    }

    private void initializeViews() {
        tvSearchFrom = findViewById(R.id.tvSearchFrom);
        tvSearchTo = findViewById(R.id.tvSearchTo);
        btnMyLocation = findViewById(R.id.btnMyLocation);

        // Bottom card views
        routeInfoCard = findViewById(R.id.routeInfoCard);
        tvRouteTitle = findViewById(R.id.tvRouteTitle);
        tvStationName = findViewById(R.id.tvStationName);
        tvDistanceTime = findViewById(R.id.tvDistanceTime);
        btnDirection = findViewById(R.id.btnDirection);
        btnRoute = findViewById(R.id.btnRoute);

        // Initially hide the route info card
        routeInfoCard.setVisibility(View.GONE);
    }

    private void setupMapFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mapFragment = new MapBoxFragment();

        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, mapFragment)
                .commit();

        mapFragment.setOnMapReadyCallback(mapboxMap -> {
            mapFragment.zoomToLocation(Point.fromLngLat(106.7009, 10.7769));
        });
    }

    private void setupClickListeners() {
        tvSearchFrom.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchPlaceActivity.class);
            startActivityForResult(intent, REQUEST_SEARCH_FROM);
        });

        tvSearchTo.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchPlaceActivity.class);
            startActivityForResult(intent, REQUEST_SEARCH_TO);
        });

        btnMyLocation.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (currentLocation != null) {
                    mapFragment.zoomToLocation(currentLocation);
                }
            }
        });

//        btnDirection.setOnClickListener(v -> {
//            if (currentSelectedRoute != null) {
//                // Show directions to the nearest bus stop
//                showDirectionsToNearestStop();
//            }
//        });
        btnDirection.setOnClickListener(v -> {
            if (pointFrom != null && pointTo != null) {
                BusDataHelper.fetchPathByStop(
                        pointFrom.latitude(), pointFrom.longitude(),
                        pointTo.latitude(), pointTo.longitude(),
                        new BusDataHelper.OnRouteListFetchedListener() {
                            @Override
                            public void onResult(List<RouteResponse> routes) {
                                if (routes.size() > 1) {
                                    showRouteSelectionDialog(routes);
                                } else if (!routes.isEmpty()) {
                                    currentSelectedRoute = routes.get(0);
                                    drawRouteOnMap(currentSelectedRoute);
                                    showRouteInfoCard(currentSelectedRoute);
                                } else {
                                    Toast.makeText(FindPathActivity.this, "Không tìm thấy tuyến đường khác", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(FindPathActivity.this, "Lỗi khi tìm tuyến đường", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Vui lòng chọn điểm bắt đầu và kết thúc", Toast.LENGTH_SHORT).show();
            }
        });


        btnRoute.setOnClickListener(v -> {
            if (currentSelectedRoute != null) {
                // Show detailed route information
                showDetailedRouteInfo();
            }
        });
    }

    private void setupLocationServices() {
        // Request permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();
                        currentLocation = Point.fromLngLat(lon, lat);

                        mapFragment.updateCurrentLocationMarker(currentLocation);
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

    private void handleIntentData() {
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

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                        if (location != null) {
                            pointFrom = Point.fromLngLat(location.getLongitude(), location.getLatitude());
                            tvSearchFrom.setText("Vị trí của tôi");
                            findAndDisplayRoute();
                        }
                    });
                }
            });
        }
    }

    private void findAndDisplayRoute() {
        if (pointFrom != null && pointTo != null) {
            BusDataHelper.fetchPathByStop(pointFrom.latitude(), pointFrom.longitude(),
                    pointTo.latitude(), pointTo.longitude(),
                    new BusDataHelper.OnRouteListFetchedListener() {
                        @Override
                        public void onResult(List<RouteResponse> routes) {
                            if (routes.isEmpty()) {
                                Toast.makeText(FindPathActivity.this, "Không tìm thấy tuyến đường phù hợp", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Show the first route by default and display route info card
                            RouteResponse bestRoute = routes.get(0);
                            currentSelectedRoute = bestRoute;
                            drawRouteOnMap(bestRoute);
                            showRouteInfoCard(bestRoute);

                            // If there are multiple routes, allow user to choose
                            if (routes.size() > 1) {
                                showRouteSelectionDialog(routes);
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(FindPathActivity.this, "Không tìm được đường", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void showRouteSelectionDialog(List<RouteResponse> routeResponses) {
        List<String> titles = new ArrayList<>();
        for (RouteResponse r : routeResponses) {
            titles.add(r.Title + " - " + r.Desc);
        }

        new AlertDialog.Builder(this)
                .setTitle("Chọn tuyến đường khác")
                .setItems(titles.toArray(new String[0]), (dialog, which) -> {
                    RouteResponse selectedRoute = routeResponses.get(which);
                    currentSelectedRoute = selectedRoute;
                    drawRouteOnMap(selectedRoute);
                    showRouteInfoCard(selectedRoute);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showRouteInfoCard(RouteResponse route) {
        routeInfoCard.setVisibility(View.VISIBLE);

        // Set route title
        tvRouteTitle.setText("Tuyến xe: " + route.Title);

        // Find nearest stop
        Stop nearestStop = findNearestStop(route.stops);
        if (nearestStop != null) {
            tvStationName.setText("Trạm: " + nearestStop.Name);

            // Calculate distance to nearest stop
            float distanceInMeters = calculateDistance(currentLocation, Point.fromLngLat(nearestStop.Lng, nearestStop.Lat));

            // Format distance display
            String distanceText;
            if (distanceInMeters >= 1000) {
                // Display in kilometers if >= 1km
                double distanceInKm = distanceInMeters / 1000.0;
                distanceText = String.format("%.1fkm", distanceInKm);
            } else {
                // Display in meters if < 1km
                distanceText = String.format("%.0fm", distanceInMeters);
            }

            // Calculate walking time (average walking speed: 5 km/h = 83.33 m/min)
            int walkingTimeMinutes = (int) Math.ceil(distanceInMeters / 83.33);
            // Minimum 1 minute for very short distances
            if (walkingTimeMinutes < 1) walkingTimeMinutes = 1;

            tvDistanceTime.setText(String.format("Cách %s - ~%d phút đi bộ", distanceText, walkingTimeMinutes));
        } else {
            tvStationName.setText("Trạm: " + route.Title);
            tvDistanceTime.setText("Thông tin khoảng cách không có sẵn");
        }
    }

    private Stop findNearestStop(List<Stop> stops) {
        if (currentLocation == null || stops.isEmpty()) {
            return null;
        }

        Stop nearestStop = null;
        float minDistance = Float.MAX_VALUE;

        for (Stop stop : stops) {
            Point stopPoint = Point.fromLngLat(stop.Lng, stop.Lat);
            float distance = calculateDistance(currentLocation, stopPoint);
            if (distance < minDistance) {
                minDistance = distance;
                nearestStop = stop;
            }
        }

        return nearestStop;
    }

    private float calculateDistance(Point point1, Point point2) {
        float[] results = new float[1];
        android.location.Location.distanceBetween(
                point1.latitude(), point1.longitude(),
                point2.latitude(), point2.longitude(),
                results
        );
        return results[0];
    }

    private void showDirectionsToNearestStop() {
        if (currentSelectedRoute == null) return;

        Stop nearestStop = findNearestStop(currentSelectedRoute.stops);
        if (nearestStop != null) {
            Toast.makeText(this, "Chỉ đường đến trạm: " + nearestStop.Name, Toast.LENGTH_SHORT).show();
        }
    }

    private void showDetailedRouteInfo() {
        if (currentSelectedRoute == null) return;

        // Create a detailed route information dialog
        StringBuilder routeInfo = new StringBuilder();
        routeInfo.append("Tuyến: ").append(currentSelectedRoute.Title).append("\n");
        routeInfo.append("Mô tả: ").append(currentSelectedRoute.Desc).append("\n\n");
        routeInfo.append("Các trạm dừng:\n");

        for (int i = 0; i < currentSelectedRoute.stops.size(); i++) {
            Stop stop = currentSelectedRoute.stops.get(i);
            routeInfo.append((i + 1)).append(". ").append(stop.Name).append("\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("Thông tin chi tiết lộ trình")
                .setMessage(routeInfo.toString())
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void drawRouteOnMap(RouteResponse route) {
        mapFragment.clearPolylines();
        mapFragment.clearAllMarkers();

        // Draw route path
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

        // Add bus stop markers
        for (Stop stop : route.stops) {
            Point stopPoint = Point.fromLngLat(stop.Lng, stop.Lat);
            mapFragment.addMarkerAt(stopPoint, R.drawable.ic_bus_stop, 70);
        }

        // Add from and to markers
        if (pointFrom != null) {
            mapFragment.addMarkerAt(pointFrom, R.drawable.location_pin, 50);
        }
        if (pointTo != null) {
            mapFragment.addMarkerAt(pointTo, R.drawable.location_pin, 50);
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
                mapFragment.addMarkerAt(pointFrom, R.drawable.location_pin, 50);
                mapFragment.zoomToLocation(pointFrom);
            } else if (requestCode == REQUEST_SEARCH_TO) {
                tvSearchTo.setText(name);
                pointTo = selectedPoint;
                mapFragment.addMarkerAt(pointTo, R.drawable.location_pin, 50);
                mapFragment.zoomToLocation(pointTo);
            }

            findAndDisplayRoute();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = LocationRequest.create()
                    .setInterval(5000) // 5 seconds
                    .setFastestInterval(2000)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}