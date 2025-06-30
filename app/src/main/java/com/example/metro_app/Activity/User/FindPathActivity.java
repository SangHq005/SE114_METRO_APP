package com.example.metro_app.Activity.User;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.example.metro_app.Activity.MapBoxFragment;
import com.example.metro_app.Model.APIcoord;
import com.example.metro_app.utils.BusDataHelper;
import com.example.metro_app.Model.Detail;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FindPathActivity extends AppCompatActivity {

    private static final int REQUEST_SEARCH_FROM = 1;
    private static final int REQUEST_SEARCH_TO = 2;

    private TextView tvSearchFrom, tvSearchTo;
    private LinearLayout lnSearchFrom,lnSearchTo;
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
        lnSearchFrom =findViewById(R.id.LnSearchFrom);
        lnSearchTo = findViewById(R.id.LnSearchTo);

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
        lnSearchFrom.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchPlaceActivity.class);
            startActivityForResult(intent, REQUEST_SEARCH_FROM);
        });

        lnSearchTo.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchPlaceActivity.class);
            startActivityForResult(intent, REQUEST_SEARCH_TO);
        });

        btnMyLocation.setOnClickListener(v -> {

//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                if (currentLocation != null) {
//                    mapFragment.zoomToLocation(currentLocation);
//                }
//            }
            pointFrom = Point.fromLngLat(106.70640854537282,10.776064339338353);
            pointTo =Point.fromLngLat(106.69920319317951,10.775128418094525);
            findAndDisplayRoute();

        });

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
                                showRouteSelectionDialog(routes);
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
        if(route.Title != null){
            tvRouteTitle.setText(route.Title);
        }
        if(route.Desc!=null){
            tvDistanceTime.setText(route.Desc);
        }
    }
    private void showDetailedRouteInfo() {
        if (currentSelectedRoute == null || currentSelectedRoute.detail == null) return;

        StringBuilder builder = new StringBuilder();

        for (Detail d : currentSelectedRoute.detail) {
            boolean isWalking = d.RouteNo == null;

            if (isWalking) {
                builder.append("🚶‍♂️ Đi bộ\n");
            } else {
                builder.append("🚌 Tuyến số ").append(d.RouteNo).append("\n");
                builder.append("Hướng đi: ").append(d.EndStop != null ? d.EndStop : "").append("\n");
            }

            builder.append("Từ: ").append(d.GetIn).append("\n");
            builder.append("Đến: ").append(d.GetOff).append("\n");

            // Tính khoảng cách
            double distanceMeters = 0;
            try {
                distanceMeters = Double.parseDouble(d.Distance);
            } catch (Exception ignored) { }

            String distanceStr = (distanceMeters >= 1000) ?
                    String.format(Locale.US, "%.1f km", distanceMeters / 1000) :
                    String.format(Locale.US, "%.0f m", distanceMeters);

            // Ước lượng thời gian đi bộ / di chuyển
            double minutes = 0;
            try {
                minutes = Double.parseDouble(d.Length) / 60;  // d.Length là giây
            } catch (Exception ignored) { }
            int minutesRounded = (int) Math.ceil(minutes);
            if (minutesRounded < 1) minutesRounded = 1;

            builder.append("➤ ").append(distanceStr).append(" – khoảng ").append(minutesRounded).append(" phút\n");

            if (!isWalking && d.Fare > 0) {
                builder.append("💵 Vé: ").append(String.format("%,d", d.Fare)).append("đ\n");
            }

            builder.append("\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("Chi tiết lộ trình")
                .setMessage(builder.toString())
                .setPositiveButton("Đóng", null)
                .show();
    }


    private void drawRouteOnMap(RouteResponse route) {
        mapFragment.clearPolylines();
        mapFragment.clearAllMarkers();

        // Draw route path
        for (Map.Entry<String, List<APIcoord>> entry : route.coordRoute.entrySet()) {
            List<APIcoord> coords = entry.getValue();
            List<Point> points = new ArrayList<>();

            for (APIcoord rc : coords) {
                Log.d("RouteCoord", "Lat: " + rc.Latitude + ", Lng: " + rc.Longitude);  // <-- log từng tọa độ
                points.add(Point.fromLngLat(rc.Longitude, rc.Latitude));
            }

            if (mapFragment != null) {
                Log.d("RouteCoord", "Tổng số điểm: " + points.size()); // <-- log số lượng điểm của đoạn route
                mapFragment.drawRouteFromPoints(points);
                mapFragment.zoomToFit(points);
            }
        }


        // Add bus stop markers
        List<Stop> stops = route.stops;
        for (int i = 1; i < stops.size() - 1; i++) {
            Stop stop = stops.get(i);
            Point stopPoint = Point.fromLngLat(stop.Lng, stop.Lat);
            mapFragment.addMarkerAt(stopPoint, R.drawable.ic_bus_stop, 70);
        }


        // Add from and to markers
        if (pointFrom != null) {
            mapFragment.addMarkerAt(pointFrom, R.drawable.location_pin, 80);
        }
        if (pointTo != null) {
            mapFragment.addMarkerAt(pointTo, R.drawable.location_pin, 80);
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
                mapFragment.addMarkerAt(pointFrom, R.drawable.location_pin, 80);
                mapFragment.zoomToLocation(pointFrom);
            } else if (requestCode == REQUEST_SEARCH_TO) {
                tvSearchTo.setText(name);
                pointTo = selectedPoint;
                mapFragment.addMarkerAt(pointTo, R.drawable.location_pin, 80);
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