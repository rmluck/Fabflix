/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */

let currentPage = 1;
let moviesPerPage = 10;
let totalPages = 1;
let resultData = [];
let titleSortDirection = "asc";
let ratingSortDirection = "asc";

function updateMoviesPerPage() {
    moviesPerPage = parseInt(document.getElementById("movies_per_page").value);
    totalPages = Math.ceil(resultData.length / moviesPerPage);
    currentPage = 1;
    updatePaginationControls();
    displayCurrentMoviesPage();
}

function nextPage() {
    if (currentPage < totalPages) {
        currentPage++;
        displayCurrentMoviesPage();
    }
}

function prevPage() {
    if (currentPage > 1) {
        currentPage--;
        displayCurrentMoviesPage();
    }
}

function updatePaginationControls() {
    document.getElementById("page_number").innerText = `Page ${currentPage} of  ${totalPages}`;
    document.getElementById("prev_button").disabled = currentPage === 1;
    document.getElementById("next_button").disabled = currentPage === totalPages;
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param data jsonObject
 */
function handleMovieResult(data) {
    console.log("handleMoviesResult: populating movies table from data");
    console.log("Number of results: ", data.length);

    resultData = data;
    totalPages = Math.ceil(resultData.length / moviesPerPage);
    currentPage = 1;
    displayCurrentMoviesPage();
}

function displayCurrentMoviesPage() {
    let moviesTableBodyElement = jQuery("#movies_table_body");
    moviesTableBodyElement.empty();

    let startIndex = (currentPage - 1) * moviesPerPage;
    let endIndex = Math.min(startIndex + moviesPerPage, resultData.length);

    for (let i = startIndex; i < endIndex; i++) {
        let rowHTML = "<tr>";
        rowHTML += "<td><a href='single-movie.html?id=" + resultData[i]["id"] + "'>" + resultData[i]["title"] + "</a></td>";
        rowHTML += "<td>" + resultData[i]["year"] + "</td>";
        rowHTML += "<td>" + resultData[i]["director"] + "</td>";

        let genresArray = resultData[i]["genres"].split(",");
        let genresHTML = "<td colspan='3'>";
        genresArray.forEach((genre, index) => {
            let [genreId, genreName] = genre.split(":");
            console.log("genreId: ", genreId, " genreName: ", genreName);
            genresHTML += `<a href='movies.html?genreId=${genreId.trim()}'>${genreName}</a>`;
            if (index < genresArray.length - 1) {
                genresHTML += ", ";
            }
        });
        genresHTML += "</td>";
        rowHTML += genresHTML;

        let starsArray = resultData[i]["stars"].split(",");
        let starsHTML = "<td colspan='3'>";
        starsArray.forEach((star, index) => {
            let [starId, starName] = star.split(":");
            console.log("starId: ", starId, " starName: ", starName);
            starsHTML += `<a href='single-star.html?id=${starId.trim()}'>${starName}</a>`;
            if (index < starsArray.length - 1) {
                starsHTML += ", ";
            }
        });
        starsHTML += "</td>";
        rowHTML += starsHTML;

        rowHTML += "<td>" + resultData[i]["rating"] + "</td>";
        rowHTML += "</tr>";

        moviesTableBodyElement.append(rowHTML);
    }
    updatePaginationControls();
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
    resultData.sort((a, b) => {
        let titleA = a.title.toLowerCase();
        let titleB = b.title.toLowerCase();

        if (titleSortDirection === "asc") {
            return titleA > titleB ? 1 : -1;
        } else {
            return titleA < titleB ? 1: -1;
        }
    });

    titleSortDirection = titleSortDirection === "asc" ? "desc" : "asc";

    currentPage = 1;
    displayCurrentMoviesPage();
}

function sortMoviesByRating() {
    resultData.sort((a, b) => {
        let ratingA = parseFloat(a.rating);
        let ratingB = parseFloat(b.rating);

        if (ratingSortDirection === "asc") {
            return ratingA > ratingB ? 1 : -1;
        } else {
            return ratingA < ratingB ? 1 : -1;
        }
    });

    ratingSortDirection = ratingSortDirection === "asc" ? "desc" : "asc";

    currentPage = 1;
    displayCurrentMoviesPage()
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
$(document).ready(function() {
    // Handle logout form submission
    // $("#logout_form").on("submit", function (event) {
    //     // Prevent default form submission
    //     event.preventDefault();
    //
    //     jQuery.ajax({
    //         type: "POST",
    //         url: "/api/logout",
    //         dataType: "json",
    //         success: function (response) {
    //             if (response.status === "success") {
    //                 // Logout successful, redirect to login page
    //                 window.location.href = "login.html";
    //             } else {
    //                 // Handle error case (no active session)
    //                 alert(response.message);
    //             }
    //         },
    //         error: function () {
    //             // Handle AJAX error
    //             alert("Error logging out. Please try again.");
    //         }
    //     });
    // });

    const queryParams = getQueryParams();

    let apiURL = "api/movies";

    if (queryParams.genreId) {
        apiURL += `?action=getMoviesByGenre&genreId=${queryParams.genreId}`;
    } else if (queryParams.letter) {
        apiURL += `?action=getMoviesByTitle&letter=${queryParams.letter}`;
    } else if (queryParams.title || queryParams.year || queryParams.director || queryParams.star) {
        apiURL += `?action=searchMovies&${$.param(queryParams)}`;
    } else {
        $("#top20_button").on("click", function () {
            fetchMovies(apiURL);
        });
        return;
    }

    fetchMovies(apiURL);
});

function fetchMovies(apiURL){
    // Existing AJAX call to fetch movie data
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: apiURL,
        success: (resultData) => handleMovieResult(resultData),
        error: (jqXHR, textStatus, errorThrown) => {
            console.error("error fetching movies:", errorThrown);
            alert("failed to fetch movies.")
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