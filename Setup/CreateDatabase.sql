CREATE DATABASE  IF NOT EXISTS `jacobsdefense` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `jacobsdefense`;
-- MySQL dump 10.13  Distrib 5.1.40, for Win32 (ia32)
--
-- Host: localhost    Database: jacobsdefense
-- ------------------------------------------------------
-- Server version	5.5.13

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `objlib`
--

DROP TABLE IF EXISTS `objlib`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `objlib` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'The unique ID of this object library entry.',
  `name` varchar(45) NOT NULL COMMENT 'The name of the object entry; helps to differentiate object purposes.',
  `tilesrc` int(11) NOT NULL COMMENT 'Represents the tile this object is associated with. Not mapped to a foreign key in the tilelib table at this time.',
  `imgXOff` int(11) NOT NULL DEFAULT '0' COMMENT 'The X offset within the specified tile image source of this object''s graphical representation.',
  `imgYOff` int(11) NOT NULL DEFAULT '0' COMMENT 'The Y offset within the specified tile image source of this object''s graphical representation.',
  `imgWidth` int(11) NOT NULL DEFAULT '1' COMMENT 'Width of the image representation in the specified tileset.',
  `imgHeight` int(11) NOT NULL DEFAULT '1' COMMENT 'Height of the image representation in the specified tileset.',
  `description` text COMMENT 'Describes the object in significant detail.',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=latin1 COMMENT='Contains all known object types. This will be refined in the future to specific categorical organizations.';
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
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=165 DEFAULT CHARSET=utf8 COMMENT='This is a table of objects, stored by absolute tile and rela';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `realms`
--

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
  PRIMARY KEY (`id`),
  KEY `ownerid` (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=16 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `userid` int(11) NOT NULL AUTO_INCREMENT COMMENT 'The uniquely generated user ID.',
  `username` varchar(32) NOT NULL COMMENT 'The unique username. Note care must be taken for lowercasing it when checking.',
  `phash` tinyblob NOT NULL COMMENT 'The user''s password hash, precalculated.',
  `email` varchar(300) NOT NULL COMMENT 'The e-mail address of the user.',
  `builderspermit` enum('Bronze','Silver','Gold','Diamond') NOT NULL DEFAULT 'Bronze' COMMENT 'The builder''s permit in the system. This is planned around the idea of different ''permits'' that indicate how much freedom is given to a user. For example, a bronze user may be limited to just a few realms and can only make private objects.  A silver user can make up to 100 realms and has the option of publishing objects.  The Diamond level is reserved for special guests and heavy contributors.  ',
  `description` varchar(2048) DEFAULT NULL COMMENT 'Allows the user to enter information about themselves, links to their home page, etc.',
  `enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Determines whether a user is active or not.  Inactive users cannot login.',
  PRIMARY KEY (`userid`),
  UNIQUE KEY `userid_UNIQUE` (`userid`),
  UNIQUE KEY `username_UNIQUE` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=latin1 COMMENT='The set of system users. Includes their login name, hashed password (maintain those salts!), e-mail address on file, and any personal info needed.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tilelib`
--

DROP TABLE IF EXISTS `tilelib`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=15 DEFAULT CHARSET=utf8 COMMENT='Stores our custom tile information.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `userauth`
--

DROP TABLE IF EXISTS `userauth`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `userauth` (
  `idauth` int(11) NOT NULL AUTO_INCREMENT COMMENT 'A unique tag for this particular authentication.',
  `userid` int(11) NOT NULL COMMENT 'The user ID we''re checking for.',
  `objtype` enum('realm','image','right') NOT NULL COMMENT 'Determines the type of entity we''re going to check for.  Currently, it can be a realm, an image, or some right to do something in the system.',
  `objid` int(11) NOT NULL COMMENT 'The ID of the entity we''re verifying against.',
  PRIMARY KEY (`idauth`),
  KEY `userid` (`userid`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1 COMMENT='Contains all permissions a particular user has to access parts of the system.  ';
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
  `tileset` int(11) NOT NULL COMMENT 'The tileset of this chunk.',
  `data` text NOT NULL COMMENT 'Tile data',
  `lastupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Time we last updated our table.',
  `detaildata` text NOT NULL COMMENT 'Extra tile data, only set as needed.',
  `userid` int(11) NOT NULL COMMENT 'The unique identification of the last user to change it.',
  PRIMARY KEY (`xcoord`,`ycoord`,`realmid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='A high-speed chunk table with realm support.';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2011-11-22 20:22:48
