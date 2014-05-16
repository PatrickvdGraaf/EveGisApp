package crepetete.arcgis.evemapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;

public class SelectionFragment extends Fragment implements LocationListener{
		
	private MapView mMapView;
	private User user;
	private GraphicsLayer gl;
	private LocationManager locationManager;
	private String provider;
	private String[] response;
	private Session session;
	
	private Point testPersoon;
	
	private static HttpURLConnection httpConn;
	
	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback= new Session.StatusCallback() {
		public void call(final Session session, final SessionState state, final Exception exception) {
	        try {
				onSessionStateChange(session, state, exception);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	};;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    uiHelper = new UiLifecycleHelper(getActivity(), callback);
	    uiHelper.onCreate(savedInstanceState);

	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		user = new User(getString(R.string.backend_site));
		
		LocationManager service =  (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
		boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
		// check if enabled and if not send user to the GSP settings
		// Better solution would be to display a dialog and suggesting to 
		// go to the settings
		if (!enabled) {
		  Intent intentLocSource = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		  startActivity(intentLocSource);
		} 
		// Get the location manager
	    locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
	    session = new Session(getActivity());
	    session.openForRead(new Session.OpenRequest(this).setCallback(callback).setPermissions(Arrays.asList("friends_birthday")));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    super.onCreateView(inflater, container, savedInstanceState);
	   
	    View view = inflater.inflate(R.layout.mainmap, container, false);
	    mMapView = (MapView)view.findViewById(R.id.map);
	    mMapView.addLayer(new ArcGISTiledMapServiceLayer("" +"http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer"));
		gl = new GraphicsLayer();
		// Define the criteria how to select the locatioin provider -> use default
	    Criteria criteria = new Criteria();
	    provider = locationManager.getBestProvider(criteria, false);
	    Location location = locationManager.getLastKnownLocation(provider);
		createPoint(location);	
		//Voeg de onclicklistener toe
		createMapViewTapList();
		mMapView.addLayer(gl);
	    return view;
	}
	
	//respond to session changes / call makeMeRequest() if session is open
	private void onSessionStateChange(final Session session, SessionState state, Exception exception) throws ClientProtocolException, IOException {	
//		 onLocationChanged(locationManager.getLastKnownLocation(provider));
		if (session != null && session.isOpened()) {
			System.out.println("session");
			makeMeRequest(session);
		}else if(!session.isOpened()){
			System.out.println("no session");
		}else{
			System.out.println("session = null");
		}
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    uiHelper.onResume();
	    locationManager.requestLocationUpdates(provider, 50000, 20, this);
		mMapView.unpause();
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
	    super.onSaveInstanceState(bundle);
	    uiHelper.onSaveInstanceState(bundle);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	    mMapView.pause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}

	public void onLocationChanged(Location location) {
		LocationDisplayManager ls = mMapView.getLocationDisplayManager();
		if (ls.isStarted() == false && isOnline()) {	
			ls.start();
		}else if(!isOnline()){
			System.out.println("Geen verbinding, skip met gegevens versturen.");
		}else {	
			ls.stop();	
		};	
		if(isOnline()){
			user.setMyLat(location.getLatitude());
			user.setMyLng(location.getLongitude());
//			try {
//				user.makeLoc_SelfParams(getString(R.string.backend_site));
//			} catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
//			} catch (ClientProtocolException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
		}
	}

	public void onProviderDisabled(String arg0) {
		Toast.makeText(getActivity(), "Disabled provider " + provider,Toast.LENGTH_SHORT).show();	
	}

	public void onProviderEnabled(String arg0) {
		 Toast.makeText(getActivity(), "Enabled new provider " + provider,Toast.LENGTH_SHORT).show();
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
	    getActivity();
		ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(FragmentActivity.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	private void createPoint(Location location) {
		if (location != null && isOnline()) {
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
	
	//request user data
	private void makeMeRequest(final Session session) {
	    Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
	    	public void onCompleted(GraphUser fbUser, Response response) {
	            // Als de request gelukt is, plaats ik de info in de daarvoor gemaakte View's. (Dit komt uit de Facebook SDK tutorials van developers.facebook)
	    		System.out.println("session2");
	    		if (session == Session.getActiveSession()) {
	            	System.out.println("me");
	                    user.setMyId(fbUser.getId());
	                    user.setMyName((fbUser.getName()));
	                    AsyncTaskRunner runner = new AsyncTaskRunner();
	                    runner.execute();
	    		}
	            if (response.getError() != null) {
	            	System.out.println(response.getError().getErrorMessage());
	            }
	        }
	    });
	    request.executeAsync();
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
		      			final Dialog dialog = new Dialog(getActivity());
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
		
	
	private class AsyncTaskRunner extends AsyncTask<String, String, String> {

		  @Override
		  protected String doInBackground(String... params) {
			  String postResult;
			   try {
					LogIn();
					postResult = response[0];
				} catch (ClientProtocolException e) {
					e.printStackTrace();
					postResult = e.getMessage();
				} catch (IOException e) {
					e.printStackTrace();
					postResult = e.getMessage();
				}
			   return postResult;
		  }
	}
			
	public HttpURLConnection LogIn() throws ClientProtocolException, IOException{
		URL url = new URL(getString(R.string.backend_site));
      httpConn = (HttpURLConnection) url.openConnection();
      httpConn.setUseCaches(false);

      httpConn.setDoInput(true); // true indicates the server returns response

      StringBuffer requestParams = new StringBuffer();
      Map<String, String> postParams = makeParams();
      if (postParams != null && postParams.size() > 0) {
      	
          httpConn.setDoOutput(true); // true indicates POST request
          System.out.println("postparams " + postParams);
          // creates the params string, encode them using URLEncoder
          Iterator<String> paramIterator = (postParams).keySet().iterator();
          while (paramIterator.hasNext()) {
              String key = paramIterator.next();
              String value = postParams.get(key);
              requestParams.append(URLEncoder.encode(key, "UTF-8"));
              requestParams.append("=").append(
                      URLEncoder.encode(value, "UTF-8"));
              requestParams.append("&");
          }
          System.out.println(requestParams.toString());
          // sends POST data
          OutputStreamWriter writer = new OutputStreamWriter(
                  httpConn.getOutputStream());
          writer.write(requestParams.toString());
          writer.flush();
      }
      response = readMultipleLinesRespone();
      System.out.println(response);      
      return httpConn;
  }
	
	public Map<String, String> makeParams() throws UnsupportedEncodingException{
		// Request parameters and other properties.
				Map<String, String> params = new HashMap<String, String>();
				params.put("id", user.getMyId());
				params.put("type", "register");
				params.put("friends", "");
				params.put("privacysetting", "full");
				return params;
	}
	
	public static String[] readMultipleLinesRespone() throws IOException {
        InputStream inputStream = null;
        if (httpConn != null) {
            inputStream = httpConn.getInputStream();
        } else {
            throw new IOException("Connection is not established.");
        }
 
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        List<String> response = new ArrayList<String>();
 
        String line = "";
        while ((line = reader.readLine()) != null) {
            response.add(line);
        }
        reader.close();
 
        return (String[]) response.toArray(new String[0]);
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
