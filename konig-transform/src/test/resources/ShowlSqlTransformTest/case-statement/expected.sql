INSERT INTO schema.PersonTarget (id, gender)
SELECT
   CONCAT("http://example.com/person/", a.person_id) AS id,
   CASE 
      WHEN a.person_gender="M" THEN "Male"
      WHEN a.person_gender="F" THEN "Female"
   END
FROM schema.PersonSource AS a