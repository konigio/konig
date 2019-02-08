INSERT INTO schema.PersonTarget (id, name, familyName, givenName)
SELECT
   CONCAT("http://example.com/person/", a.person_id) AS id,
   CONCAT(a.first_name, " ", a.last_name) AS name,
   a.last_name AS familyName,
   a.first_name AS givenName
FROM schema.PersonSource AS a