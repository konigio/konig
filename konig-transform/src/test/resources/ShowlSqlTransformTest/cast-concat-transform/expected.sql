INSERT INTO schema.PersonTarget (id, name)
SELECT
   CONCAT("http://example.com/person/", a.person_id) AS id,
   CONCAT(CAST(a.float_value AS STRING), FORMAT_DATETIME("%Y-%m-%dT%TZ", a.datetime_value), FORMAT_DATE("%Y-%m-%d", a.date_value)) AS name
FROM schema.PersonSource AS a