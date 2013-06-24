drop table if exists customers;
drop table if exists products;
drop table if exists orders;

create table customers (
    id integer primary key autoincrement,
    name text not null
);
create table products (
    upc NUMERIC primary key,
    name TEXT not null,
    price REAL not null
);
create table orders (
    id INTEGER primary key autoincrement,
    customer_id INTEGER not null,
    upc NUMERIC not null,
    quantity INTEGER not null
);

INSERT INTO customers (name) VALUES ("Kevin");
INSERT INTO products (upc, name, price) VALUES (111111111111, "Jacket", 42.99);
INSERT INTO products (upc, name, price) VALUES (222222222222, "Apples", 0.50);
INSERT INTO products (upc, name, price) VALUES (333333333333, "Big TVs", 569.99);
INSERT INTO products (upc, name, price) VALUES (444444444444, "Bagel Bites", 6.99);
INSERT INTO products (upc, name, price) VALUES (555555555555, "Bicycles!", 199.99);
INSERT INTO orders (customer_id, upc, quantity) VALUES (1, 111111111111, 2);
INSERT INTO orders (customer_id, upc, quantity) VALUES (1, 222222222222, 13);
INSERT INTO orders (customer_id, upc, quantity) VALUES (1, 444444444444, 3);