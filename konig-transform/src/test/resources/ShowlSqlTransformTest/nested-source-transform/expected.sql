INSERT INTO example.PersonTarget (id, uid, name)
SELECT
   CONCAT("http://example.com/person/", a.author_id) AS id,
   a.author_id AS uid,
   a.author_name AS name
FROM example.BookSource AS a