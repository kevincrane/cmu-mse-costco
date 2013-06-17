package cmu.costco.simplifiedcheckout.nfc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CustomerActivity extends Activity implements CreateNdefMessageCallback, 
		OnNdefPushCompleteCallback {

	private final static String TAG = "CustomerActivity";
	private ShoppingList shoppingList;
	NfcAdapter nfcAdapter;
	private static final int MESSAGE_SENT = 1;
	
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
		
		// Add all items in shopping cart to view
		updateShoppingListView();
		
		// Set up NFC Adapter
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (nfcAdapter == null) {
			Toast.makeText(this, "NFC is not available on this device. :(", Toast.LENGTH_LONG).show();
			return;  // NFC not available on this device
		}
		nfcAdapter.setNdefPushMessageCallback(this, this);
		nfcAdapter.setOnNdefPushCompleteCallback(this, this);
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
	 * Convert a serializable object 
	 * @param object
	 * @return
	 * @throws IOException
	 */
	private byte[] serializeObject(Object object) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		byte[] serialized;
		try {
			out = new ObjectOutputStream(bos);   
			out.writeObject(object);
			serialized = bos.toByteArray();
		} finally {
			out.close();
			bos.close();
		}
		return serialized;
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
		Toast.makeText(this, "This doesn't really do anything yet. :/", Toast.LENGTH_LONG).show();
	}

	
// NDEF Stuff
	
	/**
	 * Generate new Ndef Message to send to whoever wants to listen
	 */
	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
        byte[] byteMessage;
		try {
			byteMessage = serializeObject(shoppingList);
		} catch (IOException e) {
			Log.e(TAG, "Error: could not serialize ShoppingList properly; " + e.getMessage());
			byteMessage = new byte[1];
		}
        NdefMessage msg = new NdefMessage(NdefRecord.createMime(
        		"application/cmu.costco.simplifiedcheckout.nfc", byteMessage)
//        		,NdefRecord.createApplicationRecord("cmu.costco.simplifiedcheckout.nfc")
        );
        Log.i(TAG, msg.getRecords()[0].toString());
        return msg;
	}

	@Override
	public void onNdefPushComplete(NfcEvent event) {
		// A handler is needed to send messages to the activity when this
        // callback occurs, because it happens from a binder thread
        mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
	}
	
	/** This handler receives a message from onNdefPushComplete */
    @SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_SENT:
                Toast.makeText(getApplicationContext(), "Message sent!", Toast.LENGTH_LONG).show();
                break;
            }
        }
    };
    
}