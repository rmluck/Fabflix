/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */

/**
 * CHANGING THIS TO HANDLE MOVIE DATA
 * AND CREATE MOVIE LIST PAGE
 * REVERT BACK TO LAST PUSH IF ISSUES
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");
    console.log("Number of results: ", resultData.length);


    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 20 entries, requirement
    for (let i = 0; i < Math.min(20, resultData.length); i++) {
        console.log("Processing movie ID: ", resultData[i]["movie_id"]);

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        // rowHTML += "<th>" + '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
        //     + resultData[i]["title"] + '</a>' + "</th>"; //creates movie entry and url to single movie page
        rowHTML += "<th>" + resultData[i]["movie_id"] + "</th>";
        rowHTML += "<th>" + resultData[i]["year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["director"] + "</th>";
        rowHTML += "<th>" + resultData[i]["rating"] + "</th>";
        rowHTML += "<th>" + resultData[i]["genres"] + "</th>";
        rowHTML += "<th>" + resultData[i]["stars"] + "</th>";

        // let starsHTML = "";
        // for (let j = 0; j < resultData[i]["stars"].length; j++) {
        //     starsHTML +=
        //         "<th>" +
        //         // Add a link to single-star.html with id passed with GET url parameter
        //         '<a href="single-star.html?id=' + resultData[i]['stars'][j]['star_id'] + '">'
        //         + resultData[i]['stars'][j]['star_name'] + '</a>';
        //     if (j < resultData[i]['stars'].length - 1) {
        //         starsHTML += ', ';
        //     }
        // }
        // rowHTML += "</th>" + starsHTML + "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleMovieResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies", // Setting request url, which is mapped by MovieServlet.java
    success: (resultData) => handleMovieResult(resultData)// Setting callback function to handle data returned successfully by the MoviesServlet
});