package crepetete.arcgis.evemapp;

import com.facebook.Request;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.*;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;


public class FbLoginMainActivity extends FragmentActivity{

	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  setContentView(R.layout.facebooklogin);

	  // start Facebook Login
	  Session.openActiveSession(this, true, new Session.StatusCallback() {

	    // callback when session changes state
	    public void call(Session session, SessionState state, Exception exception) {
	    	if (session.isOpened()) {
	    		// make request to the /me API
	    		Request.newMeRequest(session, new Request.GraphUserCallback() {

	    		  // callback after Graph API response with user object
	    		  public void onCompleted(GraphUser user, Response response) {
	    		  }
	    		}).executeAsync();
	    	}
	    }
	  });
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	  super.onActivityResult(requestCode, resultCode, data);
	  Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}
	
}
