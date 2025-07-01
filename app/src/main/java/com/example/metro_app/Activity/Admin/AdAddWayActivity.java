package com.example.metro_app.Activity.Admin;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
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
    private Bitmap resizedRouteIcon;

    private Button finishButton;
    private FloatingActionButton addLastRoute, addFirstRoute;
    private ImageView btnSwap, btnBack;
    private TextView tvStart, tvEnd;
    private Boolean isLuotDi = true;
    private String docId = "LuotDi";
    private final List<Point> tempRoutePoints = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ad_add_way);

        addStation = findViewById(R.id.fabAddStation);
        addLastRoute = findViewById(R.id.fabAddLastRoute);
        addFirstRoute = findViewById(R.id.fabAddFirstRoute);
        finishButton = findViewById(R.id.btnFinish);
        tvStart = findViewById(R.id.tvStartStation);
        tvEnd = findViewById(R.id.tvEndStation);
        btnSwap = findViewById(R.id.btnSwap);
        btnBack = findViewById(R.id.btnBack);
        Bitmap routeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.deletebtn);
        resizedRouteIcon = Bitmap.createScaledBitmap(routeIcon, 40, 40, false);


        FragmentManager fragmentManager = getSupportFragmentManager();
        mapFragment = new MapBoxFragment();
        Bundle args = new Bundle();
        args.putString("ROLE", "admin");
        args.putString("DOC_ID", docId);
        mapFragment.setArguments(args);
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, mapFragment)
                .commit();

        mapFragment.setOnMapReadyCallback(mapboxMap -> {
            fetchAndDrawRoute(docId);
            mapFragment.zoomToLocation(Point.fromLngLat(106.81406, 10.879499));
        });

        btnSwap.setOnClickListener(v -> {
            isLuotDi = !isLuotDi;
            docId = isLuotDi ? "LuotDi" : "LuotVe";
            fetchAndDrawRoute(docId);
        });

        addFirstRoute.setOnClickListener(v -> {
            if (mapFragment != null) {
                Point center = mapFragment.getCenterPoint();
                tempRoutePoints.add(0, center);
                drawTempRoute(true); // Thêm đầu
                Toast.makeText(this, "Đã thêm điểm đầu tạm thời", Toast.LENGTH_SHORT).show();
            }
        });

        addLastRoute.setOnClickListener(v -> {
            if (mapFragment != null) {
                Point center = mapFragment.getCenterPoint();
                tempRoutePoints.add(center);
                drawTempRoute(false); // Thêm cuối
                Toast.makeText(this, "Đã thêm điểm cuối tạm thời", Toast.LENGTH_SHORT).show();
            }
        });


        addStation.setOnClickListener(v -> {
            if (mapFragment != null) {
                Point center = mapFragment.getCenterPoint();
                Station newStation = new Station();
                newStation.StopId = 0;
                newStation.Lat = center.latitude();
                newStation.Lng = center.longitude();
                newStation.Name = "Trạm mới";
                newStation.Ward = "Phường";
                newStation.Zone = "Quận";

                AdStationBottomSheet sheet = new AdStationBottomSheet(newStation);
                sheet.show(getSupportFragmentManager(), "AdStationBottomSheet");
            }
        });

        finishButton.setOnClickListener(v -> {
            FireStoreHelper.overwriteMetroWay(docId, tempRoutePoints, new FireStoreHelper.FirestoreCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(AdAddWayActivity.this, "Đã lưu tuyến thành công!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(AdAddWayActivity.this, "Lỗi lưu tuyến: " + message, Toast.LENGTH_SHORT).show();
                }
            });

        });

        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void drawTempRoute(boolean isAddToStart) {
        int size = tempRoutePoints.size();
        if (size >= 2) {
            List<Point> segment = new ArrayList<>();
            if (isAddToStart) {
                segment.add(tempRoutePoints.get(0));
                segment.add(tempRoutePoints.get(1));
            } else {
                segment.add(tempRoutePoints.get(size - 2));
                segment.add(tempRoutePoints.get(size - 1));
            }
            mapFragment.drawPartialRouteFromLastPoints(segment);
        }

        // Vẽ marker cho điểm mới
        Point point = isAddToStart ? tempRoutePoints.get(0) : tempRoutePoints.get(tempRoutePoints.size() - 1);

        Station routePoint = new Station();
        routePoint.Lat = point.latitude();
        routePoint.Lng = point.longitude();

        Gson gson = new Gson();
        PointAnnotationOptions options = new PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(resizedRouteIcon)
                .withIconSize(1.0f)
                .withData(JsonParser.parseString(gson.toJson(routePoint)));

        mapFragment.createStationMarker(options);
    }

    public void fetchAndDrawRoute(String documentId) {
        tempRoutePoints.clear();
        mapFragment.clearAllMarkers();

        FireStoreHelper.getMetroWay(documentId, new FireStoreHelper.MetroWayCallback() {
            @Override
            public void onSuccess(List<GeoPoint> geoPoints) {
                tempRoutePoints.clear();
                for (GeoPoint gp : geoPoints) {
                    tempRoutePoints.add(Point.fromLngLat(gp.getLongitude(), gp.getLatitude()));
                }
                mapFragment.clearPolylines();
                // 🔴 Vẽ lại toàn bộ tuyến ban đầu
                mapFragment.drawRouteFromPoints(tempRoutePoints);

                // 🔴 Vẽ lại marker tạm thời nếu có
                for (Point point : tempRoutePoints) {
                    Station routePoint = new Station();
                    routePoint.Lat = point.latitude();
                    routePoint.Lng = point.longitude();

                    Gson gson = new Gson();
                    PointAnnotationOptions options = new PointAnnotationOptions()
                            .withPoint(point)
                            .withIconImage(resizedRouteIcon)
                            .withIconSize(1.0f)
                            .withData(JsonParser.parseString(gson.toJson(routePoint)));

                    mapFragment.createStationMarker(options);
                }
            }

            @Override
            public void onFailure(String message) {
                Log.e("FIREBASE_ROUTE", message);
            }
        });

        FireStoreHelper.getAllStations(new FireStoreHelper.StationListCallback() {
            @Override
            public void onSuccess(List<Station> stationList) {
                Bitmap stationIcon = BitmapFactory.decodeResource(getResources(), R.drawable.metro_station);
                Bitmap resizedStationIcon = Bitmap.createScaledBitmap(stationIcon, 80, 80, false);
                Gson gson = new Gson();

                for (Station station : stationList) {
                    Point p = Point.fromLngLat(station.Lng, station.Lat);
                    PointAnnotationOptions options = new PointAnnotationOptions()
                            .withPoint(p)
                            .withIconImage(resizedStationIcon)
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
    public void onStationUpdated() {
        fetchAndDrawRoute(docId);
    }
}
