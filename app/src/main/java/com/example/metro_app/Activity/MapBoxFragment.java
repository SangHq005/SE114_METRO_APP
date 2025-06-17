package com.example.metro_app.Activity;

import static com.mapbox.maps.plugin.animation.CameraAnimationsUtils.getCamera;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import  static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;
import static com.mapbox.navigation.base.extensions.RouteOptionsExtensions.applyDefaultNavigationOptions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.metro_app.Activity.Admin.AdStationBottomSheet;
import com.example.metro_app.Activity.User.StationBottomSheet;
import com.example.metro_app.Model.Station;
import com.example.metro_app.R;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.Bearing;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.bindgen.Expected;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.EdgeInsets;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationPluginImplKt;
import com.mapbox.maps.plugin.annotation.AnnotationType;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotation;
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.generated.LocationComponentSettings;
import com.mapbox.navigation.base.options.NavigationOptions;
import com.mapbox.navigation.base.route.NavigationRoute;
import com.mapbox.navigation.base.route.NavigationRouterCallback;
import com.mapbox.navigation.base.route.RouterFailure;
import com.mapbox.navigation.base.route.RouterOrigin;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.directions.session.RoutesObserver;
import com.mapbox.navigation.core.directions.session.RoutesUpdatedResult;
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp;
import com.mapbox.navigation.core.trip.session.LocationMatcherResult;
import com.mapbox.navigation.core.trip.session.LocationObserver;
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer;
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView;
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineError;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources;
import com.mapbox.navigation.ui.maps.route.line.model.RouteSetValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import kotlin.Suppress;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class MapBoxFragment extends Fragment {
    private String role = "admin"; // default
    private String docId;
    boolean isFirstRoute = true;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private Point lastUserLocation;
    private PointAnnotation currentUserMarker;
    private PointAnnotationManager pointAnnotationManager;
    private PolylineAnnotationManager polylineAnnotationManager;
    private List<PolylineAnnotation> drawnPolylines = new ArrayList<>();
    private OnMapReadyCallback onMapReadyCallback;
    private final NavigationLocationProvider navigationLocationProvider = new NavigationLocationProvider();;
private MapboxRouteLineView routeLineView;
private MapboxRouteLineApi routeLineApi;
private final LocationObserver locationObserver= new LocationObserver() {
    @Override
    public void onNewRawLocation(@NonNull Location location) {

    }

    @Override
    public void onNewLocationMatcherResult(@NonNull LocationMatcherResult locationMatcherResult) {
        Location location = locationMatcherResult.getEnhancedLocation();
        navigationLocationProvider.changePosition(location,locationMatcherResult.getKeyPoints(),null,null);
        if(focusLocation){
            Log.d("LOCATION", "Got location: " + location.getLatitude() + "," + location.getLongitude());
            updateCamera(Point.fromLngLat(location.getLongitude(),location.getLatitude()), (double) location.getBearing());
        }
    }
};
    private final RoutesObserver routesObserver = new RoutesObserver() {
        @Override
        public void onRoutesChanged(@NonNull RoutesUpdatedResult routesUpdatedResult) {
            routeLineApi.setNavigationRoutes(routesUpdatedResult.getNavigationRoutes(), new MapboxNavigationConsumer<Expected<RouteLineError, RouteSetValue>>() {
                @Override
                public void accept(Expected<RouteLineError, RouteSetValue> routeLineErrorRouteSetValueExpected) {
                    Style style = mapView.getMapboxMap().getStyle();
                    if (style != null) {
                        routeLineView.renderRouteDrawData(style,routeLineErrorRouteSetValueExpected);
                    }
                }
            });
        }
    };
    boolean focusLocation = true;
    private MapboxNavigation mapboxNavigation;
    //move base on device movement
    public void updateCamera(Point point, double bearing){
        MapAnimationOptions animationOptions = new MapAnimationOptions.Builder().duration(1500L).build();
        CameraOptions cameraOptions = new CameraOptions.Builder().center(point).zoom(12.0).bearing(bearing)
                .pitch(45.0).padding(new EdgeInsets(1000.0, 0.0, 0.0,0.0)).build();
        getCamera(mapView).easeTo(cameraOptions,animationOptions);
    }
    private final OnMoveListener onMoveListener = new OnMoveListener() {
        @Override
        public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {
            focusLocation = false;
            getGestures(mapView).removeOnMoveListener(this);
        }

        @Override
        public boolean onMove(@NonNull MoveGestureDetector moveGestureDetector) {
            return false;
        }

        @Override
        public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {

        }
    };
    public interface OnMapReadyCallback {
        void onMapReady(MapboxMap mapboxMap);
    }

    public void setOnMapReadyCallback(OnMapReadyCallback callback) {
        this.onMapReadyCallback = callback;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_box, container, false);
        mapView = view.findViewById(R.id.mapView);
        mapboxMap = mapView.getMapboxMap();
        if (getArguments() != null) {
            role = getArguments().getString("ROLE", "user");
            docId = getArguments().getString("DOC_ID", "LuotDi");
        }
        MapboxRouteLineOptions options = new MapboxRouteLineOptions.Builder(requireContext())
                .withRouteLineResources(new RouteLineResources.Builder().build()).withRouteLineBelowLayerId("road-label-navigation").build();
        routeLineView = new MapboxRouteLineView(options);
        routeLineApi = new MapboxRouteLineApi(options);
        NavigationOptions navigationOptions = new NavigationOptions.Builder(requireContext()).accessToken(getString(R.string.mapbox_access_token)).build();
        if (!MapboxNavigationApp.isSetup()) {
            MapboxNavigationApp.setup(navigationOptions);
            mapboxNavigation = new MapboxNavigation(navigationOptions);
            mapboxNavigation.registerRoutesObserver(routesObserver);
            mapboxNavigation.registerLocationObserver(locationObserver);
        }
        LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
        getGestures(mapView).addOnMoveListener(onMoveListener);
        mapboxMap.loadStyleUri(
                Style.MAPBOX_STREETS
        );
        AnnotationPlugin annotationPlugin = mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);
        if (annotationPlugin != null) {
            AnnotationConfig config = new AnnotationConfig(); // có thể truyền context hoặc cấu hình nâng cao
            polylineAnnotationManager = (PolylineAnnotationManager)annotationPlugin.createAnnotationManager(mapView,AnnotationType.PolylineAnnotation,config);
            pointAnnotationManager = (PointAnnotationManager)
                    annotationPlugin.createAnnotationManager(mapView, AnnotationType.PointAnnotation, config);
            pointAnnotationManager.addClickListener(annotation -> {
                JsonElement data = annotation.getData();
                if (data != null && data.isJsonObject()) {
                    if("user".equals(role)) {
                        Station station = new Gson().fromJson(data, Station.class);
                        if (station != null) {
                            StationBottomSheet sheet = new StationBottomSheet(station);
                            sheet.show(getParentFragmentManager(), "station_sheet");
                        }
                    }
                    if("admin".equals(role)) {
                        Station station = new Gson().fromJson(data, Station.class);
                        if (station != null) {
                            AdStationBottomSheet sheet = AdStationBottomSheet.newInstance(station, docId);
                            sheet.show(getParentFragmentManager(), "station_sheet");
                        }
                    }
                }
                return true;
            });


        }
        if (onMapReadyCallback != null) {
            onMapReadyCallback.onMapReady(mapboxMap);
        }

        return view;
    }

    // Hàm zoom đến vị trí
    public void zoomToLocation(Point point) {
        if (mapboxMap != null && point != null) {
            CameraOptions cameraOptions = new CameraOptions.Builder()
                    .center(point)
                    .zoom(16.0) // hoặc tuỳ chỉnh độ zoom
                    .build();
            mapboxMap.setCamera(cameraOptions);
        }
    }
    public void zoomToFit(List<Point> points) {
        if (points == null || points.isEmpty() || mapboxMap == null) return;

        // Padding (pixels): top, left, bottom, right
        EdgeInsets padding = new EdgeInsets(100.0, 100.0, 100.0, 100.0); // có thể điều chỉnh

        CameraOptions cameraOptions = mapboxMap.cameraForCoordinates(points, padding, null, null);

        if (cameraOptions != null) {
            mapboxMap.setCamera(cameraOptions);
        }
    }


    public void addMarkerAt(Point point, int drawableResId,int size) {
        if (pointAnnotationManager == null) return;
        Bitmap rawBitmap = BitmapFactory.decodeResource(getResources(), drawableResId);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(rawBitmap, size, size, false);

        PointAnnotationOptions options = new PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(resizedBitmap)
                .withIconSize(1.0f);

        pointAnnotationManager.create(options);
    }
    public void createStationMarker(PointAnnotationOptions options) {
        if (pointAnnotationManager != null) {
            pointAnnotationManager.create(options);
        }
    }

    // Hàm vẽ đường từ danh sách các Point
    public void drawRouteFromPoints(List<Point> pointList) {
        if (getArguments() != null) {
            docId = getArguments().getString("DOC_ID", "LuotDi");
        }
        String color1 = "#FF0000"; // Tuyến 1 - đỏ
        String color2 = "#0000FF"; // Tuyến 2 - xanh dương
        if (mapView == null || pointList == null || pointList.size() < 2) return;
        String color = isFirstRoute ? color1 : color2;
        isFirstRoute = !isFirstRoute; // Sau khi vẽ tuyến đầu, chuyển sang tuyến 2
        PolylineAnnotationOptions polylineOptions = new PolylineAnnotationOptions()
                .withPoints(pointList)
                .withLineColor(color)
                .withLineWidth(5.0);
        PolylineAnnotation polyline = polylineAnnotationManager.create(polylineOptions);
        drawnPolylines.add(polyline);
    }
    public MapboxMap getMapboxMap() {
        return mapView.getMapboxMap();
    }
    public void clearAllMarkers() {
        if (pointAnnotationManager != null) {
            pointAnnotationManager.deleteAll();
        }
    }
    public void clearPolylines() {
        if (polylineAnnotationManager != null) {
            polylineAnnotationManager.deleteAll();
            drawnPolylines.clear();
        }
    }

    public void updateCurrentLocationMarker(Point point) {
        if (point == null || mapView == null || point.equals(lastUserLocation)) return;

        lastUserLocation = point;

        if (currentUserMarker != null) {
            pointAnnotationManager.delete(currentUserMarker);
        }

        SharedPreferences prefs = requireActivity().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        String avatarUrl = prefs.getString("photo", "");

        if (!avatarUrl.isEmpty()) {
            Glide.with(this)
                    .asBitmap()
                    .load(avatarUrl)
                    .circleCrop()
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            PointAnnotationOptions options = new PointAnnotationOptions()
                                    .withPoint(point)
                                    .withIconImage(resource)
                                    .withIconSize(0.75f);

                            currentUserMarker = pointAnnotationManager.create(options);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        } else {
            Bitmap rawBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.user);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(rawBitmap, 50, 50, false);

            PointAnnotationOptions options = new PointAnnotationOptions()
                    .withPoint(point)
                    .withIconImage(resizedBitmap)
                    .withIconSize(1.0f);
            currentUserMarker = pointAnnotationManager.create(options);
        }
    }
    @Nullable
    public Point getCenterPoint() {
        if (mapboxMap == null) return null;
        return mapboxMap.getCameraState().getCenter();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) {
            mapView.onDestroy();
        }
        if (mapboxNavigation != null) {
            mapboxNavigation.onDestroy();
            mapboxNavigation.unregisterRoutesObserver(routesObserver);
            mapboxNavigation.unregisterLocationObserver(locationObserver);
            mapboxNavigation = null; // tránh giữ reference cũ
        }
    }

}
