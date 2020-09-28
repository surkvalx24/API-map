package com.example.mapsapppp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Button btnMyLocation;
    private TextView geoInfo;
    private FusedLocationProviderClient fusedLocationProviderClient;
    // для поиска
    private SearchView searchView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Context context = getApplicationContext() ;

        final TextView geoInfo = findViewById(R.id. geo_info);
        Button btnMyLocation = findViewById(R.id. btnShowgeo);

        // иницилизируем карты
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // проверка включена ли геолокация на устройстве
        if(isLocationEnabled(context) == false) {
            geoInfo.setText("Гелокация не включена! Не могу определить ваше местоположение");
            geoInfo.setTextColor(getResources().getColor(R.color.colorError));
        } else {
            geoInfo.setText("Всё ок");
            geoInfo.setTextColor(getResources().getColor(R.color.colorOk));
        }
        // определение местоположения
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        MarkerOptions a = new MarkerOptions().position(new LatLng(50,6));
 //       Marker m = mMap.addMarker(a);
 //       mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(50,6)));
        // иницилизируем поиск
        final SearchView searchView = findViewById(R.id. search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();
                List<Address> addresslist = null;
                if(location != null || !location.equals("")) {
                    Geocoder geocoder = new Geocoder(MainActivity.this);
                    try {
                        addresslist = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address = addresslist.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                }
                return false;
            }
            // ----------------------

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        // действия по кнопкам
        btnMyLocation.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                if(isLocationEnabled(MainActivity.this) == false) {
                    geoInfo.setText("Гелокация не включена! Не могу определить ваше местоположение");
                    geoInfo.setTextColor(getResources().getColor(R.color.colorError));
                    Toast.makeText(getApplicationContext(), "Включите геолокацию!", Toast.LENGTH_LONG).show();
                } else {
                    geoInfo.setText("Всё ок");
                    geoInfo.setTextColor(getResources().getColor(R.color.colorOk));
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if(getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                            // location
                            fusedLocationProviderClient.getLastLocation()
                                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                                        @Override
                                        public void onSuccess(Location location) {
                                            if(location!=null) {
                                                Double lat = location.getLatitude();
                                                Double longt = location.getLatitude();
                                                Toast.makeText(getApplicationContext(), lat + " - " + longt, Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(getApplicationContext(), " - ", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        } else {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
                        }
                    }
                }
            }
        }) ;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // добавляем изначальную позицию карт

//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        MarkerOptions a = new MarkerOptions().position(sydney);
//        Marker m = mMap.addMarker(a);
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(this,
//                    Manifest.permission.ACCESS_FINE_LOCATION)
//                    == PackageManager.PERMISSION_GRANTED) {
//                mMap.setMyLocationEnabled(true);
//            }
//        }
//        else {
//            mMap.setMyLocationEnabled(true);
//        }
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }


}
