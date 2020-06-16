-- Using a cursor, this resets the id in the item table to sequence beginning with 1.  
-- It first disables the foreign keys so we don't have to deal with foreign key problems.
-- It then sets all user PKs to the negative of their value to avoid duplicate key problems.
-- It updates the collection_item table with the new item ids

DELIMITER //

SET FOREIGN_KEY_CHECKS=0//

UPDATE item SET id = -1*id//

DROP PROCEDURE IF EXISTS UpdateItems//

CREATE PROCEDURE UpdateItems()

BEGIN
DECLARE i INT DEFAULT 0;
DECLARE old_id INT DEFAULT 0;
DECLARE finished INTEGER DEFAULT 0;

DECLARE curIds CURSOR FOR SELECT id FROM item ORDER BY id DESC;

DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;

SET i=0;
OPEN curIds;

	updateIds: LOOP
		FETCH curIds INTO old_id;
		IF finished = 1 THEN 
			LEAVE updateIds;
		END IF;
		SET i = i + 1;
		UPDATE item SET id = i WHERE id = old_id;
		SET old_id = -1*old_id;
		UPDATE collection_item SET ITEMS_ID = i WHERE ITEMS_ID = old_id;
	END LOOP updateIds;
	CLOSE curIds;
	
END//
DELIMITER ;

CALL UpdateItems();

DROP PROCEDURE IF EXISTS UpdateItems;

SET FOREIGN_KEY_CHECKS=1;