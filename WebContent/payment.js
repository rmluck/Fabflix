$(document).ready(function() {
    fetchTotalPrice();

    $('#payment-form').on('submit', function(event) {
        event.preventDefault(); // Prevent default form submission
        $('#payment-error').hide(); // Clear previous error messages

        // Perform client-side validation
        var isValid = true;
        var creditCardNumber = $('#credit-card-number').val();

        // Check if credit card number is numeric (and not empty)
        if (!/^\d+$/.test(creditCardNumber)) {
            $('#payment-error').text('Please enter a valid credit card number.').show();
            isValid = false;
        }

        if (isValid) {
            const data = {
                firstName: $('#first-name').val(),
                lastName: $('#last-name').val(),
                creditCardNumber: creditCardNumber,
                expirationDate: $('#expiration-date').val()
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
                    $('#payment-error').text('Payment failed. Please try again.').show();
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
