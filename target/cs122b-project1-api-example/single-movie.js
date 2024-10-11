/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it knows which id to look for
 *      2. Use jQuery to talk to backend API to get the json data
 *      3. Populate the data to correct html elements
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Uses regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return "";

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, reads the jsonObject and populates data into html elements
 * @param resultData jsonObject
 */
function handleResult(resultData) {
    let homeElement = jQuery("#home");
    homeElement.append("<a href='index.html'>Home</a>");

    console.log("handleResult: populating movie info from resultData");

    // Populates movie info h3
    // Finds empty h3 body by id "movie_info"
    let movieInfoElement = jQuery("#movie_info");
    movieInfoElement.append("<p id='movie_information'>" + resultData["movie_title"] + " (" + resultData["movie_year"] + ")" + "</p>" +
        "<p id='movie_director'>Director: " + resultData["movie_director"] + "</p>"
    );

    console.log("handleResult: populating star table from resultData");

    // Populates movie details table
    // Finds empty table body by id "movie_details_body
    let movieDetailsBodyElement = jQuery("#movie_details_body");
    let rowHTML = "";
    rowHTML += "<tr>";
    rowHTML += "<td>" + (resultData["genres"] || "N/A") + "</td>"; // Default to N/A if genres are missing

    // Adds stars info
    let starsHTML = "";
    let starsArray = resultData["stars"] ? resultData["stars"].split(",") : [];
    for (let star of starsArray) {
        let [id, name] = star.split(":");
        starsHTML += "<a href='single-star.html?id=" + id + "'>" + name + "</a><br>";

    }
    rowHTML += "<td>" + starsHTML + "</td>";
    rowHTML += "<td>" + (resultData["rating"] || "N/A") + "</td>"; // Default to N/A if rating is missing
    rowHTML += "</tr>";

    // Append the row created to the table body
    movieDetailsBodyElement.append(rowHTML);
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by MoviesServlet in Movies.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleMovieServlet
});