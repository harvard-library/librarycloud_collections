-- Using a cursor, this resets the ids in the user table to sequence beginning with 1.  
-- It first disables the foreign keys so we don't have to deal with foreign key problems.
-- It then sets all user PKs to the negative of their value to avoid duplicate key problems.
-- It updates the user_collection table with the new user_ids
-- It updates the collection table set_spec with the proper user_ids for the HDC set_specs 
-- which have the format of hdc_<setname>_<user_id>

DELIMITER //

SET FOREIGN_KEY_CHECKS=0//

UPDATE user SET id = -1*id//

DROP PROCEDURE IF EXISTS UpdateUser//

CREATE PROCEDURE UpdateUser()

BEGIN
DECLARE i INT DEFAULT 0;
DECLARE old_id INT DEFAULT 0;
DECLARE finished INTEGER DEFAULT 0;

DECLARE curIds CURSOR FOR SELECT id FROM user ORDER BY id DESC;

DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;

SET i=0;
OPEN curIds;

	updateIds: LOOP
		FETCH curIds INTO old_id;
		IF finished = 1 THEN 
			LEAVE updateIds;
		END IF;
		SET i = i + 1;
		UPDATE user SET id = i WHERE id = old_id;
		SET old_id = -1*old_id;
		UPDATE user_collection SET user_id = i WHERE user_id = old_id;
		UPDATE collection SET set_spec = REPLACE(set_spec, SUBSTRING_INDEX(set_spec, '_', -1), i) WHERE SUBSTRING_INDEX(set_spec, '_',1) = 'hdc' AND CONVERT(SUBSTRING_INDEX(set_spec, '_', -1),UNSIGNED INTEGER) = old_id;
	END LOOP updateIds;
	CLOSE curIds;
	
END//
DELIMITER ;

CALL UpdateUser();

DROP PROCEDURE IF EXISTS UpdateUser;

SET FOREIGN_KEY_CHECKS=1;