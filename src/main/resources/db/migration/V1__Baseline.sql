DROP TABLE IF EXISTS `OPENJPA_SEQUENCE_TABLE`;
CREATE TABLE `OPENJPA_SEQUENCE_TABLE` (`ID` tinyint(4) NOT NULL,`SEQUENCE_VALUE` bigint(20) DEFAULT NULL,PRIMARY KEY (`ID`));
DROP TABLE IF EXISTS `auth_user`;
CREATE TABLE `auth_user` (`id` int(11) NOT NULL,`UID` varchar(255) NOT NULL,`authSource` varchar(255) NOT NULL,`user_id` int(11) DEFAULT NULL,PRIMARY KEY (`id`),KEY I_UTH_USR_USER_0 (`user_id`));
DROP TABLE IF EXISTS `collection`;
CREATE TABLE `collection` (`id` int(11) NOT NULL,`extent` int(11) DEFAULT NULL,`language` varchar(255) DEFAULT NULL,`rights` text,`summary` text,`title` varchar(255) NOT NULL,PRIMARY KEY (`id`));
DROP TABLE IF EXISTS `collection_item`;
CREATE TABLE `collection_item` (`COLLECTIONS_ID` int(11) DEFAULT NULL,`ITEMS_ID` int(11) DEFAULT NULL,KEY I_CLLC_TM_COLLECTIONS_ID_0 (`COLLECTIONS_ID`),KEY I_CLLC_TM_ELEMENT_0 (`ITEMS_ID`));
DROP TABLE IF EXISTS `item`;
CREATE TABLE `item` (`id` int(11) NOT NULL,`itemId` varchar(255) NOT NULL,PRIMARY KEY (`id`));
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (`id` int(11) NOT NULL,`description` varchar(255) DEFAULT NULL,`name` varchar(255) NOT NULL,PRIMARY KEY (`id`),UNIQUE KEY U_ROLE_NAME_0 (`name`));
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (`id` int(11) NOT NULL,`email` varchar(255) NOT NULL,`name` varchar(255) DEFAULT NULL,`role` varchar(255) DEFAULT NULL,`token` varchar(255) NOT NULL,`usertype_id` int(11) DEFAULT NULL,PRIMARY KEY (`id`),UNIQUE KEY U_USER_TOKEN_0 (`token`),KEY I_USER_USERTYPE_0 (`usertype_id`));
DROP TABLE IF EXISTS `user_collection`;
CREATE TABLE `user_collection` (`collection_id` int(11) NOT NULL,`user_id` int(11) NOT NULL,`role_id` int(11) NOT NULL,PRIMARY KEY (`collection_id`,`user_id`),KEY I_SR_CCTN_ROLE_0 (`role_id`));
DROP TABLE IF EXISTS `user_type`;
CREATE TABLE `user_type` (`id` int(11) NOT NULL,`description` varchar(255) DEFAULT NULL,`name` varchar(255) NOT NULL,PRIMARY KEY (`id`),UNIQUE KEY U_USR_TYP_NAME_0 (`name`));
