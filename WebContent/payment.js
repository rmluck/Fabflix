$(document).ready(function() {
    // Fetch the total price from the server when the document is ready
    fetchTotalPrice();

    // Handle form submission
    $('#payment-form').on('submit', function(event) {
        event.preventDefault(); // Prevent default form submission

        // Clear any previous error messages
        $('#payment-error').hide();

        // Perform client-side validation
        var isValid = true; // Placeholder for actual validation logic

        // Validate credit card format (allowing more flexibility)
        var creditCardNumber = $('#credit-card-number').val();
        if (!/^\d+$/.test(creditCardNumber)) { // Check if it's all digits
            $('#payment-error').text('Please enter a valid credit card number.').show();
            isValid = false;
        }

        if (isValid) {
            $.ajax({
                url: '/api/payment',
                type: 'POST',
                data: {
                    firstName: $('#first-name').val(),
                    lastName: $('#last-name').val(),
                    creditCardNumber: creditCardNumber,
                    expirationDate: $('#expiration-date').val()
                },
                success: function(response) {
                    window.location.href = "confirmation.html";
                },
                error: function(xhr) {
                    // Handle error - display an error message
                    $('#payment-error').text('Payment failed. Please try again.').show();
                }
            });
        }
    });

    function fetchTotalPrice() {
        $.ajax({
            url: '/api/cart/total',
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
