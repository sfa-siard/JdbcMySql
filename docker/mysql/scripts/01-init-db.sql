CREATE USER 'testuser'@'%' IDENTIFIED BY 'testpwd';
CREATE SCHEMA testschema;
GRANT ALL PRIVILEGES ON testschema.* TO 'testuser'@'%';