DELIMITER $$

CREATE PROCEDURE add_movie(
    IN movie_title VARCHAR(100),
    IN movie_year INT,
    IN movie_director VARCHAR(100),
    IN star_name VARCHAR(100),
    IN genre_name VARCHAR(32),
    OUT out_movie_id VARCHAR(10),
    OUT out_star_id VARCHAR(10),
    OUT out_genre_id INT
)
BEGIN
    DECLARE movie_id VARCHAR(10);
    DECLARE star_id VARCHAR(10);
    DECLARE genre_id INT;

    -- Step 1: Check if movie already exists in movies table
    SELECT `id` INTO movie_id
    FROM movies
    WHERE `title` = movie_title AND `year` = movie_year AND `director` = movie_director
    LIMIT 1;

    IF movie_id IS NULL THEN

        -- Step 2: Generate a new unique movie_id
        SELECT MAX(`id`) INTO movie_id FROM movies;
        IF movie_id IS NULL THEN
            SET movie_id = 'tt0000001';
        ELSE
            SET movie_id = CONCAT('tt', LPAD(CAST(SUBSTRING(movie_id, 3) AS UNSIGNED) + 1, 7, '0'));
        END IF;

        -- Insert the new movie into movies table
        INSERT INTO movies (`id`, `title`, `year`, `director`) VALUES (movie_id, movie_title, movie_year, movie_director);

        -- Insert a default rating entry for the movie in ratings table
        INSERT INTO ratings (`movieId`, `rating`, `numVotes`) VALUES (movie_id, 0, 0);

        -- Step 3: Check if the star already exists in stars table
        SELECT `id` INTO star_id
        FROM stars
        WHERE `name` = star_name
        LIMIT 1;

        IF star_id IS NULL THEN
            -- Generate a new unique star_id if the star does not exist
            SELECT MAX(`id`) INTO star_id FROM stars;
            IF star_id IS NULL THEN
                SET star_id = 'nm0000001';
            ELSE
                SET star_id = CONCAT('nm', LPAD(CAST(SUBSTRING(star_id, 3) AS UNSIGNED) + 1, 7, '0'));
            END IF;

            -- Insert new star into stars table
            INSERT INTO stars (`id`, `name`, `birthYear`) VALUES (star_id, star_name, NULL);
        END IF;

        -- Link the star to the movie in stars_in_movies table
        INSERT INTO stars_in_movies (`starId`, `movieId`) VALUES (star_id, movie_id);

        -- Step 4: Check if the genre already exists in genres table
        SELECT `id` INTO genre_id
        FROM genres
        WHERE `name` = genre_name
        LIMIT 1;

        IF genre_id IS NULL THEN
            -- Generate a new unique genre_id if the genre does not exist
           SELECT MAX(`id`) INTO genre_id FROM genres;
            IF genre_id is NULL THEN
               SET genre_id = 1;
            ELSE
                SET genre_id = genre_id + 1;
            END IF;

            -- Insert new genre into genres table
            INSERT INTO genres (`id`, `name`) VALUES (genre_id, genre_name);
        END IF;

        -- Link the genre to the movies in genres_in_movies table
        INSERT INTO genres_in_movies (`genreId`, `movieId`) VALUES (genre_id, movie_id);

        SET out_movie_id = movie_id;
        SET out_star_id = star_id;
        SET out_genre_id = genre_id;
    END IF;
END
$$

DELIMITER ;