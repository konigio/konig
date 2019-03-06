INSERT INTO schema.PersonTarget (id, monthCreated)
SELECT
   CONCAT("http://example.com/person/", a.person_id) AS id,
   DATE_TRUNC(a.dateCreated, MONTH) AS monthCreated
FROM schema.PersonSource AS a