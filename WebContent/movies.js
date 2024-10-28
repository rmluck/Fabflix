/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */

let totalMovies = 0;
let moviesPerPage = 10;
let currentPage = 1;
let totalPages = 1;
let data = [];
let titleSortDirection = "desc";
let ratingSortDirection = "desc";
let sortPriority = "r";
let apiURL = "api/movies";
let searchQuery = "";

function savePageState() {
    $.post("api/session", {
        currentPage: currentPage,
        moviesPerPage: moviesPerPage,
        searchQuery: searchQuery,
        titleSortDirection: titleSortDirection,
        ratingSortDirection: ratingSortDirection,
        sortPriority: sortPriority
    });
}

function loadPageState(callback) {
    $.get("api/session", function(data) {
        if (data) {
            currentPage = data.currentPage;
            moviesPerPage = data.moviesPerPage;
            searchQuery = data.searchQuery;
            titleSortDirection = data.titleSortDirection;
            ratingSortDirection = data.ratingSortDirection;
            sortPriority = data.sortPriority;
        }

        if (typeof callback === "function") {
            callback();
        }
    })
}

function updateMoviesPerPage() {
    moviesPerPage = parseInt(document.getElementById("movies_per_page").value);
    currentPage = 1;
    // updatePaginationControls();
    // displayCurrentMoviesPage();
    let currentAPIURL = apiURL + searchQuery;
    if (sortPriority === "r") {
        currentAPIURL += `&sortOptions=r.rating%20${ratingSortDirection},m.title%20${titleSortDirection}`;
    } else if (sortPriority === "t") {
        currentAPIURL += `&sortOptions=m.title%20${titleSortDirection},r.rating%20${ratingSortDirection}`;
    }
    currentAPIURL += `&moviesPerPage=${moviesPerPage}`;
    currentAPIURL += `&page=${currentPage}`;
    savePageState();
    fetchMovies(currentAPIURL);
}

function nextPage() {
    if (currentPage < totalPages) {
        currentPage++;
        let currentAPIURL = apiURL + searchQuery;
        if (sortPriority === "r") {
            currentAPIURL += `&sortOptions=r.rating%20${ratingSortDirection},m.title%20${titleSortDirection}`;
        } else if (sortPriority === "t") {
            currentAPIURL += `&sortOptions=m.title%20${titleSortDirection},r.rating%20${ratingSortDirection}`;
        }
        currentAPIURL += `&moviesPerPage=${moviesPerPage}`;
        currentAPIURL += `&page=${currentPage}`;
        savePageState();
        fetchMovies(currentAPIURL);
    }
}

function prevPage() {
    if (currentPage > 1) {
        currentPage--;
        let currentAPIURL = apiURL + searchQuery;
        if (sortPriority === "r") {
            currentAPIURL += `&sortOptions=r.rating%20${ratingSortDirection},m.title%20${titleSortDirection}`;
        } else if (sortPriority === "t") {
            currentAPIURL += `&sortOptions=m.title%20${titleSortDirection},r.rating%20${ratingSortDirection}`;
        }
        currentAPIURL += `&moviesPerPage=${moviesPerPage}`;
        currentAPIURL += `&page=${currentPage}`;
        savePageState();
        fetchMovies(currentAPIURL);
    }
}

function updatePaginationControls() {
    document.getElementById("page_number").innerText = `Page ${currentPage} of  ${totalPages}`;
    document.getElementById("prev_button").disabled = currentPage === 1;
    document.getElementById("next_button").disabled = currentPage === totalPages;
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleMoviesResult(resultData) {
    loadPageState(() => {
        console.log("API Response: ", resultData);

        data = resultData.movies;
        totalMovies = resultData.totalMovies;
        totalPages = resultData.totalPages;

        console.log("handleMoviesResult: populating movies table from data");
        console.log("Number of results: ", data.length);

        displayCurrentMoviesPage();
    });
}

function displayCurrentMoviesPage() {
    let moviesTableBodyElement = jQuery("#movies_table_body");
    moviesTableBodyElement.empty();

    for (let i = 0; i < data.length; i++) {
        let rowHTML = "<tr>";
        rowHTML += "<td><a href='single-movie.html?id=" + data[i]["id"] + "'>" + data[i]["title"] + "</a></td>";
        rowHTML += "<td>" + data[i]["year"] + "</td>";
        rowHTML += "<td>" + data[i]["director"] + "</td>";

        let genresArray = data[i]["genres"].split(",");
        let genresHTML = "<td colspan='3'>";
        genresArray.forEach((genre, index) => {
            let [genreId, genreName] = genre.split(":");
            // console.log("genreId: ", genreId, " genreName: ", genreName);
            genresHTML += `<a href='movies.html?genreId=${genreId.trim()}'>${genreName}</a>`;
            if (index < genresArray.length - 1) {
                genresHTML += ", ";
            }
        });
        genresHTML += "</td>";
        rowHTML += genresHTML;

        let starsArray = data[i]["stars"].split(",");
        let starsHTML = "<td colspan='3'>";
        starsArray.forEach((star, index) => {
            let [starId, starName] = star.split(":");
            // console.log("starId: ", starId, " starName: ", starName);
            starsHTML += `<a href='single-star.html?id=${starId.trim()}'>${starName}</a>`;
            if (index < starsArray.length - 1) {
                starsHTML += ", ";
            }
        });
        starsHTML += "</td>";
        rowHTML += starsHTML;

        rowHTML += "<td>" + data[i]["rating"] + "</td>";
        rowHTML += `<td id="movies_add_to_cart"><button id="movies_add_to_cart_button" onclick="addToCart('${data[i]["id"]}', '${data[i]["title"]}', '${data[i]["year"]}')">Add to Cart</button></td>`;
        rowHTML += "</tr>";

        moviesTableBodyElement.append(rowHTML);
    }
    updatePaginationControls();
}


function addToCart(movieId, movieTitle, movieYear) {
    // Send a POST request to add the movie to the cart
    $.ajax({
        url: 'api/cart',
        method: 'POST',
        data: {
            action: 'add',
            movieId: movieId,
            title: movieTitle,
            year: movieYear
        },
        success: function(response) {
            alert(movieTitle + " added to cart!");
        },
        error: function(error) {
            console.error("Error adding to cart:", error);
            alert("Failed to add movie to cart.");
        }
    });
}

// $(document).ready(function() {
//     // Handle the "View Cart" button click
//     $('#movies_view_cart_button').on('click', function() {
//         loadCartItems();
//     });
// });


function loadCartItems() {
    $.ajax({
        url: 'api/cart',
        method: 'GET',
        dataType: 'json',
        success: function(cartItems) {
            if (cartItems.length === 0) {
                alert('Your cart is empty.');
            } else {
                console.log(cartItems);
            }
        },
        error: function() {
            alert('Failed to load cart items.');
        }
    });
}


function getQueryParams() {
    const params = {};
    const queryString = window.location.search.substring(1);
    const queryArray = queryString.split("&");

    for (let i = 0; i < queryArray.length; i++) {
        const pair = queryArray[i].split("=");
        if (pair.length === 2) {
            params[decodeURIComponent(pair[0])] = decodeURIComponent(pair[1]);
        }
    }
    return params;
}

function sortMoviesByTitle() {
    titleSortDirection = titleSortDirection === "asc" ? "desc" : "asc";
    sortPriority = "t";
    currentPage = 1;
    let currentAPIURL = apiURL + searchQuery;
    currentAPIURL += `&sortOptions=m.title%20${titleSortDirection},r.rating%20${ratingSortDirection}`;
    currentAPIURL += `&moviesPerPage=${moviesPerPage}`;
    currentAPIURL += `&page=${currentPage}`;
    savePageState();
    fetchMovies(currentAPIURL);
}

function sortMoviesByRating() {
    ratingSortDirection = ratingSortDirection === "asc" ? "desc" : "asc";
    sortPriority = "r";
    currentPage = 1;
    let currentAPIURL = apiURL + searchQuery;
    currentAPIURL += `&sortOptions=r.rating%20${ratingSortDirection},m.title%20${titleSortDirection}`;
    currentAPIURL += `&moviesPerPage=${moviesPerPage}`;
    currentAPIURL += `&page=${currentPage}`;
    savePageState();
    fetchMovies(currentAPIURL);
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
$(document).ready(function() {
    $('#cart_container').hide();

    $("#movies_view_cart_button").on("click", function() {
        loadCartItems();
        window.location.href = "movie-cart.html";
    });

    loadPageState(() => {
        const queryParams = getQueryParams();
        let sq = "";
        if (queryParams.genreId) {
            sq += `?action=getMoviesByGenre&genreId=${queryParams.genreId}`;
        } else if (queryParams.letter) {
            sq += `?action=getMoviesByTitle&letter=${queryParams.letter}`;
        } else if (queryParams.title || queryParams.year || queryParams.director || queryParams.star) {
            sq += `?action=searchMovies&${$.param(queryParams)}`;
        }

        if (searchQuery !== sq && sq !== "") {
            searchQuery = sq;
        }

        let currentAPIURL = apiURL + searchQuery;
        if (sortPriority === "r") {
            currentAPIURL += `&sortOptions=r.rating%20${ratingSortDirection},m.title%20${titleSortDirection}`;
        } else if (sortPriority === "t") {
            currentAPIURL += `&sortOptions=m.title%20${titleSortDirection},r.rating%20${ratingSortDirection}`;
        }
        currentAPIURL += `&moviesPerPage=${moviesPerPage}`;
        currentAPIURL += `&page=${currentPage}`;

        savePageState();
        fetchMovies(currentAPIURL);
    });
});

function fetchMovies(apiURL){
    // Existing AJAX call to fetch movie data
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: apiURL,
        success: (resultData) => handleMoviesResult(resultData),
        error: (jqXHR, textStatus, errorThrown) => {
            console.error("error fetching movies:", errorThrown);
            alert("failed to fetch movies.")
        }
    });
}

function clearSession() {
    $.ajax({
        url: "api/session",
        method: "DELETE",
        success: () => {
            console.log("Session parameters reset.");
        },
        error: (jqXHR, textStatus, errorThrown) => {
            console.error("Failed to reset session parameters: ", errorThrown);
        }
    });
}

let logout_form = $("#logout_form");

function handleLogoutResult(resultDataJson) {
    console.log("handle logout response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    if (resultDataJson["status"] === "success") {
        window.location.replace("logout.html");
    } else {
        console.log("show error message");
        console.log(resultDataJson["message"]);
    }
}

function submitLogoutForm(formSubmitEvent) {
    console.log("submit logout form");

    formSubmitEvent.preventDefault();

    $.ajax(
        "api/logout", {
            method: "POST",
            success: handleLogoutResult,
            error: handleLogoutResult
        }
    );
}

logout_form.submit(submitLogoutForm)