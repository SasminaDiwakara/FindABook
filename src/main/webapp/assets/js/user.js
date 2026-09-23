window.addEventListener("load", async () => {
    Notiflix.Loading.dots("Loading Data...", {
        clickToClose: false,
        svgColor: '#0284c7'
    });

    try {
        await getCities();
        await getUserData();
        // await getAddresses();
        await loadWishlistProducts();
        await loadOrders();


    } finally {
        Notiflix.Loading.remove();
    }

});


function renderAddresses(addresses) {

    const list = document.getElementById("addressList");
    list.innerHTML = "";
    addresses.forEach(addr => {
            list.innerHTML = `
        <div className="card card-container shadow-sm rounded-3 p-4 w-100">

            <p className="fs-5 fw-semibold text-dark lh-sm mb-1">
                ${addr.lineOne + " " + addr.lineTwo}
            </p>

            <p className="fs-6 text-secondary mb-4">
                  ${addr.cityName}
            </p>

            <div className="d-flex align-items-center gap-2 mb-4">
                <i className="fa-solid fa-phone icon-pink fs-6"></i>
                <span className="fs-6 fw-medium text-secondary">
                  ${addr.mobile}
                
            </span>
            </div>

            <div className="d-flex gap-3 mt-4">

                <button type="button" className="btn btn-primary fw-semibold shadow-sm rounded-3 w-100">
                    Make Primary
                </button>

                <button type="button" className="btn btn-outline-danger fw-semibold rounded-3 w-100">
                    Delete
                </button>

            </div>
        </div>
       
       `;
        }
    )

}

async function getAddresses() {
    Notiflix.Loading.dots("Loading Data...", {
        clickToClose: false,
        svgColor: '#0284c7'
    });


    try {
        const response = await fetch("/findabook/api/profile/addresses");

        if (response.ok) {
            const data = await response.json();
            // console.log(data);
            document.getElementById("addName").innerHTML = `Name: ${data.name != null ? data.name : "Name"}`;
            document.getElementById("addEmail").innerHTML = `Email: ${data.email != null ? data.email : "example@email.com"}`;
            document.getElementById("contact").innerHTML = `Phone: ${data.addresses[0].mobile != null ? data.addresses[0].mobile : "Phone"}`;
            renderAddresses(data.addresses);
        } else {
            Notiflix.Notify.faliure("Failed to load users Addresses", {
                position: "right-top"
            });
        }

    } catch (e) {

        Notiflix.Notify.failure(e.message, {
                position: "right-top"
            }
        );
    } finally {
        Notiflix.Loading.remove(1000);
    }

}

async function getCities() {


    try {

        const response = await fetch("/findabook/api/data/cities");

        if (response.ok) {
            const data = await response.json();
            const citySelect = document.getElementById("citySelect");
            citySelect.innerHTML = "";
            citySelect.innerHTML = `<option value="0">Select</option>`;
            data.cities.forEach(city => {
                const option = document.createElement("option");
                option.value = city.id;
                option.innerHTML = city.name;
                citySelect.appendChild(option);
            });


        } else {
            Notiflix.Notify.failure("City loading failed ?", {
                position: 'right-top',
            });

        }

    } catch (e) {
        Notiflix.Notify.failure(e.message, {
            position: 'right-top',
        });
        console.log(e.message);
    }


}

async function getUserData() {

    try {
        const response = await fetch("/findabook/api/profile/user-profile");
        if (response.ok) {

            if (response.redirected) {
                window.location.href = response.url;
                return;
            }

            const data = await response.json();
            // console.log(data);
            let repText = String(data.user.sinceAt).replace("-", " ");
            let since = repText.split(" ");
            document.getElementById("username").innerHTML = `Welcome back, 
            ${data.user.firstName + " " + data.user.lastName}`;

            // document.getElementById("since").innerHTML = since[1] + " " + since[0];
            document.getElementById("firstName").value = data.user.firstName;
            document.getElementById("lastName").value = data.user.lastName;
            document.getElementById("lineOne").value = data.user.lineOne ? data.user.lineOne : "";
            document.getElementById("lineTwo").value = data.user.lineTwo ? data.user.lineTwo : "";
            document.getElementById("postalCode").value = data.user.postalCode ? data.user.postalCode : "";
            document.getElementById("citySelect").value = data.user.cityId ? data.user.cityId : 0;
            document.getElementById("currentPassword").value = data.user.password;
            document.getElementById("mobile").value = data.user.mobile ? data.user.mobile : "";

        } else {
            Notiflix.Notify.failure("User Data Loading Failed ?", {
                position: 'right-top',
            });
        }

    } catch (e) {
        // console.log(e.message);
        Notiflix.Notify.failure(e.message, {
            position: 'right-top',
        });
    }

}

async function saveChanges() {

    let firstName = document.getElementById("firstName");
    let lastName = document.getElementById("lastName");
    let lineOne = document.getElementById("lineOne");
    let lineTwo = document.getElementById("lineTwo");
    let postalCode = document.getElementById("postalCode");
    let mobile = document.getElementById("mobile");
    let citySelect = document.getElementById("citySelect");
    // let currentPassword = document.getElementById("currentPassword");
    // let newPassword = document.getElementById("newPassword");
    // let confirmPassword = document.getElementById("confirmPassword");

    const userObj = {
        firstName: firstName.value,
        lastName: lastName.value,
        lineOne: lineOne.value,
        lineTwo: lineTwo.value,
        postalCode: postalCode.value,
        mobile: mobile.value,
        cityId: citySelect.value,
        // password: currentPassword.value,
        // newPassword: newPassword.value,
        // confirmPassword: confirmPassword.value

    }

    // console.log(userObj);

    try {

        const response = await fetch("/findabook/api/profile/update-profile", {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(userObj)
        });

        if (response.status === 200) {
            const data = await response.json();

            if (data.status) {
                Notiflix.Report.success("FindABook",
                    data.message,
                    'Okay',
                );

                await getUserData();

            } else {
                Notiflix.Notify.warning(data.message, {
                    position: 'right-top',
                });
            }


        } else {
            Notiflix.Notify.failure("Something went wrong when updating!", {
                position: 'right-top',
            });
        }

    } catch (e) {
        Notiflix.Notify.failure(e.message, {
            position: 'right-top',
        });
    } finally {
        Notiflix.Loading.remove(1000);
    }


}

async function signOut() {
    Notiflix.Loading.pulse("Wait...", {
        clickToClose: false,
        svgColor: '#0284c7'
    });

    try {
        const response = await fetch("/findabook/api/profile/logout", {
            method: "GET",
            credentials: "include",

        });

        if (response.status === 200) {

            Notiflix.Report.success("FindABook",
                "Logout Successful",
                'Okay',
                () => {
                    window.location = "signIn.html"

                },
            );


        } else {
            Notiflix.Notify.failure("Something went Wrong ! Logout process paused ", {
                position: 'right-top',
            });
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message, {
            position: 'right-top',
        });
    } finally {
        Notiflix.Loading.remove(1000);
    }
}


// let products = [];

async function loadWishlistProducts() {

    // alert("okay")
    try {
        const response = await fetch("/findabook/api/data/loadWishlistProducts");

        if (response.ok) {
            const data = await response.json();

            console.log(data);

            if(data.message ==="empty"){
                document.getElementById("no_items").classList.remove('d-none');
            }else{

                if (data.status) {
                    renderWishlistProducts(data.wProducts);
                    let products = data.wProducts;
                    document.getElementById("wishlistCount").innerText = products.length? products.length+ " items" :0;
                    document.getElementById("wishlist_qty").innerText = products.length ? products.length:0;


                }
            }


        } else {
            Notiflix.Notify.warning("Data loading failed ?", {
                position: 'right-top',
            });

        }

    } catch (e) {
        Notiflix.Notify.failure(e.message, {
            position: 'right-top',
        });
        console.log(e.message);
    }
}

function renderWishlistProducts(products) {

    const productDiv = document.getElementById("productCards");
    productDiv.innerHTML = "";
    // console.log(products.length);
    products.forEach(p => {
        productDiv.innerHTML += `
                <div class="wishlist-item">
                            <img src="${p.image[0] || 'assets/images/no-items.jpg'}"
                                 class="wishlist-image">
                            <div class="wishlist-info">
                                <h3 class="wishlist-title">${p.title}</h3>
                                <input type="text" style="display: none;" value="${p.id}" id="product-id">
                                <div class="wishlist-author">by ${p.author}</div>
                                <div class="wishlist-price">Rs.${p.price}.00</div>
                                <div class="wishlist-actions">
                                    <button class="btn-action" onclick="addToCart();">Add to Cart</button>
                                    <button class="btn-action" style="color: #f56565; border-color: #f56565;" onclick="addToWishlist();">Remove
                                    </button>
                                </div>
                            </div>
                        </div>
            `
    });

    feather.replace();
}

let orderdata = [];

async function loadOrders() {

    // alert("okay")
    try {
        const response = await fetch("/findabook/api/data/loadOrders");

        if (response.ok) {
            const data = await response.json();

            console.log(data);

            if (data.status) {
                renderOrders(data.orders);
                orderdata = data.orders;
            }

        } else {
            Notiflix.Notify.warning("Data loading failed ?", {
                position: 'right-top',
            });

        }

    } catch (e) {
        Notiflix.Notify.failure(e.message, {
            position: 'right-top',
        });
        console.log(e.message);
    }
}

function renderOrders(orders) {

    const recentContainer = document.getElementById("recentOrders");
    const allContainer = document.getElementById("allOrders");

    document.getElementById("order_qty").innerHTML = orders.length;
    document.getElementById("pending_ord").innerHTML = orders.filter(order=>order.trackState !=="RECEIVED").length;



    orders.slice(0, 2).forEach(order => {
        recentContainer.innerHTML += createOrderCard(order);
    });


    orders.forEach(order => {
        allContainer.innerHTML += createOrderCard(order);
    });

}

function createOrderCard(order) {

    let state = getStateColor(order.trackState);

    const total = order.orderDetails.reduce((sum, item) => {
        return sum + (parseInt(item.price) * parseInt(item.qty));
    }, 0);

    const date = splitText(order.createdAt , "T");

    const imagesHTML = order.orderDetails.map(item => `
        <img src="${item.images}" class="product-thumb">
    `).join("");

    return `         <div class="order-item">
                            <div class="order-header">
                                <div>
                                    <div class="order-id">#ORD-${order.order_id}</div>
                                    <div class="order-date">Ordered on ${date[0]}</div>
                                </div>
                                <span class="order-status ${state.cls}">${state.text}</span>
                            </div>
                            <div class="order-products">
                               ${imagesHTML}
                            </div>
                            <div class="order-footer">
                                <div class="order-total">Total:Rs.${total + 400}.00</div>
                                <div class="order-actions">
                                    <button class="btn-action track-btn" data-bs-target="#trackingState" 
                                    data-bs-toggle="modal" data-status="${order.trackState}" >Track Order</button>
                                    <button class="btn-action" data-bs-target="#exampleModal"
                                     data-bs-toggle="modal" onclick="loadOrderDetails(${order.order_id},${total})">View Details</button>
                                </div>
                            </div>
                        </div>`;


}


function getStateColor(state) {

    if (state === "PROCESSING") {
        return {text: "Processing", cls: "status-processing"};
    } else if (state === "PACKING") {
        return {text: "Packing", cls: "status-packing"};

    } else if (state === "SHIPPING") {
        return {text: "Shipping", cls: "status-shipping"};

    } else if (state === "DELIVERED") {
        return {text: "Delivered", cls: "status-delivered"};

    } else if (state === "RECEIVED") {

        return {text: "Received", cls: "status-received"};
    }

}

function loadOrderDetails(order_id, total) {

    const order = orderdata.find(o => o.order_id == order_id);

    document.getElementById("modal_ord").innerText = "#ORD-" + order_id;
    const order_table = document.getElementById("order_table");

    order_table.innerHTML = "";
    if (!order) {
        Notiflix.Notify.warning("Data loading failed ?", {
            position: 'right-top',
        });
    } else {
        let i = 1;
        order.orderDetails.forEach(ord => {
            const subTot = parseInt(ord.price) * parseInt(ord.qty);
            order_table.innerHTML += `
               <tr>
                                <td>#${i}</td>
                                <td>${ord.title}</td>
                                <td>${ord.author}</td>
                                <td>Rs.${ord.price}.00</td>
                                <td>${ord.qty}</td>
                                <td>Rs.${subTot}.00</td>
                            </tr>
            `;
            i++;
        });
        document.getElementById("sub_tot").innerHTML = "Rs." + total + ".00";
        document.getElementById("shipping").innerHTML = "Rs.400.00";
        let fullTot = parseInt(total) + 400;
        document.getElementById("full_tot").innerHTML = "Rs." + fullTot + ".00";

    }
    // console.log(orderdata);
    // console.log(order);
}

document.addEventListener("click", function (e) {
    if (e.target.classList.contains("track-btn")) {
        const state = e.target.dataset.status;
        let currentState = 0;
        if (state === "PROCESSING") {
            currentState = 1;
        } else if (state === "PACKING") {
            currentState = 2;
        } else if (state === "SHIPPING") {
            currentState = 3;
        } else if (state === "DELIVERED") {
            currentState = 4;
        } else if (state === "RECEIVED") {
            currentState = 5;
        }

        loadTrackState(currentState);
    }
});

const stateObj = {
    1: {text: "Processing", icon: "bi-hourglass-split", cls: "processing"},
    2: {text: "Packing", icon: "bi-box-seam", cls: "packing"},
    3: {text: "Shipping", icon: "bi-truck", cls: "shipped"},
    4: {text: "Delivered", icon: "bi-check-circle", cls: "out"},
    5: {text: "Received", icon: "bi-house-check", cls: "delivered"}
};

function loadTrackState(currentState) {


    let html = '<div>';

    Object.keys(stateObj).forEach(key => {

        const step = stateObj[key];
        // console.log(currentState);
        // console.log(key);

        let statusClass = "";

        if (key < currentState) {
            statusClass = "status-completed";
        } else if (key == currentState) {
            statusClass = "status-active";
        }

        html += `
      <div class="status-card ${statusClass}">
                                <div class="status-icon ${step.cls}">
                                    <i class="bi ${step.icon}"></i>
                                </div>
                                <div class="status-title">${step.text}</div>
                            </div>
        `;

        html += '</div>';
    });

    document.getElementById("status-grid").innerHTML = html;

}

function splitText(text, splitBy) {

    return text.split(splitBy);
}

// document.addEventListener("DOMContentLoaded", function () {
//     getUserData();
//     getCities();
//     getAddresses();
// });


