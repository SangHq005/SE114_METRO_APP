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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.metro_app.Activity.MapBoxFragment;
import com.example.metro_app.Adapter.RawRouteAdapter;
import com.example.metro_app.utils.BusDataHelper;
import com.example.metro_app.Model.BusStop;
import com.example.metro_app.Model.RawRoute;
import com.example.metro_app.Model.RouteVariant;
import com.example.metro_app.Model.Station;
import com.example.metro_app.R;
import com.example.metro_app.utils.FireStoreHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.mapbox.geojson.Point;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;

import java.util.ArrayList;
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
    private ImageView btnSelectRoute;
    private BottomSheetDialog routeDialog;
    private RecyclerView recyclerViewRoutes;
   private RawRouteAdapter routeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_journey);
        btnSwap = findViewById(R.id.btnSwap);
        myLocation = findViewById(R.id.fabMyLocation);
        tvStartStation = findViewById(R.id.tvStartStation);
        tvEndStation = findViewById(R.id.tvEndStation);
        btnSelectRoute = findViewById(R.id.btnSelectRoute);
        btnSelectRoute.setOnClickListener(v -> showRouteSelectionDialog());

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
//            List<RawRoute> routes = FireStoreHelper.parseRoutesFromAsset(this, "routes.json");
//            FireStoreHelper.uploadRawRoutes(routes, new FireStoreHelper.FirestoreCallback() {
//                @Override
//                public void onSuccess() {
//                    Log.d("UPLOAD", "Đã upload thành công!");
//                }
//
//                @Override
//                public void onFailure(String message) {
//                    Log.e("UPLOAD", "Lỗi: " + message);
//                }
//            });
//            FireStoreHelper.uploadAllRouteVariantsSequential(new BusDataHelper(), new FireStoreHelper.UploadCallback() {
//                @Override
//                public void onSuccess() {
//                    Toast.makeText(getApplicationContext(), "Tải lên tất cả RouteVariants thành công!", Toast.LENGTH_SHORT).show();
//                }
//
//                @Override
//                public void onFailure(Exception e) {
//                    Toast.makeText(getApplicationContext(), "Lỗi tải RouteVariants: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                    Log.e("UploadAll", "Lỗi", e);
//                }
//            });
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
        // 1. Lấy tuyến đường
        FireStoreHelper.getMetroWay(documentId, new FireStoreHelper.MetroWayCallback() {
            @Override
            public void onSuccess(List<GeoPoint> geoPoints) {
                List<Point> pointList = new ArrayList<>();
                for (GeoPoint gp : geoPoints) {
                    pointList.add(Point.fromLngLat(gp.getLongitude(), gp.getLatitude()));
                }

                mapFragment.clearPolylines();
                mapFragment.drawRouteFromPoints(pointList);
                mapFragment.zoomToFit(pointList);
            }

            @Override
            public void onFailure(String message) {
                Log.e("FIREBASE_ROUTE", message);
            }
        });

        // 2. Lấy danh sách trạm
        FireStoreHelper.getAllStations(new FireStoreHelper.StationListCallback() {
            @Override
            public void onSuccess(List<Station> stationList) {
                mapFragment.clearAllMarkers();

                Bitmap rawBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.metro_station);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(rawBitmap, 80, 80, false);
                Gson gson = new Gson();

                for (Station station : stationList) {
                    Point p = Point.fromLngLat(station.Lng, station.Lat);
                    PointAnnotationOptions options = new PointAnnotationOptions()
                            .withPoint(p)
                            .withIconImage(resizedBitmap)
                            .withIconSize(1.0f)
                            .withData(JsonParser.parseString(gson.toJson(station)));
                    mapFragment.createStationMarker(options);
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(JourneyActivity.this, "Lỗi khi tải trạm dừng: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showRouteSelectionDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_route_selection, null);
        recyclerViewRoutes = view.findViewById(R.id.recyclerViewRoutes);
        recyclerViewRoutes.setLayoutManager(new LinearLayoutManager(this));
        routeAdapter = new RawRouteAdapter(new ArrayList<>(), selectedRoute -> {
            routeDialog.dismiss();
            showDirectionSelection(selectedRoute);
        });
        recyclerViewRoutes.setAdapter(routeAdapter);

        routeDialog = new BottomSheetDialog(this);
        routeDialog.setContentView(view);
        routeDialog.show();
        FireStoreHelper.loadRawRoutes(new FireStoreHelper.FirestoreRoutesCallback() {
            @Override
            public void onSuccess(List<RawRoute> routeList) {
                routeAdapter = new RawRouteAdapter(routeList, selectedRoute -> {
                    routeDialog.dismiss();
                    showDirectionSelection(selectedRoute);
                });
                recyclerViewRoutes.setAdapter(routeAdapter);
            }
            @Override
            public void onFailure(String message) {
                Toast.makeText(JourneyActivity.this, "Lỗi khi tải tuyến", Toast.LENGTH_SHORT).show();
            }
        });
//        BusDataHelper.fetchAllRoutes(new BusDataHelper.OnAllRoutesFetchedListener() {
//            @Override
//            public void onResult(List<RawRoute> routes) {
//                routeAdapter = new RawRouteAdapter(routes, selectedRoute -> {
//                    routeDialog.dismiss();
//                    showDirectionSelection(selectedRoute);
//                });
//                recyclerViewRoutes.setAdapter(routeAdapter);
//            }
//
//            @Override
//            public void onError(Exception e) {
//                Toast.makeText(JourneyActivity.this, "Lỗi khi tải tuyến", Toast.LENGTH_SHORT).show();
//            }
//        });
    }
    private void showDirectionSelection(RawRoute selectedRoute) {
        BottomSheetDialog directionDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_direction_choice, null);

        view.findViewById(R.id.btnBusLuotDi).setOnClickListener(v -> {
            directionDialog.dismiss();
            fetchStopsByRouteAndDraw(selectedRoute.getRouteId(), 1);
        });

        view.findViewById(R.id.btnBusLuotVe).setOnClickListener(v -> {
            directionDialog.dismiss();
            fetchStopsByRouteAndDraw(selectedRoute.getRouteId(), 2);
        });

        directionDialog.setContentView(view);
        directionDialog.show();
    }
    private void fetchStopsByRouteAndDraw(int routeId, int direction) {
        FireStoreHelper.fetchRouteVars(routeId, new FireStoreHelper.OnRouteVarsFetchedListener() {
            @Override
            public void onResult(List<RouteVariant> routeVars) {
                if (routeVars == null || routeVars.isEmpty()) {
                    Toast.makeText(JourneyActivity.this, "Không có chiều tuyến!", Toast.LENGTH_SHORT).show();
                    return;
                }

                int varId = 0;
                if (direction == 1 && routeVars.size() >= 1) {
                    varId = routeVars.get(0).getRouteVarId();
                } else if (direction == 2 && routeVars.size() >= 2) {
                    varId = routeVars.get(1).getRouteVarId();
                } else {
                    Toast.makeText(JourneyActivity.this, "Không có dữ liệu chiều tuyến phù hợp!", Toast.LENGTH_SHORT).show();
                    return;
                }

                RouteVariant selectedVariant = null;
                for (RouteVariant v : routeVars) {
                    if (v.getRouteVarId() == varId) {
                        selectedVariant = v;
                        break;
                    }
                }

                if (selectedVariant == null) {
                    Toast.makeText(JourneyActivity.this, "Không tìm thấy variant phù hợp!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Vẽ các trạm
                mapFragment.clearAllMarkers();
                Gson gson = new Gson();
                Bitmap rawBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_bus_stop);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(rawBitmap, 80, 80, false);

                List<BusStop> stops = selectedVariant.getStops();
                if (stops != null) {
                    for (BusStop stop : stops) {
                        if (stop != null) {
                            Point p = Point.fromLngLat(stop.Lng, stop.Lat);
                            PointAnnotationOptions options = new PointAnnotationOptions()
                                    .withPoint(p)
                                    .withIconImage(resizedBitmap)
                                    .withIconSize(1.0f)
                                    .withData(JsonParser.parseString(gson.toJson(stop))); // Gắn dữ liệu
                            mapFragment.createStationMarker(options);
                        }
                    }
                }

                // Vẽ tuyến đường
                List<Point> path = selectedVariant.getPointPath();
                if (path != null && !path.isEmpty()) {
                    mapFragment.clearPolylines();
                    mapFragment.drawRouteFromPoints(path);
                    mapFragment.zoomToFit(path);
                } else {
                    Toast.makeText(JourneyActivity.this, "Không có dữ liệu đường đi!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(JourneyActivity.this, "Lỗi khi tải dữ liệu từ Firestore", Toast.LENGTH_SHORT).show();
                Log.e("FirestoreFetch", "Lỗi khi fetch RouteVariant", e);
            }
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
