INSERT INTO orders (customer_name, product_name, quantity, price, status, created_at, updated_at, customer_email, customer_phone) VALUES 
('John Smith', 'Laptop Pro', 1, 1299.99, 'CREATED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'john.smith@example.com', '+1-555-0101'),
('Sarah Johnson', 'Wireless Mouse', 2, 29.99, 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'sarah.j@example.com', '+1-555-0102'),
('Mike Davis', 'Mechanical Keyboard', 1, 149.99, 'CREATED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'mike.davis@example.com', '+1-555-0103'),
('Emily Brown', 'Monitor 27"', 1, 399.99, 'CANCELLED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'emily.brown@example.com', '+1-555-0104'),
('David Wilson', 'USB-C Hub', 3, 79.99, 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'david.w@example.com', '+1-555-0105');