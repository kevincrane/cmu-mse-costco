#!flask/bin/python
# Kevin Crane

# all the imports
import sqlite3
from flask import Flask, request, session, g, redirect, url_for, \
     abort, render_template, flash
from contextlib import closing
import logging



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
    return make_response(jsonify( { 'error': 'Not found' } ), 404)


@app.route('/todo/api/v1.0/tasks', methods = ['GET'])
def get_tasks():
    return jsonify( { 'tasks': tasks } )

@app.route('/')
def index():
    # Get all customers
    cur = g.db.execute('SELECT id, name FROM customers ORDER BY id ASC')
    customers = [dict(id=row[0], name=row[1]) for row in cur.fetchall()]

    # Get all products
    cur = g.db.execute('SELECT upc, name, price FROM products ORDER BY upc ASC')
    products = [dict(upc=row[0], name=row[1], price=row[2]) for row in cur.fetchall()]
    app.logger.debug("products: %s" % products)

    # Get all orders
    cur = g.db.execute('SELECT id, customer_id, upc, quantity FROM orders ORDER BY id ASC')
    orders = [dict(id=row[0], customer_id=row[1], name=row[2], quantity=row[3]) for row in cur.fetchall()]

    return render_template('index.html', customers=customers, products=products, orders=orders)


if __name__ == '__main__':
    app.run(debug = True)