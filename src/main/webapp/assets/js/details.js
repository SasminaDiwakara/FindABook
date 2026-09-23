let product = [];
let relatedProducts = [];

window.addEventListener('load', async () => {
    Notiflix.Loading.pulse({
        clickToClose: false,
        svgColor: '#0284c7',
    });

    try {

        await loadProductData();


    } finally {
        Notiflix.Loading.remove(1000);
    }
});

let param = new URLSearchParams(window.location.search);
const pid = param.get("pid");

async function loadProductData() {

    try {
        const response = await fetch(`api/products/${pid}/loadProductData`);

        if (response.ok) {
            const data = await response.json();

            // console.log(data);

            if (data.status) {

                product = data.singleProduct;
                renderProduct();
                stockStatus();

                if (data.relatedProducts != null) {
                    relatedProducts = [];
                    relatedProducts = data.relatedProducts;
                    // document.getElementById("related-product-heading").classList.remove("hidden");

                    renderRelatedProducts();
                } else {

                    const relatedHeading = document.getElementById("related-product-heading");
                    if (relatedHeading) relatedHeading.classList.add("hidden");

                }


            } else {
                Notiflix.Notify.warning("Couldn't find any product ?", {
                    position: 'right-top',
                });
            }


        } else {
            Notiflix.Notify.warning("Product data loading failed ?", {
                position: 'right-top',
            });

        }

    } catch (e) {
        console.log(e.message);
        Notiflix.Notify.failure(e.message, {
            position: 'right-top',
        });
    }


}

function renderProduct() {

    let currentPrice = parseInt(product.price);
    let beforeDiscount = (currentPrice * 24) / 100 + currentPrice;

    const splitDate = splitText(product.createdAt, "T");
    // const date = splitDate[0];

    document.getElementById("title0").innerText = product.title;
    document.getElementById("product-title").innerText = product.title;
    document.getElementById("product-author").innerText = "by " + product.author;
    document.getElementById("category").innerHTML = product.categoryName;
    document.getElementById("current-price").innerText = "Rs." + currentPrice + ".00";
    document.getElementById("original-price").innerText = "Rs." + beforeDiscount + ".00";
    document.getElementById("main-img").src = (product.images && product.images.length > 0)
        ? product.images[0]
        : "assets/images/placeholder.jpg";
    document.getElementById("description").innerHTML = product.description;
    document.getElementById("date").innerHTML = splitDate[0];
    document.getElementById("quantity").max = product.stock;
    document.getElementById("product-id").value = product.id;
}

// function stockStatus() {
//     const stockContainer = document.querySelector('.stockDisplay');
//     if (!stockContainer || !product) return;
//
//     const isAvailable = Number(product.stock) > 0;
//     if (isAvailable) {
//         stockContainer.innerHTML = `
//             <div class="stock-status">
//                 <span class="stock-badge">✓ In Stock</span>
//                 <span class="text-muted ms-2">Ships within 24 hours</span>
//             </div>
//         `;
//     } else {
//         stockContainer.innerHTML = `
//             <div class="stock-status">
//                 <span class="stock-badge2 text-danger">× Out Of Stock</span>
//                 <span class="text-muted ms-2">Currently unavailable</span>
//             </div>
//         `;
//     }
// }

function stockStatus() {
    const stockContainer = document.querySelector('.stockDisplay');
    const buyNowBtn = document.querySelector('.btn-buyNow');
    const addToCartBtn = document.querySelector('.btn-add-cart');
    const quantityInput = document.getElementById('quantity');
    const qtyButtons = document.querySelectorAll('.quantity-btn');

    if (!product) return;

    const isAvailable = Number(product.stock) > 0;

    if (stockContainer) {
        if (isAvailable) {
            stockContainer.innerHTML = `
                <div class="stock-status">
                    <span class="stock-badge">✓ In Stock</span>
                    <span class="text-muted ms-2">Ships within 24 hours</span>
                </div>
            `;
        } else {
            stockContainer.innerHTML = `
                <div class="stock-status">
                    <span class="stock-badge2">× Out Of Stock</span>
                    <span class="text-muted ms-2">Currently unavailable</span>
                </div>
            `;
        }
    }

    if (buyNowBtn) buyNowBtn.disabled = !isAvailable;
    if (addToCartBtn) addToCartBtn.disabled = !isAvailable;
    if (quantityInput) quantityInput.disabled = !isAvailable;

    qtyButtons.forEach(btn => {
        btn.disabled = !isAvailable;
    });
}

function renderRelatedProducts() {

    const productDiv = document.getElementById("related-product-container");
    productDiv.innerHTML = "";
    relatedProducts.slice(0, 9).forEach(p => {
        const cover = (p.images && p.images.length > 0) ? p.images[0] : "assets/images/placeholder.jpg";
        productDiv.innerHTML += `   <div class="related-book-card" onclick="">
                    <img src="${cover}" alt="Book" class="related-book-image">
                    <div class="related-book-info">
                        <h4 class="related-book-title">${p.title}</h4>
                        <p class="related-book-author">by ${p.author}</p>
                        <div class="related-book-price">Rs.${p.price}.00</div>
                    </div>
                </div>
`;
    });

}

function splitText(text, splitBy) {

    return text.split(splitBy);
}

async function authenticateBuy() {

    const qty = document.getElementById("quantity").value;

    const dataObject = {
        qty: qty,
        pid: pid,
        shipping: 400
    }

    try {
        const response = await fetch(`api/action/authenticateBuy`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(dataObject)
        });

        if (response.ok) {
            const data = await response.json();
            console.log(data);
            if (data.status) {

                // completeBuy(data);

                // Payment completed. It can be a successful failure.
                payhere.onCompleted = function onCompleted(orderId) {
                    // console.log(orderId);
                    completeBuy(data);
                };

                // Payment window closed
                payhere.onDismissed = function onDismissed() {
                    console.log("Payment dismissed");
                };

                // Error occurred
                payhere.onError = function onError(error) {
                    console.log("Error:" + error);
                };

                // Put the payment variables here
                const url = "http://localhost:8080/findabook/details.html?pid=" + data.data.pid;
                const payment = {
                    "sandbox": true,
                    "merchant_id": data.data.merchant_id,    // Replace your Merchant ID
                    "return_url": url,     // Important
                    "cancel_url": url,     // Important
                    "notify_url": "http://sample.com/notify",
                    "order_id": data.data.order_id,
                    "items": data.data.book_name,
                    "amount": data.data.amount,
                    "currency": "LKR",
                    "hash": data.data.hash, // *Replace with generated hash retrieved from backend
                    "first_name": data.data.first_name,
                    "last_name": data.data.last_name,
                    "email": data.data.email,
                    "phone": data.data.mobile,
                    "address": data.data.address,
                    "city": data.data.city_name,
                    "country": "Sri Lanka",
                };

                payhere.startPayment(payment);

            } else {
                Notiflix.Notify.warning(data.message, {
                    position: 'right-top',
                });
            }

        } else {
            Notiflix.Notify.warning("Something went wrong!", {
                position: 'right-top',
            });

        }
    } catch (e) {
        Notiflix.Notify.failure(e.message, {
            position: 'right-top',
        });
    }

}

async function completeBuy(data) {

    const dataObj = {
        pid: data.data.pid,
        qty: data.data.qty,
        amount: data.data.amount,
        order_id: data.data.order_id,
    }

    try {
        const response = await fetch(`api/action/completeBuy`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(dataObj)
        });

        if (response.ok) {
            const responseData = await response.json();

            if (responseData.status) {
                console.log("Payment Completed!");

                if (data.status) {
                    Notiflix.Report.success(
                        'Find A Book',
                        "Payment completed !",
                        'Okay', () => {
                            window.location = "invoice.html?oid=" + data.data.order_id;
                        }
                    );
                }

            } else {
                Notiflix.Notify.warning("Payment Saving failed !", {
                    position: 'right-top',
                });
            }
        } else {
            Notiflix.Notify.warning("Something went wrong!", {
                position: 'right-top',
            });
        }

    } catch (e) {
        Notiflix.Notify.failure(e.message, {
            position: 'right-top',
        });
    }
}


//     document.getElementById('payhere-payment').onclick = function (e) {
//         payhere.startPayment(payment);
// };

// function limitWords(text, limit) {
//     return text.split(/\s+/).slice(0, limit).join(" ");
//
// }