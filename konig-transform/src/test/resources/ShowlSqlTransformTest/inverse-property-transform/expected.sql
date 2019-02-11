INSERT INTO example.BookTarget (id, name, author, isbn)
SELECT
   CONCAT("http://example.com/book/", a.most_famous_book_isbn) AS id,
   a.most_famous_book_title AS name,
   STRUCT(
      CONCAT("http://example.com/person/", a.person_id) AS id,
      a.person_name AS name
   ) AS author,
   a.most_famous_book_isbn AS isbn
FROM example.PersonSource AS a