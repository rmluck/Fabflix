/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
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
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleResult(resultData) {
    let homeElement = jQuery("#home");
    homeElement.append("<a href='movies.html'>Home</a>");

    console.log("handleResult: populating star info from resultData");

    // Populates the star info h3
    // Finds empty h3 body by id "star_info"
    let starInfoElement = jQuery("#star_info");
    let starDOB = resultData[0]["star_dob"] ? resultData[0]["star_dob"] : "N/A";
    // append two html <p> created to the h3 body, which will refresh the page
    starInfoElement.append("<p>" + resultData[0]["star_name"] + "</p>" +
        "<p>Date Of Birth: " + starDOB + "</p>");

    console.log("handleResult: populating star table from resultData");

    // Populates star table
    // Finds empty table body by id "movie_table_body"
    let starBodyElement = jQuery("#star_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < Math.min(10, resultData.length); i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td><a href='single-movie.html?id=" + resultData[i]["movie_id"] + "'>" + resultData[i]["movie_title"] + "</a></td>";
        rowHTML += "<td>" + resultData[i]["movie_year"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_director"] + "</td>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        starBodyElement.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
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

    // Get id from URL
    let starId = getParameterByName("id");

    // Existing AJAX call to fetch single star data
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "/api/single-star?id=" + starId,
        success: (resultData) => handleResult(resultData)
    });
});