package costco.cmu.simplifiedcheckout.web;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ShoppingList implements Serializable {
	private static final long serialVersionUID = 1L;
	public String shopper;
	public List<StoreItem> storeItems;
	
	public ShoppingList(String shopper) {
		this.shopper = shopper;
		storeItems = new ArrayList<StoreItem>();
	}
	
	@Override
	public String toString() {
		// Create String representation of ShoppingList
		double totalPrice = 0;
		DecimalFormat decim = new DecimalFormat("0.00");
		
		String out = shopper + "'s List\n";
		for(int i=0; i<storeItems.size(); i++) {
			StoreItem item = storeItems.get(i);
			totalPrice += item.price * item.quantity;
			out += (i+1) + ".\t$" + decim.format(item.price * item.quantity) + "\t" + 
					item.quantity + "x " + item.name + "\n";  
		}
		out += "Total:\t$" + decim.format(totalPrice);
		return out;
	}
	
	public void addItem(StoreItem item) {
		storeItems.add(item);
	}
	
	public int size() {
		return storeItems.size();
	}
	
	public StoreItem get(int i) {
		if(i >= storeItems.size()) {
			return null;
		}
		return storeItems.get(i);
	}
}
