-- Using a cursor, this resets the ids in the collection table to sequence beginning with 1.  
-- It first disables the foreign keys so we don't have to deal with foreign key problems.
-- It then sets all user PKs to the negative of their value to avoid duplicate key problems.
-- It updates the user_collection table with the new collection_ids
-- It updates the collection_items table with the new collection_ids 

DELIMITER //

SET FOREIGN_KEY_CHECKS=0//

UPDATE collection SET id = -1*id//

DROP PROCEDURE IF EXISTS UpdateCollection//

CREATE PROCEDURE UpdateCollection()

BEGIN
DECLARE i INT DEFAULT 0;
DECLARE old_id INT DEFAULT 0;
DECLARE finished INTEGER DEFAULT 0;

DECLARE curIds CURSOR FOR SELECT id FROM collection ORDER BY id DESC;

DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;

SET i=0;
OPEN curIds;

	updateIds: LOOP
		FETCH curIds INTO old_id;
		IF finished = 1 THEN 
			LEAVE updateIds;
		END IF;
		SET i = i + 1;
		UPDATE collection SET id = i WHERE id = old_id;
		SET old_id = -1*old_id;
		UPDATE collection_item SET COLLECTIONS_ID = i WHERE COLLECTIONS_ID = old_id;
		UPDATE user_collection SET collection_id = i WHERE collection_id = old_id;
	END LOOP updateIds;
	CLOSE curIds;
	
END//
DELIMITER ;

CALL UpdateCollection();

DROP PROCEDURE IF EXISTS UpdateCollection;

SET FOREIGN_KEY_CHECKS=1;