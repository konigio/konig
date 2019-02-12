INSERT INTO example.BookTarget (id, name, author, isbn)
SELECT
   CONCAT("http://example.com/book/", a.book_isbn) AS id,
   a.book_title AS name,
   CONCAT("http://example.com/person/", a.author_id) AS id AS author,
   a.book_isbn AS isbn
FROM example.BookSource AS a