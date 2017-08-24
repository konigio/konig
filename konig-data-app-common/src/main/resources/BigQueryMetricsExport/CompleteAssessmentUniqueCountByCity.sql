SELECT 
  split(locationType,':')[OFFSET(3)] as country,
  split(locationType,':')[OFFSET(4)] as state,
  split(locationType,':')[OFFSET(5)] as city,
  assessmentType,
  STRUCT(
    "http://www.w3.org/2006/time#unitMonth" as durationUnit,
     TIMESTAMP(CONCAT(CAST(year As String),"-",CAST(month As String) ,"-01")) as intervalStart) as timeInterval,   
  uniqueCount	
  FROM (
    SELECT location.type as locationType, object.type as assessmentType,COUNT(DISTINCT actor.id) as uniqueCount, 
      EXTRACT(Month FROM eventTime) as month,
      EXTRACT(Year FROM eventTime) as year
    FROM xas.CompleteAssessment, UNNEST(location) AS location 
    WHERE location.id = 'schema:City'	
    GROUP BY locationType, assessmentType , month,  year
    ORDER BY year,month
   )
