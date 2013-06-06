package cmu.costco.shoppinglist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import cmu.costco.shoppinglist.db.DatabaseAdaptor;
import cmu.costco.shoppinglist.objects.Category;

/**
 * Notification activity
 * @author A.Samiyev
 *
 */
public class NotificationActivity extends Activity implements LocationListener {

	private LocationManager locationManager;
	
	private ListView alertListView;
	private ArrayList<String> alertList;
	private ArrayAdapter<String> adapter;
	
	private Map<String, Category.Location> poi;
	
	private final float RADIUS = 2;
	private final int EXPIRATION = -1;
	
	private DatabaseAdaptor db;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        
        // Open database
 		db = new DatabaseAdaptor(this);
 		db.open();
        
        // Populate Alert Category spinner with possible categories
        Spinner spinner = (Spinner)findViewById(R.id.alertCategory);
        ArrayAdapter<CharSequence> catAdapter = ArrayAdapter.createFromResource(this,
        		R.array.categories, android.R.layout.simple_spinner_item);
		catAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
		spinner.setAdapter(catAdapter);
        
		
        poi = new HashMap<String, Category.Location>();
        
        // Create adapter for alertList that allows us to display all the current alerts
        alertListView = (ListView)findViewById(R.id.alertList);
        alertList = new ArrayList<String>();
    	adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, alertList);
    	alertListView.setAdapter(adapter);
    	
    	// Add all existing proximity alerts to ListView
    	Map<String, Category.Location> proximityAlerts = db.dbGetProxAlerts();
    	for(String category : proximityAlerts.keySet()) {
    		alertList.add(category + " - (" + proximityAlerts.get(category).getLat() + 
    				", " + proximityAlerts.get(category).getLon()+ ")");
    	}
    	adapter.notifyDataSetChanged();
    	
    	//Register the listener with the Location Manager to receive location updates
		locationManager =
		        (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		try {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		}
		catch (Exception e) {
			//FIXME: don't catch generic Exceptions. Identify what type can be thrown. Good coding practice.
			Log.e(getClass().getSimpleName(), e.getMessage());
		}
    }
    
    @Override
	public void onPause() {
		super.onPause();
		if(locationManager!=null) {
			locationManager.removeUpdates(this);
			locationManager = null;
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_notification, menu);
        return true;
    }
    
    
    /**
     * Called on "Add Alert" button press
     * @param v
     * @return
     */
    public boolean alertOnClick(View v) {
    	double latitude, longitude; 
    	EditText lat = (EditText)findViewById(R.id.Latitude);		
		EditText lon = (EditText)findViewById(R.id.Longitude);
		Spinner alertCategory = (Spinner)findViewById(R.id.alertCategory);
		String category = alertCategory.getSelectedItem().toString();
		
		// check if alertCategory already exists
		if(poi.containsKey(category)) {
			Toast.makeText(this, "Name already exists", Toast.LENGTH_LONG).show();
			return false;
		}
		
    	try {
			latitude = Double.parseDouble(lat.getText().toString());
			longitude = Double.parseDouble(lon.getText().toString());
    	} catch(NumberFormatException e) {
    		Log.e("scanner", "Error parsing latitude and longitude as numbers.");
    		return false;
    	}
    	
    	poi.put(category, new Category.Location(latitude, longitude));
    	
    	ShoppingListApplication application = (ShoppingListApplication)getApplication();
    	application.addProximityAlert(latitude, longitude, RADIUS, 
    			EXPIRATION, category);
    	
    	// Add new alert to the DB
    	db.dbCreateAlert(category, latitude, longitude);
    	alertList.add(category + " - (" + latitude + ", " + longitude + ")");
    	adapter.notifyDataSetChanged();
    	
    	// Show alert onscreen to indicate to user that alert made successfully
    	Toast.makeText(this, "New alert created for '" + category + "' at ("
    			+ latitude + ", " + longitude + ")!", Toast.LENGTH_LONG).show();

    	return true;
    }
    
    
//    TODO:
//    	BONUS POINTS: X BUTTON TO DELETE ALERT FROM DB; look at how editlistactivity and its special row did it



	@Override
	public void onLocationChanged(Location location) {
		// Get GPS coordinates
		EditText lat = (EditText)findViewById(R.id.Latitude);		
		EditText lon = (EditText)findViewById(R.id.Longitude);
		
		lat.setText(Double.toString(location.getLatitude()));
		lon.setText(Double.toString(location.getLongitude()));        	
	}
	
	@Override
	public void onProviderDisabled(String provider) {
		locationManager.removeUpdates(this);
	}
	
	@Override
	public void onProviderEnabled(String provider) {}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}
    
    
} 