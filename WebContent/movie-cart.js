$(document).ready(function() {
    loadCartItems();
    $('#proceed_to_payment_button').on('click', function() {
        window.location.href = 'payment.html'; // Redirect to the payment page
    });
    function loadCartItems() {
        $.ajax({
            url: 'api/cart',
            method: 'GET',
            dataType: 'json',
            success: function(cartItems) {
                $('#cart_table_body').empty();

                if (cartItems.length === 0) {
                    $('#cart_table_body').append('<tr><td colspan="4">No movies in cart</td></tr>');
                } else {
                    let totalPrice = 0.00;
                    console.log("total price 0: " + totalPrice);
                    cartItems.forEach(movie => {
                        const price = (20 * movie.quantity).toFixed(2);
                        totalPrice += parseFloat(price);
                        console.log("price: " + price);
                        console.log("total price: " + totalPrice);
                        $('#cart_table_body').append(`
                            <tr>
                                <td><a href='single-movie.html?id=${movie.id}'>${movie.title}</a></td>
                                <td><input type="number" id="quantity_input" class="quantity-input" data-movie-id="${movie.id}" value="${movie.quantity}" min="0" /></td>
                                <td>$${price}</td>
                                <td id="remove_from_cart"><button id="remove_from_cart_button" class='remove' data-movie-id='${movie.id}'>Remove</button></td>
                            </tr>
                        `);
                    });
                    $('#cart_table_body').append(`
                        <tr id="total_price">
                            <td></td>
                            <td></td>
                            <td>$${totalPrice.toFixed(2)}</td>
                            <td></td>
                        </tr>
                    `);
                }
            },
            error: function(jqXHR, textStatus, errorThrown) {
                console.error("Error loading cart items:", textStatus, errorThrown);
                alert("Failed to load cart items.");
            }
        });
    }


    $(document).on('change', '.quantity-input', function() {
        const movieId = $(this).data('movie-id');
        const newQuantity = $(this).val();

        $.ajax({
            url: 'api/cart',
            method: 'POST',
            data: {
                action: 'update',
                movieId: movieId,
                quantity: newQuantity
            },
            success: function() {
                loadCartItems();
                $('#cart_success_message').text('Quantity updated successfully!').show().delay(3000).fadeOut();
            },
            error: function(jqXHR, textStatus, errorThrown) {
                console.error("Error updating quantity:", textStatus, errorThrown);
                alert('Error updating quantity.');
            }
        });
    });

    $(document).on('click', '.remove', function() {
        const movieId = $(this).data('movie-id');

        if (confirm('Are you sure you want to remove this movie from your cart?')) {
            $.ajax({
                url: 'api/cart',
                method: 'POST',
                data: {
                    action: 'remove',
                    movieId: movieId
                },
                success: function() {
                    loadCartItems();
                    $('#cart_success_message').text('Movie removed successfully!').show().delay(3000).fadeOut();
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    const errorMessage = jqXHR.responseText || "Error removing movie from cart.";
                    console.error("Error removing movie from cart:", textStatus, errorThrown);
                    alert(`Error removing movie from cart: ${errorMessage}`);
                }
            });
        }
    });
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