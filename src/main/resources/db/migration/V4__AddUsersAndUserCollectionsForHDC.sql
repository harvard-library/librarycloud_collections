-- Add the api_id column to users and a unique constraint based on the combination of api_id and usertype_id
-- This is because a user could theoretically have the same email in different apps that use this api
ALTER TABLE `user` ADD `api_id` varchar(45) DEFAULT NULL;
ALTER TABLE `user` ADD CONSTRAINT UQ_EXTRNL_ID_USER_TYPE UNIQUE(api_id, usertype_id);

-- Create the two current types of users, HDC and Staff
INSERT INTO user_type (id, name, description) VALUES ('0', 'HDC', 'Harvard Digital Collections User');
INSERT INTO user_type (id, name, description) VALUES ('1', 'Staff', 'Staff User');

-- All users in the db are currently staff, so update them accordingly
ALTER TABLE `user` ADD CONSTRAINT FK_USER_USERTYPE_ID FOREIGN KEY (usertype_id) REFERENCES user_type(id);
UPDATE `user` SET usertype_id = '1';

-- Add admin role
INSERT INTO role (id, description, name) VALUES (3, 'Admin user', 'admin');

-- email should be unique
ALTER TABLE `user` ADD UNIQUE INDEX `email_UNIQUE` (`email` ASC) VISIBLE;

-- set_spec should be unique
ALTER TABLE `collection` ADD UNIQUE INDEX `set_spec_UNIQUE` (`set_spec` ASC) VISIBLE;