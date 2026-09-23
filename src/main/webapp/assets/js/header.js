function loadHeader() {
    fetch("head/header.html")
        .then(res => res.text())
        .then(data => {
            document.getElementById("header").innerHTML = data;
            loadUserDetails();
        });
}

document.addEventListener("DOMContentLoaded", loadHeader);


function loadUserDetails() {
    const list = document.querySelector(".ContentDropdown");

    if (!list) return;
    fetch("/findabook/api/profile/user-profile")
        .then(res => res.json())
        .then(data => {


            list.innerHTML = "";

            if (!data.logged) {
                list.innerHTML = `
                    <div class="cart-icon position-relative" >
                        <i data-feather="shopping-cart" class="text-dark" style="width:24px;height:24px;" onclick="window.location='cart.html'"></i>
                        <span class="cart-badge position-absolute top-0 start-100 translate-middle badge rounded-pill">3</span>
                    </div>

                    <div class="dropdown">
                        <a href="#" class="profile-icon text-dark" id="profileDropdown" data-bs-toggle="dropdown">
                            <i data-feather="user" style="width:24px;height:24px;"></i>
                        </a>
                        <ul class="dropdown-menu dropdown-menu-end shadow">
                            <li class="dropdown-header-custom">
                                <h6>Welcome Back!</h6>
                                <p>Sign in to access your account</p>
                            </li>
                            <li>
                                <button class="dropdown-item text-center btn btn-primary w-100 mb-2" onclick="loadSignInPage()">Sign In</button>
                            </li>
                            <li>
                                <button class="dropdown-item text-center btn btn-outline-primary w-100 mb-3" onclick="loadSignUpPage()">Sign Up</button>
                            </li>
                        </ul>
                    </div>
                `;
            } else {
                const user = data.user || {};

                list.innerHTML = `
                    <div class="cart-icon position-relative">
                        <i data-feather="shopping-cart" class="text-dark" style="width:24px;height:24px;" onclick="window.location='cart.html'"></i>
                        <span class="cart-badge position-absolute top-0 start-100 translate-middle badge rounded-pill">3</span>
                    </div>

                    <div class="dropdown">
                        <a href="#" class="profile-icon text-dark" id="profileDropdown" data-bs-toggle="dropdown">
                            <i data-feather="user" style="width:24px;height:24px;"></i>
                        </a>
                        <ul class="dropdown-menu dropdown-menu-end shadow">
                            <li class="dropdown-header-custom">
                                <h6>Hello, ${user.firstName} ${user.lastName}</h6>
                                <p>Manage your account</p>
                            </li>
                            <li><a class="dropdown-link" href="user.html"><i data-feather="user"></i> My Profile</a></li>
<!--                            <li><a class="dropdown-link" href="user.html"><i data-feather="book"></i> My Library</a></li>-->
                            <li><a class="dropdown-link" href="user.html"><i data-feather="heart"></i> Wishlist</a></li>
                            <li><a class="dropdown-link" href="user.html"><i data-feather="settings"></i> Settings</a></li>
                            <li><button class="dropdown-item text-center btn btn-danger w-100 mt-3 logout-btn" onclick="signOut()">Logout</button></li>

                        </ul>
                    </div>
                `;
            }

            feather.replace();
        }).catch(err => {
        console.error("Failed to load user details", err);
    });
}

