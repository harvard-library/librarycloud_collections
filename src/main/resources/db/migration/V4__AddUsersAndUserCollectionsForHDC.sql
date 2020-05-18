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

-- Add foreign keys as appropriate to users, collections, and items
ALTER TABLE `user_collection` ADD CONSTRAINT FK_USER_COLLECTION_USER FOREIGN KEY (user_id) REFERENCES user(id);
ALTER TABLE `user_collection` ADD CONSTRAINT FK_USER_COLLECTION_COLLECTION FOREIGN KEY (collection_id) REFERENCES collection(id);

ALTER TABLE `collection_item` ADD CONSTRAINT FK_COLLECTION_ITEM_COLLECTION FOREIGN KEY (COLLECTIONS_ID) REFERENCES collection(id);
ALTER TABLE `collection_item` ADD CONSTRAINT FK_COLLECTION_ITEM_ITEM FOREIGN KEY (ITEMS_ID) REFERENCES item(id);

-- Add admin role
INSERT INTO role (id, description, name) VALUES (3, 'Admin user', 'admin');