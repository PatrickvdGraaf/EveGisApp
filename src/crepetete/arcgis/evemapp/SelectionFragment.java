package crepetete.arcgis.evemapp;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.ClientProtocolException;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.esri.android.map.GraphicsLayer;
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

public class SelectionFragment extends Fragment{

	private MapView mMapView;
	private User user;
	private GraphicsLayer gl;
	private Session session;
	private int myPoint;
	private GPSTracker gps;
	private HttpManager hm;

	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback = new Session.StatusCallback() {
		public void call(final Session session, final SessionState state,
				final Exception exception) {
				try {
					onSessionStateChange(session, state, exception);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		uiHelper = new UiLifecycleHelper(getActivity(), callback);
		uiHelper.onCreate(savedInstanceState);
		session = new Session(getActivity());
		session.openForRead(new Session.OpenRequest(this).setCallback(callback)
				.setPermissions(Arrays.asList("friends_birthday")));	
		
		user = new User();
		hm = new HttpManager();
		
		gps = new GPSTracker(getActivity());
		MyTimerTask myTask = new MyTimerTask();
		Timer myTimer = new Timer();
//        public void schedule(TimerTask task, long delay, long period)
 //       Schedule a task for repeated fixed-delay execution after a specific delay.
//        Parameters
//        task  the task to schedule. 
//        delay  amount of time in milliseconds before first execution. 
//        period  amount of time in milliseconds between subsequent executions. 
 
        	myTimer.schedule(myTask, 3000, 1500);  
        
		
		myPoint = -1;
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);		
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		View view = inflater.inflate(R.layout.mainmap, container, false);
		
		mMapView = (MapView) view.findViewById(R.id.map);
		mMapView.addLayer(new ArcGISTiledMapServiceLayer("" + "http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer"));
		gl = new GraphicsLayer();
		// Voeg de onclicklistener toe
		createMapViewTapList();
		mMapView.addLayer(gl);
		
		return view;
	}

	// respond to session changes / call makeMeRequest() if session is open
	private void onSessionStateChange(final Session session,
			SessionState state, Exception exception)
			throws ClientProtocolException, IOException {
		// onLocationChanged(locationManager.getLastKnownLocation(provider));
		if (session != null && session.isOpened()) {
			makeMeRequest(session);
		} else if (!session.isOpened()) {
			System.out.println("no session");
		} else {
			System.out.println("session = null");
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();
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



	private void createPoint(Location location, String type) {
		if (location != null && hm.isOnline(getActivity())) {
			if (myPoint != -1) {
				gl.removeGraphic(myPoint);
			}

			Point p = ToWebMercator(location.getLongitude(), location.getLatitude());
			// create a point marker symbol (red, size 10, of type circle)
			SimpleMarkerSymbol marker = new SimpleMarkerSymbol(Color.CYAN, 10,
					SimpleMarkerSymbol.STYLE.CIRCLE);

			Map<String, Object> attr = new HashMap<String, Object>();
			attr.put("name", user.getMyName());
			attr.put("type", type);

			// create a graphic with the geometry and marker symbol
			Graphic g = new Graphic(p, marker, attr);
			myPoint = g.getUid();
			// add the graphic to the graphics layer
			System.out.println("Point");
			gl.addGraphic(g);

		} else if (!hm.isOnline(getActivity())) {
			System.out.println("Er is geen internetverbinding.");
		} else if (location == null) {
			System.out.println("fail on location");
			Intent intentLocSource = new Intent(
					Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intentLocSource);
		}
	}

	// request user data
	private void makeMeRequest(final Session session) {
		Request request = Request.newMeRequest(session,
				new Request.GraphUserCallback() {
					public void onCompleted(GraphUser fbUser, Response response) {
						// Als de request gelukt is, plaats ik de info in de
						// daarvoor gemaakte View's. (Dit komt uit de Facebook
						// SDK tutorials van developers.facebook)
						if (session == Session.getActiveSession()) {
							user.setMyId(fbUser.getId());
							System.out.println(fbUser.getId());
							user.setMyName((fbUser.getName()));
							if (user.getMyId() != null) {
								try {
									hm.makeRegisterParams(user, getActivity());
								} catch (ClientProtocolException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
						if (response.getError() != null) {
							System.out.println(response.getError()
									.getErrorMessage());
						}
					}
				});
		request.executeAsync();
	}

	private void createMapViewTapList() {
		mMapView.setOnSingleTapListener(new OnSingleTapListener() {
			private static final long serialVersionUID = 1L;

			// Onclick voor een Point
			public void onSingleTap(float x, float y) {
				int[] ids = gl.getGraphicIDs(x, y, 10, 1); // experiment with
															// tolerance and num
															// of results params
															// here
				if (ids != null && ids.length > 0) {
					Graphic foundGraphic = gl.getGraphic(ids[0]);
					// Als er geklikt is op een Graphic
					if (foundGraphic != null) {
						// custom dialog
						final Dialog dialog = new Dialog(getActivity());
						dialog.setContentView(R.layout.infodialog);
						dialog.setTitle("Informatie");

						TextView text = (TextView) dialog
								.findViewById(R.id.text);
						text.setText("Name = "
								+ foundGraphic.getAttributeValue("name"));
						ImageView image = (ImageView) dialog
								.findViewById(R.id.image);
						image.setImageResource(R.drawable.ic_launcher);

						Button dialogButton = (Button) dialog
								.findViewById(R.id.dialogButtonOK);
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

	// Verander normale coördinaten naar Mercator voor esri(longitude, latitude)
	private Point ToWebMercator(double mercatorX_lon, double mercatorY_lat) {
		if ((Math.abs(mercatorX_lon) > 180 || Math.abs(mercatorY_lat) > 90)) {
			return null;
		}
		double num = mercatorX_lon * 0.017453292519943295;
		double x = 6378137.0 * num;
		double a = mercatorY_lat * 0.017453292519943295;

		mercatorX_lon = x;
		mercatorY_lat = 3189068.5 * Math.log((1.0 + Math.sin(a))
				/ (1.0 - Math.sin(a)));
		Point p = new Point(mercatorX_lon, mercatorY_lat);
		return p;
	}
	
	private class MyTimerTask extends TimerTask {
    	  public void run() {
    		if(gps.canGetLocation()){
    			double latitude = gps.getLatitude();
    			double longitude = gps.getLongitude();
    			user.setMyLat(latitude);
    			user.setMyLng(longitude);
    			if(user.getMyId()!= null){
	    			try {
						hm.makeLoc_SelfParams(user, getActivity());
					} catch (ClientProtocolException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
    			}
    			Location l = new Location("");
    			l.setLatitude(latitude);
    			l.setLongitude(longitude);
    			createPoint(l, "self");
    		}
    	  }
    }
};

// Voor als we ooit de X/Y van Points (die tot nu toe NullPointers gaven)
// naar normale Coordinaten moeten omrekenen
// private Point ToGeographic(double mercatorX_lon, double mercatorY_lat)
// {
// if (Math.abs(mercatorX_lon) < 180 && Math.abs(mercatorY_lat) < 90){
// return null;
// }
// if ((Math.abs(mercatorX_lon) > 20037508.3427892) ||
// (Math.abs(mercatorY_lat) > 20037508.3427892)){
// return null;
// }
// double x = mercatorX_lon;
// double y = mercatorY_lat;
// double num3 = x / 6378137.0;
// double num4 = num3 * 57.295779513082323;
// double num5 = Math.floor((double)((num4 + 180.0) / 360.0));
// double num6 = num4 - (num5 * 360.0);
// double num7 = 1.5707963267948966 - (2.0 * Math.atan(Math.exp((-1.0 * y) /
// 6378137.0)));
// mercatorX_lon = num6;
// mercatorY_lat = num7 * 57.295779513082323;
//
// Point p = new Point(mercatorX_lon, mercatorY_lat);
// return p;
// }
