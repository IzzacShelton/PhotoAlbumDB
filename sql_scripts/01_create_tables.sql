drop database if exists PhotoApp;
create database PhotoApp;
use PhotoApp;

# create the photo table 
create table Tags(
	TagID int auto_increment primary key,
	Title varchar(64) not null unique,
	TagColor mediumint unsigned default 0xFFFFFF, -- Default to 'white'
	TagType varchar(64)
);

# create the user table
create table Users (
	UserId int auto_increment,
	Name varchar(100) not null,
	Email varchar(100) null,
	DateJoined datetime default current_timestamp(),
	constraint users_pk primary key (UserId)
);

# create the camera table
create table Camera (
	CameraId int auto_increment,
	Brand varchar(100) not null,
	Model varchar(100) not null,
	SerialNumber varchar(100),
	constraint camera_pk primary key (CameraId),
	constraint camera_serialnumber_uk unique (SerialNumber),
	constraint camera_brand_model_uk unique (Brand, Model)
);

# create the photo table 
create table Photo(
	PhotoID int auto_increment primary key,
	CameraID int,
	Filepath varchar(256) unique,
	FileSize bigint,
	Latitude decimal(8,6),
	Longitude decimal(9,6),
	ImageWidth int unsigned,
	ImageHeight int unsigned,
	DateTimeTaken datetime,
	DateTimeAdded datetime default current_timestamp,
	
	foreign key (CameraID) 
		references Camera(CameraID)
);

# create the album table
CREATE TABLE Album (
	AlbumID INT AUTO_INCREMENT PRIMARY KEY,
	OwnerID INT,
	AlbumName VARCHAR(255),
	AlbumDescription TEXT,
	AlbumType Enum('Library', 'Auto', 'User'),
	CreatedAt DATETIME default current_timestamp,
	AlbumUpdated DATETIME,

	FOREIGN KEY (OwnerID)
		REFERENCES Users(UserID)
);

# display the table structures
describe Tags;
describe Photo;
describe Users;
describe Album;
describe Camera;

