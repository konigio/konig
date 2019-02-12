INSERT INTO example.PersonTarget (id, uid, address, familyName, givenName)
SELECT
   CONCAT("http://example.com/person/", a.person_id) AS id,
   a.person_id AS uid,
   STRUCT(
      a.city AS addressLocality,
      a.state AS addressRegion,
      a.street AS streetAddress
   ) AS address,
   b.last_name AS familyName,
   b.first_name AS givenName
FROM example.PersonAddressSource AS a
JOIN example.PersonNameSource AS b
   ON a.person_id=b.person_id