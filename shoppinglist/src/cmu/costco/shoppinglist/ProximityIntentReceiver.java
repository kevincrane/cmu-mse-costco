package cmu.costco.shoppinglist;

import java.util.ArrayList;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;
import cmu.costco.shoppinglist.db.DatabaseAdaptor;
import cmu.costco.shoppinglist.objects.Customer;
import cmu.costco.shoppinglist.objects.Item;
import cmu.costco.shoppinglist.objects.ShoppingListItem;

public class ProximityIntentReceiver extends BroadcastReceiver {

	private static final int NOTIFICATION_ID = 1000;
	
	private DatabaseAdaptor db;
	private Customer cust;

	@Override
	public void onReceive(Context context, Intent intent) {

		String key = LocationManager.KEY_PROXIMITY_ENTERING;
		String category = intent.getAction();

		Boolean entering = intent.getBooleanExtra(key, false);

		// Get the message from the intent
		int memberId = intent.getIntExtra(LoginActivity.MEMBERID, 1);

		// Open database
		db = new DatabaseAdaptor(context);
		db.open();

		Map<String, ArrayList<ShoppingListItem>> shoppingList = db.dbGetShoppingListItems(memberId);
		ArrayList<ShoppingListItem> listItems;
		String uncheckedItems = " ";

		if (shoppingList.containsKey(category)) {

			for (ShoppingListItem listItem : shoppingList.get(category)) {

				if (!listItem.isChecked()) {
					Item item = listItem.getItem();
					uncheckedItems = uncheckedItems + item.getDescription();
				}

			}
		}

		if (entering) {
			Log.d(getClass().getSimpleName(), "entering");
			Toast.makeText(context, category + " section entering. Unchecked items: " + uncheckedItems,
					Toast.LENGTH_LONG).show();

			NotificationManager notificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			Notification notification = createNotification();

			Intent viewIntent = new Intent(context, ViewListActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
					viewIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			notification.setLatestEventInfo(context, category
					+ " section entering. Unchecked items: " + uncheckedItems,
					"You are entering your point of interest.", pendingIntent);
			notificationManager.notify(NOTIFICATION_ID, notification);
		} else {
			Log.d(getClass().getSimpleName(), "exiting");
			Toast.makeText(context, category + " section exiting.",
					Toast.LENGTH_LONG).show();
			// notification.setLatestEventInfo(context,
			// category + " section entering",
			// "You are leaving your point of interest.", pendingIntent);
		}
	}

	private Notification createNotification() {
		Notification notification = new Notification();

		notification.icon = R.drawable.ic_launcher;
		notification.when = System.currentTimeMillis();

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;

		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.defaults |= Notification.DEFAULT_LIGHTS;

		notification.ledARGB = Color.WHITE;
		notification.ledOnMS = 1500;
		notification.ledOffMS = 1500;

		return notification;
	}

}
