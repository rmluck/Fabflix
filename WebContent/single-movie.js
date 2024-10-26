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
    console.log("handleResult: populating movie info from resultData");
    console.log(resultData);
    resultData = resultData[0];
    console.log(resultData);

    // Populates movie info h3
    // Finds empty h3 body by id "movie_info"
    let movieInfoElement = jQuery("#movie_info");
    movieInfoElement.append("<p id='movie_information'>" + resultData["title"] + " (" + resultData["year"] + ")" + "</p>" +
        "<p id='movie_director'>Director: " + resultData["director"] + "</p>"
    );

    console.log("handleResult: populating star table from resultData");

    // Populates movie details table
    // Finds empty table body by id "movie_details_body
    let movieBodyElement = jQuery("#movie_table_body");
    let rowHTML = "";
    rowHTML += "<tr>";
    // rowHTML += "<td>" + (resultData["genres"] || "N/A") + "</td>"; // Default to N/A if genres are missing

    let genresArray = resultData["genres"].split(",");
    let genresHTML = "<td>";
    for (let g = 0; g < genresArray.length; g++) {
        let [genreId, genreName] = genresArray[g].split(":");
        genreId = genreId.trim();
        genresHTML += "<a href='movies.html?genreId=" + genreId + "'>" + genreName + "</a><br>";
    }
    genresHTML += "</td>";
    rowHTML += genresHTML;

    let starsArray = resultData["stars"].split(",");
    let starsHTML = "<td>";
    for (let j = 0; j < starsArray.length; j++) {
        let [starId, starName] = starsArray[j].split(":");
        starId = starId.trim();
        starsHTML += "<a href='single-star.html?id=" + starId + "'>" + starName + "</a><br>";
    }
    starsHTML += "</td>";
    rowHTML += starsHTML;
    rowHTML += "<td>" + resultData["rating"] + "</td>";
    rowHTML += "</tr>";

    // Append the row created to the table body
    movieBodyElement.append(rowHTML);
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

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
$(document).ready(function() {
    // Handle logout form submission

    // $("#logout_form").on("submit", function(event) {
    //     // Prevent default form submission
    //     event.preventDefault();
    //
    //     $.ajax({
    //         type: "POST",
    //         url: "/api/logout",
    //         dataType: "json",
    //         success: function(response) {
    //             if (response.status === "success") {
    //                 // Logout successful, redirect to login page
    //                 window.location.href = "login.html";
    //             } else {
    //                 // Handle error case (no active session)
    //                 alert(response.message);
    //             }
    //         },
    //         error: function() {
    //             // Handle AJAX error
    //             alert("Error logging out. Please try again.");
    //         }
    //     });
    // });

    const queryParams = getQueryParams();

    let apiURL = `api/single-movie?id=${queryParams.id}`;

    // Existing AJAX call to fetch single movie data
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: apiURL,
        success: (resultData) => handleResult(resultData),
        error: (jqXHR, textStatus, errorThrown) => {
            console.error("error fetching movie:", errorThrown);
            alert("failed to fetch movie.")
        }
    });
});

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