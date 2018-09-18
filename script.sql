USE bristol_streetview_schema;

SELECT id FROM (

  SELECT * FROM Photo
  WHERE bucketName = "bsv"
  AND routeId = 5
  ORDER BY photoTimestamp

) a;




