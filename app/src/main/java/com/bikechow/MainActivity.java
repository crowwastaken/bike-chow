package com.bikechow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.microsoft.maps.Geopath;
import com.microsoft.maps.Geopoint;
import com.microsoft.maps.Geoposition;
import com.microsoft.maps.MapAnimationKind;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapImage;
import com.microsoft.maps.MapPolyline;
import com.microsoft.maps.MapRenderMode;
import com.microsoft.maps.MapScene;
import com.microsoft.maps.MapView;
import com.microsoft.maps.search.MapLocationAddress;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{

    public MapView mMapView;


    private LocationManager locationManager;
    private MapLocationAddress mapLocationAddress;
    private Geopoint startLocation = Data.RICHMOND;

    public Requests requestCreator;
    private Draw draw;

    private MapIcon user = new MapIcon();
    private IconData userData = new IconData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = new MapView(this, MapRenderMode.VECTOR);  // or use MapRenderMode.RASTER for 2D map
        mMapView.setCredentialsKey(BuildConfig.CREDENTIALS_KEY);  //
        ((FrameLayout) findViewById(R.id.map_view)).addView(mMapView);
        mMapView.onCreate(savedInstanceState);

        // disable the ugly buttons
        mMapView.getUserInterfaceOptions().setZoomButtonsVisible(false);
        mMapView.getUserInterfaceOptions().setCompassButtonVisible(false);
        mMapView.getUserInterfaceOptions().setTiltButtonVisible(false);

        // Create our requests class, which will handle GET and POST requests
        requestCreator = new Requests(this);

        // Create our draw class, which will enable us to draw onto the map
        draw = new Draw(this);

        getLocationPermissions(); // Attempt to get our location permissions

        mMapView.setScene(MapScene.createFromLocationAndRadius(startLocation, Data.DEFAULT_RADIUS_IN_METERS), MapAnimationKind.LINEAR); // Moves the camera to the specified position with the specified animation

        initUser(); // Initiate user point
    }


    @Override
    protected void onStop() {
        super.onStop();
        requestCreator.clearRequests();
    }

    // this should make things neater or something
    void alertUser(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void initUser(){
        // getAssets().open() should open the assets folder and u can access a file through its name, but i wouldnt know because i cant add the map icon
        try{
            userData = new IconData(startLocation, "You", new MapImage(getAssets().open("mrchow.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(startLocation != null){
            draw.addIcon(userData);
        }
    }


    // When we have permissions to access locations
    @SuppressLint("MissingPermission")
    private void onLocationPermsAccepted() {
        System.out.println("Entered location perms accepted condition");

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        startLocation = getCurrentLocation() == null ? startLocation : getCurrentLocation();
    }

    // When the user rejects our request to access locations
    private void onLocationPermsDenied() {
        Toast.makeText(this, "Some features will be disabled without location permissions.", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("MissingPermission")
    private Geopoint getCurrentLocation() {
        Geopoint geopoint = null;

        if(!Data.locationPermsGranted) {
            return null;
        }

        Location l = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if(l != null) {
            geopoint = new Geopoint(l.getLatitude(), l.getLongitude());
        }

        return geopoint;
    }

    // Attempt to fetch location perms
    private void getLocationPermissions() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        boolean a = (ContextCompat.checkSelfPermission(getApplicationContext(), Data.FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        boolean b = (ContextCompat.checkSelfPermission(getApplicationContext(), Data.COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);

        Data.locationPermsGranted = (a && b);

        if (!Data.locationPermsGranted) {
            ActivityCompat.requestPermissions(this, permissions, Data.LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            onLocationPermsAccepted();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Data.locationPermsGranted = false;

        if (requestCode == Data.LOCATION_PERMISSION_REQUEST_CODE) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    onLocationPermsDenied();
                    return;
                }

                Data.locationPermsGranted = true;
                onLocationPermsAccepted();
                Toast.makeText(this, "Location permissions granted!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
