package crepetete.arcgis.evemapp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;
import crepetete.arcgis.evemapp.R;

public class MainMap extends Activity implements LocationListener {
		
	final Context context = this;
	
	private MapView mMapView;
	private User user;
	private GraphicsLayer gl;
	private LocationManager locationManager;
	private String provider;
	private Point testPersoon;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		user = new User();
		Intent intent = getIntent();
		user.setMyId(Integer.parseInt(intent.getStringExtra("id")));
		mMapView = (MapView)findViewById(R.id.map);
		// Voeg een dynamische layer toe aan mMapView
		mMapView.addLayer(new ArcGISTiledMapServiceLayer("" +"http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer"));
		gl = new GraphicsLayer();
		//Voeg de onclicklistener toe
		createMapViewTapList();
		mMapView.addLayer(gl);
			
			LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
			boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// check if enabled and if not send user to the GSP settings
			// Better solution would be to display a dialog and suggesting to 
			// go to the settings
			if (!enabled) {
			  Intent intentLocSource = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			  startActivity(intentLocSource);
			} 
			
			// Get the location manager
		    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		 // Define the criteria how to select the locatioin provider -> use default
		    Criteria criteria = new Criteria();
		    provider = locationManager.getBestProvider(criteria, false);
		    Location location = locationManager.getLastKnownLocation(provider);
		    
		 // Initialize the location fields
		    createPoint(location);	 
	}

	protected void onPause() {
		super.onPause();
		mMapView.pause();
	}

	protected void onResume() {
		super.onResume();
		//requestLocationUpdates(.../Interval tussen request om locatie!/.../...)
		locationManager.requestLocationUpdates(provider, 50000, 20, this);
		mMapView.unpause();
	}
	
	//Get coördinates. (Hier wordt atm gebruik gemaakt van ESRI's LocationDisplayManager om een punt aan te geven waar jij bent, maar (android) Location wordt gebruikt
	//voor long en lat (dubbel GPS gebruik??)
	public void onLocationChanged(Location location) {
		LocationDisplayManager ls = mMapView.getLocationDisplayManager();
			if (ls.isStarted() == false && isOnline()) {	
				ls.start();
				
			}else if(!isOnline()){
				System.out.println("Geen verbinding, skip met gegevens versturen.");
			}else {	
				ls.stop();	
		};	
		user.setMyLat(location.getLatitude());
		user.setMyLng(location.getLongitude());
		try {
				user.POST();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
		}
	}

	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider,
		        Toast.LENGTH_SHORT).show();
		
	}

	public void onProviderEnabled(String provider) {
		 Toast.makeText(this, "Enabled new provider " + provider,
			        Toast.LENGTH_SHORT).show();
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		if(isOnline()){
			System.out.println("Verbinding is terug");
			onLocationChanged(locationManager.getLastKnownLocation(provider));
		}else{
			System.out.println("Geen internerverbinding");
		}
	}
	
	//Check of er internet is
	public boolean isOnline() {
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	private void createPoint(Location location) {
		if (location != null && isOnline()) {
		      System.out.println("Provider " + provider + " has been selected.");
		      onLocationChanged(location);
		      
		    //testpersoon info (hier gebruik ik de lat/long van Papendrecht, 51.8294792/4.6964865
		      testPersoon = ToWebMercator(4.6964865, 51.8294792);
		        
		   // create a point marker symbol (red, size 10, of type circle)
		      SimpleMarkerSymbol simpleMarker = new SimpleMarkerSymbol(Color.RED, 10, SimpleMarkerSymbol.STYLE.CIRCLE);
		      
		   // voeg attributen toe
		      Map<String,Object> attr = new HashMap<String, Object>();
		      attr.put("name", "Patrick van de Graaf");
		      
		   // create a graphic with the geometry and marker symbol
		      Graphic testGraphic = new Graphic(testPersoon, simpleMarker, attr);      
		          
		   // add the graphic to the graphics layer
		      gl.addGraphic(testGraphic);   
	   
		    } else if (!isOnline()){
		    	System.out.println("Er is geen internetverbinding.");
		    }else if(location == null){
		    	System.out.println("fail on location");
		    	Intent intentLocSource = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intentLocSource);
		    }
	}

	private void createMapViewTapList() {
		mMapView.setOnSingleTapListener(new OnSingleTapListener(){
			private static final long serialVersionUID = 1L;
			
			//Onclick voor een Point
			public void onSingleTap(float x, float y) {
				  int[] ids = gl.getGraphicIDs(x,  y,  10, 1); // experiment with tolerance and num of results params here
				  if (ids != null && ids.length > 0)
		            {
		              Graphic foundGraphic = gl.getGraphic(ids[0]);
		              //Als er geklikt is op een Graphic
		              if (foundGraphic != null) {
		            	// custom dialog
		      			final Dialog dialog = new Dialog(context);
		      			dialog.setContentView(R.layout.infodialog);
		      			dialog.setTitle("Informatie");
		       
		      			TextView text = (TextView) dialog.findViewById(R.id.text);
		      			text.setText("Name = " + foundGraphic.getAttributeValue("name"));
		      			ImageView image = (ImageView) dialog.findViewById(R.id.image);
		      			image.setImageResource(R.drawable.ic_launcher);
		       
		      			Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
		      			// if button is clicked, close the custom dialog
		      			dialogButton.setOnClickListener(new OnClickListener() {
		      				public void onClick(View v) {
		      					dialog.dismiss();
		      				}
		      			});
		       
		      			dialog.show();
		              }
		            }
			}			
		});	
	}
	
	//Verander normale coördinaten naar Mercator voor esri(longitude, latitude)
		private Point ToWebMercator(double mercatorX_lon, double mercatorY_lat)
		{
		    if ((Math.abs(mercatorX_lon) > 180 || Math.abs(mercatorY_lat) > 90)){
		        return null;
		    }
		    double num = mercatorX_lon * 0.017453292519943295;
		    double x = 6378137.0 * num;
		    double a = mercatorY_lat * 0.017453292519943295;

		    mercatorX_lon = x;
		    mercatorY_lat = 3189068.5 * Math.log((1.0 + Math.sin(a)) / (1.0 - Math.sin(a)));
		    Point p = new Point(mercatorX_lon, mercatorY_lat);
		    return p;
		}

	//Voor als we ooit de X/Y van Points (die tot nu toe NullPointers gaven) naar normale Coordinaten moeten omrekenen
//	private Point ToGeographic(double mercatorX_lon, double mercatorY_lat)
//	{
//	    if (Math.abs(mercatorX_lon) < 180 && Math.abs(mercatorY_lat) < 90){
//	        return null;
//	    }
//	    if ((Math.abs(mercatorX_lon) > 20037508.3427892) || (Math.abs(mercatorY_lat) > 20037508.3427892)){
//	        return null;
//	    }
//	    double x = mercatorX_lon;
//	    double y = mercatorY_lat;
//	    double num3 = x / 6378137.0;
//	    double num4 = num3 * 57.295779513082323;
//	    double num5 = Math.floor((double)((num4 + 180.0) / 360.0));
//	    double num6 = num4 - (num5 * 360.0);
//	    double num7 = 1.5707963267948966 - (2.0 * Math.atan(Math.exp((-1.0 * y) / 6378137.0)));
//	    mercatorX_lon = num6;
//	    mercatorY_lat = num7 * 57.295779513082323;
//	    
//	    Point p = new Point(mercatorX_lon, mercatorY_lat);
//	    return p;
//	}
}