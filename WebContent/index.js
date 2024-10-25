/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */

let cart = $("#cart_form");

/**
 * Handle the data returned by IndexServlet
 * @param resultDataString jsonObject, consists of session info
 */
function handleSessionData(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle session response");
    console.log(resultDataJson);
    console.log(resultDataJson["sessionId"]);

    // show the session information
    $("#sessionId").text("Session ID: " + resultDataJson["sessionId"]);
    $("#lastAccessTime").text("Last access time: " + resultDataJson["lastAccessTime"]);

    // show cart information
    handleCartArray(resultDataJson["previousItems"]);

    // call functions to populate genres and alphabet browsing options
    loadGenres(resultDataJson["genres"]);
    loadTitleLetters();
}

/**
 * Handle the items in item list
 * @param resultArray jsonObject, needs to be parsed to html
 */
function handleCartArray(resultArray) {
    console.log(resultArray);
    let item_list = $("#item_list");
    let res = "<ul>";

    if (resultArray.length === 0) {
        item_list.html("<p>No items in cart.</p>")
    } else {
        // change it to html list
        //let res = "<ul>";
        for (let i = 0; i < resultArray.length; i++) {
            // each item will be in a bullet point
            res += "<li>" + resultArray[i] + "</li>";
        }
    }
    res += "</ul>";

    // clear the old array and show the new array in the frontend
    item_list.html("");
    item_list.append(res);
    //item_list.html(res);
}

/**
 * Submit form content with POST method
 * @param cartEvent
 */
function handleCartInfo(cartEvent) {
    console.log("submit cart form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    cartEvent.preventDefault();

    $.ajax("api/index", {
        method: "POST",
        data: cart.serialize(),
        success: resultDataString => {
            let resultDataJson = JSON.parse(resultDataString);
            handleCartArray(resultDataJson["previousItems"]);
        },
        error: (jqXHR, textStatus, errorThrown) => {
            console.error("Error adding item: ", textStatus, errorThrown);
            alert("Failed to add item. Please try again.");
        }
    });

    // clear input form
    cart[0].reset();
}

/**
 * Load genres and display as links for browsing movies by genre
 */
function loadGenres() {
    // fetch genres from server if they are not already part of session
    fetch("api/index?action=getGenres")
        .then(response => response.json())
        .then(genres => {
            const genreContainer = document.getElementById("genres_list");
            genreContainer.innerHTML = "";

            genres.forEach(genre => {
                const link = document.createElement("a");
                link.href = "movies.html?genreId=" + genre.id;
                link.textContent = genre.name;
                // link.onclick = function () {
                //     browseMoviesByGenre(genre.id);
                // };
                genreContainer.appendChild(link);
                genreContainer.appendChild(document.createTextNode(" | "));
            });
        });
}

/**
 * Load alphabet letters for browsing movies by title.
 */
function loadTitleLetters() {
    const titleContainer = document.getElementById("alphabet_list");
    const letters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ*".split("");

    titleContainer.innerHTML = "";

    letters.forEach(letter => {
        const link = document.createElement("a");
        link.href = "#";
        link.textContent = letter;
        link.onclick = function () {
            browseMoviesByTitle(letter);
        };
        titleContainer.appendChild(link);
        titleContainer.appendChild(document.createTextNode(" | "));
    });
}

// /**
//  * Fetch movies by genre from server and display them
//  */
// function browseMoviesByGenre(genreId) {
//     // fetch(`api/index?action=getMoviesByGenre&genreId=${genreId}`)
//     //     .then(response => response.json())
//     //     .then(movies => {
//     //         displayMovies(movies);
//     //     });
//
//     //redirect this to movies.html in order to handle in new window
//     window.location.href = `movies.html?genreId=${genreId}`;
// }

/**
 * Fetch movies by title from server and display them
 */
function browseMoviesByTitle(letter) {
    // fetch(`api/index?action=getMoviesByTitle&letter=${letter}`)
    //     .then(response => response.json())
    //     .then(movies => {
    //         displayMovies(movies);
    //     });
    //redirect this to movies.html in order to handle in new window
    window.location.href = `movies.html?letter=${letter}`;
}

/**
 * Display movies in movie container
 */
function displayMovies(movies) {
    const movieContainer = document.getElementById("filtered_movies_list");
    movieContainer.innerHTML = "";

    if (typeof movies === "string") {
        try {
            movies = JSON.parse(movies);
        } catch (error) {
            movieContainer.textContent = "Error parsing";
            console.error("failed to parse", error);
            return;
        }
    }


    if (!Array.isArray(movies)) {
        movieContainer.textContent = "No movies found.";
        return;
    }

    if (movies.length === 0) {
        movieContainer.textContent = "No movies found.";
        return;
    }

    movies.forEach(movie => {
        const movieElement = document.createElement("div");
        movieElement.textContent = `${movie.title} (${movie.year}), directed by ${movie.director}`;
        movieContainer.appendChild(movieElement);
    });
}

$("#search_form").submit(function (event) {
    event.preventDefault();
    let title = $("#search_title").val();
    let year = $("#search_year").val();
    let director = $("#search_director").val();
    let star = $("#search_star").val();

    // $.ajax("api/index?action=searchMovies", {
    //     method: "GET",
    //     data: {title : title, year : year, director : director, star : star},
    //     success: function(movies) {
    //         displayMovies(movies);
    //     },
    //     error: function (jqXHR, textStatus, errorThrown) {
    //         alert("failed to search movies: " + errorThrown);
    //     }
    // });

    let queryParams = $.param({title:title, year:year, director:director, star:star})

    window.location.href = `movies.html?${queryParams}`;
});

$.ajax("api/index", {
    method: "GET",
    success: handleSessionData
});

// Bind the submit action of the form to a event handler function
cart.submit(handleCartInfo);