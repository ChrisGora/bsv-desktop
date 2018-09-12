USE bristol_streetview_schema;

SELECT id FROM (

  SELECT * FROM Photo
  WHERE bucketName = "bristol-streetview-photos"
  ORDER BY photoTimestamp

) a;
