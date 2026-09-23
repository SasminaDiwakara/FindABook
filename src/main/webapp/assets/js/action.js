
let page = window.location.pathname.split("/").pop();
// console.log(page);
async function addToWishlist(){

    let pid = document.getElementById("product-id").value;

    try {

        const response = await fetch(`api/action/${pid}/addToWishList`);

        if (response.ok) {

            data = await response.json();

            // console.log(data);
            // console.log(page);

            if (data.status) {
                Notiflix.Notify.success(data.message, {
                    position: 'right-top',
                });

                if (page === "user.html"){
                    window.location.reload();
                }

            } else {
                Notiflix.Notify.warning(data.message, {
                    position: 'right-top',
                });
            }
        } else {
            Notiflix.Notify.warning("Something went wrong ?", {
                position: 'right-top',
            });
        }

    } catch (e) {
        Notiflix.Notify.failure(e.message, {
            position: 'right-top',
        });
    }

}


async function addToCart() {

    const pid = document.getElementById("product-id").value;

    try {

        const response = await fetch(`api/action/${pid}/addToCart`);

        if (response.ok) {

            const data = await response.json();

            // console.log(data);

            if (data.status) {
                Notiflix.Report.success(
                    'Find A Book',
                    data.message,
                    'Okay',
                );


            } else {
                Notiflix.Notify.warning("Carting process failed ?", {
                    position: 'right-top',
                });
            }

        } else {
            Notiflix.Notify.warning("Something went wrong ?", {
                position: 'right-top',
            });

        }

    } catch (e) {
        Notiflix.Notify.failure(e.message, {
            position: 'right-top',
        });
    }

}


