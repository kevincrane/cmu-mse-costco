package costco.cmu.simplifiedcheckout.web;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import costco.cmusimplifiedcheckout.web.R;

public class CustomerActivity extends Activity {

	private final static String TAG = "CustomerActivity";
	private ShoppingList shoppingList;
	private String customerName;
	private final String SERVER_IP = "128.237.126.157";
	private final String API_POST_LOCATION = "/costco/api/order";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_customer);
		// Show the Up button in the action bar.
		setupActionBar();
		
		// Set up ShoppingList with dummy data
		shoppingList = new ShoppingList("Kevin");
		shoppingList.addItem(new StoreItem("Couch cushions", "184930574960", 37.50, 3));
		shoppingList.addItem(new StoreItem("Pre-made sandwichs", "185038672860", 5.37, 13));
		shoppingList.addItem(new StoreItem("Woolen jackets aplenty", "123456789048", 68, 1));
		
		// Initialize demo customer name
		customerName = "Customer" + (System.currentTimeMillis() % 1000);
		
		// Add all items in shopping cart to view
		updateShoppingListView();
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	/**
	 * Update the shopping list view (LinearLayout) in the customer mode
	 */
	public void updateShoppingListView() {
		LinearLayout itemList = (LinearLayout)findViewById(R.id.itemListView);
		itemList.removeAllViews();
		for(int i=0; i<shoppingList.size(); i++) {
			StoreItem item = shoppingList.get(i);
			TextView text = new TextView(this);
			String rowText = (i+1) + ". $" + item.price + "\t " + item.quantity + "x " + item.name;
			text.setText(rowText);
			itemList.addView(text);
		}
	}
	
	/**
	 * Called when the Add To Cart button is pressed; adds new StoreItem to ShoppingList
	 * @param view
	 */
	public void addToCart(View view) {
		double price;
		int quantity;
		// Find EditText views
		EditText productText = (EditText)findViewById(R.id.prodNameText);
		EditText upcText = (EditText)findViewById(R.id.upcText);
		EditText priceText = (EditText)findViewById(R.id.priceText);
		EditText quantityText = (EditText)findViewById(R.id.quantityText);
		
		// Get text from all 4 text boxes about the product
		String product = productText.getText().toString();
		String upc = upcText.getText().toString();
		try {
			price = Double.parseDouble(priceText.getText().toString());
		} catch(NumberFormatException e) {
			price = 0.0;
		}
		try {
			quantity = Integer.parseInt(quantityText.getText().toString());
		} catch(NumberFormatException e) {
			quantity = 1;
		}
		
		// Add item to shopping list and update list view
		shoppingList.addItem(new StoreItem(product, upc, price, quantity));
		updateShoppingListView();
		
		// Clear EditText views
		productText.setText("");
		upcText.setText("");
		priceText.setText("");
		quantityText.setText("");
	}
	
	/**
	 * Called when Send to Cashier button is pressed; activates NFC transmit
	 * @param view
	 */
	public void broadcastShoppingList(View view) {
		Map<String, Object> orderMap = new HashMap<String, Object>();
		orderMap.put("customer", customerName);
		orderMap.put("order", shoppingListToArray(shoppingList));
		Log.d(TAG, "Created JSON Object: " + orderMap.toString());
		
		// Try to make an HTTP JSON Post
		String apiPostAddress = "http://" + SERVER_IP + ":5000" + API_POST_LOCATION; 
		try {
			JSONObject jsonObjSend = getJsonObjectFromMap(orderMap);
			sendAsyncJsonRequest(apiPostAddress, jsonObjSend, this);
		} catch(Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Something broked. :( \nCheck the looogs.", Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * Convert the shopping list to a JSONArray
	 * @param list
	 * @return
	 */
	private JSONArray shoppingListToArray(ShoppingList list) {
		JSONArray jsonListArray = new JSONArray();
		for(int i=0; i<list.size(); i++) {
			JSONObject jsonItemMap = new JSONObject();
			try {
				jsonItemMap.put("upc", list.get(i).upc);
				jsonItemMap.put("quantity", list.get(i).quantity);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        jsonListArray.put(jsonItemMap);
	    }
	    return jsonListArray;
	}
	
	
	private void sendAsyncJsonRequest(String requestUrl, final JSONObject jsonObjSend, final Context ctx) {
		new AsyncTask<String, Void, JSONObject>() {
		    @Override
		    protected JSONObject doInBackground(String... urls) {
				JSONObject jsonObjRecv = HttpJsonClient.SendHttpPost(urls[0], jsonObjSend);
				return jsonObjRecv;
		    }
		
		    @Override
		    protected void onPostExecute(JSONObject jsonObjRecv) {
		    	if(jsonObjRecv == null) {
		    		Toast.makeText(ctx, "Something broked. :( \nCheck the looogs.", Toast.LENGTH_LONG).show();
		    	} else {
		    		//TODO: check to see if successful post; response code 201 in HttpJsonClient
			    	Log.i(TAG, "Flask Server Response!: " + jsonObjRecv.toString());
					Toast.makeText(ctx, "Successfully sent shopping list to server!", Toast.LENGTH_LONG).show();
		    	}
		    }
		}.execute(requestUrl);
	}
	
	
	private static JSONObject getJsonObjectFromMap(Map<String, Object> params) throws JSONException {
	    Iterator<Entry<String, Object>> iter = params.entrySet().iterator();

	    //Stores JSON
	    JSONObject holder = new JSONObject();

	    while (iter.hasNext()) {
	        Entry<String, Object> pair = iter.next();
			holder.put((String)pair.getKey(), pair.getValue());
	    }
	    return holder;
	}

}