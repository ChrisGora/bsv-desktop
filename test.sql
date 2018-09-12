USE bristol_streetview_schema;

SELECT id FROM (

  SELECT * FROM Photo
  WHERE bucketName = "test_bucket"
  ORDER BY photoTimestamp

) a;
