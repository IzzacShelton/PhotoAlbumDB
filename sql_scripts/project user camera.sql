# create the database
drop database if exists photoalbum;
create database photoalbum;
use photoalbum;

# create the user table
create table users (
 userid int auto_increment,
 name varchar(100) not null,
 email varchar(100) not null,
 datejoined datetime default current_timestamp,
 constraint users_pk primary key (userid),
 constraint users_email_uk unique (email)
);

# create the camera table
create table camera (
 cameraid int auto_increment,
 brand varchar(100) not null,
 model varchar(100) not null,
 serialnumber varchar(100),
 userid int not null,
 constraint camera_pk primary key (cameraid),
 constraint camera_serialnumber_uk unique (serialnumber),
 constraint camera_users_fk foreign key (userid)
   references users (userid)
);

# display the table structures
describe users;
describe camera;