package crepetete.arcgis.evemapp;

import java.io.IOException;
import java.util.Arrays;
import org.apache.http.client.ClientProtocolException;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;

public class SelectionFragment extends Fragment{


	private static User user;
	private Session session;
	private static HttpManager hm;

	
	//Facebook's must-have in een app, This class helps to create, 
	//automatically open (if applicable), save, 
	//and restore the Active Session in a way that is similar to Android UI lifecycles. 
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
		
		//Solves NetworkOnMainThreadException
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);	
		
		session = new Session(getActivity());
		session.openForRead(new Session.OpenRequest(this).setCallback(callback)
				.setPermissions(Arrays.asList("friends_birthday")));	
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.selectionfragment, container, false);
		
		return view;
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
       super.onActivityCreated(savedInstanceState);
//       Intent myIntent = new Intent(getActivity(), MainMap.class);
//		getActivity().startActivity(myIntent);
   }
	
	// respond to session changes / call makeMeRequest() if session is open
	private void onSessionStateChange(final Session session, SessionState state, Exception exception)
			throws ClientProtocolException, IOException {
		// onLocationChanged(locationManager.getLastKnownLocation(provider));
		if (session != null && session.isOpened()) {		
			hm = new HttpManager();
			user = new User();
			makeMeRequest(session);
		} else if (!session.isOpened()) {
			System.out.println("no session");
		} else {
			System.out.println("session = null");
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
//							if (user.getMyId() != null) {
//								try {
//									hm.makeRegisterParams(user, getActivity());
									Intent myIntent = new Intent(getActivity(), MainMap.class);
									getActivity().startActivity(myIntent);
//								} catch (ClientProtocolException e) {
//									e.printStackTrace();
//								} catch (IOException e) {
//									e.printStackTrace();
//								}
//							}
						}
						if (response.getError() != null) {
							System.out.println(response.getError()
									.getErrorMessage());
						}
					}
				});
		request.executeAsync();
	}

	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();
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
	}
	public void onStatusChanged(String provider, int status, Bundle extras) {
	    // TODO Auto-generated method stub

	  }

	  public void onProviderEnabled(String provider) {
	    Toast.makeText(getActivity(), "Enabled new provider " + provider,
	        Toast.LENGTH_SHORT).show();

	  }

	  public void onProviderDisabled(String provider) {
	    Toast.makeText(getActivity(), "Disabled provider " + provider,
	        Toast.LENGTH_SHORT).show();
	  }
	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	public static User getUser() {
		return user;
	}

	public static HttpManager getHm() {
		return hm;
	}
	
	
};

