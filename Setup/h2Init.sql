-- MySQL dump 10.13  Distrib 5.5.16, for Win32 (x86)
--
-- Host: localhost    Database: jacobsdefense
-- ------------------------------------------------------
-- Server version	5.5.22

--
-- Table structure for table `objlib`
--
SET MODE MYSQL;

CREATE TABLE `objlib` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'The unique ID of this object library entry.',
  `name` varchar(45) NOT NULL COMMENT 'The name of the object entry; helps to differentiate object purposes.',
  `tilesrc` int(11) NOT NULL COMMENT 'Represents the tile this object is associated with. Not mapped to a foreign key in the tilelib table at this time.',
  `imgXOff` int(11) NOT NULL DEFAULT '0' COMMENT 'The X offset within the specified tile image source of this object''s graphical representation.',
  `imgYOff` int(11) NOT NULL DEFAULT '0' COMMENT 'The Y offset within the specified tile image source of this object''s graphical representation.',
  `imgWidth` int(11) NOT NULL DEFAULT '1' COMMENT 'Width of the image representation in the specified tileset.',
  `imgHeight` int(11) NOT NULL DEFAULT '1' COMMENT 'Height of the image representation in the specified tileset.',
  `description` text COMMENT 'Describes the object in significant detail.',
  `defaultidentity` varchar(1000) DEFAULT NULL COMMENT 'If specified, an object that does not have an internal name will be given this when the map is written.  This is not to be confused with the unique internal id number; instead, this strictly applies to what games will use to differentiate the purpose of objects.',
  `defaultattributes` text COMMENT 'In some cases, an object being instantiated should have certain attributes already set.  This allows the creation of a ''global palette'' that provides the basic information a map consume may need while giving the end user full control over the final product.',
  PRIMARY KEY (`id`)
);

--
-- Table structure for table 'users'
--

CREATE TABLE `users` (
  `userid` int(11) NOT NULL AUTO_INCREMENT COMMENT 'The uniquely generated user ID.',
  `username` varchar(32) NOT NULL COMMENT 'The unique username. Note care must be taken for lowercasing it when checking.',
  `phash` tinyblob NOT NULL COMMENT 'The user''s password hash, precalculated.',
  `email` varchar(300) NOT NULL COMMENT 'The e-mail address of the user.',
  `builderspermit` varchar(40) NOT NULL DEFAULT 'Bronze' COMMENT 'The builder''s permit in the system. This is planned around the idea of different ''permits'' that indicate how much freedom is given to a user. For example, a bronze user may be limited to just a few realms and can only make private objects.  A silver user can make up to 100 realms and has the option of publishing objects.  The Diamond level is reserved for special guests and heavy contributors.  ',
  `description` varchar(2048) DEFAULT NULL COMMENT 'Allows the user to enter information about themselves, links to their home page, etc.',
  `enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Determines whether a user is active or not.  Inactive users cannot login.',
  PRIMARY KEY (`userid`),
  UNIQUE KEY `userid_UNIQUE` (`userid`),
  UNIQUE KEY `username_UNIQUE` (`username`)
);
/*!40101 SET character_set_client = @saved_cs_client */;

CREATE TABLE `tilelib` (
  `name` varchar(32) NOT NULL COMMENT 'The name of the tile set.',
  `imagedata` longblob NOT NULL COMMENT 'The image data; should be done consistently.',
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'The unique identification',
  `tilecount` int(11) NOT NULL COMMENT 'Number of tiles we''re dealing with.',
  `tilewidth` int(11) NOT NULL COMMENT 'The width of the tile.',
  `description` varchar(1024) DEFAULT NULL COMMENT 'Description of the image.',
  `defaulttile` int(11) NOT NULL DEFAULT '0' COMMENT 'The default tile to use.',
  `usebackground` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Determines whether or not the rendering engine shoudl draw a background. This is useful for games which have their own backgrounds.',
  `fullwidth` int(11) DEFAULT NULL COMMENT 'The full width of the image.',
  `fullheight` int(11) DEFAULT NULL COMMENT 'The full height of the image.',
  `border` int(11) NOT NULL DEFAULT '0' COMMENT 'Describes the padding/border around the tiles themselves.',
  `spacing` int(11) NOT NULL DEFAULT '0' COMMENT 'Describes the number of pixels between each tile.',
  PRIMARY KEY (`id`)
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chunks`
--

DROP TABLE IF EXISTS `chunks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chunks` (
  `xcoord` int(11) NOT NULL COMMENT 'X coordinate of the chunk.',
  `ycoord` int(11) NOT NULL COMMENT 'Y-coordinate of the chunk.',
  `realmid` int(11) NOT NULL COMMENT 'Realm ID of the chunk.',
  `tileset` int(11) NOT NULL DEFAULT '-1' COMMENT 'The tileset of this chunk. To be deprecated.',
  `data` text NOT NULL COMMENT 'Tile data',
  `lastupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Time we last updated our table. Must be populated manually when updated.',
  `detaildata` text COMMENT 'Extra tile data, only set as needed. Deprecated due to layering.',
  `userid` int(11) NOT NULL COMMENT 'The unique identification of the last user to change it.',
  PRIMARY KEY (`xcoord`,`ycoord`,`realmid`)
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_order`
--

DROP TABLE IF EXISTS `t_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `customer` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);


SET MODE MySQL;

DROP TABLE IF EXISTS `realms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `realms` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'The ID of the realm.',
  `name` varchar(80) NOT NULL COMMENT 'Name of the realm.',
  `description` text NOT NULL COMMENT 'A detailed description.',
  `tileset` int(11) NOT NULL DEFAULT '1' COMMENT 'The tileset of choice for the realm.',
  `defaulttile` int(11) NOT NULL DEFAULT '0' COMMENT 'The tile to use for empty chunks.',
  `public` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Determines whether or not a realm is publicly visible.',
  `ownerid` int(11) DEFAULT NULL COMMENT 'The owner, if any, of the realm. Can be null (no owner assigned). If an owner is present, they have the ability to control access to the realm.',
  `sublayer` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Determines if this realm is actually a sub-layer.\n',
  `tileWidth` int(11) DEFAULT '32' COMMENT 'Defines the width of a tile used on the map.',
  `tileHeight` int(11) DEFAULT '32' COMMENT 'Defines the height of a tile used on a map.',
  `publicflag` bit(1) NOT NULL DEFAULT 1 COMMENT 'Determines whether or not a realm is publicly visible.',
  PRIMARY KEY (`id`),
  KEY `ownerid` (`id`)
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `userauth`
--


 
DROP TABLE IF EXISTS `layerdata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `layerdata` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `realmid` int(11) NOT NULL,
  `ordernum` int(11) DEFAULT NULL,
  `name` varchar(80) DEFAULT NULL COMMENT 'Names the layer. This is best used to declare purpose, but it is not necessary.',
  `masterrealmid` int(11) NOT NULL COMMENT 'Serves as the ''root'' realm indicator. Allows us to query the associate layers quickly.',
  `defaultVisibility` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Determines, on load, whether this layer is visible.  Typically, this is best used for metadata layers such as collision or boundary data.',
  PRIMARY KEY (`id`),
  KEY `realms` (`realmid`),
  KEY `realms_fk` (`realmid`),
  KEY `masterrealms_fk` (`masterrealmid`),
  KEY `realms3_fk` (`masterrealmid`),
  CONSTRAINT `realms3_fk` FOREIGN KEY (`masterrealmid`) REFERENCES `realms` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `realms_fk` FOREIGN KEY (`id`) REFERENCES `realms` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tileproperties`
--

DROP TABLE IF EXISTS `tileproperties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tileproperties` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `realmid` int(11) NOT NULL COMMENT 'The realm this information is associated with. Note this needs to be the root-realm, not the layers.',
  `tileindex` int(11) NOT NULL COMMENT 'The index of the tile within the realm to annotate',
  `name` varchar(200) NOT NULL COMMENT 'The name of the attribute to add.',
  `value` text NOT NULL COMMENT 'The value to assign. ',
  `tilesetindex` int(11) NOT NULL DEFAULT '-1' COMMENT 'The id of the tileset to which this tile definition belongs to.',
  PRIMARY KEY (`id`),
  KEY `REALM_INDEX` (`realmid`),
  KEY `realmlink_fk` (`realmid`),
  CONSTRAINT `realmlink_fk` FOREIGN KEY (`realmid`) REFERENCES `realms` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `item`
--

DROP TABLE IF EXISTS `item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `desireability` int(11) NOT NULL,
  `price` double NOT NULL,
  `product` varchar(255) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
  `order_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK22EF33F6F8171A` (`order_id`),
  CONSTRAINT `FK22EF33F6F8171A` FOREIGN KEY (`order_id`) REFERENCES `t_order` (`id`)
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `realmtilesets`
--

DROP TABLE IF EXISTS `realmtilesets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `realmtilesets` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `realmid` int(11) NOT NULL,
  `tilesetid` int(11) NOT NULL,
  `order` int(11) NOT NULL DEFAULT '0',
  `startGid` int(11),
  `endGid` int(11),
  PRIMARY KEY (`id`),
  KEY `realm_fk` (`realmid`),
  KEY `tileset_fk` (`tilesetid`),
  CONSTRAINT `realm_fk` FOREIGN KEY (`realmid`) REFERENCES `realms` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `tileset_fk` FOREIGN KEY (`tilesetid`) REFERENCES `tilelib` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `objects`
--

DROP TABLE IF EXISTS `objects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `objects` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'The unique identifier of the object',
  `tilerealm` int(11) NOT NULL COMMENT 'The realm of the tile.',
  `tilexcoord` int(11) NOT NULL COMMENT 'The x coordinate of the tile this is attached to.',
  `tileycoord` int(11) NOT NULL COMMENT 'The y coordinate of the tile this object is attached to.',
  `offsetx` int(11) NOT NULL COMMENT 'The x-offset of the object ON the tile.',
  `offsety` int(11) NOT NULL COMMENT 'The y-offset of the object ON the tile.',
  `definition` int(11) NOT NULL COMMENT 'The definition of the object to use.',
  `custom` varchar(1024) DEFAULT NULL COMMENT 'Custom information about the object. Serves as a refinement of definition, but limited in size.',
  `lastupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'The last time this object was updated. Very important for incremental operations.',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Indicates whether or not this object was deleted.  This is crucial to insuring objects are correctly tracked across several simultaneous instances, and even provides a simple means of ''undoing'' a prior delete.  This automatically requires that we periodically flush out deleted objects in order to keep space down.',
  `width` int(11) NOT NULL DEFAULT '-1' COMMENT 'Instance width. Optional. Only for use with regions.',
  `height` int(11) NOT NULL DEFAULT '-1' COMMENT 'Instance height. Optional, only for use with regions.',
  PRIMARY KEY (`id`)
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `realms`
--

DROP TABLE IF EXISTS `userauth`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `userauth` (
  `idauth` int(11) NOT NULL AUTO_INCREMENT COMMENT 'A unique tag for this particular authentication.',
  `userid` int(11) NOT NULL COMMENT 'The user ID we''re checking for.',
  `objtype` VARCHAR(50) NOT NULL COMMENT 'Determines the type of entity we''re going to check for.  Currently, it can be a realm, an image, or some right to do something in the system.',
  `objid` int(11) NOT NULL COMMENT 'The ID of the entity we''re verifying against.',
  `authtype` varchar(20) NOT NULL DEFAULT 'write' COMMENT 'Determines the kind of authorization for access, if applicable.',
  PRIMARY KEY (`idauth`),
  KEY `userid` (`userid`)
);
/*!40101 SET character_set_client = @saved_cs_client */;


-- Dump completed on 2013-12-19 19:14:00