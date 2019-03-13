INSERT INTO schema.PersonSource (id, mother)
SELECT
   a.id,
   a.parent.id AS mother
FROM schema.Person AS a
WHERE a.parent.gender="Female"