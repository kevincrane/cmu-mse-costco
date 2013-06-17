package cmu.costco.simplifiedcheckout.nfc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class CashierActivity extends Activity  {
	private final static String TAG = "CashierActivity";
	private ShoppingList shoppingList;
	
	TextView shoppingListView;
	boolean readyForCustomer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cashier);
		// Show the Up button in the action bar.
		setupActionBar();
		
		// Set up TextView for customer's shopping list
		shoppingListView = (TextView)findViewById(R.id.cashierText);
		readyForCustomer = true;
		
//		// Set up NFC Adapter
//		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
//		if (nfcAdapter == null) {
//			Toast.makeText(this, "NFC is not available on this device. :(", Toast.LENGTH_LONG).show();
//			return;  // NFC not available on this device
//		}
//		nfcAdapter.setNdefPushMessageCallback(this, this);
//		nfcAdapter.setOnNdefPushCompleteCallback(this, this);
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
	
	
	// Receive NDEF message from CustomerActivity
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
		Log.i(TAG, "Received an NFC message!!!");
		
		// If the cashier is ready, accept the NDEF message as a stream of bytes
		if(readyForCustomer) {
			// only one message sent during the beam
			NdefMessage msg = (NdefMessage)rawMsgs[0];
			readyForCustomer = false;
			// record 0 contains the MIME type, record 1 is the AAR, if present
			byte[] response = msg.getRecords()[0].getPayload();
			try {
				// Deserialize the byte[] and cast it as a ShoppingList
				ShoppingList shoppingList = (ShoppingList)deserializeObject(response);
				if(shoppingList == null) {
					shoppingListView.setText("Error: I was not able to read a ShoppingList from that message. :(");
					return;
				}
				
				// Success. Set the text to show the shopping list
				shoppingListView.setText(shoppingList.toString());
			} catch(IOException e) {
				// Handle errors
				shoppingListView.setText("Error: I was not able to read a ShoppingList from that message. :(" +
						"\n\n" + e.getMessage());
			}
		}
	}
	
	
	/**
	 * Called on New Customer button; clears the TextView and sets Cashier as ready again
	 * @param view
	 */
	public void resetCustomer(View view) {
		shoppingListView.setText(R.string.cashierTextDefault);
		readyForCustomer = true;
	}
	
	/**
	 * Read a byte[] and deserialize it back into an object
	 * @param byteInput
	 * @return
	 * @throws IOException
	 */
	private Object deserializeObject(byte[] byteInput) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(byteInput);
		ObjectInput in = null;
		Object deserialized = null;
		try {
			in = new ObjectInputStream(bis);
			deserialized = in.readObject(); 
		} catch (ClassNotFoundException e) {
		} finally {
			bis.close();
			in.close();
		}
		return deserialized;
	}
}
