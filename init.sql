
USE mysql;
UPDATE user SET plugin='mysql_native_password' WHERE User='root';
ALTER USER 'root'@'localhost' IDENTIFIED BY 'Cy3M22Yar2UJ';
FLUSH PRIVILEGES;

USE mysql;
CREATE USER 'java-db-client'@'localhost' IDENTIFIED BY 'Re278nErRowD';
GRANT ALL PRIVILEGES ON *.* TO 'java-db-client'@'localhost';
FLUSH PRIVILEGES;

CREATE SCHEMA `bristol_streetview_schema` ;
USE bristol_streetview_schema;
CREATE TABLE Photo
(
  id                 CHAR(32)     NOT NULL
    PRIMARY KEY,
  height             INT          NOT NULL,
  width              INT          NOT NULL,
  photoTimestamp     TIMESTAMP    NULL,
  uploadTimestamp    TIMESTAMP    NULL,
  latitude           DOUBLE       NULL,
  longitude          DOUBLE       NULL,
  routeId            INT          NOT NULL,
  bucketName         VARCHAR(100) NULL,
  fileKey            VARCHAR(200) NULL,
  cameraSerialNumber VARCHAR(45)  NULL,
  locationAccuracy   DOUBLE       NULL,
  bearing            DOUBLE       NULL,
  bearingAccuracy    DOUBLE       NULL,
  CONSTRAINT id_UNIQUE
  UNIQUE (id)
)
  ENGINE = InnoDB
  CHARSET = latin1;



