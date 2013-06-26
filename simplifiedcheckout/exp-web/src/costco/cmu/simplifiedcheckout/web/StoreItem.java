package costco.cmu.simplifiedcheckout.web;

import java.io.Serializable;

public class StoreItem implements Serializable {
	private static final long serialVersionUID = 1L;
	public String name;
	public String upc;
	public double price;
	public int quantity;
	
	public StoreItem(String name, String upc, double price, int quantity) {
		this.name = name;
		this.upc = upc;
		this.price = price;
		this.quantity = quantity;
	}
}
