-- Create the two current types of users, HDC and Staff
INSERT INTO user_type (id, name, description) VALUES ('1', 'Staff', 'Staff User');
INSERT INTO user_type (id, name, description) VALUES ('2', 'HDC', 'Harvard Digital Collections User');

-- All users in the db are currently staff, so update them accordingly
ALTER TABLE `user` ADD CONSTRAINT FK_USER_USERTYPE_ID FOREIGN KEY (usertype_id) REFERENCES user_type(id);
UPDATE `user` SET usertype_id = '1';

-- Add admin role
INSERT INTO role (id, description, name) VALUES (3, 'Admin user', 'admin');

-- email should be unique
ALTER TABLE user ADD CONSTRAINT UQ_EMAIL_UNIQUE UNIQUE (email);

-- set_spec should be unique
ALTER TABLE collection ADD CONSTRAINT UQ_COLLECTION_SETSPEC UNIQUE (set_spec);

-- After running this migration, you must manually insert an admin user with the following values:
    -- role: 3
    -- token: An admin API key of your choice
    -- usertype_id: 2
    -- email and name can be anything
