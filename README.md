# 2024-fall-cs-122b-team_greg_rohan

## PROJECT 4
### DEMO LINK
https://youtu.be/VcpDOyQKEfg

### Team Contributions

Gregory:
- Master-slave replication
- Scaling with cluster of MySQL/Tomcat and load balancer

Rohan:
- Full-text search
- Autocomplete
- JDBC connection pooling

### Deployment Instructions
- Database should already be stored in MySQL beforehand
- Clone repository locally
- Create new project from existing source (use cloned folder) in IntelliJ
- Add new Tomcat local configuration, find Tomcat directory stored locally
- Add .war file as external source for deployment, apply changes
- Create Maven package
- Run locally, application will open in browser
- Connection to database already configured in context settings as long as database exists locally

### Connection Pooling
- Configuration for JDBC connection pooling was implemented in /WebContent/META-INF/context.xml
- factory, maxTotal, maxIdle, and maxWaitMillis attributes added
- JDBC connection url extended with flags: autoReconnect, allowPublicKeyRetrieval, useSSL, and cachePrepStmts (caches prepared statements)
- Defines DataSource (connection pool) which is used by application to connect to database
- In each servlet, application retrieves connection from connection pool by looking up jdbc/moviedbexample resource defined in context.xml file
- Once connection to DataSource is confirmed, it is used to interact with database via PreparedStatements
- PreparedStatements already implemented for all JDBC statements throughout project that involve user input and connecting to database

### Master/Slave Replication and Read/Write Routing
- Changes were made in the web.xml and context.xml in order to handle the load balancing portion of this assignment.
- Changes to xml were for JDNI references to the main db, we now incorporated JDNI references to master and slave dbs, with the appropriate IPs for the instance.
- To put these changes into effect we had to change the way we create our db connections. Instead of creating a datesource referencing our local main db, we had to reference one of the master/slave dbs based on what we were trying to do.
- This led us to create DatabaseConnectionManager.java and DatabaseConnectionManagerListener.java in order to be able to handle the new way we instantiate and create db connections. 

## PROJECT 3

https://www.youtube.com/watch?v=ggu5y8HTk3s

### Team Contributions

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

### Design Decisions
1. All dupes are ignored, based on these conditions 
2. actorsXML: actorId and name already in star table as an entry
3. mainXML: moveId and corresponding movie name already in movie table as an entry
4. castXML: if star id and movieId already match in stars_in_movie table
5. While parsing casts, if actor name not in star table a new star id will be created for the actor if the movie name can be matched to a movie id in movie table, then the star is inserted into star table, if movie id cannot be matched, entry is ignored
6. While parsing main and actor, if id is taken but entry is not considered a dupe, a new id is created  

### Performance Tuning
1. Bare Minimum Dupe checking
2. INSERT IGNORE INTO,  to avoid dupe. 
3. Batch Insertion
4. Data inserted into db in batches.
5. Memory Caching
6. Cache maps for quick lookups of the data.

## PROJECT 2

https://www.youtube.com/watch?v=_sUNhE-zZCw

### Team Contributions

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

### LIKE Predicate
- LIKE predicate for substring pattern matching used in MoviesServlet.java file (lines 123-157) to match inexact patterns for titles, directors, and stars.

## PROJECT 1

https://youtu.be/m8RwyEqivXw?si=A8lPKEtABFEE_7sL

### Team Contributions

Gregory:
- Movie list page
- Single movie page

Rohan:
- Single star page
- All CSS
- Return to main page
