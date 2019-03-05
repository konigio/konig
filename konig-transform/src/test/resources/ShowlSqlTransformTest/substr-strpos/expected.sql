INSERT INTO schema.Person (id, name)
SELECT
   CONCAT("http://example.com/person/", a.person_id) AS id,
   SUBSTR(a.info, STRPOS(a.info, "MDM:") + 4, a.length - 2) AS name
FROM schema.Person AS a