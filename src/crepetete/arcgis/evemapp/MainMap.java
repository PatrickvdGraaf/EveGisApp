package crepetete.arcgis.evemapp;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.facebook.widget.ProfilePictureView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
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
	private ProfilePictureView profilePictureView;
	private Button friendsPage, activityPage, myLocation, homeButton, zoomIn, zoomOut;
	private int level = 19;
	private String eventId;
	private ArrayList<Friend> friendsList;
	private ArrayList<String> selectedFriendsList;
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
		popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
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
		mMapView.setOnTouchListener(new MapOnTouchListener(this, mMapView){
			@Override
			public boolean onPinchPointersDown (MotionEvent event){
				return true;
			}
			@Override
			public boolean onPinchPointersUp (MotionEvent event){
				return true;
			}
			@Override
			public boolean onPinchPointersMove (MotionEvent event){
				return true;
			}
			@Override
			public boolean onDoubleTap (MotionEvent point){
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
		zoomIn = (Button) findViewById(R.id.zoomin);
		zoomIn.setOnClickListener(zoomInButtonHandler);
		zoomOut = (Button) findViewById(R.id.zoomout);
		zoomOut.setOnClickListener(zoomOutButtonHandler);

		// Add dynamic layer to MapView
		mMapView.addLayer(new ArcGISTiledMapServiceLayer("http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer"));
		gl = new GraphicsLayer(GraphicsLayer.RenderingMode.STATIC);
		selectedFriendsList = new ArrayList<String>();
		createMapViewTapList();
		mMapView.addLayer(gl);
		
		this.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		String provider = LocationManager.NETWORK_PROVIDER;
		Toast.makeText(this, "Provider: " + provider, Toast.LENGTH_SHORT).show();
		locationManager.requestLocationUpdates(provider, 8000, 1, this);
		location = locationManager.getLastKnownLocation(provider);

		hm = new HttpManager();

		friendsList = (ArrayList<Friend>) getIntent().getSerializableExtra("friendList");
		eventId = getIntent().getStringExtra("eventId");
		if(eventId != null){
			new loadEvent(eventId).execute("");
		}

		user = new User();
		if (getIntent().getExtras() != null) {
			user.setMyId(getIntent().getStringExtra("id"));
			user.setMyName(getIntent().getStringExtra("name"));
			try {
				hm.makeRegisterParams(user, this, friendsList);
				profilePictureView.setProfileId(user.getMyId());
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(location!=null){
			onLocationChanged(location);
		}
		Point p = ToWebMercator(52.3, 5.45);
		mMapView.zoomToScale(p, 9244646.48029992);
	}
	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
	  super.onActivityResult(requestCode, resultCode, data); 
	  switch(requestCode) { 
	    case (1) : { 
	      if (resultCode == Activity.RESULT_OK) { 
	    	  eventId = data.getStringExtra("eventId");
	      new loadEvent(eventId).execute("");
	      } 
	      break; 
	    } 
	    case (2) : { 
		      if (resultCode == Activity.RESULT_OK) { 
		      selectedFriendsList = data.getStringArrayListExtra("friendsToDisplay");
		      System.out.println(selectedFriendsList.toString());
			      if(location!=null){
			    	  onLocationChanged(location);
			      }
		      } 
		      break; 
		    }
	  } 
	}

	/* Request updates at startup */
	@Override
	public void onResume() {
		super.onResume();
		mMapView.unpause();
		String provider = LocationManager.NETWORK_PROVIDER;
		locationManager.requestLocationUpdates(provider, 8000, 1, this);
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
		System.out.println(mMapView.getScale());
		location = loc;
		double lat = loc.getLatitude();
		double lng = loc.getLongitude();
		user.setMyLat(lat);
		user.setMyLng(lng);
		int[] gid = gl.getGraphicIDs();
		if(gid!=null){
			for(int i=0;i<gid.length;i++){
				Graphic g = gl.getGraphic(gid[i]);
				if(g.getAttributeValue("type").equals("self")||g.getAttributeValue("type").equals("friend")){
					gl.removeGraphic(g.getUid());
				}
			}
		}
		try {
			hm.makeLoc_SelfParams(user, this);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		new createPoint(lat, lng, "self", user.getMyName(), user.getMyId()).execute();

		try {
			JSONObject jsonObj = hm.getLocOthers(user, this);
			JSONArray arr = jsonObj.getJSONArray("friendData");

			for (int i = 0; i < arr.length(); i++) {
				String userId = arr.getJSONObject(i).getString("userId");
				System.out.println(userId);
				if (selectedFriendsList.contains(userId)  && selectedFriendsList != null) {
					String userName = "friend";
					double latitude = Double.parseDouble(arr.getJSONObject(i)
							.getString("latitude"));
					double longitude = Double.parseDouble(arr.getJSONObject(i)
							.getString("longitude"));
					new createPoint(latitude, longitude, "friend", userName, userId).execute();
				}
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

	//Set TapList on the map to find out if we need to show a popup.
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
					final Dialog dialog = new Dialog(MainMap.this);
					if(foundGraphic.getAttributes().get("type").equals("self") || foundGraphic.getAttributes().get("type").equals("friend")){
						// custom dialog
						dialog.setContentView(R.layout.infodialog);
						dialog.setTitle("Informatie");
	
						TextView text = (TextView) dialog.findViewById(R.id.text);
						text.setText((String) foundGraphic
								.getAttributeValue("name"));
						ProfilePictureView dialogPicture = (ProfilePictureView) dialog
								.findViewById(R.id.dialog_profile_pic);
	
						String id1 = (String) foundGraphic.getAttributeValue("id");
						dialogPicture.setProfileId(id1);
						Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
						// if button is clicked, close the custom dialog
						dialogButton.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								dialog.dismiss();
							}
						});
						dialog.show();
					}else if (objects.get(Integer.parseInt((String) foundGraphic.getAttributes().get("id"))) instanceof Stage){
						Stage s = (Stage) objects.get(Integer.parseInt((String) foundGraphic.getAttributes().get("id")));
						s.showDialog(dialog);
					}else if (objects.get(Integer.parseInt((String) foundGraphic.getAttributes().get("id"))) instanceof Toilet){
						Toilet t = (Toilet) objects.get(Integer.parseInt((String) foundGraphic.getAttributes().get("id")));
						t.showDialog(dialog);
					}else if (objects.get(Integer.parseInt((String) foundGraphic.getAttributes().get("id"))) instanceof InfoStand){
						InfoStand is = (InfoStand) objects.get(Integer.parseInt((String) foundGraphic.getAttributes().get("id")));
						is.showDialog(dialog);
					}else if (objects.get(Integer.parseInt((String) foundGraphic.getAttributes().get("id"))) instanceof FoodStand){
						FoodStand fs = (FoodStand) objects.get(Integer.parseInt((String) foundGraphic.getAttributes().get("id")));
						fs.showDialog(dialog);
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
	
	
	public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
	    int width = bm.getWidth();
	    int height = bm.getHeight();
	    float scaleWidth = ((float) newWidth) / width;
	    float scaleHeight = ((float) newHeight) / height;
	    // CREATE A MATRIX FOR THE MANIPULATION
	    Matrix matrix = new Matrix();
	    // RESIZE THE BIT MAP
	    matrix.postScale(scaleWidth, scaleHeight);

	    // "RECREATE" THE NEW BITMAP
	    Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
	    return resizedBitmap;
	}

	View.OnClickListener friendButtonHandler = new View.OnClickListener() {
		public void onClick(View v) {
			Intent myIntent = new Intent(MainMap.this, FriendsList.class);
			myIntent.putExtra("userId", user.getMyId());
			myIntent.putExtra("userName", user.getMyName());
			myIntent.putExtra("friendsList", friendsList);
			myIntent.putExtra("selectedFriendsList", selectedFriendsList);
			startActivityForResult(myIntent, 2);
		}
	};

	View.OnClickListener eventButtonHandler = new View.OnClickListener() {
		public void onClick(View v) {
			Intent myIntent = new Intent(MainMap.this, EventPicker.class);
			myIntent.putExtra("userId", user.getMyId());
			myIntent.putExtra("userName", user.getMyName());
			myIntent.putExtra("friendList", friendsList);
			startActivityForResult(myIntent, 1);
		}
	};

	View.OnClickListener locationButtonHandler = new View.OnClickListener() {
		public void onClick(View v) {
			if(level<17 && location != null){
				while(level < 17){
					mMapView.zoomin(true);
					System.out.println(mMapView.getScale());
					level++;
					Point myPoint = ToWebMercator(location.getLongitude(), location.getLatitude());
					mMapView.centerAt(myPoint, true);
					if(objects!=null){
						int[] grs = gl.getGraphicIDs();
						for(int i = 0; i<grs.length;i++){
							Graphic g = gl.getGraphic(grs[i]);
							if(g.getAttributeValue("type")!="self" || g.getAttributeValue("type")!="friend"){
								gl.removeGraphic(grs[i]);
							}
						}
						for (int i = 0; i < objects.size(); i++) {
							FestivalObject fo = objects.get(i);
							int width = (int) (Integer.parseInt(fo.getObj_width()));
							int height = (int) (Integer.parseInt(fo.getObj_height()));
							new createEventObjectPoint(fo, width*2, height*2 ,i).execute(); 
							fo.setObj_height(Integer.toString(height*2));
							fo.setObj_width(Integer.toString(width*2));
						}
					}
				}
			}else if (level > 17 && location != null){
				while(level > 17){
					mMapView.zoomout(true);
					System.out.println(mMapView.getScale());
					level--;
					Point myPoint = ToWebMercator(location.getLongitude(), location.getLatitude());
					mMapView.centerAt(myPoint, true);
					if(objects!=null){
						int[] grs = gl.getGraphicIDs();
						for(int i = 0; i<grs.length;i++){
							Graphic g = gl.getGraphic(grs[i]);
							if(g.getAttributeValue("type")!="self" || g.getAttributeValue("type")!="friend"){
								gl.removeGraphic(grs[i]);
							}
						}
					
						for (int i = 0; i < objects.size(); i++) {
							FestivalObject fo = objects.get(i);
							int width = (int) (Integer.parseInt(fo.getObj_width()));
							int height = (int) (Integer.parseInt(fo.getObj_height()));
							new createEventObjectPoint(fo, width/2, height/2, i).execute(); 
							fo.setObj_height(Integer.toString(height/2));
							fo.setObj_width(Integer.toString(width/2));
						}
					}
				}
			}
		}
	};

	View.OnClickListener homeButtonHandler = new View.OnClickListener() {
		public void onClick(View v) {	
			mMapView.centerAt(e.getCenter(), true);
			if(level<17 && eventId!=null){
				while(level < 17){
					mMapView.zoomin(true);
					System.out.println(mMapView.getScale());
					level++;
					if(objects!=null){
						int[] grs = gl.getGraphicIDs();
						for(int i = 0; i<grs.length;i++){
							Graphic g = gl.getGraphic(grs[i]);
							if(g.getAttributeValue("type")!="self" || g.getAttributeValue("type")!="friend"){
								gl.removeGraphic(grs[i]);
							}
						}
						for (int i = 0; i < objects.size(); i++) {
							FestivalObject fo = objects.get(i);
							int width = (int) (Integer.parseInt(fo.getObj_width()));
							int height = (int) (Integer.parseInt(fo.getObj_height()));
							new createEventObjectPoint(fo, width*2, height*2, i).execute(); 
							fo.setObj_height(Integer.toString(height*2));
							fo.setObj_width(Integer.toString(width*2));
						}
					}
				}
			}else if (level > 17 && eventId!=null){
				while(level > 17){
					mMapView.zoomout(true);
					System.out.println(mMapView.getScale());
					level--;
					if(objects!=null){
						int[] grs = gl.getGraphicIDs();
						while(grs==null){
							grs = gl.getGraphicIDs();
						}
						for(int i = 0; i<grs.length;i++){
							Graphic g = gl.getGraphic(grs[i]);
							if(g.getAttributeValue("type")!="self" || g.getAttributeValue("type")!="friend"){
								gl.removeGraphic(grs[i]);
							}
						}
						for (int i = 0; i < objects.size(); i++) {
							FestivalObject fo = objects.get(i);
							int width = (int) (Integer.parseInt(fo.getObj_width()));
							int height = (int) (Integer.parseInt(fo.getObj_height()));
							new createEventObjectPoint(fo, width/2, height/2, i).execute(); 
							fo.setObj_height(Integer.toString(height/2));
							fo.setObj_width(Integer.toString(width/2));
						}
					}
				}
			}
		}
	};
	
	View.OnClickListener zoomInButtonHandler = new View.OnClickListener() {
		public void onClick(View v) {
			if(level !=19 && eventId!=null){
				mMapView.zoomin(true);
				System.out.println(mMapView.getScale());
				level++;
				int[] grs = gl.getGraphicIDs();
				for(int i = 0; i<grs.length;i++){
					Graphic g = gl.getGraphic(grs[i]);
					if(g.getAttributeValue("type")!="self" || g.getAttributeValue("type")!="friend"){
						gl.removeGraphic(grs[i]);
					}
				}
				for (int i = 0; i < objects.size(); i++) {
					FestivalObject fo = objects.get(i);
					int width = (int) (Integer.parseInt(fo.getObj_width()));
					int height = (int) (Integer.parseInt(fo.getObj_height()));
					fo.setObj_height(Integer.toString(height*2));
					fo.setObj_width(Integer.toString(width*2));
					new createEventObjectPoint(fo, width*2, height*2, i).execute(); 
				}
			}
		}
	};
	
	View.OnClickListener zoomOutButtonHandler = new View.OnClickListener() {
		public void onClick(View v) {
			if(level!=5 && eventId!=null){
				mMapView.zoomToScale(mMapView.getCenter(), mMapView.getScale()*2);
				System.out.println(mMapView.getScale());
				level--;
				int[] grs = gl.getGraphicIDs();
				while(grs==null){
					grs = gl.getGraphicIDs();
				}
				for(int i = 0; i<grs.length;i++){
					Graphic g = gl.getGraphic(grs[i]);
					if(g.getAttributeValue("type")!="self" || g.getAttributeValue("type")!="friend"){
						gl.removeGraphic(grs[i]);
					}
				}
				for (int i = 0; i < objects.size(); i++) {
					FestivalObject fo = objects.get(i);
					int width = Integer.parseInt(fo.getObj_width())/2;
					int height = Integer.parseInt(fo.getObj_height())/2;
					if(width>0 && height>0){
						fo.setObj_height(Integer.toString(height));
						fo.setObj_width(Integer.toString(width));
						new createEventObjectPoint(fo, width, height, i).execute();
					}
				}
			}
		}
	};
			
	public class createEventObjectPoint extends AsyncTask<String, Void, String> {
		private double lat; 
		private double lng; 
		private Drawable d;
		private String type;
		private int width;
		private int height;
		private int angle;
		int i;
		
        public createEventObjectPoint(FestivalObject fo, int width, int height, int i) {
            super();
            this.lat=Double.parseDouble(fo.getObj_lat());
            this.lng=Double.parseDouble(fo.getObj_lng());
            this.type=fo.getObj_type();
            this.d=fo.getObj_image();
            this.width=width;
            this.height=height;
            this.angle=Integer.parseInt(fo.getObj_angle());
            this.i=i;
        }
        
        @Override
        protected String doInBackground(String... params) {
        	if (d != null){
    			Map<String, Object> attr = new HashMap<String, Object>();
    			attr.put("type", type);
    			attr.put("id", Integer.toString(i));
    			Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
    			d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, width, height, true));
    			PictureMarkerSymbol pms = new PictureMarkerSymbol(d);
    			pms.setAngle(angle);
    			Point p = ToWebMercator(lat, lng);
    			Graphic g = new Graphic(p, pms, attr);
    			gl.addGraphic(g);
    			return "Done";
    		}else{
    			return "drawable was null";
    		}	
        }
	}
	
	public class createPoint extends AsyncTask<String, Void, String> {
		private double lat; 
		private double lng; 
		private String type; 
		private String name;
		private String id;
		
        public createPoint(double lat, double lng, String type, String name, String id) {
            super();
            this.lat=lat;
            this.lng=lng;
            this.id=id;
            this.type=type;
            this.name=name;
        }
        
        @Override
        protected String doInBackground(String... params) {
        	Point p = ToWebMercator(lng, lat);
        	Map<String, Object> attr = new HashMap<String, Object>();
    		attr.put("name", name);
    		attr.put("type", type);
    		attr.put("id", id);

    		String s = "https://graph.facebook.com/" + id + "/picture?width=100&height=100";
    		Bitmap mIcon1 = null;
			try {
				mIcon1 = hm.getFacebookBitMap(s);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (mIcon1 != null){
	    		Drawable d = new BitmapDrawable(getResources(),mIcon1);
	    		// Scale it to 50 x 50
	    		PictureMarkerSymbol pms = new PictureMarkerSymbol(d);
	    		Graphic g = new Graphic(p, pms, attr);
	    		// add the graphic to the graphics layer
	    		gl.addGraphic(g);
			}
			return "Done";
        }
	}
	
	public class loadEvent extends AsyncTask<String, Void, String> {
		
		private String eventId;
		
		public loadEvent(String eventId){
			this.eventId=eventId;
		}
        @Override
        protected String doInBackground(String... params) {
        	objects = new ArrayList<FestivalObject>();
    		try {
    			JSONObject jsonObj = hm.getEventMapInfo(eventId, getBaseContext());
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
    				mMapView.setMaxExtent(e);    				
    			}

    			arr = jsonObj.getJSONArray("objects");
    			for (int i = 0; i < arr.length(); i++) {
    				JSONObject info = arr.getJSONObject(i);
    				String obj_type = info.getString("type");
    				if(obj_type.equals("Stage")){
    					Stage s = new Stage(info, i, getParent());        				
	    				objects.add(s);
    				}else if(obj_type.equals("Toilet")){
    					Toilet t = new Toilet(info, i);
        				objects.add(t);
    				}else if(obj_type.equals("InfoStand")){
						InfoStand is = new InfoStand(info, i);
	    				objects.add(is);
    				}else if(obj_type.equals("FoodStand")){
						FoodStand fs = new FoodStand(info, i);	    				
	    				objects.add(fs);
    				}
    			}
    			for (int i = 0; i < objects.size(); i++) {
    				FestivalObject fo = objects.get(i);
    				int width = Integer.parseInt(fo.getObj_width());
    				int height = Integer.parseInt(fo.getObj_height());
    				new createEventObjectPoint(fo, width, height, i).execute();
    			}		
    		} catch (ClientProtocolException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		} catch (JSONException e) {
    			e.printStackTrace();
    		} catch (ParseException e1) {
				e1.printStackTrace();
			}
 
    		mMapView.zoomToScale(e.getCenter(), 1878.6649902747558);
    		level = 19;
            return "Executed";
        }
    }
}

