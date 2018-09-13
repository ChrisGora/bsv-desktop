USE bristol_streetview_schema;

SELECT id FROM (

  SELECT * FROM Photo
  WHERE bucketName = "bristol-streetview-photos"
  AND routeId = 21
  ORDER BY photoTimestamp

) a;
