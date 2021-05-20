package edu.eci.ieti.googlemapapi;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;

    private SupportMapFragment mapFragment;

    private FusedLocationProviderClient fusedLocationClient;

    private final int ACCESS_LOCATION_PERMISSION_CODE = 44;

    private Location mCurrentLocation;

    private TextView address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        address = (TextView) findViewById( R.id.address );

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        showMyLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == -1) {
                return;
            }
        }
        switch (requestCode) {
            case ACCESS_LOCATION_PERMISSION_CODE:
                showMyLocation();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void showMyLocation() {
        if (googleMap != null) {
            String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION};
            if (hasPermissions(this, permissions)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                googleMap.setMyLocationEnabled(true);

                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    addMarkerAndZoom(location, "My Location", 15);
                                }
                            }
                        });
            } else {
                ActivityCompat.requestPermissions(this, permissions, ACCESS_LOCATION_PERMISSION_CODE);
            }
        }
    }

    public static boolean hasPermissions(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    public void addMarkerAndZoom(Location location, String title, int zoom) {
        LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.addMarker(new MarkerOptions().position(myLocation).title(title));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, zoom));

        mCurrentLocation = location;
    }

    public void onFindAddressClicked(View view) {
        startFetchAddressIntentService();
    }

    public void startFetchAddressIntentService() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            AddressResultReceiver addressResultReceiver = new AddressResultReceiver(new Handler());
                            addressResultReceiver.setAddressResultListener(new AddressResultListener() {
                                @Override
                                public void onAddressFound(final String address) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            MapsActivity.this.address.setText(address);
                                            MapsActivity.this.address.setVisibility(View.VISIBLE);
                                        }
                                    });


                                }
                            });
                            Intent intent = new Intent(MapsActivity.this, FetchAddressIntentService.class);
                            intent.putExtra(FetchAddressIntentService.RECEIVER, addressResultReceiver);
                            intent.putExtra(FetchAddressIntentService.LOCATION_DATA_EXTRA, location);
                            startService(intent);
                        }
                    }
                });
    }

    public void openNewLocationActivity(View view) {
        Intent intent = new Intent(this, inputNewLocation.class);
        startActivity(intent);
    }
}