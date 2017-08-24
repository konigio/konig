SELECT 
  split(locationType,':')[OFFSET(3)] as country,
  split(locationType,':')[OFFSET(4)] as state,
  split(locationType,':')[OFFSET(5)] as city,
  split(locationType,':')[OFFSET(6)] as school,
  assessmentType,
  STRUCT(
    "http://www.w3.org/2006/time#unitMonth" as durationUnit,
     TIMESTAMP(CONCAT(CAST(year As String),"-",CAST(month As String) ,"-01")) as intervalStart) as timeInterval,   
  uniqueCount	
  FROM (
    SELECT REGEXP_EXTRACT(location.type,'(.*)/grade') as locationType, object.type as assessmentType,COUNT(DISTINCT actor.id) as uniqueCount, 
      EXTRACT(Month FROM eventTime) as month,
      EXTRACT(Year FROM eventTime) as year
    FROM xas.CompleteAssessment, UNNEST(location) AS location 
    WHERE location.id = 'schema:School'	
    GROUP BY locationType, assessmentType , month,  year
    ORDER BY year,month
   )
