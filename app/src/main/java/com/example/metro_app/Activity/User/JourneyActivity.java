package com.example.metro_app.Activity.User;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.example.metro_app.Activity.MapBoxFragment;
import com.example.metro_app.Model.BusDataHelper;
import com.example.metro_app.Model.Station;
import com.example.metro_app.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.mapbox.geojson.Point;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JourneyActivity extends AppCompatActivity{
    private Point currentLocation;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationClient;
    private MapBoxFragment mapFragment;
    private ImageView btnSwap,btnBack;
    private TextView tvStartStation,tvEndStation;
    private Boolean Luotdi = true;
    private final FirebaseFirestore database = FirebaseFirestore.getInstance();
    private FloatingActionButton myLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_journey);
        btnSwap = findViewById(R.id.btnSwap);
        myLocation = findViewById(R.id.fabMyLocation);
        tvStartStation = findViewById(R.id.tvStartStation);
        tvEndStation = findViewById(R.id.tvEndStation);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        mapFragment = new MapBoxFragment();
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(JourneyActivity.this, HomeActivity.class));
        });
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, mapFragment)
                .commit();

        Point startPoint =  Point.fromLngLat(106.7009,10.7769 ); // Vị trí trung tâm Sài Gòn
        mapFragment.setOnMapReadyCallback(mapboxMap -> {
            fetchAndDrawRoute("MetroWay","LuotDi");
//            BusDataHelper busDataHelper = new BusDataHelper();
//            busDataHelper.uploadStationsToFirestore(this);
//            busDataHelper.uploadGeoPointListToFirestore(this);
        });
        btnSwap.setOnClickListener(v -> {
            Luotdi = !Luotdi;
            if(Luotdi){
                fetchAndDrawRoute("MetroWay","LuotDi");
                tvStartStation.setText("Bến Thành");
                tvEndStation.setText("Suối Tiên");

            }
            else{
                fetchAndDrawRoute("MetroWay","LuotVe");
                tvEndStation.setText("Bến Thành");
                tvStartStation.setText("Suối Tiên");
            }
        });
        myLocation.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (currentLocation != null) {
                    mapFragment.zoomToLocation(currentLocation);
                }
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

                    mapFragment.updateCurrentLocationMarker(currentLocation);
                }
            }
        };
    }

    public void fetchAndDrawRoute(String collectionName, String documentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(collectionName)
                .document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<GeoPoint> geoPoints = (List<GeoPoint>) documentSnapshot.get("points");
                        if (geoPoints != null && geoPoints.size() >= 2) {
                            List<Point> pointList = new ArrayList<>();
                            for (GeoPoint gp : geoPoints) {
                                Point point = Point.fromLngLat(gp.getLongitude(), gp.getLatitude());
                                pointList.add(point);
                            }
                            mapFragment.clearPolylines();
                            mapFragment.drawRouteFromPoints(pointList);
                            mapFragment.zoomToFit(pointList);
                        } else {
                            Log.e("FIREBASE_ROUTE", "Không có đủ điểm để vẽ tuyến");
                        }
                    } else {
                        Log.e("FIREBASE_ROUTE", "Document không tồn tại");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE_ROUTE", "Lỗi khi lấy dữ liệu Firestore", e);
                });
        // 2. Lấy các trạm dừng và hiển thị marker
        Gson gson = new Gson();
        Bitmap rawBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.metro_station);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(rawBitmap, 80, 80, false);

        database.collection("stations")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    mapFragment.clearAllMarkers();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Station station = doc.toObject(Station.class);
                        if (station != null) {
                            Point p = Point.fromLngLat(station.Lng, station.Lat);
                            PointAnnotationOptions options = new PointAnnotationOptions()
                                    .withPoint(p)
                                    .withIconImage(resizedBitmap)
                                    .withIconSize(1.0f)
                                    .withData(JsonParser.parseString(gson.toJson(station))); // Gắn dữ liệu
                            mapFragment.createStationMarker(options);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải trạm dừng", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onResume() {
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
    public void onPause() {
        super.onPause();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}