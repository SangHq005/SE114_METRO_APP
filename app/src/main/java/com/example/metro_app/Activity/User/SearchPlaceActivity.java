package com.example.metro_app.Activity.User;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.metro_app.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion;
import com.mapbox.search.ResponseInfo;
import com.mapbox.search.SearchEngine;
import com.mapbox.search.SearchEngineSettings;
import com.mapbox.search.autocomplete.PlaceAutocomplete;
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion;
import com.mapbox.search.result.SearchResult;
import com.mapbox.search.ui.adapter.autocomplete.PlaceAutocompleteUiAdapter;
import com.mapbox.search.ui.view.CommonSearchViewConfiguration;
import com.mapbox.search.ui.view.DistanceUnitType;
import com.mapbox.search.ui.view.SearchResultsView;
import com.mapbox.search.ui.view.place.SearchPlace;
import java.util.List;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

public class SearchPlaceActivity extends AppCompatActivity {

    private TextInputEditText editTextSearch;
    private SearchResultsView searchResultsView;
    private SearchEngine searchEngine;
    private PlaceAutocomplete placeAutocomplete;
    private PlaceAutocompleteUiAdapter placeAutocompleteUiAdapter;
    private LinearLayout llMyLocation;

    private boolean ignoreNextQueryUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_place);

        editTextSearch = findViewById(R.id.editTextSearch);
        searchResultsView = findViewById(R.id.searchResultsView);
        llMyLocation = findViewById(R.id.vmylocation);

        placeAutocomplete = PlaceAutocomplete.create(getString(R.string.mapbox_access_token));
        searchResultsView.initialize(new SearchResultsView.Configuration(new CommonSearchViewConfiguration()));
        placeAutocompleteUiAdapter = new PlaceAutocompleteUiAdapter(searchResultsView,placeAutocomplete, LocationEngineProvider.getBestLocationEngine(SearchPlaceActivity.this));


        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (ignoreNextQueryUpdate){
                    ignoreNextQueryUpdate = false;
                } else
                {
                    placeAutocompleteUiAdapter.search(s.toString(), new Continuation<Unit>() {
                        @NonNull
                        @Override
                        public CoroutineContext getContext() {
                            return EmptyCoroutineContext.INSTANCE;
                        }

                        @Override
                        public void resumeWith(@NonNull Object o) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    searchResultsView.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        placeAutocompleteUiAdapter.addSearchListener(new PlaceAutocompleteUiAdapter.SearchListener() {
            @Override
            public void onSuggestionsShown(@NonNull List<PlaceAutocompleteSuggestion> list) {

            }

            @Override
            public void onSuggestionSelected(@NonNull PlaceAutocompleteSuggestion placeAutocompleteSuggestion) {
                ignoreNextQueryUpdate = true;
                editTextSearch.setText(placeAutocompleteSuggestion.getName());
                searchResultsView.setVisibility(View.GONE);
                if (placeAutocompleteSuggestion.getCoordinate() != null) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("name", placeAutocompleteSuggestion.getName());
                    resultIntent.putExtra("lat", placeAutocompleteSuggestion.getCoordinate().latitude());
                    resultIntent.putExtra("lon", placeAutocompleteSuggestion.getCoordinate().longitude());
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }

            @Override
            public void onPopulateQueryClick(@NonNull PlaceAutocompleteSuggestion placeAutocompleteSuggestion) {

            }

            @Override
            public void onError(@NonNull Exception e) {

            }
        });
        llMyLocation.setOnClickListener(v -> {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(SearchPlaceActivity.this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            double lat = location.getLatitude();
                            double lon = location.getLongitude();

                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("name", "Vị trí của tôi");
                            resultIntent.putExtra("lat", lat);
                            resultIntent.putExtra("lon", lon);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {

                        }
                    })
                    .addOnFailureListener(e -> {
                        // Log lỗi nếu cần
                    });
        });

    }
}
