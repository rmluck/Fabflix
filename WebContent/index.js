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
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");
    console.log("Number of results: ", resultData.length);

    // Populate star table
    // Find empty table body by id "star_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 20 entries, requirement
    for (let i = 0; i < Math.min(20, resultData.length); i++) {
        console.log("Processing movie ID: ", resultData[i]["movie_id"]);

        // Concatenate html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td>" + "<a href='single-movie.html?id=" + resultData[i]["movie_id"] + "'>"
            + resultData[i]["title"] + "</a>" + "</td>"; // Creates movie entry and url to single movie page
        rowHTML += "<td>" + resultData[i]["year"] + "</td>";
        rowHTML += "<td>" + resultData[i]["director"] + "</td>";
        rowHTML += "<td>" + resultData[i]["genres"] + "</td>";

        let starsArray = resultData[i]["stars"].split(",");
        let starsHTML = "<td colspan='3'>";
        for (let j = 0; j < starsArray.length; j++) {
            let [starId, starName] = starsArray[j].split(":");
            // Add link to single-star.html with id passed with GET url parameter
            starsHTML += "<a href='single-star.html?id=" + starId + "'>" + starName + "</a>";
            if (j < starsArray.length - 1) {
                starsHTML += ", ";
            }
        }
        starsHTML += "</td>";
        rowHTML += starsHTML;
        rowHTML += "<td>" + resultData[i]["rating"] + "</td>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
// Makes HTTP GET request and registers on success callback function handleMovieResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies", // Setting request url, which is mapped by MovieServlet.java
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the MoviesServlet
});