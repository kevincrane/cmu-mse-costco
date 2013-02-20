package cmu.costco.shoppinglist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import android.widget.Toast;

/**
 * Notification activity
 * @author A.Samiyev
 *
 */
public class NotificationActivity extends Activity implements LocationListener {

	private LocationManager locationManager;
	
	private EditText lat;
	private EditText lon;
	private EditText alertName;
	
	
	private ListView alertList;
	private ArrayList<String> list;
	private ArrayAdapter<String> adapter;
	
	
	private Map<String,List<Double>> poi;
	
	private final float RADIUS = 2;
	private final int EXPIRATION = -1;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        
        poi = new HashMap<String,List<Double>>();
        
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
			//FIXME: don't catch generic Exceptions. Identify what type can be thrown. Good coding practice.
			Log.e(getClass().getSimpleName(), e.getMessage());
		}
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_notification, menu);
        return true;
    }
    
    
    public boolean alertOnClick(View v) {
    	
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
    	
    	ShoppingListApplication application = 
    			(ShoppingListApplication) getApplication();
    	application.addProximityAlert(coordinates.get(0), coordinates.get(1), 
    			RADIUS, EXPIRATION, this.alertName.getText().toString());
    	
   	
    	list.add(this.alertName.getText().toString());
    	adapter.notifyDataSetChanged();

    	return false;
    }



	@Override
	public void onLocationChanged(Location location) {
		// Get GPS coordinates
		lat = (EditText)findViewById(R.id.Latitude);		
		lon = (EditText)findViewById(R.id.Longitude);
		
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