package cmu.costco.shoppinglist.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import cmu.costco.shoppinglist.objects.Category.Location;
import cmu.costco.shoppinglist.objects.Customer;
import cmu.costco.shoppinglist.objects.Item;
import cmu.costco.shoppinglist.objects.ShoppingListItem;

public class DatabaseAdaptor {
	
	// If you change the database schema, you must increment the database version.
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "ShoppingList.db";
	
	private static final String TAG = "DatabaseAdaptor";
	private final Context ctx;
	private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

	
    /**
     * Create tables for ShoppingList database
     * @author kevin
     *
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
		private static final String TEXT_TYPE = " TEXT";
		private static final String INT_TYPE = " INTEGER";
		private static final String DOUBLE_TYPE = " DOUBLE";
		private static final String COMMA_SEP = ",";
		private static final String SQL_CREATE_CUSTOMER =
		    "CREATE TABLE " + DbContract.CustomerEntry.TABLE_NAME + " (" +
		    DbContract.CustomerEntry.MEMBER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
		    DbContract.CustomerEntry.NAME_FIRST + TEXT_TYPE + COMMA_SEP +
		    DbContract.CustomerEntry.NAME_LAST + TEXT_TYPE + COMMA_SEP +
		    DbContract.CustomerEntry.ADDRESS + TEXT_TYPE + 
		    " )";
		
		private static final String SQL_CREATE_LISTITEM =
			    "CREATE TABLE " + DbContract.ListItemEntry.TABLE_NAME + " (" +
			    DbContract.ListItemEntry.ROW_KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			    DbContract.ListItemEntry.MEMBER_ID + INT_TYPE + COMMA_SEP +
			    DbContract.ListItemEntry.ITEM_ID + INT_TYPE + COMMA_SEP +
			    DbContract.ListItemEntry.CHECKED + " BOOLEAN" + COMMA_SEP +
			    DbContract.ListItemEntry.POSITION + " INT_TYPE" +
			    " )";
		
		private static final String SQL_CREATE_ITEM =
			    "CREATE TABLE " + DbContract.ItemEntry.TABLE_NAME + " (" +
			    DbContract.ItemEntry.ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			    DbContract.ItemEntry.DESCRIPTION + TEXT_TYPE + COMMA_SEP +
			    DbContract.ItemEntry.CATEGORY_NAME + TEXT_TYPE + 
			    " )";
		
		private static final String SQL_CREATE_CATEGORY =
			    "CREATE TABLE " + DbContract.CategoryEntry.TABLE_NAME + " (" +
			    DbContract.CategoryEntry.CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			    DbContract.CategoryEntry.CAT_NAME + TEXT_TYPE +
			    " )";
		
		private static final String SQL_CREATE_ALERTS = 
				"CREATE TABLE " + DbContract.AlertsEntry.TABLE_NAME + " (" +
				DbContract.AlertsEntry.ALERT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
				DbContract.AlertsEntry.CATEGORY_NAME + " TEXT"+ COMMA_SEP +
				DbContract.AlertsEntry.LATITUDE + DOUBLE_TYPE + COMMA_SEP +
				DbContract.AlertsEntry.LONGITUDE + DOUBLE_TYPE + " )";
	
		//TODO: doesn't work
		private static final String SQL_DELETE_ENTRIES =
				"DROP TABLE IF EXISTS " +
				DbContract.CustomerEntry.TABLE_NAME + COMMA_SEP +
				DbContract.ListItemEntry.TABLE_NAME + COMMA_SEP +
				DbContract.ItemEntry.TABLE_NAME + COMMA_SEP +
				DbContract.CategoryEntry.TABLE_NAME;
	
		
		
		/** Constructor*/
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
	
		/**
		 * On creation of DB, generate all 4 ShoppingList tables
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE_CUSTOMER);
			db.execSQL(SQL_CREATE_LISTITEM);
			db.execSQL(SQL_CREATE_ITEM);
			db.execSQL(SQL_CREATE_CATEGORY);
			db.execSQL(SQL_CREATE_ALERTS);
		}
	
		/**
		 * On upgrade of DB, discard all previous results, recreate original tables
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL(SQL_DELETE_ENTRIES);
			onCreate(db);
		}
    }


// ##### DATABASE ADAPTOR #####
    
	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx the Context within which to work
	 */
	public DatabaseAdaptor(Context ctx) {
		this.ctx = ctx;
	}
	
	/**
	 * Open the notes database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException if the database could be neither opened or created
	 */
	public DatabaseAdaptor open() throws SQLException {
		dbHelper = new DatabaseHelper(ctx);
		if(db == null || !db.isOpen()) {
			db = dbHelper.getWritableDatabase();
		}
		return this;
	}
	
	/**
	 * Close DatabaseHelper, freeing its resources
	 */
	public void close() {
		dbHelper.close();
	}
	
	
// ##### CLASS METHODS #####
	
// 		##### DB GET METHODS #####
	
	/**
	 * Return a new Customer object, created from DB data in 'customers' table with 'memberId'
	 * @param memberId
	 * @return
	 */
	public Customer dbGetCustomer(int memberId) {
		// Return cursor location of row with matching memberId
		Cursor mCursor = db.query(true, DbContract.CustomerEntry.TABLE_NAME, 
				new String[] {
					DbContract.CustomerEntry.MEMBER_ID,
					DbContract.CustomerEntry.NAME_FIRST,
					DbContract.CustomerEntry.NAME_LAST,
					DbContract.CustomerEntry.ADDRESS
				}, DbContract.CustomerEntry.MEMBER_ID + "=" + memberId, null,
				null, null, null, null);
		if (mCursor != null && mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			Log.d(TAG, "Read list of Customers with memberId " + memberId 
					+ ". Found " + mCursor.getCount() + " matches.");
		} else {
			// No matches found
			Log.d(TAG, "No Customer with memberId " + memberId + " found.");
			return null;
		}
		
		// Create new Customer object, return it
		String firstName = mCursor.getString(
			    mCursor.getColumnIndexOrThrow(DbContract.CustomerEntry.NAME_FIRST)
				);
		String lastName = mCursor.getString(
			    mCursor.getColumnIndexOrThrow(DbContract.CustomerEntry.NAME_LAST)
				);
		String address = mCursor.getString(
			    mCursor.getColumnIndexOrThrow(DbContract.CustomerEntry.ADDRESS)
				);
		Customer cust = new Customer(memberId, firstName, lastName, address);
		
		//TODO: Add shoppingList (dbGetShoppingListItems) here?
		
		return cust;
	}
	
	
	/**
	 * Return an ShoppingListItem corresponding to MemberId
	 * @param memberId
	 * @return
	 */
	public Map<String, ArrayList<ShoppingListItem>> dbGetShoppingListItems(int memberId) {
		// Return cursor location of row with matching memberId
		Cursor mCursor = db.query(true, DbContract.ListItemEntry.TABLE_NAME, 
				new String[] {
					DbContract.ListItemEntry.ITEM_ID,
					DbContract.ListItemEntry.CHECKED,
					DbContract.ListItemEntry.POSITION
				}, DbContract.ListItemEntry.MEMBER_ID + "=" + memberId, null,
				null, null, null, null);
		if (mCursor != null && mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			Log.d(TAG, "Read list of ShoppingListItems for memberId " + memberId 
					+ ". Found " + mCursor.getCount() + " matches.");
		} else {
			// No matches found
			Log.d(TAG, "No ListItems with memberId " + memberId + " found.");
			return null;
		}
		

		// Create Customer's list of ListItems mapped to category
		Map<String, ArrayList<ShoppingListItem>> shoppingList = 
				new HashMap<String, ArrayList<ShoppingListItem>>();
		
		// Iterate through all ShoppingListItems
		for(int i=0; i<mCursor.getCount(); i++) {
			// Read ShoppingListItem properties
			int itemId = mCursor.getInt(
				    mCursor.getColumnIndexOrThrow(DbContract.ListItemEntry.ITEM_ID)
					);
			boolean checked = mCursor.getInt(
					mCursor.getColumnIndexOrThrow(DbContract.ListItemEntry.CHECKED)
					) > 0;
			int position = mCursor.getInt(
				    mCursor.getColumnIndexOrThrow(DbContract.ListItemEntry.POSITION)
					);
			
			// Get Item object from DB
			Item item = dbGetItemById(itemId);
			String category = item.getCategory();
			
			// Get list of items for current ListItem category
			ArrayList<ShoppingListItem> listItems;
			if(shoppingList.containsKey(category)) {
				listItems = shoppingList.get(category);
			} else {
				listItems = new ArrayList<ShoppingListItem>();
			}
			listItems.add(new ShoppingListItem(itemId, checked, position, item));
			
			// Add this ShoppingListItem to shoppingList
			shoppingList.put(category, listItems);
			mCursor.moveToNext();
		}
		
		Log.i(TAG, "Reading from DB: ShoppingList with " + shoppingList.size() + " categories");
		return shoppingList;
	}
	
	
	/**
	 * Returns an Item object by its itemId
	 * @param listId
	 * @return
	 */
	public Item dbGetItemById(int itemId) {
		// Return cursor location of row with matching memberId
		Cursor mCursor = db.query(true, DbContract.ItemEntry.TABLE_NAME, 
				new String[] {
					DbContract.ItemEntry.ITEM_ID,
					DbContract.ItemEntry.DESCRIPTION,
					DbContract.ItemEntry.CATEGORY_NAME
				}, DbContract.ItemEntry.ITEM_ID + "=" + itemId, null,
				null, null, null, null);
		if (mCursor != null && mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			Log.d(TAG, "Read list of Items with itemId " + itemId 
					+ ". Found " + mCursor.getCount() + " matches.");
		} else {
			// No matches found
			Log.d(TAG, "No Items with itemId " + itemId + " found.");
			return null;
		}
		
		// Read Item description and category from DB
		String description = mCursor.getString(
				mCursor.getColumnIndexOrThrow(DbContract.ItemEntry.DESCRIPTION)
				);
		String category = mCursor.getString(
			    mCursor.getColumnIndexOrThrow(DbContract.ItemEntry.CATEGORY_NAME)
				);
		
		// Create Item object and return it
		Log.i(TAG, "Reading from DB: Item (id " + itemId + ") in category '" 
				+ category + "': '" + description + "'");
		return new Item(itemId, description, category);
	}
	
	
	/**
	 * Return a map of all proximity alerts, tying category name to GPS coordinates
	 * @param memberId
	 * @return
	 */
	public Map<String, Location> dbGetProxAlerts() {
		// Return cursor location of row with matching memberId
		Cursor mCursor = db.query(true, DbContract.AlertsEntry.TABLE_NAME, 
				new String[] {
					DbContract.AlertsEntry.CATEGORY_NAME,
					DbContract.AlertsEntry.LATITUDE,
					DbContract.AlertsEntry.LONGITUDE
				}, null, null,
				null, null, null, null);
		if (mCursor != null && mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			Log.d(TAG, "Read list of Proximity Alerts, found " + mCursor.getCount() + " matches.");
		} else {
			// No matches found
			Log.d(TAG, "No Proximity Alerts found.");
			return new HashMap<String, Location>();
		}
		
		
		// Create the map object of proximity alerts
		Map<String, Location> proximityAlerts = new HashMap<String, Location>();
		
		// Iterate through all Proximity Alerts found
		for(int i=0; i<mCursor.getCount(); i++) {
			String category = mCursor.getString(
				    mCursor.getColumnIndexOrThrow(DbContract.AlertsEntry.CATEGORY_NAME)
					);
			Double latitude = mCursor.getDouble(
					mCursor.getColumnIndexOrThrow(DbContract.AlertsEntry.LATITUDE));
			Double longitude = mCursor.getDouble(
					mCursor.getColumnIndexOrThrow(DbContract.AlertsEntry.LONGITUDE));
			
			// Add new proximity alert and move on to the next
			proximityAlerts.put(category, new Location(latitude, longitude));
			mCursor.moveToNext();
		}
		
		return proximityAlerts;
	}
	
	
//		##### DB CREATE METHODS #####
	
	/**
	 * Create a new row in the Customers db
	 * @param firstName
	 * @param lastName
	 * @param address
	 * @return new memberId
	 */
	public int dbCreateCustomer(String firstName, String lastName, String address) {
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(DbContract.CustomerEntry.NAME_FIRST, firstName);
		values.put(DbContract.CustomerEntry.NAME_LAST, lastName);
		values.put(DbContract.CustomerEntry.ADDRESS, address);
		
		// Insert the new row, returning the primary key value of the new row
		return (int)db.insert(DbContract.CustomerEntry.TABLE_NAME, null, values);
	}
	
	/**
	 * Create a new row in the ShoppingListItem db; returns memberId
	 * @param itemId
	 * @param memberId
	 * @param checked
	 * @param position
	 * @return
	 */
	public int dbCreateShoppingListItem(int itemId, int memberId, boolean checked, int position) {
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(DbContract.ListItemEntry.ITEM_ID, itemId);
		values.put(DbContract.ListItemEntry.MEMBER_ID, memberId);
		values.put(DbContract.ListItemEntry.CHECKED, checked);
		values.put(DbContract.ListItemEntry.POSITION, position);
		
		// Insert the new row, returning the primary key value of the new row (memberId)
		return (int)db.insert(DbContract.ListItemEntry.TABLE_NAME, null, values);
	}
	
	/**
	 * Create a new row in the Item db; returns new itemId
	 * @param description
	 * @param category
	 * @return
	 */
	public int dbCreateItem(String description, String category) {
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(DbContract.ItemEntry.DESCRIPTION, description);
		values.put(DbContract.ItemEntry.CATEGORY_NAME, category);
		
		// Insert the new row, returning the primary key value of the new row (itemId)
		return (int)db.insert(DbContract.ItemEntry.TABLE_NAME, null, values);
	}
	
	/**
	 * Create a new proximity alert with name 'category' at location (latitude, longitude)
	 * @param category
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	public int dbCreateAlert(String category, double latitude, double longitude) {
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(DbContract.AlertsEntry.CATEGORY_NAME, category);
		values.put(DbContract.AlertsEntry.LATITUDE, latitude);
		values.put(DbContract.AlertsEntry.LONGITUDE, longitude);
		
		// Insert the new row, returning the primary key value of the new row (itemId)
		return (int)db.insert(DbContract.AlertsEntry.TABLE_NAME, null, values);
	}
	
	
//		##### UPDATE DB METHODS #####
	
	/**
	 * Sets checked status of a ListItem to 'checked'
	 * @param memberId
	 * @param itemId
	 * @param checked
	 * @return
	 */
	public int dbSetItemChecked(int memberId, int itemId, boolean checked) {
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(DbContract.ListItemEntry.CHECKED, checked);

		// Which row to update, based on the ID
		String selection = DbContract.ListItemEntry.MEMBER_ID + "=" + memberId
				+ " AND " + DbContract.ListItemEntry.ITEM_ID + "=" + itemId;
		
		return (int)db.update(DbContract.ListItemEntry.TABLE_NAME, values, selection, null);
	}
	
	/**
	 * Delete a row from ListItem db for selected member, 
	 * @param memberId
	 * @param itemId
	 * @return
	 */
	public int dbDeleteItemRow(int memberId, int itemId) {
		// Which row to update, based on the ID
		String selection = DbContract.ListItemEntry.MEMBER_ID + "=" + memberId
				+ " AND " + DbContract.ListItemEntry.ITEM_ID + "=" + itemId;
		return (int) db.delete(DbContract.ListItemEntry.TABLE_NAME, selection, null);
	}
	
	/**
	 * Update a row in the Item db with new category/description
	 * @param itemId
	 * @param category
	 * @param description
	 * @return
	 */
	public int dbUpdateItem(int itemId, String category, String description) {
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(DbContract.ItemEntry.CATEGORY_NAME, category);
		values.put(DbContract.ItemEntry.DESCRIPTION, description);

		// Which row to update, based on the ID
		String selection = DbContract.ItemEntry.ITEM_ID + "=" + itemId;
		
		return (int)db.update(DbContract.ItemEntry.TABLE_NAME, values, selection, null);
	}

	/**
	 * Delete a proximity alert of a given category from the DB
	 * @param category
	 */
	public int dbDeleteProxAlert(String category, double latitude, double longitude) {
		// Which row to delete, based on 'category'
		String selection = DbContract.AlertsEntry.CATEGORY_NAME + "='" + category + "'" +
				" AND " + DbContract.AlertsEntry.LATITUDE + "=" + latitude + 
				" AND " + DbContract.AlertsEntry.LONGITUDE + "=" + longitude;
		return (int)db.delete(DbContract.AlertsEntry.TABLE_NAME, selection, null);
	}
    
}
