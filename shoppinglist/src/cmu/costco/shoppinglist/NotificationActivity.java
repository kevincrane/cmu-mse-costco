package cmu.costco.shoppinglist;

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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
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
	
	private LinearLayout alertListView;
	private Map<String, Category.Location> poi;
	private double currentLatitude;
	private double currentLongitude;
	
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
        alertListView = (LinearLayout)findViewById(R.id.alertList);
    	
    	// Add all existing proximity alerts to ListView
    	Map<String, Category.Location> proximityAlerts = db.dbGetProxAlerts();
    	for(String category : proximityAlerts.keySet()) {
    		alertListView.addView(createProxAlertRow(this, category, proximityAlerts.get(category)));
    	}
    	
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
    
    private View createProxAlertRow(final Context ctx, final String category, final Category.Location location) {
    	// Create the view that will be returned
        View view = getLayoutInflater().inflate(R.layout.activity_prox_alert_row, null);
        
        // Add Delete Proximity Alert listener
        Button deleteButton = (Button)view.findViewById(R.id.proxAlertDelete);
        deleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i("NotificationActivity", "Deleting proximity alert for category '" + category + "'.");
				db.dbDeleteProxAlert(category, location.getLat(), location.getLon());
				((ViewGroup)v.getParent()).removeAllViews();
				Toast.makeText(ctx, "Deleted proximity alert for category '" + 
						category + "'.", Toast.LENGTH_LONG).show();
			}
		});

        // Fill row with item text
        TextView text = (TextView)view.findViewById(R.id.proxAlertText);
		text.setText(category + " (" + location.getLat() + ", " + location.getLon()+ ")");
		
		// Return the generated row view
		return view;
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
    	alertListView.addView(createProxAlertRow(this, category, new Category.Location(latitude, longitude)));
    	
    	// Show alert onscreen to indicate to user that alert made successfully
    	Toast.makeText(this, "New alert created for '" + category + "' at ("
    			+ latitude + ", " + longitude + ")!", Toast.LENGTH_LONG).show();

    	return true;
    }
    
    
    /**
     * Set the input text boxes to the user's current GPS coords 
     * @param view
     */
    public void getCurrentLocation(View view) {
    	// Get GPS coordinates
		EditText lat = (EditText)findViewById(R.id.Latitude);		
		EditText lon = (EditText)findViewById(R.id.Longitude);
		
		if(currentLatitude != 0 && currentLongitude != 0) {
			lat.setText(currentLatitude + "");
			lon.setText(currentLongitude + "");
		} else {
			Toast.makeText(this, "Could not detect your location. Please wait until we can locate you " +
					"or move to somewhere with better signal.", Toast.LENGTH_LONG).show();
		}
    }
    
    
	@Override
	public void onLocationChanged(Location location) {
		currentLatitude = location.getLatitude();
		currentLongitude = location.getLongitude();
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