const modalElement = document.getElementById('exampleModal');
let myModal;


async function forgotPassword() {
    myModal = new bootstrap.Modal(modalElement);
    // myModal.show();

    let email = document.getElementById("email").value.trim();

    if (email == "") {

        Notiflix.Notify.warning("Email is required to proceed !", {
            position: 'center-top',
        });
        return;
    }

    try {

        Notiflix.Loading.pulse("Wait...", {
            clickToClose: false,
            svgColor: '#0284c7'
        });

        const response = await fetch(`api/users/${email}/forgotPassword`);

        if (response.ok) {

            const data = await response.json();

            console.log(data);

            if (data.status) {

                const modalElement = document.getElementById('exampleModal');
                const myModal = new bootstrap.Modal(modalElement);
                Notiflix.Report.success(
                    'Find A Book',
                    data.message,
                    'Okay',
                    () => {
                        myModal.show();
                    }
                );

            } else {
                Notiflix.Notify.warning(data.message, {
                    position: 'center-top',
                });
            }


        } else {
            Notiflix.Notify.failure("Something went Wrong ! ", {
                position: 'center-top',
            });
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message, {
            position: 'center-top',
        });
    } finally {
        Notiflix.Loading.remove(1000);
    }

}

async function resetPassword() {

    Notiflix.Loading.pulse("Wait...", {
        clickToClose: false,
        svgColor: '#0284c7'
    });

    let email = document.getElementById("email-name");
    let code = document.getElementById("code");
    let newPw = document.getElementById("newPassword");

    const dataObj = {
        email: email.value,
        code: code.value,
        newPassword: newPw.value
    }

    try {
        const response = await fetch(`api/users/resetPassword`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(dataObj)
        });

        if (response.ok) {
            const data = await response.json();

            console.log(data);

            if (data.status) {
                Notiflix.Report.success(
                    'Find A Book',
                    data.message,
                    'Okay', () => {
                        myModal.hide();
                    }
                );
            } else {
                Notiflix.Notify.warning(data.message, {
                    position: 'center-top',
                });
            }


        } else {
            Notiflix.Notify.failure("Something went Wrong !", {
                position: 'center-top',
            });
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message, {
            position: 'center-top',
        });
    } finally {
        Notiflix.Loading.remove(1000);
    }


}
