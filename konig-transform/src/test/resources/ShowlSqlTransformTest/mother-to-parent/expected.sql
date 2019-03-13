INSERT INTO schema.Person (id, parent)
SELECT
   a.id,
   STRUCT(
      a.mother AS id,
      "Female" AS gender
   ) AS parent
FROM schema.PersonSource AS a