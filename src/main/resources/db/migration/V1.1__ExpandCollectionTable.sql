ALTER TABLE `collection` ADD `dcp` BIT NOT NULL DEFAULT 0;
ALTER TABLE `collection` ADD `is_public` BIT NOT NULL DEFAULT 0;
ALTER TABLE `collection` ADD `abstract` text;
ALTER TABLE `collection` ADD `thumbnail_urn` varchar(255);
ALTER TABLE `collection` ADD `collection_urn` varchar(255);
ALTER TABLE `collection` ADD `base_url` varchar(255);

