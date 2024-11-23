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

// Bind the submit action of the form to a event handler function
cart.submit(handleCartInfo);

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

$.ajax(
    "api/index", {
        method: "GET",
        success: handleSessionData
    }
);

let autocompleteCache = new Map();
let autocompleteTimer;
// let suggestionsContainer = document.getElementById("search_title_suggestions");

$("#search_title").autocomplete({
    source: function(request, doneCallback) {
        const title = request.term;
        if (title.length < 3) {
            console.log("Title too short, skipping autocomplete.");
            // suggestionsContainer.classList.add("hidden");
            return;
        }

        console.log("Autocomplete initiated for title: ", title);

        if (autocompleteCache.has(title)) {
            console.log("Using cached results for title: ", title);
            let suggestions = autocompleteCache.get(title);
            doneCallback(suggestions);
            return;
        }

        clearTimeout(autocompleteTimer);
        autocompleteTimer = setTimeout(() => {
            console.log("Sending AJAX request for title: ", title);
            let apiURL = `api/movies?action=searchMovies&title=${encodeURIComponent(title)}&sortOptions=autocomplete&moviesPerPage=10&page=1`;

            $.ajax({
                method: "GET",
                url: apiURL,
                success: function (data) {
                    console.log("Received suggestions from server: ", data);
                    let suggestions = data.movies.map(item => ({
                        label: item["title"],
                        value: item["title"],
                        id: item["id"],
                    }));
                    autocompleteCache.set(title, suggestions);
                    console.log("suggestions from database: ", suggestions);
                    // showSuggestions(suggestions);
                    doneCallback(suggestions);
                },
                error: function (xhr) {
                    console.error("Error fetching suggestions: ", xhr.responseText);
                    // suggestionsContainer.classList.add("hidden");
                },
            });
        }, 300);
    },
    select: function(event, ui) {
        console.log("Suggestion selected: ", ui.item);
        window.location.href = `single-movie.html?id=${ui.item.id}`;
    },
});

// function showSuggestions(suggestions) {
//     activeSuggestion = -1;
//     suggestionsContainer.innerHTML = "";
//
//     if (suggestions.length > 0) {
//         suggestions.forEach((suggestion, index) => {
//            const suggestionDiv = document.createElement("div");
//            suggestionDiv.textContent = suggestion.value;
//            suggestionDiv.classList.add("suggestion");
//            suggestionDiv.dataset.index = index;
//            suggestionDiv.addEventListener("click", () => {
//                search_input.value = suggestion.value;
//                window.location.href = `single-movie.html?id=${suggestion.id}`;
//            });
//            suggestionsContainer.appendChild(suggestionDiv);
//         });
//         suggestionsContainer.classList.remove("hidden");
//     } else {
//         suggestionsContainer.classList.add("hidden");
//     }
// }

// search_input.addEventListener("keydown", (event) => {
//     const suggestions = Array.from(suggestionsContainer.children);
//
//     if (event.key === "ArrowDown") {
//         event.preventDefault();
//         if (suggestions.length > 0) {
//             activeSuggestion = (activeSuggestion + 1) % suggestions.length;
//             updateActiveSuggestion(suggestions);
//         }
//     } else if (event.key === "ArrowUp") {
//         event.preventDefault();
//         if (suggestions.length > 0) {
//             activeSuggestion = (activeSuggestion - 1 + suggestions.length) % suggestions.length;
//             updateActiveSuggestion(suggestions);
//         }
//     } else if (event.key === "Enter") {
//         if (activeSuggestion >= 0 && suggestions[activeSuggestion]) {
//             event.preventDefault();
//             suggestions[activeSuggestion].click();
//         }
//     }
// });

// function updateActiveSuggestion(suggestions) {
//     suggestions.forEach((item, index) => {
//         item.classList.toggle("active", index === activeSuggestion);
//     });
//
//     if (activeSuggestion >= 0 && suggestions[activeSuggestion]) {
//         search_input.value = suggestions[activeSuggestion].textContent;
//     }
// }

// document.addEventListener("click", (event) => {
//     if (!suggestionsContainer.contains(event.target) && event.target !== search_input) {
//         suggestionsContainer.classList.add("hidden");
//     }
// });

$("#search_form").submit(function (event) {
    event.preventDefault();
    let title = $("#search_title").val();
    let year = $("#search_year").val();
    let director = $("#search_director").val();
    let star = $("#search_star").val();

    let queryParams = $.param({title:title, year:year, director:director, star:star})

    window.location.href = `movies.html?${queryParams}`;
});