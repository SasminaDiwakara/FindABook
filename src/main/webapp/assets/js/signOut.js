async function signOut() {

    Notiflix.Loading.pulse("Wait...", {
        clickToClose: false,
        svgColor: '#0284c7'
    });

    try {
        const response = await fetch("api/users/logout", {
            method: "GET",
            credentials: "include",
        });

        if (response.status === 200) {

            window.location = "index.html"

        } else {
            Notiflix.Notify.failure("Something went Wrong ! Logout process paused ", {
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
