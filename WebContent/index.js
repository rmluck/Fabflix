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
        link.href = "movies.html?letter=" + letter;
        link.textContent = letter;
        titleContainer.appendChild(link);
        titleContainer.appendChild(document.createTextNode(" | "));
    });
}

$(document).ready(function() {
    $("#index_view_cart_button").on("click", function() {
        window.location.href = "movie-cart.html";
    });
});

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

$.ajax(
    "api/index", {
        method: "GET",
        success: handleSessionData
    }
);

// Bind the submit action of the form to a event handler function
cart.submit(handleCartInfo);

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