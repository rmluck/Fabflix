# 2024-fall-cs-122b-team_greg_rohan

## PROJECT 3
Prokect 3 demo: https://youtu.be/C2YxPHHHT3A?si=H2O6WGw38r3R2nRh
Team Contributions:

Gregory:
- reCaptcha
- HTTPS
- Encrypted passwords for customers
- XML parsing

Rohan:
- PreparedStatement
- Encrypted passwords for employees
- Dashboard
- Stored procedure

Design decisions:
1. all dupes are ignored, based on these conditions 
2. actorsXML: actorId and name already in star table as an entry.
3. mainXML: moveId and corresponding movie name already in movie table as an entry.
4. castXML: if star id and movieId already match in stars_in_movie table.
5. While parsing casts, if actor name not in star table a new star id will be created for the actor if the movie name can be matched to a movie id in movie table, then the star is inserted into star table, if movie id cannot be matched, entry is ignored
6. While parsing main and actor, if id is taken but entry is not considered a dupe, a new id is created.  

Performance Tuning:
	1.	Bare Minimum Dupe checking
	2. INSERT IGNORE INTO,  to avoid dupe. 
	3. Batch Insertion
	4. Data inserted into db in batches.
	5. Memory Caching
	6. Cache maps for quick lookups of the data.


## PROJECT 2

https://www.youtube.com/watch?v=_sUNhE-zZCw

Team Contributions:

Gregory:
- Login page, redirecting to login page
- Getting genres from database
- Search functionality
- Shopping cart, add to cart buttons
- Payment functionality
- Confirmation page

Rohan:
- Main page
- Logout buttons, view cart buttons
- Browse functionality
- Sorting for movie list pages and single pages, previous/next buttons
- Pagination from database
- Jump functionality
- All CSS styling

LIKE predicate for substring pattern matching used in MoviesServlet.java file (lines 123-157) to match inexact patterns for titles, directors, and stars.

## PROJECT 1

https://youtu.be/m8RwyEqivXw?si=A8lPKEtABFEE_7sL

Team Contributions:

Gregory:
-Movie List page
-Single Movie page

Rohan:
-Single Star Page
-CSS for site 
-return to top 20 throughout pages
