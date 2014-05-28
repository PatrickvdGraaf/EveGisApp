package crepetete.arcgis.evemapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.Request.GraphUserListCallback;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.ProfilePictureView;

public class Login extends Activity {
	private GraphUser user;
	private LoginButton loginbut;
	private ProfilePictureView profilePictureView;
	private TextView username;
	private ProgressBar spinner;
	private UiLifecycleHelper uihelper;
	private ArrayList<Friend> friendsList;
	private boolean meRequestCompleted = false;
	private boolean friendRequestCompleted = false;
	
	private Session.StatusCallback callback = new Session.StatusCallback() {
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Remove title bar
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);
		spinner = (ProgressBar) findViewById(R.id.spinner);
		loginbut = (LoginButton) findViewById(R.id.login_button);
		loginbut.setVisibility(View.GONE);
		profilePictureView = (ProfilePictureView) findViewById(R.id.selection_profile_pic);
		profilePictureView.setVisibility(View.GONE);
		username = (TextView) findViewById(R.id.selection_user_name);
		uihelper = new UiLifecycleHelper(this, callback);
		uihelper.onCreate(savedInstanceState);
		
		Session session = new Session(getApplicationContext());
		Session.setActiveSession(session);
		session.openForRead(new Session.OpenRequest(this).setCallback(callback).setPermissions());

		
		if (loginbut != null) {
			loginbut.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {

				public void onUserInfoFetched(GraphUser user) {
					Login.this.user = user;
					updateprofile();
				}
			});
		} else {
			System.out.println("huh");
		}
	}

	private void updateprofile() {

		Session session = Session.getActiveSession();
		boolean validsession = session != null && session.isOpened();
		if (validsession && user != null) {
			makeMeRequest(session);
			getFriends();
		}
	}

	private void makeMeRequest(final Session session) {
		Request request = Request.newMeRequest(session,
				new Request.GraphUserCallback() {


					public void onCompleted(GraphUser user, Response response) {
						if (user.getId() != null && user.getName() != null) {
								meRequestCompleted = true;
								profilePictureView.setProfileId(user.getId());
								if (friendRequestCompleted){
									startMainMap(user);
								}
						}else {
							System.out.println("wat");
						}
					}
				});
		request.executeAsync();
	}
	
	//Getting friend information. (deels gevonden op http://stackoverflow.com/questions/13957181/get-friend-list-facebook-3-0)
		private void getFriends(){
		        Request friendRequest = Request.newMyFriendsRequest(Session.getActiveSession(), new GraphUserListCallback(){
		                public void onCompleted(List<GraphUser> users,Response response) {
		                	//als de request gelukt is, maak ik er iets makkelijk leesbaars van
		                	friendsList = parseUserFromFQLResponse(response);
		                	//om het netjes te houden heb ik ervoor gekozen om de namen te sorteren (http://stackoverflow.com/questions/19471005/sorting-an-arraylist-of-objects-alphabetically)
		                	Collections.sort(friendsList, new Comparator<Friend>() {
		                	    public int compare(Friend v1, Friend v2) {
		                	        return v1.getName().compareTo(v2.getName());
		                	    }
		                	});
		                	friendRequestCompleted = true;
		                	if (meRequestCompleted){
								startMainMap(user);
							}
		                }
		        });
		        Bundle params = new Bundle();
		        //http://stackoverflow.com/questions/13984873/android-how-to-get-friends-birth-dates-from-facebook-calendar-using-graph-api om de juiste params te vinden.
		        //de parames houden in welke info opgehaald wordt.
		        params.putString("fields", "birthday, name, id, work");
		        friendRequest.setParameters(params);
		        friendRequest.executeAsync();
		}
	
		//hier parse ik de response van de request naar info van vrienden. (geholpen door http://stackoverflow.com/questions/14422718/how-to-read-fql-response-in-android)
		private  ArrayList<Friend> parseUserFromFQLResponse( Response response ){
			ArrayList<Friend> friendsList = new ArrayList<Friend>();
		    try
		    {
		        GraphObject go  = response.getGraphObject();
		        JSONObject  jso = go.getInnerJSONObject();
		        JSONArray   arr = jso.getJSONArray( "data" );
		        //voor elke gevonden persoon
		        for ( int i = 0; i < ( arr.length() ); i++ )
		        {
		            JSONObject json_obj = arr.getJSONObject( i );
		            //als er info meegegeven is, sla het op en stop het in een Friend object, die op zijn beurt weer in een friendList wordt gestopt,
		            //zodat de list later kan worden gesorteerd en er makkelijker een adapter voor gemaakt kan worden om de info in de ListView te zetten.
		            //als de info niet ingevoerd is door de vriend of niet is opgehaald, zal de app 'undefined' aangeven voor naam en verjaardag. Voor werk geeft hij '' weer,
		            //omdat iemand geen werk kan hebben, maar niemand heeft geen naam of verjaardag.
		            String id = "undefined";
		            String name   = "undefined";
		            String birthday = "undefined";
		            String work = "";
		            
		            if(json_obj.has("id")){
		            	id = json_obj.getString("id");
		            }
		            
		            if(json_obj.has("name")){
		            	name   = json_obj.getString( "name" );
		            	System.out.println(name	);
		            }
		            
		            if(json_obj.has("birthday")){
		            	birthday   = json_obj.getString( "birthday" );
		            }
		            
		            friendsList.add(new Friend(name, birthday, id, work));
		        }
		    }
		    //Error catching
		    catch ( Throwable t )
		    {
		    	System.out.println("Something went wrong");
		        t.printStackTrace();
		    }
			return friendsList;
		}

	private void startMainMap(GraphUser user) {
		Intent myIntent = new Intent(Login.this, MainMap.class);
		myIntent.putExtra("id", user.getId()); 
		myIntent.putExtra("birthday", user.getBirthday());
		myIntent.putExtra("name", user.getName());
		myIntent.putExtra("friendList", friendsList);
		loginbut.setVisibility(View.VISIBLE);
		profilePictureView.setVisibility(View.VISIBLE);
        username.setText(user.getName());
        spinner.setVisibility(View.GONE);
		Login.this.startActivity(myIntent);	
	}

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (state.isOpened()) {
	        Log.i("", "Logged in...");
	    } else if (state.isClosed()) {
	        Log.i("", "Logged out...");
	    }
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uihelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		uihelper.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		uihelper.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		uihelper.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uihelper.onSaveInstanceState(outState);
	}
}