package cmu.costco.shoppinglist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import cmu.costco.shoppinglist.db.DatabaseAdaptor;

public class LoginActivity extends Activity {
	
	private DatabaseAdaptor dbHelper;
	public final static String MEMBERID = "MEMBERID";
	
	private static final String TAG = "LoginActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		dbHelper = new DatabaseAdaptor(this);
		dbHelper.open();
		
		setContentView(R.layout.activity_login);
    }
    
	@Override
	public void onResume() {
		super.onResume();
		dbHelper.open();
	}
    
    @Override
    public void onStop() {
    	super.onStop();
    	dbHelper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_login, menu);
        return true;
    }
    
    
    /** Called when the user clicks the Login button */
    public void login(View view) {
		Intent intent = new Intent(this, ViewListActivity.class);
		EditText emailText = (EditText) findViewById(R.id.loginEmailInput);
		String email = emailText.getText().toString();
		EditText pwordText = (EditText) findViewById(R.id.loginPwordInput);
		String password = pwordText.getText().toString();
		intent.putExtra("EMAIL", email);
		
		int memberId = validateUser(email, password);
		if(memberId != -1) {
			//TODO: Logging
			intent.putExtra(MEMBERID, memberId);
			startActivity(intent);
		} else {
			//TODO: Logging, error handling
		}
    }
    
    /** Return true if password is valid for given email */
    private int validateUser(String email, String password) {
    	/*
    	 * TODO: Query DB for rows wither EMAIL=email and PWORD=password.
    	 * - Take customerId from this row
    	 * - getCustomer with that ID
    	 * or - get the list 
    	 */
    	createDummyInfo();
    	return 1;
    	
//    	return -1;
    }
    
    
    /**
     * TODO: Temp method, intended to generate a few dummy values and test user
     */
    final int DUMMY_MEMBER_ID = 1;
	private void createDummyInfo() {
		if(dbHelper.dbGetCustomer(DUMMY_MEMBER_ID) == null) {
			int memberId = dbHelper.dbCreateCustomer("Kevin", "Crane", "515 S Aiken Ave");
			memberId = 1;
			Log.d(TAG, "Created Dummy member Kevin with memberId " + memberId + ".");
			
			int itemId1 = dbHelper.dbCreateItem("Milk", "Food");
			dbHelper.dbCreateShoppingListItem(itemId1, memberId, false, 1);
			Log.d(TAG, "Created dummy item Milk with itemId " + itemId1 + ".");
			
			int itemId2 = dbHelper.dbCreateItem("Cheez-its", "Food");
			dbHelper.dbCreateShoppingListItem(itemId2, memberId, false, 2);
			Log.d(TAG, "Created dummy item Cheez-its with itemId " + itemId2 + ".");
			
			int itemId3 = dbHelper.dbCreateItem("New Jacket", "Clothing");
			dbHelper.dbCreateShoppingListItem(itemId3, memberId, false, 3);
			Log.d(TAG, "Created dummy item New Jacket with itemId " + itemId3 + ".");
		}
	}
	
}
