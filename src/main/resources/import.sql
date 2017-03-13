-- Create roles
insert into role (id, name) values (1, 'owner');
insert into role (id, name) values (2, 'editor');
-- Create test user
insert into user (id, email, name, token) values (1, 'nick@podconsulting.com', 'nick caramello', '999999999');