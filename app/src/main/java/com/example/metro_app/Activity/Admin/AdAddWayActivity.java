package com.example.metro_app.Activity.Admin;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.metro_app.Activity.MapBoxFragment;
import com.example.metro_app.Model.Station;
import com.example.metro_app.R;
import com.example.metro_app.utils.FireStoreHelper;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton; // Import kiểu mới
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.mapbox.geojson.Point;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;

import java.util.ArrayList;
import java.util.List;

public class AdAddWayActivity extends AppCompatActivity implements AdStationBottomSheet.OnStationUpdatedListener {
    private MapBoxFragment mapFragment;

    private ExtendedFloatingActionButton addStation;
    private FloatingActionButton addLastRoute, addFirstRoute;

    private ImageView btnSwap, btnBack;
    private TextView tvStart, tvEnd;
    private Boolean isLuotDi = true;
    private String docId = "LuotDi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ad_add_way);

        addStation = findViewById(R.id.fabAddStation);
        addLastRoute = findViewById(R.id.fabAddLastRoute);
        addFirstRoute = findViewById(R.id.fabAddFirstRoute);
        tvStart = findViewById(R.id.tvStartStation);
        tvEnd = findViewById(R.id.tvEndStation);
        btnSwap = findViewById(R.id.btnSwap);
        btnBack = findViewById(R.id.btnBack);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mapFragment = new MapBoxFragment();
        // Dùng Bundle
        Bundle args = new Bundle();
        args.putString("ROLE", "admin");
        args.putString("DOC_ID", docId);
        mapFragment.setArguments(args);
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, mapFragment)
                .commit();

        mapFragment.setOnMapReadyCallback(mapboxMap -> {
            fetchAndDrawRoute("LuotDi");
            mapFragment.zoomToLocation(Point.fromLngLat(106.81406, 10.879499));
        });

        btnSwap.setOnClickListener(v -> {
            isLuotDi = !isLuotDi;
            docId = isLuotDi ? "LuotDi" : "LuotVe";
            Bundle args1 = new Bundle();
            args1.putString("ROLE", "admin");
            args1.putString("DOC_ID", docId);
            mapFragment.setArguments(args1);
            fetchAndDrawRoute(docId);
        });

        addLastRoute.setOnClickListener(v -> {
            if (mapFragment != null) {
                Point center = mapFragment.getCenterPoint();
                FireStoreHelper.insertPointAt(docId, center, 99999, new FireStoreHelper.InsertCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(AdAddWayActivity.this, "Đã thêm điểm cuối tại " + center.longitude() + "," + center.latitude(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String message) {
                        Toast.makeText(AdAddWayActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        addFirstRoute.setOnClickListener(v -> {
            if (mapFragment != null) {
                Point center = mapFragment.getCenterPoint();
                FireStoreHelper.insertPointAt(docId, center, -1, new FireStoreHelper.InsertCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(AdAddWayActivity.this, "Đã thêm điểm đầu tại " + center.longitude() + "," + center.latitude(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String message) {
                        Toast.makeText(AdAddWayActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        addStation.setOnClickListener(v -> {
            if (mapFragment != null) {
                Point center = mapFragment.getCenterPoint();

                Station newStation = new Station();
                newStation.StopId = 0;
                newStation.Lat = center.latitude();
                newStation.Lng = center.longitude();
                newStation.Name = "";
                newStation.Ward = "";
                newStation.Zone = "";

                AdStationBottomSheet sheet = new AdStationBottomSheet(newStation);
                sheet.show(getSupportFragmentManager(), "AdStationBottomSheet");
            }
        });

        btnBack.setOnClickListener(v -> onBackPressed());
    }

    public void fetchAndDrawRoute(String documentId) {
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
                Toast.makeText(AdAddWayActivity.this, "Lỗi khi tải trạm dừng: " + message, Toast.LENGTH_SHORT).show();
            }
        });
        // Lấy tuyến đường
        FireStoreHelper.getMetroWay(documentId, new FireStoreHelper.MetroWayCallback() {
            @Override
            public void onSuccess(List<GeoPoint> geoPoints) {
                List<Point> pointList = new ArrayList<>();
                for (GeoPoint gp : geoPoints) {
                    pointList.add(Point.fromLngLat(gp.getLongitude(), gp.getLatitude()));
                }
                Bitmap rawBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.deletebtn);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(rawBitmap, 40, 40, false);
                Gson gson = new Gson();

                for (Point point : pointList) {
                    Station Route = new Station();
                    Route.Lat = point.latitude();
                    Route.Lng = point.longitude();
                    PointAnnotationOptions options = new PointAnnotationOptions()
                            .withPoint(point)
                            .withIconImage(resizedBitmap)
                            .withIconSize(1.0f)
                            .withData(JsonParser.parseString(gson.toJson(Route)));
                    Log.d("veeee", "onSuccess: " + docId);
                    mapFragment.createStationMarker(options);
                }
                mapFragment.clearPolylines();
                mapFragment.drawRouteFromPoints(pointList);

                FireStoreHelper.getStartEndStationNames(new FireStoreHelper.StationNameCallback() {
                    @Override
                    public void onNamesFetched(String startName, String endName) {
                        if ("LuotDi".equals(docId)) {
                            tvStart.setText(startName);
                            tvEnd.setText(endName);
                        } else {
                            tvStart.setText(endName);
                            tvEnd.setText(startName);
                        }
                    }

                    @Override
                    public void onFailure(String message) {
                        tvStart.setText("?");
                        tvEnd.setText("?");
                    }
                });
            }

            @Override
            public void onFailure(String message) {
                Log.e("FIREBASE_ROUTE", message);
            }
        });
    }

    @Override
    public void onStationUpdated() {
        fetchAndDrawRoute(docId);
    }
}