package cmu.costco.shoppinglist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import cmu.costco.shoppinglist.db.DatabaseAdaptor;
import cmu.costco.shoppinglist.objects.Customer;
import cmu.costco.shoppinglist.objects.ShoppingListItem;

public class ViewListActivity extends Activity implements LocationListener {

	private final static String TAG = "ViewListActivity";

	private DatabaseAdaptor db;
	private Customer cust;
	
	private EditText lat;
	private EditText lon;
	private EditText alertName;
	
	private Map<String,List<Double>> poi; 
	

	private LocationManager locationManager=null;
	
	private final float RADIUS = 2;
	private final long EXPIRATION = -1;
	
	private ArrayList<String> list;
	private ArrayAdapter<String> adapter;
	private ListView alertList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set the text view as the activity layout
		setContentView(R.layout.activity_view_list);

		// Open database
		db = new DatabaseAdaptor(this);
		db.open();

		// Get the message from the intent
		Intent intent = getIntent();
		int memberId = intent.getIntExtra(LoginActivity.MEMBERID, 1);

		// Load Customer and shoppingList from DB
		cust = db.dbGetCustomer(memberId);
		cust.setShoppingList(db.dbGetShoppingListItems(memberId));

		// Add list of ShoppingListItems
		ScrollView scroll = (ScrollView) findViewById(R.id.viewListScroll);
		LinearLayout itemList = generateListView(this, cust.getShoppingList());
		scroll.addView(itemList);
		
		poi = new HashMap<String,List<Double>>();
		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if(locationManager!=null)
		{
			locationManager.removeUpdates(this);
			locationManager = null;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		db.open();
	}

	/**
	 * Generate an Android View to display the ShoppingList
	 * 
	 * @param shoppingList
	 */
	private LinearLayout generateListView(Context ctx,
			Map<String, ArrayList<ShoppingListItem>> shoppingList) {
		// Create the view that will be returned
		LinearLayout view = new LinearLayout(ctx);
		view.setOrientation(LinearLayout.VERTICAL);

		if (shoppingList.size() == 0) {
			TextView emptyRow = new TextView(ctx);
			emptyRow.setTextSize(24);
			emptyRow.setText("No items in Shopping List");
			view.addView(emptyRow);
			return view;
		}

		// Iterate through each item category
		for (String category : shoppingList.keySet()) {
			Log.i(TAG, "Iterating through category '" + category + "'.");

			if (shoppingList.get(category).size() > 0) {
				// Generate the TextView row to display the category name
				TextView catRow = new TextView(ctx);
				catRow.setTextSize(18);
				catRow.setText(category);
				view.addView(catRow);

				// Iterate through each item within the category
				for (final ShoppingListItem item : shoppingList.get(category)) {
					Log.i(TAG, "    Item: " + item.getItem().getDescription()
							+ " - " + item.isChecked());
					view.addView(createItemCheckbox(ctx, item));
				}
			}
		}

		return view;
	}

	/**
	 * Create a checkbox/description row view to be added to the shopping list
	 * 
	 * @param ctx
	 * @param item
	 * @return CheckBox view
	 */
	private View createItemCheckbox(Context ctx, final ShoppingListItem item) {
		// Generate the CheckBox/text row for the item
		CheckBox checkbox = new CheckBox(ctx);
		checkbox.setText(item.getItem().getDescription());
		if (item.isChecked()) {
			checkbox.setChecked(true);
			checkbox.setPaintFlags(checkbox.getPaintFlags()
					| Paint.STRIKE_THRU_TEXT_FLAG);
		}

		// Create a listener to change the item state when checked
		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton checkBoxView,
					boolean isChecked) {
				Log.i(TAG, "Setting checkbox of item " + item.getItemId()
						+ " to " + isChecked);
				db.dbSetItemChecked(cust.getMemberId(), item.getItemId(),
						isChecked);
				if (isChecked) {
					checkBoxView.setPaintFlags(checkBoxView.getPaintFlags()
							| Paint.STRIKE_THRU_TEXT_FLAG);
				} else {
					checkBoxView.setPaintFlags(checkBoxView.getPaintFlags()
							& ~Paint.STRIKE_THRU_TEXT_FLAG);
				}
			}
		});

		return checkbox;
	}

	/**
	 * Switch to EditList activity
	 * 
	 * @param view
	 */
	public void editList(View view) {
		Intent intent = new Intent(this, EditListActivity.class);
		intent.putExtra(LoginActivity.MEMBERID, cust.getMemberId());
		startActivity(intent);
	}

	// Proximity Alert menu function
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_proximityalert, menu);
		return true;
	}

	// Add an item into the shopping list
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		if (item.getItemId() == R.id.AddProximityAlert) {
			setContentView(R.layout.proximityalert);
			
	    	alertList = (ListView)findViewById(R.id.alertList);
	    	list = new ArrayList<String>();
	    	adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
	    	alertList.setAdapter(adapter);
			
            //Register the listener with the Location Manager to receive location updates
			locationManager =
			        (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
			try {
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
    public boolean alertOnClick(View v) {
    	
//    	NotificationManager notificationManager = 
//                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//            Notification notification = createNotification();
//            
//            Intent intent = new Intent(ViewListActivity.this, ViewListActivity.class);
//            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//            
//        
//            notification.setLatestEventInfo(this, 
//            		"Test", "You are entering your point of interest.", pendingIntent);
//            notificationManager.notify(1, notification);
            
    	List<Double> coordinates = new ArrayList<Double>();
    	
    	this.lat = (EditText)findViewById(R.id.Latitude);		
		this.lon = (EditText)findViewById(R.id.Longitude);
		this.alertName = (EditText)findViewById(R.id.alertName);
		
		// check if alertName already exists
		if(poi.containsKey(alertName.getText().toString())) {
			Toast.makeText(this, "Name already exists", Toast.LENGTH_LONG).show();
			return false;
		}
    	
    	coordinates.add(Double.valueOf(this.lat.getText().toString()));
    	coordinates.add(Double.valueOf(this.lon.getText().toString()));
    	
    	poi.put(this.alertName.getText().toString(), coordinates);
    	
    	PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(this.alertName.getText().toString()), 0);
    	locationManager.addProximityAlert(coordinates.get(0), coordinates.get(1), RADIUS, EXPIRATION, pendingIntent);

    	IntentFilter intentFilter = new IntentFilter(this.alertName.getText().toString());
    	registerReceiver(new ProximityIntentReceiver(),intentFilter);
    	  
//    	alertList = (ListView)findViewById(R.id.alertList);
//    	list = new ArrayList<String>();
//    	adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
//    	alertList.setAdapter(adapter);
    	
    	list.add(this.alertName.getText().toString());
    	adapter.notifyDataSetChanged();

    	return false;
    }
    

    
    @Override
    public void onLocationChanged(Location location) 
    {
		// Get GPS coordinates
		lat = (EditText)findViewById(R.id.Latitude);		
		lon = (EditText)findViewById(R.id.Longitude);
		
		lat.setText(Double.toString(location.getLatitude()));
		lon.setText(Double.toString(location.getLongitude()));        	
    }

    @Override
    public void onProviderDisabled(String provider) 
    {
    	locationManager.removeUpdates(this);
    }

    @Override
    public void onProviderEnabled(String provider) 
    {  
    	
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) 
    {       
    }

    
    
}
