async function signIn() {

    Notiflix.Loading.pulse("Wait...", {
        clickToClose: false,
        svgColor: '#0284c7'
    });

    let email = document.getElementById("email");
    let password = document.getElementById("password");
    let remember = document.getElementById("remember");


    const userLoginObj = {
        email: email.value,
        password: password.value,
        remember: remember.checked
    }

    try {

        const response = await fetch("api/users/login", {
            method: "POST",
            headers: {
                "Content-Type":"application/json"
            },
            body: JSON.stringify(userLoginObj)
        });
        if (response.ok) {
            const data = await response.json();

            if (data.status) {
                Notiflix.Report.success("FindABook",
                    data.message,
                    'Okay',
                    () => {
                        window.location = "index.html"

                    },
                );
            }else{
                Notiflix.Notify.warning("Warning "+data.message,{
                    position:'right-top',
                });
            }
            console.log(data);
        } else {
            Notiflix.Notify.failure("Login failed ! please try again later",{
                position:'right-top',
            });

            // console.log(response.status);
        }


    } catch (e) {
        Notiflix.Notify.failure(e.message);
        console.log(e.message);
    } finally {
        Notiflix.Loading.remove(1000);
    }
}

// On page load, check for a saved "remember me" cookie and prefill the
// email field (and re-check the box) so a returning user doesn't have
// to retype their email. Uses the existing GET /users/loadCookies
// endpoint, which already reads and decodes the remember_me cookie
// server-side.
async function prefillRememberedUser() {
    try {
        const response = await fetch("api/users/loadCookies");
        if (!response.ok) return;

        const data = await response.json();
        if (data.status && data.email) {
            const email = document.getElementById("email");
            const remember = document.getElementById("remember");

            if (email) email.value = data.email;
            if (remember) remember.checked = true;
        }
    } catch (e) {
        // Non-fatal — just skip prefilling if this fails.
        console.log("Could not load remembered user:", e.message);
    }
}

document.addEventListener("DOMContentLoaded", prefillRememberedUser);

async function AdminSignIn() {

    Notiflix.Loading.pulse("Wait...", {
        clickToClose: false,
        svgColor: '#0284c7'
    });

    let email = document.getElementById("email");
    let password = document.getElementById("password");


    const userLoginObj = {
        email: email.value,
        password: password.value,
    }

    try {

        const response = await fetch("api/users/adminLogin", {
            method: "POST",
            headers: {
                "Content-Type":"application/json"
            },
            body: JSON.stringify(userLoginObj)
        });
        if (response.ok) {
            const data = await response.json();

            if (data.status) {
                Notiflix.Report.success("FindABook",
                    data.message,
                    'Okay',
                    () => {
                        window.location = "admin.html"

                    },
                );
            }else{
                Notiflix.Notify.warning("Warning "+data.message,{
                    position:'right-top',
                });
            }
            console.log(data);
        } else {
            Notiflix.Notify.failure("Login failed ! please try again later",{
                position:'right-top',
            });

            // console.log(response.status);
        }


    } catch (e) {
        Notiflix.Notify.failure(e.message);
        console.log(e.message);
    } finally {
        Notiflix.Loading.remove(1000);
    }
}