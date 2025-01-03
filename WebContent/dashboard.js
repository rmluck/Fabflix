/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */

// /**
//  * Handle the data returned by IndexServlet
//  * @param resultDataString jsonObject, consists of session info
//  */
// function handleSessionData(resultDataString) {
//     let resultDataJson = JSON.parse(resultDataString);
//
//     console.log("handle session response");
//     console.log(resultDataJson);
//     console.log(resultDataJson["sessionId"]);
//
//     // show cart information
//     handleCartArray(resultDataJson["previousItems"]);
//
//     // call functions to populate genres and alphabet browsing options
//     loadGenres(resultDataJson["genres"]);
//     loadTitleLetters();
// }

// /**
//  * Handle the items in item list
//  * @param resultArray jsonObject, needs to be parsed to html
//  */
// function handleCartArray(resultArray) {
//     console.log(resultArray);
//     let item_list = $("#item_list");
//     let res = "<ul>";
//
//     if (resultArray.length === 0) {
//         item_list.html("<p>No items in cart.</p>")
//     } else {
//         // change it to html list
//         //let res = "<ul>";
//         for (let i = 0; i < resultArray.length; i++) {
//             // each item will be in a bullet point
//             res += "<li>" + resultArray[i] + "</li>";
//         }
//     }
//     res += "</ul>";
//
//     // clear the old array and show the new array in the frontend
//     item_list.html("");
//     item_list.append(res);
//     //item_list.html(res);
// }

// /**
//  * Submit form content with POST method
//  * @param cartEvent
//  */
// function handleCartInfo(cartEvent) {
//     console.log("submit cart form");
//     /**
//      * When users click the submit button, the browser will not direct
//      * users to the url defined in HTML form. Instead, it will call this
//      * event handler when the event is triggered.
//      */
//     cartEvent.preventDefault();
//
//     $.ajax("api/index", {
//         method: "POST",
//         data: cart.serialize(),
//         success: resultDataString => {
//             let resultDataJson = JSON.parse(resultDataString);
//             handleCartArray(resultDataJson["previousItems"]);
//         },
//         error: (jqXHR, textStatus, errorThrown) => {
//             console.error("Error adding item: ", textStatus, errorThrown);
//             alert("Failed to add item. Please try again.");
//         }
//     });
//
//     // clear input form
//     cart[0].reset();
// }

function handleMetadata(data) {
    const metadataContainer = document.getElementById("metadata_list");
    metadataContainer.innerHTML = "";

    for (const [tableName, columns] of Object.entries(data)) {
        const tableDiv = document.createElement("div");
        tableDiv.className = "table_metadata";

        const tableHeader = document.createElement("h2");
        tableHeader.textContent = `${tableName}`;
        tableDiv.appendChild(tableHeader);

        const columnList = document.createElement("ul");
        columnList.className = "table_columns";

        columns.forEach(column => {
           const columnItem = document.createElement("li");
           columnItem.textContent = `${column.name}: ${column.type}`;
           columnList.appendChild(columnItem);
        });

        tableDiv.appendChild(columnList);
        metadataContainer.appendChild(tableDiv);
    }
}

function handleInsertStar(resultDataJson) {
    console.log("API Response: ", resultDataJson);
    document.getElementById("insert_star_message").innerHTML = `<p>Star ID: ${resultDataJson["starId"]}, Name: ${resultDataJson["name"]}, Year: ${resultDataJson["year"]}</p>`;
}

function handleInsertMovie(resultDataJson) {
    console.log("API Response: ", resultDataJson);
    let movieId = resultDataJson["movieId"];
    let starId = resultDataJson["starId"];
    let genreId = resultDataJson["genreId"];

    if (movieId != null) {
        document.getElementById("insert_movie_message").innerHTML = `<p>Movie ID: ${movieId}, Star ID: ${starId}, Genre ID: ${genreId}</p>`;
    } else {
        document.getElementById("insert_movie_message").innerHTML = `<p>Error: Movie already in database.</p>`;
    }
}

// /**
//  * Load genres and display as links for browsing movies by genre
//  */
// function loadGenres() {
//     // fetch genres from server if they are not already part of session
//     fetch("api/index?action=getGenres")
//         .then(response => response.json())
//         .then(genres => {
//             const genreContainer = document.getElementById("genres_list");
//             genreContainer.innerHTML = "";
//
//             genres.forEach(genre => {
//                 const link = document.createElement("a");
//                 link.href = "movies.html?genreId=" + genre.id;
//                 link.textContent = genre.name;
//                 genreContainer.appendChild(link);
//                 genreContainer.appendChild(document.createTextNode(" | "));
//             });
//         });
// }

// /**
//  * Load alphabet letters for browsing movies by title.
//  */
// function loadTitleLetters() {
//     const titleContainer = document.getElementById("alphabet_list");
//     const letters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ*".split("");
//
//     titleContainer.innerHTML = "";
//
//     letters.forEach(letter => {
//         const link = document.createElement("a");
//         link.href = "movies.html?letter=" + letter;
//         link.textContent = letter;
//         titleContainer.appendChild(link);
//         titleContainer.appendChild(document.createTextNode(" | "));
//     });
// }

// $(document).ready(function() {
    // $("#index_view_cart_button").on("click", function() {
    //     window.location.href = "movie-cart.html";
    // });
// });

$("#insert_star_form").submit(function (event) {
    event.preventDefault();
    let apiURL = "api/_dashboard";
    let name = $("#insert_star_name").val();
    let year = $("#insert_star_year").val();
    let searchQuery = "";

    searchQuery += `?action=insertStar&name=${name}`;
    if (year) {
        searchQuery += `&year=${year}`;
    }
    apiURL += searchQuery;

    $.ajax(
      apiURL, {
          method: "GET",
            data: $("#insert_star_form"),
            success: handleInsertStar,
            error: handleInsertStar
        }
    );

    // jQuery.ajax({
    //    dataType: "json",
    //    method: "GET",
    //    url: apiURL,
    //    success: (resultData) => handleInsertStar(resultData),
    //    error: (jqXHR, textStatus, errorThrown) => {
    //        console.error("error inserting star:", errorThrown);
    //        alert("failed to insert star.")
    //    }
    // });

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

    // let queryParams = $.param({name:name, year:year})

    // window.location.href = `movies.html?${queryParams}`;
});

$("#insert_movie_form").submit(function (event) {
    event.preventDefault();
    let apiURL = "api/_dashboard";
    let title = $("#insert_movie_title").val();
    let year = $("#insert_movie_year").val();
    let director = $("#insert_movie_director").val();
    let star = $("#insert_movie_star").val();
    let genre = $("#insert_movie_genre").val();
    let searchQuery = "";

    searchQuery += `?action=insertMovie&title=${title}&year=${year}&director=${director}&star=${star}&genre=${genre}`;
    apiURL += searchQuery;

    $.ajax(
      apiURL, {
          method: "GET",
            data: $("#insert_movie_form"),
            success: handleInsertMovie,
            error: handleInsertMovie
        }
    );

    // jQuery.ajax({
    //    dataType: "json",
    //    method: "GET",
    //    url: apiURL,
    //    success: (resultData) => handleInsertMovie(resultData),
    //    error: (jqXHR, textStatus, errorThrown) => {
    //        console.error("error inserting star:", errorThrown);
    //        alert("failed to insert star.")
    //    }
    // });
});

$.ajax(
    "api/_dashboard", {
        method: "GET",
        success: handleMetadata
    }
);

// // Bind the submit action of the form to a event handler function
// cart.submit(handleCartInfo);
//
// let logout_form = $("#logout_form");
//
// function handleLogoutResult(resultDataJson) {
//     console.log("handle logout response");
//     console.log(resultDataJson);
//     console.log(resultDataJson["status"]);
//
//     if (resultDataJson["status"] === "success") {
//         window.location.replace("logout.html");
//     } else {
//         console.log("show error message");
//         console.log(resultDataJson["message"]);
//     }
// }
//
// function submitLogoutForm(formSubmitEvent) {
//     console.log("submit logout form");
//
//     formSubmitEvent.preventDefault();
//
//     $.ajax(
//         "api/logout", {
//             method: "POST",
//             success: handleLogoutResult,
//             error: handleLogoutResult
//         }
//     );
// }
//
// logout_form.submit(submitLogoutForm)