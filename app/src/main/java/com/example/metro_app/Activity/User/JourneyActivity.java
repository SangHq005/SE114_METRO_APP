package com.example.metro_app.Activity.User;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.metro_app.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.Arrays;
import java.util.List;

public class JourneyActivity extends AppCompatActivity{
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // OSMDroid Configuration
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_journey);
        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // Center map
        IMapController mapController = mapView.getController();
        mapController.setZoom(14.5);
        GeoPoint startPoint = new GeoPoint(10.7769, 106.7009); // Vị trí trung tâm Sài Gòn
        mapController.setCenter(startPoint);

        showMetroRoute();
    }

    private void showMetroRoute() {
        List<GeoPoint> metroPoints = Arrays.asList(
                new GeoPoint(10.773224, 106.698461), // Bến Thành
                new GeoPoint(10.776957, 106.703053), // Nhà hát TP
                new GeoPoint(10.782016, 106.705992), // Ba Son
                new GeoPoint(10.794029, 106.718309), // Văn Thánh
                new GeoPoint(10.802974, 106.730279), // Thảo Điền
                new GeoPoint(10.803607, 106.737500), // An Phú
                new GeoPoint(10.804055, 106.745833), // Rạch Chiếc
                new GeoPoint(10.804335, 106.752222), // Phước Long
                new GeoPoint(10.803959, 106.761111), // Bình Thái
                new GeoPoint(10.852763, 106.766279), // Thủ Đức
                new GeoPoint(10.860142, 106.775071), // Khu CN Cao (ĐH Quốc Gia)
                new GeoPoint(10.870026, 106.787864), // Suối Tiên
                new GeoPoint(10.878842, 106.800229)  // Bến xe Miền Đông mới
        );

        List<String> stationNames = Arrays.asList(
                "Bến Thành", "Nhà hát TP", "Ba Son", "Văn Thánh",
                "Thảo Điền", "An Phú", "Rạch Chiếc", "Phước Long",
                "Bình Thái", "Thủ Đức", "Khu CNC", "Suối Tiên", "BX Miền Đông"
        );

        // Vẽ tuyến metro
        Polyline line = new Polyline();
        line.setPoints(metroPoints);
        line.setColor(0xFF0066CC); // Màu xanh dương
        line.setWidth(8f);
        mapView.getOverlayManager().add(line);

        // Thêm các marker
        for (int i = 0; i < metroPoints.size(); i++) {
            Marker marker = new Marker(mapView);
            marker.setPosition(metroPoints.get(i));
            marker.setTitle(stationNames.get(i));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(marker);
        }

        mapView.invalidate();
    }



    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
}