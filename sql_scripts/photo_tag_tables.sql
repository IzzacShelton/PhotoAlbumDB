drop database if exists PhotoApp;
create database PhotoApp;

use PhotoApp;

create table Tags(
  tagID serial primary key,
  title varchar(64) not null unique,
  tagColor mediumint unsigned default 0xFFFFFF, -- Default to 'white'
  tagType varchar(64)
);

create table Photo(
  photoID serial primary key,
  filepath varchar(256) unique,
  latitude decimal(8,6),
  longitude decimal(9,6),
  imageWidth int unsigned,
  imageHeight int unsigned,
  dateTimeAdded datetime default current_timestamp
)