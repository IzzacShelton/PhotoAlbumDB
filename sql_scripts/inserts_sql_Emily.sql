-- Insert to user table
INSERT INTO Users (Name, Email)
VALUES
    ('Bill_Nye', 'ScienceGuy@example.com'),
    ('Steve_Irwin', 'AustralianMan@example.com'),
    ('George_Lopez', 'LowRider@example.com'),
    ('Elton_John', 'TinyDancer@example.com'),
    ('Robin_Williams', 'carpeperdiem@example.com');
    
-- Insert to tags table
INSERT INTO Tags (Title, TagColor, TagType)
VALUES 
    ('Vacation', 0xFFD700, 'Category'),
    ('Family', 0xFF69B4, 'People'),
    ('Nature', 0x32CD32, 'Category'),
    ('Work', 0x1E90FF, 'Category'),
    ('Friends', 0xFFA500, 'People');
    
-- Insert to camera table
INSERT INTO Camera (Brand, Model, SerialNumber)
VALUES
    ('Canon', 'EOS Rebel T7', 'SN1001'),
    ('Nikon', 'D3500', 'SN1002'),
    ('Sony', 'Alpha a6000', 'SN1003'),
    ('Fujifilm', 'X-T30', 'SN1004'),
    ('Panasonic', 'Lumix GH5', 'SN1005');
    
-- Insert to album table
INSERT INTO Album (OwnerID, AlbumName, AlbumDescription, AlbumType, CreatedAt, AlbumUpdated)
VALUES 
(1, 'Vacation to Mexico', 'Photos from cruise trip', 'User', NOW(), NOW()),
(2, 'Pets', 'Pics of my furry friends', 'Auto', NOW(), NOW()),
(3, 'Friends', 'Pics of my besties', 'Library', NOW(), NOW()),
(1, 'Family Album', 'Family images', 'User', NOW(), NOW()),
(2, 'Me', 'Selfies and other pictures of me', 'Auto', NOW(), NOW());

-- Emily's insert into photo
call sp_InsertPhoto(@Bill_Nye, '/home/soms/Vault/DBMS Project Photos/Emily/IMG_4850.jpeg', 
 2988641, NULL, NULL, 4032, 3024, '2024-04-20 18:53:09', 
 'Apple', 'iPhone 13 Pro Max', NULL);

call sp_InsertPhoto(@Elton_John, '/home/soms/Vault/DBMS Project Photos/Emily/IMG_6011.jpeg', 
 8163523, NULL, NULL, 8914, 3910, '2025-11-07 09:49:17', 
 'Apple', 'iPhone 11', NULL);

call sp_InsertPhoto(@Steve_Irwin, '/home/soms/Vault/DBMS Project Photos/Emily/IMG_3916.jpeg', 
 2323241, NULL, NULL, 4000, 3000, NULL, 
 NULL, NULL, NULL);

call sp_InsertPhoto(@George_Lopez, '/home/soms/Vault/DBMS Project Photos/Emily/IMG_5978.jpeg', 
 1434221, NULL, NULL, 3088, 2316, '2025-11-04 15:34:35', 
 'Apple', 'iPhone 11', NULL);

call sp_InsertPhoto(@Robbin_Williams, '/home/soms/Vault/DBMS Project Photos/Emily/IMG_1403.jpeg', 
 2105270, 39.060300, -95.646089, 4032, 3024, '2023-12-17 17:33:19', 
 'Apple', 'iPhone 12 Pro Max', NULL);

call sp_InsertPhoto(@Bill_Nye, '/home/soms/Vault/DBMS Project Photos/Emily/IMG_5979.jpeg', 
 3210044, NULL, NULL, 4032, 3024, '2025-11-05 07:33:28', 
 'Apple', 'iPhone 11', NULL);

call sp_InsertPhoto(@Robbin_Williams, '/home/soms/Vault/DBMS Project Photos/Emily/IMG_5579.jpeg', 
 2003150, NULL, NULL, 3088, 2316, '2025-03-18 13:59:46', 
 'Apple', 'iPhone 11', NULL);

call sp_InsertPhoto(@Steve_Irwin, '/home/soms/Vault/DBMS Project Photos/Emily/IMG_1845.jpeg', 
 841901, 39.060342, -95.646150, 1612, 2222, '2024-09-28 20:16:35', 
 'Apple', 'iPhone 12 Pro Max', NULL);