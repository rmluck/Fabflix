// $(document).ready(function() {
//     loadCartItems();
//
//     // Function to load cart items
//     function loadCartItems() {
//         $.get('/fabflix_com_war/cart', function(data) {
//             $('#cart-items').html(data);
//             attachRemoveHandlers();
//         });
//     }
//
//     // Attach click handlers to the remove buttons
//     function attachRemoveHandlers() {
//         $(document).on('click', '.remove', function() {
//             const movieId = $(this).data('movie-id'); // Ensure you're using data-movie-id
//
//             // Send a POST request to remove the movie
//             $.ajax({
//                 url: '/fabflix_com_war/cart',
//                 method: 'POST',
//                 data: {
//                     action: 'remove',
//                     movieId: movieId
//                 },
//                 success: function() {
//                     loadCartItems(); // Reload the cart items after successful removal
//                 },
//                 error: function() {
//                     alert('Error removing movie from cart.');
//                 }
//             });
//         });
//     }
// });
// $(document).ready(function() {
//     loadCartItems(); // Load the initial cart items when the page is ready
//
//     // Function to load cart items
//     function loadCartItems() {
//         $.getJSON('/fabflix_com_war/cart', function(data) {
//             $('#cart-body').empty(); // Clear existing rows
//
//             if (data.length === 0) {
//                 $('#cart-body').append('<tr><td colspan="4">No movies in cart</td></tr>');
//             } else {
//                 data.forEach(movie => {
//                     const totalPrice = (1 * movie.quantity).toFixed(2);
//                     $('#cart-body').append(`
//                         <tr>
//                             <td><a href='single-movie.html?id=${movie.id}'>${movie.title}</a></td>
//                             <td>${movie.quantity}</td>
//                             <td>$${totalPrice}</td>
//                             <td><button class='remove' data-movie-id='${movie.id}'>Remove</button></td>
//                         </tr>
//                     `);
//                 });
//             }
//         });
//     }
//
//     // Attach click handlers to the remove buttons
//     $(document).on('click', '.remove', function() {
//         const movieId = $(this).data('movie-id'); // Get the movie ID
//
//         // Send a POST request to remove the movie
//         $.ajax({
//             url: '/fabflix_com_war/cart',
//             method: 'POST',
//             data: {
//                 action: 'remove',
//                 movieId: movieId
//             },
//             success: function() {
//                 loadCartItems(); // Reload the cart items after successful removal
//             },
//             error: function(jqXHR, textStatus, errorThrown) {
//                 console.error("Error removing movie from cart:", textStatus, errorThrown);
//                 alert('Error removing movie from cart.');
//             }
//         });
//     });
// });


$(document).ready(function() {
    loadCartItems(); // Load the initial cart items when the page is ready

    // Function to load cart items
    function loadCartItems() {
        $.get('/fabflix_com_war/cart', function(data) {
            $('#cart-body').empty(); // Clear existing rows

            if (data.length === 0) {
                $('#cart-body').append('<tr><td colspan="4">No movies in cart</td></tr>');
            } else {
                data.forEach(movie => {
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
        });
    }

    // Attach click handlers to the remove buttons
    $(document).on('click', '.remove', function() {
        const movieId = $(this).data('movie-id'); // Get the movie ID

        // Send a POST request to remove the movie
        $.ajax({
            url: '/fabflix_com_war/cart',
            method: 'POST',
            data: {
                action: 'remove',
                movieId: movieId
            },
            success: function() {
                loadCartItems(); // Reload the cart items after successful removal
            },
            error: function(jqXHR, textStatus, errorThrown) {
                console.error("Error removing movie from cart:", textStatus, errorThrown);
                alert('Error removing movie from cart.');
            }
        });
    });
});


