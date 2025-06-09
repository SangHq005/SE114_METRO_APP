package com.example.metro_app.Activity.User;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.metro_app.Activity.MapBoxFragment;
import com.example.metro_app.Model.BusDataHelper;
import com.example.metro_app.Model.Station;
import com.example.metro_app.R;
import com.google.android.gms.maps.MapFragment;
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
    private MapBoxFragment mapFragment;
    private ImageView btnSwap;
    private Boolean Luotdi = true;
    private final FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_journey);
        btnSwap = findViewById(R.id.btnSwap);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mapFragment = new MapBoxFragment();

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
            }
            else{
                fetchAndDrawRoute("MetroWay","LuotVe");
            }
        });


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
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}