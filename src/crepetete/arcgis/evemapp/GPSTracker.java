package crepetete.arcgis.evemapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class GPSTracker extends Service implements LocationListener {
	 
    private final Context mContext;
 
    // flag for GPS status
    boolean isGPSEnabled = false;
 
    // flag for network status
    boolean isNetworkEnabled = false;
 
    boolean canGetLocation = false;
 
    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
 
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
 
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 500 * 60 * 1; // 1 minute
 
    // Declaring a Location Manager
    protected LocationManager locationManager;
 
    public GPSTracker(Context context) {
        this.mContext = context;
        getLocation();
    }
    
    String provider;
    
    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
 
            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 
            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
         // Creating an empty criteria object
            Criteria criteria = new Criteria();
     
            // Getting the name of the provider that meets the criteria
            provider = locationManager.getBestProvider(criteria, false);
 
            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(provider,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(provider);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            System.out.println("lactknownlocationgetter");
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                System.out.println("lactknownlocation" + latitude);
                    			System.out.println(longitude);
                            }
                        }
                    }
                }
            }
 
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }
    public void onLocationChanged(Location location) {
    	if(canGetLocation()){
			double latitude = getLatitude();
			double longitude = getLongitude();
			System.out.println("locationchanged" + latitude);
			System.out.println(longitude);
		}
    }
 
    public void onProviderDisabled(String provider) {
    	Toast.makeText(this, "Disabled provider " + provider, Toast.LENGTH_SHORT).show();
    }
    
    public void onProviderEnabled(String provider) {
    	Toast.makeText(this, "Enabled new provider " + provider, Toast.LENGTH_SHORT).show();
    }
 
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
 
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
    
    /**
     * Function to get latitude
     * */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }
         
        // return latitude
        return latitude;
    }
     
    /**
     * Function to get longitude
     * */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }
         
        // return longitude
        return longitude;
    }
    
    /**
     * Function to check if best network provider
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }
     
    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GPSTracker.this);
        }       
    }
}