package cmu.costco.simplifiedcheckout.nfc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.NavUtils;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CustomerActivity extends Activity implements CreateNdefMessageCallback, 
		OnNdefPushCompleteCallback {

	private final static String TAG = "CustomerActivity";
	private ShoppingList shoppingList;
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
		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (nfcAdapter == null) {
			Toast.makeText(this, "NFC is not available on this device. :(", Toast.LENGTH_LONG).show();
			return;  // NFC not available on this device
		}
		nfcAdapter.setNdefPushMessageCallback(this, this);
		nfcAdapter.setOnNdefPushCompleteCallback(this, this);

		
		//TODO:
		//	Make broadcast function (look at how NFC works and do that)
		//	Make Cashier one (when they touch, receive serialized ShoppingList, deserialize it, print as string)
	}
	
	@Override
    public void onResume() {
		super.onResume();
		// Check to see that the Activity started due to an Android Beam
		if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			processIntent(getIntent());
		}
    }


	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.customer, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
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
		//TODO: also clear all the text from each box after entering
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
		Time time = new Time();
		time.setToNow();
        String text = ("Beam me up!\n\n" +
                "Beam Time: " + time.format("%H:%M:%S"));
        
        NdefMessage msg = new NdefMessage(NdefRecord.createMime(
        		"application/cmu.costco.simplifiedcheckout.nfc", text.getBytes())
//        		,NdefRecord.createApplicationRecord("cmu.costco.simplifiedcheckout.nfc")
        );
        return msg;
	}
	
	@Override
	public void onNewIntent(Intent intent) {
		// onResume gets called after this to handle the intent
		setIntent(intent);
	}

	/**
	 * Parses the NDEF Message from the intent and prints to the TextView
	 */
	void processIntent(Intent intent) {
		Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
				NfcAdapter.EXTRA_NDEF_MESSAGES);
		// only one message sent during the beam
		NdefMessage msg = (NdefMessage) rawMsgs[0];
		// record 0 contains the MIME type, record 1 is the AAR, if present
		String response = new String(msg.getRecords()[0].getPayload());
		Toast.makeText(this, response, Toast.LENGTH_LONG).show();
//		new AlertDialog.Builder(CustomerActivity.this).setTitle(response).create().show();
	}

	@Override
	public void onNdefPushComplete(NfcEvent event) {
		// A handler is needed to send messages to the activity when this
        // callback occurs, because it happens from a binder thread
        mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
	}
	
	/** This handler receives a message from onNdefPushComplete */
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
