package cmu.costco.shoppinglist;

import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import cmu.costco.shoppinglist.db.DatabaseAdaptor;
import cmu.costco.shoppinglist.objects.Customer;
import cmu.costco.shoppinglist.objects.ShoppingListItem;

public class ViewListActivity extends Activity {

	private final static String TAG = "ViewListActivity";
	
	private DatabaseAdaptor db;
	private Customer cust;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set the text view as the activity layout
		setContentView(R.layout.activity_view_list);
		
		// Open database
		db = new DatabaseAdaptor(this);
		db.open();
		
		// Get the message from the intent
		Intent intent = getIntent();
		int memberId = intent.getIntExtra(LoginActivity.MEMBERID, 1);
		
		// Load Customer and shoppingList from DB
		cust = db.dbGetCustomer(memberId);
		cust.setShoppingList(db.dbGetShoppingListItems(memberId));
				
		// Add list of ShoppingListItems
		ScrollView scroll = (ScrollView)findViewById(R.id.viewListScroll);
		LinearLayout itemList = generateListView(this, cust.getShoppingList());
		scroll.addView(itemList);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		db.open();
	}

	
	/**
	 * Generate an Android View to display the ShoppingList
	 * @param shoppingList
	 */
	private LinearLayout generateListView(Context ctx, Map<String, ArrayList<ShoppingListItem>> shoppingList) {
		// Create the view that will be returned
		LinearLayout view = new LinearLayout(ctx);
		view.setOrientation(LinearLayout.VERTICAL);
		
		if(shoppingList.size() == 0) {
			TextView emptyRow = new TextView(ctx);
			emptyRow.setTextSize(24);
			emptyRow.setText("No items in Shopping List");
			view.addView(emptyRow);
			return view;
		}
		
		// Iterate through each item category
		for(String category : shoppingList.keySet()) {
			Log.i(TAG, "Iterating through category '" + category + "'.");
			
			if(shoppingList.get(category).size() > 0) {
				// Generate the TextView row to display the category name
				TextView catRow = new TextView(ctx);
				catRow.setTextSize(18);
				catRow.setText(category);
				view.addView(catRow);
				
				// Iterate through each item within the category
				for(final ShoppingListItem item : shoppingList.get(category)) {
					Log.i(TAG, "    Item: " + item.getItem().getDescription() + " - " + item.isChecked());
					view.addView(createItemCheckbox(ctx, item));
				}
			}
		}
		
		return view;
	}
	
	/**
	 * Create a checkbox/description row view to be added to the shopping list
	 * @param ctx
	 * @param item
	 * @return CheckBox view
	 */
	private View createItemCheckbox(Context ctx, final ShoppingListItem item) {
		// Generate the CheckBox/text row for the item
		CheckBox checkbox = new CheckBox(ctx);
		checkbox.setText(item.getItem().getDescription());
		if(item.isChecked()) {
			checkbox.setChecked(true);
			checkbox.setPaintFlags(checkbox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		}
		
		// Create a listener to change the item state when checked
		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton checkBoxView,
					boolean isChecked) {
				Log.i(TAG, "Setting checkbox of item " + item.getItemId() + " to " + isChecked);
				db.dbSetItemChecked(cust.getMemberId(), item.getItemId(), isChecked);
				if(isChecked) {
					checkBoxView.setPaintFlags(checkBoxView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				} else {
					checkBoxView.setPaintFlags(checkBoxView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
				}
			}
		});
		
		return checkbox;
	}
	
	/**
	 * Switch to EditList activity
	 * @param view
	 */
	public void editList(View view) {
		Intent intent = new Intent(this, EditListActivity.class);
		intent.putExtra(LoginActivity.MEMBERID, cust.getMemberId());
		startActivity(intent);
	}
	
}
