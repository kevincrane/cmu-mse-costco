package costco.cmu.simplifiedcheckout.web;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import costco.cmusimplifiedcheckout.web.R;

public class CashierActivity extends Activity {
	private final static String TAG = "CashierActivity";
	private final String API_GET_LOCATION = "/costco/api/order/";
	
	TextView shoppingListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cashier);
		// Show the Up button in the action bar.
		setupActionBar();
		
		// Set up TextView for customer's shopping list
		shoppingListView = (TextView)findViewById(R.id.cashierText);
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	
	// Receive NDEF message from CustomerActivity
	@Override
	public void onNewIntent(Intent intent) {
		// onResume gets called after this to handle the intent
		setIntent(intent);
	}


	/**
	 * Called on New Customer button; clears the TextView and sets Cashier as ready again
	 * @param view
	 */
	public void resetCustomer(View view) {
		shoppingListView.setText(R.string.cashierTextDefault);
	}
	
	/**
	 * Called on Retrieve List button; sends async GET request to retrieve a customer's shopping list
	 * @param view
	 */
	public void retrieveShoppingList(View view) {
		String customerIdText = ((EditText)findViewById(R.id.customerIdText)).getText().toString();
		if(customerIdText.isEmpty()) {
			Toast.makeText(this, "Please enter a customer ID.", Toast.LENGTH_LONG).show();
			return;
		}
		
		int customerId = Integer.parseInt(customerIdText);
		String apiPostAddress = "http://" + CustomerActivity.SERVER_IP + ":5000" + 
				API_GET_LOCATION + customerId;
		try {
			sendAsyncGetRequest(apiPostAddress, this);
		} catch(Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Something broked. :( \nCheck the looogs.", Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * Call an asynchronous GET request to the server to save a shopping list.
	 * @param requestUrl
	 * @param ctx
	 */
	private void sendAsyncGetRequest(String requestUrl, final Context ctx) {
		new AsyncTask<String, Void, JSONObject>() {
		    @Override
		    protected JSONObject doInBackground(String... urls) {
				JSONObject jsonObjRecv = HttpJsonClient.SendHttpGet(urls[0]);
				return jsonObjRecv;
		    }
		
		    @Override
		    protected void onPostExecute(JSONObject jsonObjRecv) {
		    	if(jsonObjRecv == null) {
		    		// Fail
		    		Toast.makeText(ctx, "Something broked. :( \nCheck the looogs.", Toast.LENGTH_LONG).show();
		    	} else {
		    		// Success
		    		//TODO: check to see if successful get; response code 201 in HttpJsonClient
			    	Log.i(TAG, "Flask Server Response!: " + jsonObjRecv.toString());
			    	ShoppingList shoppingList = createShoppingListFromJson(jsonObjRecv);
					shoppingListView.setText(shoppingList.toString());
					Toast.makeText(ctx, "Successfully retrieved shopping list from server!", Toast.LENGTH_LONG).show();
		    	}
		    }
		}.execute(requestUrl);
	}
	
	/**
	 * Convert the values in a JSONObject to a real ShoppingList
	 * @param jsonResponse
	 * @return
	 */
	private ShoppingList createShoppingListFromJson(JSONObject jsonResponse) {
		ShoppingList shoppingList = null;
		try {
			String customerName = jsonResponse.getString("customer");
			shoppingList = new ShoppingList(customerName);
			
			JSONArray orders = jsonResponse.getJSONArray("order");
			for(int i=0; i< orders.length(); i++) {
				JSONObject order = orders.getJSONObject(i);
				String name = order.getString("name");
				String upc = order.getString("upc");
				double price = order.getDouble("price");
				int quantity = order.getInt("quantity");
				shoppingList.addItem(new StoreItem(name, upc, price, quantity));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return shoppingList;
	}

}
