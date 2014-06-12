package crepetete.arcgis.evemapp;

import java.util.ArrayList;
import java.util.List;

import com.facebook.widget.ProfilePictureView;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FriendsList extends Activity {

	private ProfilePictureView profilePictureView;
	private TextView userNameView;
	private Button backButton;
	private Button submit;
	private ListView friends;
	private List<Friend> friendsList;
	private ArrayList<String> friendsToDisplay;
	private ListAdapter adapter;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_friends_list);

		friends = (ListView) findViewById(R.id.friends_list);
		friendsList = (ArrayList<Friend>) getIntent().getSerializableExtra("friendList");
		friends.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		friendsList = (ArrayList<Friend>) getIntent().getSerializableExtra("friendsList");
		adapter = new FriendListAdapter(this, friendsList);
		friendsToDisplay = getIntent().getStringArrayListExtra("selectedFriendsList");
		friends.setAdapter(adapter);

		profilePictureView = (ProfilePictureView) findViewById(R.id.selection_profile_pic);
		profilePictureView.setCropped(true);

		userNameView = (TextView) findViewById(R.id.selection_user_name);
		backButton = (Button) findViewById(R.id.back);
		submit = (Button) findViewById(R.id.submit);
		
		profilePictureView.setProfileId(getIntent().getStringExtra("userId"));
		userNameView.setText(getIntent().getStringExtra("userName"));
		backButton.setOnClickListener(backHandler);
		submit.setOnClickListener(submitHandler);	
		friends.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
				CheckBox cb = (CheckBox) v.findViewById(R.id.checkBox1);
				System.out.println("clicked, cb is " + cb.isChecked());
				if (!cb.isChecked()){
					cb.setChecked(true);
					friends.setItemChecked(position, true);
				}else{
					cb.setChecked(false);
					friends.setItemChecked(position, false);
					friendsToDisplay.remove(friendsList.get(position).getId());
				}
			}

	    
	    });
	}

	// Adapter to put the information from the friendsList in the listView. Info
	// found during practicing with the Facebook SDK tutorial
	public class FriendListAdapter extends ArrayAdapter<Friend> {
		public FriendListAdapter(Context context, List<Friend> friendsList) {
			super(context, R.layout.friendlistitem, friendsList);
			for (int i = 0; i < friendsList.size(); i++) {
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
			if (f != null) {
				TextView name = (TextView) view.findViewById(R.id.name);
				ProfilePictureView picture = (ProfilePictureView) view.findViewById(R.id.friend_profile_pic);
				CheckBox cb = (CheckBox) view.findViewById(R.id.checkBox1);
				if(friendsToDisplay != null && friendsToDisplay.contains(f.getId())){
					if(name.getText()== null || name.getText().equals("")){
						cb.setChecked(true);
						friends.setItemChecked(position, true);
					}
				}
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
	
	View.OnClickListener backHandler = new View.OnClickListener() {
	    public void onClick(View v) {
	    	finish();
	    }
	};
	
	View.OnClickListener submitHandler = new View.OnClickListener() {
	    public void onClick(View v) {
	    	SparseBooleanArray checked = friends.getCheckedItemPositions();
	        ArrayList<Friend> selectedItems = new ArrayList<Friend>();
	        for (int i = 0; i < checked.size(); i++) {
	            int position = checked.keyAt(i);
	            if (checked.valueAt(i)){
					selectedItems.add(friendsList.get(position));     
					if(friendsToDisplay == null){
						friendsToDisplay = new ArrayList<String>();
					}
					if(!friendsToDisplay.contains(friendsList.get(position).getId())){
						friendsToDisplay.add(friendsList.get(position).getId());
					}
	            }
	        }
	 
	        ArrayList<String> outputStrArr = new ArrayList<String>();
	 
	        for (int i = 0; i < selectedItems.size(); i++) {
	            outputStrArr.add(selectedItems.get(i).getId());
	        }
	 
	        Intent resultIntent = new Intent();
    		   resultIntent.putStringArrayListExtra("friendsId", outputStrArr);
    		   resultIntent.putStringArrayListExtra("friendsToDisplay", friendsToDisplay);
    		   setResult(Activity.RESULT_OK, resultIntent);
    		   finish();
	    }
};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.friends_list, menu);
		return true;
	}

}
