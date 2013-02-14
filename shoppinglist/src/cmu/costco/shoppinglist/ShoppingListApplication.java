package cmu.costco.shoppinglist;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;

/**
 * Application class that initiates LocationManager 
 * and NotificatinReceiver objects.
 * 
 * @author THUNDER-AZA
 *
 */
public class ShoppingListApplication extends Application {

	private LocationManager locationManager;
	
	private ProximityIntentReceiver notificationReceiver;
	
	@Override
	public void onCreate() {
		
		/* Register the listener with the Location Manager to receive location updates */
		locationManager =
		        (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		
		notificationReceiver = new ProximityIntentReceiver();
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
		
		
		IntentFilter intentFilter = new IntentFilter(message);
		registerReceiver(notificationReceiver, intentFilter);
		
	}
	
	
}
