let login_form = $("#dashboard_login_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleLoginResult(resultDataJson) {
    console.log("handle login response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If login succeeds, it will redirect the user to index.html
    if (resultDataJson["status"] === "success") {
        window.location.replace("dashboard.html");
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#dashboard_login_error_message").text(resultDataJson["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    let recaptchaResponse = grecaptcha.getResponse();

    if (recaptchaResponse.length === 0) {
        $("#login_error_message").text("Please complete the reCAPTCHA.")
        return;
    }

    $.ajax(
        "api/_dashboard_login", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: login_form.serialize(),
            success: handleLoginResult,
            error: handleLoginResult
        }
    );
}

// Bind the submit action of the form to a handler function
login_form.submit(submitLoginForm);

// login_form.on("submit", function() {
//     console.log("Form submission detected");
// });