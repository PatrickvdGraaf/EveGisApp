package crepetete.arcgis.evemapp;

import java.util.ArrayList;
import java.util.List;

import com.facebook.widget.ProfilePictureView;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FriendsList extends Activity {

	private ProfilePictureView profilePictureView;
	private TextView userNameView;
	private ListView friends;
	private List<Friend> friendsList;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_friends_list);

		friends = (ListView) findViewById(R.id.friends_list);
		friendsList = (ArrayList<Friend>) getIntent().getSerializableExtra("friendList");
		
		friends.setAdapter(new FriendListAdapter(this, friendsList));

		profilePictureView = (ProfilePictureView) findViewById(R.id.selection_profile_pic);
		profilePictureView.setCropped(true);

		userNameView = (TextView) findViewById(R.id.selection_user_name);
		
		profilePictureView.setProfileId(getIntent().getStringExtra("userId"));
		userNameView.setText(getIntent().getStringExtra("userName"));

	}

	// Adapter to put the information from the friendsList in the listView. Info
	// found during practicing with the Facebook SDK tutorial
	public class FriendListAdapter extends ArrayAdapter<Friend> {
		@SuppressWarnings("unchecked")
		public FriendListAdapter(Context context, List<Friend> friendsList) {
			super(context, R.layout.friendlistitem, friendsList);
			friendsList = (ArrayList<Friend>) getIntent().getSerializableExtra("friendList");
			for (int i = 0; i < friendsList.size(); i++) {
				System.out.println("name: " + friendsList.get(i).getName());
				friendsList.get(i).setAdapter(this);
			}
		}
		@Override
		// Creating the View for an item in the ListView
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.friendlistitem, null);
			}
			// Hier haal ik de Friend objecten uit de friendsList en stop ze per
			// object in de listview
			Friend f = friendsList.get(position);
			System.out.println("name: " + f.getName());
			if (f != null) {
				TextView name = (TextView) view.findViewById(R.id.name);
				ProfilePictureView picture = (ProfilePictureView) view.findViewById(R.id.friend_profile_pic);
				if (name != null) {
					name.setText(f.getName());
				}
				if (picture != null) {
					picture.setProfileId(f.getId());
				}
			}
			return view;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.friends_list, menu);
		return true;
	}

}
