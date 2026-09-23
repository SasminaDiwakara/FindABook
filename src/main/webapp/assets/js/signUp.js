async function signUp() {

    let firstName = document.getElementById("firstName");
    let lastName = document.getElementById("lastName");
    let email = document.getElementById("email");
    let password = document.getElementById("password");
    let confirmPassword = document.getElementById("confirmPassword");

    const userSignUpObj = {
        firstName: firstName.value,
        lastName: lastName.value,
        email: email.value,
        password: password.value,
        confirmPassword: confirmPassword.value
    }

    try {

        const response = await fetch("api/users", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(userSignUpObj)
        });

        if (response.ok) {

            console.log(" working", response.status)

            const data = await response.json();

            console.log("Server Response" + data.message);

            if (data.status) {
                Notiflix.Loading.pulse("Wait...", {
                    clickToClose: false,
                    svgColor: '#0284c7'
                });

                Notiflix.Report.success("FindABook",
                    data.message,
                    'Okay',
                    () => {
                        window.location = "verify.html"
                    }
                );

            } else {

                Notiflix.Notify.warning("Error : " + data.message,

                    {
                        position: 'right-top',

                    });
            }


        } else {
            Notiflix.Notify.failure("Sign-Up failed ! please try again later." + response.status,

                {
                    position: 'right-top',

                });
        }

    } catch (e) {
        Notiflix.Notify.failure(e.message);
    } finally {
        Notiflix.Loading.remove(1000);
    }
}

async function resendCode() {

    Notiflix.Loading.pulse("Wait...", {
        clickToClose: false,
        svgColor: '#0284c7',
    });

    const email = document.getElementById("email").value.trim();

    if (email == "") {
        Notiflix.Notify.warning("Email is Required to proceed !");
        return;
    }

    try {
        const response = await fetch(`api/users/${email}/resendCode`);

        if (response.ok) {

            const data = await response.json();
            console.log(data);

            if (data.status) {
                Notiflix.Report.success(
                    'Find A Book',
                    data.message,
                );

                startTimer();

            } else {
                Notiflix.Notify.warning(data.message);
            }

        } else {
            Notiflix.Notify.failure("Verification Code sending failed , Please try again.");
        }

    } catch (e) {
        Notiflix.Notify.failure(e.message);
    } finally {
        Notiflix.Loading.remove(1000);
    }


}

let timeLeft = 30;
let timer;

function startTimer() {

    const timerText = document.getElementById("timerText");
    const resendBtn = document.getElementById("resendLink");

    timerText.classList.remove("d-none");
    resendBtn.classList.add("d-none");
    resendBtn.style.cursor = "not-allowed";

    timer = setInterval(() => {

        let seconds = timeLeft < 10 ? "0" + timeLeft : timeLeft;
        timerText.innerText = `Resend in 00:${seconds}`;

        timeLeft--;

        if (timeLeft < 0) {
            clearInterval(timer);
            timerText.innerText = "Didn't receive code?";
            resendBtn.classList.remove("d-none");
            resendBtn.style.cursor = "pointer";
        }

    }, 1000);
}

async function verify() {


    const email = document.getElementById("email");
    const verificationCode = document.getElementById("verify_code");

    const veriyObj = {
        email: email.value,
        verificationCode: verificationCode.value,
    }

    // console.log(veriyObj);

    try {
        const response = await fetch("api/users/verify", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(veriyObj)
        });

        console.log(response.status);
        // console.log(veriyObj);

        if (response.ok) {
            const data = await response.json();
            console.log(data);
            if (data.status) {
                Notiflix.Report.success(
                    'Find A Book',
                    data.message,
                    'Approved',
                    () => {
                        window.location = "signIn.html"
                    },
                );

            } else {
                Notiflix.Notify.warning(data.message);
            }

        } else {
            Notiflix.Notify.failure("Verification process failed , Please try again later.");
        }

    } catch (e) {
        Notiflix.Notify.failure(e.message);
    } finally {
        Notiflix.Loading.remove(1000);
    }


}