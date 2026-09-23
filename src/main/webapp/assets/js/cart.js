window.addEventListener('load', async () => {

    Notiflix.Loading.pulse("Wait...", {
        clickToClose: false,
        svgColor: '#0284c7',
    });

    try {

        await loadCart();

    } finally {
        Notiflix.Loading.remove(1000);
    }

});

let cartItems = [];
let subTotal = 0;
let itemCount = 0;

async function loadCart() {

    // alert("kay");

    try {

        const response = await fetch("/findabook/api/data/loadCart");

        if (response.ok) {
            const data = await response.json();
            // console.log(data);

            if (data.message === "empty") {
                document.getElementById("checkout").disabled = data.message === "empty";
                document.getElementById("checkout").style.cursor = "not-allowed";
                const container = document.getElementById("cart-item-container");
                container.innerText = " ";
                container.classList.add("text-center");
                const h2 = document.createElement("h2");
                const img = document.createElement("img");
                img.src = "assets/images/no-items.jpg";
                img.style.width = "210px";
                img.style.height = "210px";

                h2.style.fontSize = "32px";
                h2.style.fontWeight = "bold";
                h2.style.color = "#1a98a8";
                h2.innerHTML = "Your cart is Empty.";
                container.appendChild(h2);
                container.appendChild(img);

            }
            if (data.status) {
                itemCount = 0;
                subTotal = 0;
                cartItems = [];
                cartItems = data.cartItems;
                // console.log(data);
                renderCartItems(data.cartItems);
                calculationRender();

            }

        } else {
            Notiflix.Notify.warning("Cart data loading failed !", {
                position: 'right-top',
            });
        }


    } catch (e) {
        console.log(e.message);
        // Notiflix.Notify.failure(e.message, {
        //     position: 'right-top',
        // });
    }
}

function renderCartItems(cartItem) {

    const container = document.getElementById("cart-item-container");
    container.innerText = "";
    let pid = 0;

    cartItem.forEach(cartItem => {
        pid = cartItem.pid;
        container.innerHTML += `
           <div class="cart-item mb-1">
                    <img src="${cartItem.images[0]}" class="cart-image" alt="${cartItem.title}">
                    <div class="cart-info">
                    <input id="product-id" class="d-none" value="${cartItem.pid}">
                    <input id="product-qty" class="d-none" value="${cartItem.productQty}">
                        <div class="cart-title">${cartItem.title}</div>
                        <div class="cart-author">by ${cartItem.author}</div>
                        <div class="cart-price">Rs.${cartItem.price}.00</div>
                    </div>

                    <div class="quantity-control">
                        <button class="qty-btn" id="minus-${cartItem.pid}"  onclick="updateItemQty(${cartItem.pid},-1)">-</button>
                        <span id="item-qty-${cartItem.pid}">${cartItem.cartQty}</span>
                        <button class="qty-btn" onclick="updateItemQty(${cartItem.pid},1)">+</button>
                    </div>

                    <button class="qty-btn text-danger" onclick="removeItem(${cartItem.pid})"><i data-feather="trash-2"></i></button>
                </div>
        
        `;

        subTotal += (cartItem.price * cartItem.cartQty);
        itemCount += cartItem.cartQty;

    });

    const minus = document.getElementById(`minus-${pid}`);
    const itemQty = document.getElementById(`item-qty-${pid}`);

    minus.disabled = itemQty.textContent === "1";
    minus.style.cursor = itemQty.textContent === "1" ? "not-allowed" : "pointer";

    feather.replace();
}

function calculationRender() {

    // console.log(subTotal);
    // console.log(itemCount);
    let shipping = 400;

    document.getElementById("sub-total").innerHTML = formatPrice(subTotal)
    document.getElementById("shipping").innerHTML = formatPrice(shipping);

    let total = subTotal + shipping;

    document.getElementById("total").innerHTML = formatPrice(total);

}

function formatPrice(price) {
    const formated = price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ' ');
    return `Rs.${formated}.00`;
}

function limitWords(text, limit) {
    return text.split(/\s+/).slice(0, limit).join(" ");

}

async function removeItem(pid) {
    try {

        const response = await fetch(`api/action/${pid}/removeItem`);

        if (response.ok) {

            const data = await response.json();
            // console.log(data);

            if (data.status) {
                Notiflix.Report.success(
                    'Find A Book',
                    data.message,
                    'Okay',
                    () => {
                        window.location.reload();
                    }
                );

            }

        } else {
            Notiflix.Notify.warning("Cart item removing failed !", {
                position: 'right-top',
            });
        }


    } catch (e) {
        Notiflix.Notify.failure("Something went wrong ?", {
            position: 'right-top',
        });
    }


}

async function updateItemQty(pid, change) {

    // console.log("okay")

    const item = cartItems.find(i => i.pid == pid);
    if (!item) {
        return;
    }
    item.cartQty += change;
    if (item.cartQty < 1) {
        item.cartQty = 1;
    } else if (item.cartQty >= item.productQty) {
        Notiflix.Notify.warning("Reached to Maximum !", {
            position: 'center-top',
        });
    }

    try {

        const response = await fetch(`api/action/${pid}/updateCart?itemCount=${item.cartQty}`);

        if (response.ok) {

            const data = await response.json();
            console.log(data);

            if (data.status) {
                loadCart();
            }


        } else {
            Notiflix.Notify.warning("Cart update failed", {
                position: 'center-top',
            });
        }


    } catch (e) {
        console.log(e.message);
    }
}

async function authenticateCheckout() {

    const total = parseInt(subTotal)+400;

    try {
        const response = await fetch(`api/action/${total}/authenticateCheckout`);

        if (response.ok) {
            const data = await response.json();
            console.log(data);
            if (data.status) {

                // completeCartBuy(data);

                // Payment completed. It can be a successful failure.
                payhere.onCompleted = function onCompleted(orderId) {
                    // console.log(orderId);
                    completeCartBuy(data);
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

async function completeCartBuy(data) {

    const dataObj = {
        amount: data.data.amount,
        order_id: data.data.order_id,
    }

    try {
        const response = await fetch(`api/action/completeCartBuy`, {
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




