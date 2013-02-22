package cmu.costco.shoppinglist;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.util.Log;

/**
 * Application class that initiates LocationManager 
 * and NotificationReceiver objects.
 * 
 * @author THUNDER-AZA
 *
 */
public class ShoppingListApplication extends Application {

	private final static String TAG = "ShoppingListApplication";
	
	private LocationManager locationManager;
	private ProximityIntentReceiver notificationReceiver;
	private List<PendingIntent> proxAlertIntents;
	
	@Override
	public void onCreate() {
		/* Register the listener with the Location Manager to receive location updates */
		locationManager =
		        (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		notificationReceiver = new ProximityIntentReceiver();
		
		proxAlertIntents = new ArrayList<PendingIntent>();
	}
	
	/**
	 * Add a new proximity alert.
	 * @param latitude
	 * @param longitude
	 * @param radius
	 * @param message
	 */
	public void addProximityAlert(double latitude, double longitude, 
			float radius, int expiration, String message) {
		
		PendingIntent pendingIntent = 
				PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(message), 0);
		locationManager.addProximityAlert(
				latitude, longitude, radius, expiration, pendingIntent);
		proxAlertIntents.add(pendingIntent);
		
		IntentFilter intentFilter = new IntentFilter(message);
		registerReceiver(notificationReceiver, intentFilter);
		
	}
	

	public void removeProximityAlert() {
		//TODO: Create method to remove specific proximity alerts; javadoc
	}
	
	/**
	 * Remove all proximity alerts that have currently been created
	 */
	public void removeAllProximityAlerts() {
		for(PendingIntent pendingIntent : proxAlertIntents) {
			locationManager.removeProximityAlert(pendingIntent);
			Log.i(TAG, "Removing proximity alert " + pendingIntent.toString());
		}
	}
	
	
}
