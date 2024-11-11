$(document).ready(function() {
    fetchTotalPrice();

    $('#payment_form').on('submit', function(event) {
        event.preventDefault(); // Prevent default form submission
        $('#payment_error').hide(); // Clear previous error messages

        // Perform client-side validation
        var isValid = true;
        var credit_card_number = $('#payment_credit_card_number').val();

        // Check if credit card number is numeric (and not empty)
        if (!/^\d+$/.test(credit_card_number)) {
            $('#payment_error').text('Please enter a valid credit card number.').show();
            isValid = false;
        }

        if (isValid) {
            const data = {
                firstName: $('#payment_first_name').val(),
                lastName: $('#payment_last_name').val(),
                creditCardNumber: credit_card_number,
                expirationDate: $('#payment_expiration_date').val()
            };

            console.log('Sending payment data:', data); // Log data for debugging

            $.ajax({
                url: 'api/payment',
                type: 'POST',
                data: data,
                success: function(response) {
                    window.location.href = "confirmation.html";
                },
                error: function(xhr) {
                    $('#payment_error').text('Payment failed. Please try again.').show();
                }
            });
        }
    });

    function fetchTotalPrice() {
        $.ajax({
            url: 'api/cart/total',
            type: 'GET',
            success: function(response) {
                if (response && response.totalPrice !== undefined) {
                    $('#total-price').text('$' + parseFloat(response.totalPrice).toFixed(2));
                } else {
                    console.error('Invalid response structure:', response);
                }
            },
            error: function(xhr) {
                console.error('Failed to fetch total price');
            }
        });
    }
});

function clearSession() {
    $.ajax({
        url: "api/session",
        method: "DELETE",
        success: () => {
            console.log("Session parameters reset.");
        },
        error: (jqXHR, textStatus, errorThrown) => {
            console.error("Failed to reset session parameters: ", errorThrown);
        }
    });
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