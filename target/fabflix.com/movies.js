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
    console.log("handleMoviesResult: populating movies table from resultData");
    console.log("Number of results: ", resultData.length);

    // Populate star table
    // Find empty table body by id "star_table_body"
    let moviesTableBodyElement = jQuery("#movies_table_body");

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
        moviesTableBodyElement.append(rowHTML);
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
$(document).ready(function() {
    // Handle logout form submission
    $("#logout_form").on("submit", function(event) {
        // Prevent default form submission
        event.preventDefault();

        $.ajax({
            type: "POST",
            url: "/api/logout",
            dataType: "json",
            success: function(response) {
                if (response.status === "success") {
                    // Logout successful, redirect to login page
                    window.location.href = "login.html";
                } else {
                    // Handle error case (no active session)
                    alert(response.message);
                }
            },
            error: function() {
                // Handle AJAX error
                alert("Error logging out. Please try again.");
            }
        });
    });

    // Existing AJAX call to fetch movie data
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/movies",
        success: (resultData) => handleMovieResult(resultData)
    });
});