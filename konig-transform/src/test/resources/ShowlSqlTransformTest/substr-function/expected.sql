INSERT INTO schema.Person (id, name)
SELECT
   CONCAT("http://example.com/person/", a.person_id) AS id,
   SUBSTR(a.info, 3, 5) AS name
FROM schema.Person AS a