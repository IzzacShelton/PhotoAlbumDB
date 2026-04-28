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

# create the photo table 
create table Photo(
	PhotoID int auto_increment primary key,
	Filepath varchar(256) unique,
	Latitude decimal(8,6),
	Longitude decimal(9,6),
	ImageWidth int unsigned,
	ImageHeight int unsigned,
	DateTimeAdded datetime default current_timestamp
);

# create the user table
create table Users (
	UserId int auto_increment,
	Name varchar(100) not null,
	Email varchar(100) not null,
	DateJoined datetime default current_timestamp(),
	constraint users_pk primary key (UserId),
	constraint users_email_uk unique (Email)
);

# create the album table
CREATE TABLE Album (
	AlbumID INT AUTO_INCREMENT PRIMARY KEY,
	OwnerID INT,
	AlbumName VARCHAR(255),
	AlbumDescription TEXT,
	CreatedAt DATETIME,
	AlbumUpdated DATETIME,
	FOREIGN KEY (OwnerID)
		REFERENCES Users(UserID)
);

# create the camera table
create table Camera (
	CameraId int auto_increment,
	Brand varchar(100) not null,
	Model varchar(100) not null,
	SerialNumber varchar(100),
	UserId int not null,
	constraint camera_pk primary key (CameraId),
	constraint camera_serialnumber_uk unique (SerialNumber),
	constraint camera_users_fk foreign key (UserId)
		references Users(UserId)
);

# display the table structures
describe Tags;
describe Photo;
describe Users;
describe Album;
describe Camera;

