package cmu.costco.shoppinglist.objects;

import android.content.Loader;


/**
 * @author kevin
 *
 */

/* FIXME: nothing here is useful, leftover artifact from earlier?
public enum Category {
	APPLIANCES(1),
	BABYTOYS(2),
	CLOTHING(3),
	COMPUTERS(4),
	ELECTRONICS(5),
	FOOD(6),
	FURNITURE(7),
	GIFTS(8),
	HARDWARE(9),
	HEALTHBEAUTY(10),
	HOMESEASONAL(11),
	JEWELRY(12),
	OFFICE(13),
	PATIOGARDEN(14),
	PET(15),
	SPORTS(16),
	OTHER(100);
	
	private int id; 
	private Category(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
}
*/
public class Category {
	private String name;
	private Location location;
	
	public Category(String name, Location location) {
		this.name = name;
		this.location = location;
	}
	
	public String getName() { return name; }
	public Location getLocation() { return location; }
	
	/** Used for saving category locations as a single object */
	public static class Location {
		final private double lat;
		final private double lon;
		
		public Location(double lat, double lon) {
			this.lat = lat;
			this.lon = lon;
		}
		
		public double getLat() { return lat; }
		public double getLon() { return lon; }
	}
}
