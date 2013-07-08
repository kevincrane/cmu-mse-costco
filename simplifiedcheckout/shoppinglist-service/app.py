#!flask/bin/python
# Kevin Crane

# all the imports
import sqlite3
from flask import Flask, request, g, abort, render_template, make_response, jsonify
from contextlib import closing
import requests
import json


# configuration
DATABASE = './order_db.db'
DEBUG = True
SECRET_KEY = 'development key'
USERNAME = 'admin'
PASSWORD = 'default'

# Initialize the application!
app = Flask(__name__)
app.config.from_object(__name__)


# Open and initialize DB
def connect_db():
    return sqlite3.connect(app.config['DATABASE'])


def init_db():
    with closing(connect_db()) as db:
        with app.open_resource('schema.sql', mode='r') as f:
            db.cursor().executescript(f.read())
        db.commit()


@app.before_request
def before_request():
    g.db = connect_db()


@app.teardown_request
def teardown_request(exception):
    db = getattr(g, 'db', None)
    if db is not None:
        db.close()


@app.errorhandler(404)
def not_found(error):
    return make_response(jsonify({'error': 'Not found'}), 404)


##### API calls for reading and posting customer orders
@app.route('/costco/api/order/<int:customer_id>', methods=['GET'])
def get_order(customer_id):
    """
    Public API to view the order for customer_id
    TODO: security, multiple orders for one person
    """
    # Get the order and products for customer_id
    cur = g.db.execute('SELECT orders.upc, products.name, products.price, orders.quantity FROM orders ' +
                       'LEFT JOIN products ON orders.upc=products.upc WHERE orders.customer_id=%d' % customer_id)
    order = [dict(upc=row[0], name=row[1], price=row[2], quantity=row[3]) for row in cur.fetchall()]
    if len(order) == 0:
        abort(404)

    # Get customers name
    cur = g.db.execute('SELECT name FROM customers WHERE id=%d' % customer_id)
    row = cur.fetchall()
    customer = row[0][0] if row else None

    return jsonify({'customer': customer, 'order': order})


@app.route('/costco/api/order', methods=['POST'])
def create_order():
    """
    Read in a JSON request, parse out the order properties, and add to DB
    """
    if not request.json or not 'customer' in request.json or not 'order' in request.json:
        abort(400)

    # Pull out customer and order info from JSON
    customer = request.json['customer']
    order = request.json['order']

    # Add the customer name to the customer table and get it's ID
    cur = g.db.cursor()
    cur.execute('INSERT INTO customers (name) VALUES (?)', [customer])
    g.db.commit()
    customer_id = cur.lastrowid

    # Add each item in the order to the order table
    for item in order:
        cur.execute('INSERT INTO orders (customer_id, upc, quantity) VALUES (?, ?, ?)',
                    [customer_id, item['upc'], item['quantity']])
    g.db.commit()
    return jsonify({'customer': customer, 'customer_id': customer_id, 'order': order}), 201


##### API Call for retrieving item from UPC
@app.route('/costco/api/product/<int:upc>', methods=['GET'])
def get_product(upc):
    """
    Public API to retrieve product information from a UPC
    TODO: security, multiple orders for one person
    """
    API_KEY = "acdb9ffa2045103f7aca2bf1b5d55fbb"
    API_URL = "http://api.upcdatabase.org/json"
    req = requests.get("%s/%s/%d" % (API_URL, API_KEY, upc))
    product_data = json.loads(req.text)
    response_data = {}
    if product_data['valid'] == 'false':
        response_data = {'valid': False, 'reason': product_data['reason']}
    else:
        response_data = {'valid': True,
                         'upc': product_data['number'],
                         'name': product_data['itemname'],
                         'price': product_data['avg_price']}
    return jsonify(response_data)


@app.route('/')
def index():
    """
    Display the contents of the DBs
    """
    # Get all customers
    cur = g.db.execute('SELECT id, name FROM customers ORDER BY id ASC')
    customers = [dict(id=row[0], name=row[1]) for row in cur.fetchall()]

    # Get all products
    cur = g.db.execute('SELECT upc, name, price FROM products ORDER BY upc ASC')
    products = [dict(upc=row[0], name=row[1], price=row[2]) for row in cur.fetchall()]

    # Get all orders
    cur = g.db.execute('SELECT id, customer_id, upc, quantity FROM orders ORDER BY id ASC')
    orders = [dict(id=row[0], customer_id=row[1], upc=row[2], quantity=row[3]) for row in cur.fetchall()]

    return render_template('index.html', customers=customers, products=products, orders=orders)


if __name__ == '__main__':
    # app.run(debug = True)
    app.run(host='0.0.0.0')
