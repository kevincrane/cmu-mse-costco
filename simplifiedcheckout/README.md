# Simplified Checkout
CMU - Costcode

### Development Checklist
#### Customer Side
* Get database of products
    * UPC
    * Name
    * MSRP
* Read UPC from barcodes
    * Scandit
    * Read product info from DB, UPC as key
    * Add product to customer's Shopping List
* ShoppingList stored in Sqlite DB
    * Model ShoppingList from experiments
* Display shopping list
    * Push X to delete item
* Prepare for Cashier
    * Build NFC message
    * Send to server
    * Build QR code with list URL
* *Bonus:* Make it pretty

#### Cashier Side
* Receive shopping list
    * Receive NFC message
    * Read QR code; pull down from server
* *Mystical back-end payment stuff*
    * Make Log calls to indicate where it should be hooked in with an inventory/payment system
* Display how much the customer should pay
* Receipt
    * Print (Log call for hooking in)
    * Email a receipt