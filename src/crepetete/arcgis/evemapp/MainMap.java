package crepetete.arcgis.evemapp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnZoomListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.facebook.widget.ProfilePictureView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

public class MainMap extends Activity implements LocationListener {

	private GraphicsLayer gl;
	private MapView mMapView;
	private HttpManager hm;
	private User user;
	private LocationManager locationManager;
	private Location location;
	private String provider;
	private ProfilePictureView profilePictureView;
	private Button friendsPage;
	private Button activityPage;
	private Button myLocation;
	private Button homeButton;
	private TextView userNameView;
	private ArrayList<Friend> friendsList;
	// Deze moet gekoppeld worden aan een event
	private List<FestivalObject> objects;
	private Envelope e;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mainmap);

		// Solves NetworkOnMainThreadException
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		// Retrieve the map and initial extent from XML layout
		mMapView = (MapView) findViewById(R.id.map);
		profilePictureView = (ProfilePictureView) findViewById(R.id.selection_profile_pic);
		profilePictureView.setCropped(true);
		final PopupMenu popupMenu = new PopupMenu(this, profilePictureView);
		popupMenu.inflate(R.menu.loginmenu);
		profilePictureView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				popupMenu.show();
			}
		});
		popupMenu
				.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						switch (item.getItemId()) {
						case R.id.logout:
							Intent logout = new Intent(MainMap.this,
									Login.class);
							MainMap.this.startActivity(logout);
							break;
						}
						return true;
					}
				});
		friendsPage = (Button) findViewById(R.id.friends);
		friendsPage.setOnClickListener(friendButtonHandler);
		activityPage = (Button) findViewById(R.id.activitymanager);
		activityPage.setOnClickListener(eventButtonHandler);
		myLocation = (Button) findViewById(R.id.myLocation);
		myLocation.setOnClickListener(locationButtonHandler);
		homeButton = (Button) findViewById(R.id.home);
		homeButton.setOnClickListener(homeButtonHandler);

		userNameView = (TextView) findViewById(R.id.selection_user_name);
		// Add dynamic layer to MapView
		mMapView.addLayer(new ArcGISTiledMapServiceLayer(
				""
						+ "http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer"));
		gl = new GraphicsLayer();
		createMapViewTapList();
		mMapView.addLayer(gl);

		this.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		// Choosing the best criteria depending on what is available.
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		provider = locationManager.getBestProvider(criteria, true);

		Toast.makeText(this, "Provider: " + provider, Toast.LENGTH_SHORT)
				.show();
		// Initialize the location fields
		if (location == null) {
			provider = LocationManager.NETWORK_PROVIDER;
		}
		location = locationManager.getLastKnownLocation(provider);

		// define user, the class to handle internet stuff and the class to
		// handle GPS stuff
		hm = new HttpManager();

		friendsList = (ArrayList<Friend>) getIntent().getSerializableExtra(
				"friendList");

		user = new User();
		if (getIntent().getExtras() != null) {
			user.setMyId(getIntent().getStringExtra("id"));
			user.setMyName(getIntent().getStringExtra("name"));
			try {
				hm.makeRegisterParams(user, this, friendsList);
				profilePictureView.setProfileId(user.getMyId());
				userNameView.setText(user.getMyName());
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		if (location != null) {
			double lat = location.getLatitude();
			double lng = location.getLongitude();
			user.setMyLat(lat);
			user.setMyLng(lng);
			try {
				hm.makeLoc_SelfParams(user, this);
				System.out.println("will make me point");
				createPoint(location.getLatitude(), location.getLongitude(),
						"self", user.getMyName(), user.getMyId());
				
				JSONObject jsonObj = hm.getLocOthers(user, this);
				if(jsonObj!=null){
					JSONArray arr = jsonObj.getJSONArray("friendData");
					for (int i = 0; i < arr.length(); i++) {
						String userId = arr.getJSONObject(i).getString("userId");
						String userName = "friend";
						double latitude = Double.parseDouble(arr.getJSONObject(i)
								.getString("latitude"));
						double longitude = Double.parseDouble(arr.getJSONObject(i)
								.getString("longitude"));
						createPoint(latitude, longitude, "friend", userName, userId);
					}
				}else{
					System.out.println("json was null");
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		objects = new ArrayList<FestivalObject>();
		try {
			JSONObject jsonObj = hm.getEventMapInfo("1", getBaseContext());
			JSONArray arr = jsonObj.getJSONArray("bounds");
			for (int i = 0; i < arr.length(); i++) {
				double xmin = Double.parseDouble(arr.getJSONObject(i)
						.getString("xmin"));
				double xmax = Double.parseDouble(arr.getJSONObject(i)
						.getString("xmax"));
				double ymin = Double.parseDouble(arr.getJSONObject(i)
						.getString("ymin"));
				double ymax = Double.parseDouble(arr.getJSONObject(i)
						.getString("ymax"));
				Point max = ToWebMercator(xmax, ymax);
				Point min = ToWebMercator(xmin, ymin);
				e = new Envelope();
				e.setCoords(min.getX(), min.getY(), max.getX(), max.getY());
//				mMapView.setMaxExtent(e);
			}
			arr = jsonObj.getJSONArray("objects");
			for (int i = 0; i < arr.length(); i++) {
				String obj_id = arr.getJSONObject(i).getString("id");
				String obj_lat = arr.getJSONObject(i).getString("lat");
				String obj_lng = arr.getJSONObject(i).getString("lng");
				String obj_type = arr.getJSONObject(i).getString("type");
				String obj_image_url = "http://web.insidion.com" + arr.getJSONObject(i).getString(
						"image_url");
				String obj_width = arr.getJSONObject(i).getString("width");
				String obj_height = arr.getJSONObject(i).getString("height");
				String obj_angle = arr.getJSONObject(i).getString("angle");

				FestivalObject fo = new FestivalObject();
				fo.setObj_angle(obj_angle);
				fo.setObj_height(obj_height);
				fo.setObj_id(obj_id);
				fo.setObj_image_url(obj_image_url);
				fo.setObj_lat(obj_lat);
				fo.setObj_lng(obj_lng);
				fo.setObj_type(obj_type);
				fo.setObj_width(obj_width);
				
				objects.add(fo);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < objects.size(); i++) {
			FestivalObject fo = objects.get(i);
				double lat = Double.parseDouble(fo.getObj_lat());
				double lng = Double.parseDouble(fo.getObj_lng());
				String type = fo.getObj_type();
				String id = fo.getObj_id();
				int width = Integer.parseInt(fo.getObj_width());
				int height = Integer.parseInt(fo.getObj_height());
				int angle = Integer.parseInt(fo.getObj_angle());
//				try {
//					createEventObjectPoint(lat, lng, fo.getObj_image_url(), type, id, width,
//							height, angle);
//				} catch (MalformedURLException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			
		}

		profilePictureView.setProfileId(user.getMyId());
		userNameView.setText(user.getMyName());
	}

	/* Request updates at startup */
	@Override
	public void onResume() {
		super.onResume();
		mMapView.unpause();
		locationManager.requestLocationUpdates(provider, 8000, 20, this);
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
	}

	@Override
	public void onPause() {
		super.onPause();
		mMapView.pause();
	}

	public void onLocationChanged(Location loc) {
		location = loc;
		double lat = loc.getLatitude();
		double lng = loc.getLongitude();
		user.setMyLat(lat);
		user.setMyLng(lng);
		gl.removeAll();
		try {
			hm.makeLoc_SelfParams(user, this);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			createPoint(lat, lng, "self", user.getMyName(), user.getMyId());
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			JSONObject jsonObj = hm.getLocOthers(user, this);
			JSONArray arr = jsonObj.getJSONArray("friendData");

			for (int i = 0; i < arr.length(); i++) {
				String userId = arr.getJSONObject(i).getString("userId");
				String userName = "friend";
				double latitude = Double.parseDouble(arr.getJSONObject(i)
						.getString("latitude"));
				double longitude = Double.parseDouble(arr.getJSONObject(i)
						.getString("longitude"));
				createPoint(latitude, longitude, "friend", userName, userId);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d("Latitude", "status");
	}

	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Enabled new provider " + provider,
				Toast.LENGTH_SHORT).show();
		Log.d("Latitude", "enable");
	}

	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider,
				Toast.LENGTH_SHORT).show();
		Log.d("Latitude", "disable");
	}

	private void createPoint(double lat, double lng, String type, String name,
			String id) throws MalformedURLException, IOException {
		Point p = ToWebMercator(lng, lat);

		Map<String, Object> attr = new HashMap<String, Object>();
		attr.put("name", name);
		attr.put("type", type);
		attr.put("id", id);

		String s = "https://graph.facebook.com/" + id + "/picture?width=100&height=100";
		Bitmap mIcon1 = hm.getFacebookBitMap(s);
		Drawable d = new BitmapDrawable(getResources(),mIcon1);
		// Scale it to 50 x 50
		PictureMarkerSymbol pms = new PictureMarkerSymbol(d);
		Graphic g = new Graphic(p, pms, attr);
		// add the graphic to the graphics layer
		System.out.println("create graphic");
		gl.addGraphic(g);
	}

	private void createEventObjectPoint(double lat, double lng, String url,
			String type, String id, int width, int height, int angle)
			throws MalformedURLException, IOException {
		Drawable d = hm.LoadImageFromWebOperations(url);
		if (d != null){
			Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
			// Scale it to 50 x 50
			double minScale = mMapView.getMinScale();
			d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, width, height, true));
			Map<String, Object> attr = new HashMap<String, Object>();
			attr.put("type", type);
			attr.put("id", id);
			PictureMarkerSymbol pms = new PictureMarkerSymbol(d);
			pms.setAngle(angle);
			Point p = ToWebMercator(lng, lat);
			Graphic g = new Graphic(p, pms, attr);
			gl.addGraphic(g);
		}else{
			System.out.println("drawable was null");
		}
	}

	private void createMapViewTapList() {
		mMapView.setOnSingleTapListener(new OnSingleTapListener() {
			private static final long serialVersionUID = 1L;

			// Onclick voor een Point
			public void onSingleTap(float x, float y) {
				int[] ids = gl.getGraphicIDs(x, y, 10, 1);
				Graphic foundGraphic = null;
				if (ids.length > 0) {
					foundGraphic = gl.getGraphic(ids[0]);
				}
				// Als er geklikt is op een Graphic
				if (foundGraphic != null) {
					// custom dialog
					final Dialog dialog = new Dialog(MainMap.this);
					dialog.setContentView(R.layout.infodialog);
					dialog.setTitle("Informatie");

					TextView text = (TextView) dialog.findViewById(R.id.text);
					text.setText((String) foundGraphic
							.getAttributeValue("name"));
					ProfilePictureView dialogPicture = (ProfilePictureView) dialog
							.findViewById(R.id.dialog_profile_pic);

					String id = (String) foundGraphic.getAttributeValue("id");
					dialogPicture.setProfileId(id);
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

	View.OnClickListener friendButtonHandler = new View.OnClickListener() {
		public void onClick(View v) {
			Intent myIntent = new Intent(MainMap.this, FriendsList.class);
			myIntent.putExtra("userId", user.getMyId());
			myIntent.putExtra("userName", user.getMyName());
			myIntent.putExtra("friendList", friendsList);
			MainMap.this.startActivity(myIntent);
		}
	};

	View.OnClickListener eventButtonHandler = new View.OnClickListener() {
		public void onClick(View v) {
			Intent myIntent = new Intent(MainMap.this, EventPicker.class);
			myIntent.putExtra("userId", user.getMyId());
			myIntent.putExtra("userName", user.getMyName());
			myIntent.putExtra("friendList", friendsList);
			MainMap.this.startActivity(myIntent);
		}
	};

	View.OnClickListener locationButtonHandler = new View.OnClickListener() {
		public void onClick(View v) {
			if (location != null) {
				Point myPoint = ToWebMercator(location.getLongitude(),
						location.getLatitude());
				mMapView.centerAt(myPoint, true);
				mMapView.zoomToResolution(myPoint, 100);
				
			}
		}
	};

	View.OnClickListener homeButtonHandler = new View.OnClickListener() {
		public void onClick(View v) {
			if (e != null) {
				mMapView.zoomToResolution(e.getCenter(), 1000);
			}
		}
	};
}
