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


