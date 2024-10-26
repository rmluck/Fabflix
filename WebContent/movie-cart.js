$(document).ready(function() {
    loadCartItems();
    function loadCartItems() {
        $.ajax({
            url: '/fabflix_com_war/api/cart',
            method: 'GET',
            dataType: 'json',
            success: function(cartItems) {
                $('#cart-body').empty();

                if (cartItems.length === 0) {
                    $('#cart-body').append('<tr><td colspan="4">No movies in cart</td></tr>');
                } else {
                    cartItems.forEach(movie => {
                        const totalPrice = (1 * movie.quantity).toFixed(2);
                        $('#cart-body').append(`
                            <tr>
                                <td><a href='single-movie.html?id=${movie.id}'>${movie.title}</a></td>
                                <td>${movie.quantity}</td>
                                <td>$${totalPrice}</td>
                                <td><button class='remove' data-movie-id='${movie.id}'>Remove</button></td>
                            </tr>
                        `);
                    });
                }
            },
            error: function(jqXHR, textStatus, errorThrown) {
                console.error("Error loading cart items:", textStatus, errorThrown);
                alert("Failed to load cart items.");
            }
        });
    }


    $(document).on('click', '.remove', function() {
        const movieId = $(this).data('movie-id');


        $.ajax({
            url: '/fabflix_com_war/api/cart',
            method: 'POST',
            data: {
                action: 'remove',
                movieId: movieId
            },
            success: function() {
                loadCartItems();
            },
            error: function(jqXHR, textStatus, errorThrown) {
                console.error("Error removing movie from cart:", textStatus, errorThrown);
                alert('Error removing movie from cart.');
            }
        });
    });
});
