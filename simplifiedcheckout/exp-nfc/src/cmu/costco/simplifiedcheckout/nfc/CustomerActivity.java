package cmu.costco.simplifiedcheckout.nfc;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class CustomerActivity extends Activity {

	List<StoreItem> shoppingList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_customer);
		// Show the Up button in the action bar.
		setupActionBar();
		
		// Set up ShoppingList with dummy data
		shoppingList = new ArrayList<StoreItem>();
		shoppingList.add(new StoreItem("Couch cushions", "184930574960", 37.50, 3));
		shoppingList.add(new StoreItem("Pre-made sandwichs", "185038672860", 5.37, 13));
		shoppingList.add(new StoreItem("Woolen jackets aplenty", "123456789048", 68, 1));
		
		// Add all items in shopping cart to view
		ScrollView scroll = (ScrollView)findViewById(R.id.shoppingListScrollView);
//		LinearLayout itemList = new LinearLayout(this);
//		itemList.setOrientation(LinearLayout.VERTICAL);
		LinearLayout itemList = (LinearLayout)findViewById(R.id.itemListView);
		for(int i=0; i<shoppingList.size(); i++) {
			StoreItem item = shoppingList.get(i);
			TextView text = new TextView(this);
			String rowText = (i+1) + ". $" + item.price + "\t " + item.quantity + "x " + item.name;
			text.setText(rowText);
			itemList.addView(text);
		}
		
		//TODO:
		//	Check that this works
		//	Make addToCart function (add item to the List<StoreItem> and update the view
		//	Make broadcast function (look at how NFC works and do that)
		//	Make Cashier one (when they touch, receive serialized ShoppingList, deserialize it, print as string)
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
	 * Called when the Add To Cart button is pressed; adds new StoreItem to ShoppingList
	 * @param view
	 */
	public void addToCart(View view) {
		
	}

}
