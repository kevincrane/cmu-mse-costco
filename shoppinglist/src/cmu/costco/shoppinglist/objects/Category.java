package cmu.costco.shoppinglist.objects;


/**
 * @author kevin
 *
 */
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
