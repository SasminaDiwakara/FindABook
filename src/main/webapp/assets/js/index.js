document.addEventListener('DOMContentLoaded', async () => {
    if (window.Notiflix && Notiflix.Loading) {
        Notiflix.Loading.pulse("Loading products...", {
            clockToClose: false,
            svgColor: '#0284c7'
        });
    }

    try {
        await loadLatestProducts();
        await loadPopularProducts();
        await loadCategories();
    } catch (err) {
        console.error("Initialization error:", err);
    } finally {
        if (window.Notiflix && Notiflix.Loading) {
            Notiflix.Loading.remove();
        }
    }
});

async function loadLatestProducts() {
    const list = document.querySelector(".productCardLatest");

    if (!list) {
        console.warn("Element .productCardLatest not found on this page.");
        return;
    }

    try {
        const res = await fetch("api/products/latest");

        if (!res.ok) {
            throw new Error("Failed to fetch latest products");
        }

        const data = await res.json();
        const { products = [] } = data;

        if ($(list).hasClass('owl-loaded')) {
            $(list).trigger('destroy.owl.carousel');
            $(list).removeClass('owl-loaded owl-drag');
            $(list).find('.owl-stage-outer').children().unwrap();
        }

        list.innerHTML = products.map(product => {
            const coverImage = (product.image && product.image.length > 0) ? product.image[0] : 'assets/images/placeholder.jpg';
            return `
                <div class="item">
                    <div class="book-card" onclick="viewProduct(${product.id})">
                        <img src="${coverImage}" alt="Book Cover">
                        <div class="book-info">
                            <h3 class="book-title">${product.title}</h3>
                            <p class="book-author">by ${product.author}</p>
                            <div class="book-price-row">
                                <span class="book-price">Rs.${product.price}.00</span>
                                <button class="book-buy-btn" type="button">
                                    <i data-feather="shopping-cart"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        }).join('');

        if (window.feather) feather.replace();

        $(list).owlCarousel({
            loop: products.length > 4,
            margin: 20,
            nav: false,
            dots: true,
            autoplay: true,
            autoplayTimeout: 4000,
            autoplayHoverPause: true,
            responsive: {
                0: { items: 1 },
                576: { items: 2 },
                768: { items: 3 },
                992: { items: 4 },
                1200: { items: 5 }
            },
            onInitialized: function () {
                if (window.feather) feather.replace();
            },
            onTranslated: function () {
                if (window.feather) feather.replace();
            }
        });

    } catch (e) {
        console.error("Latest Products Error:", e);
        if (window.Notiflix && Notiflix.Notify) {
            Notiflix.Notify.failure(e.message, { position: "center-top" });
        }
    }
}

async function loadPopularProducts() {
    const list = document.querySelector(".productCardPopular");

    if (!list) {
        console.warn("Element .productCardPopular not found on this page.");
        return;
    }

    try {
        const res = await fetch("api/products/popular");

        if (!res.ok) {
            throw new Error("Failed to fetch popular products");
        }

        const data = await res.json();
        const { products = [] } = data;

        if ($(list).hasClass('owl-loaded')) {
            $(list).trigger('destroy.owl.carousel');
            $(list).removeClass('owl-loaded owl-drag');
            $(list).find('.owl-stage-outer').children().unwrap();
        }

        list.innerHTML = products.map(product => {
            const coverImage = (product.image && product.image.length > 0) ? product.image[0] : 'assets/images/placeholder.jpg';
            return `
                <div class="item">
                    <div class="book-card" onclick="viewProduct(${product.id})">
                        <img src="${coverImage}" alt="Book Cover">
                        <div class="book-info">
                            <h3 class="book-title">${product.title}</h3>
                            <p class="book-author">by ${product.author}</p>
                            <div class="book-price-row">
                                <span class="book-price">Rs.${product.price}.00</span>
                                <button class="book-buy-btn" type="button">
                                    <i data-feather="shopping-cart"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        }).join('');

        if (window.feather) feather.replace();

        $(list).owlCarousel({
            loop: products.length > 4,
            margin: 20,
            nav: false,
            dots: true,
            autoplay: true,
            autoplayTimeout: 4000,
            autoplayHoverPause: true,
            responsive: {
                0: { items: 1 },
                576: { items: 2 },
                768: { items: 3 },
                992: { items: 4 },
                1200: { items: 5 }
            },
            onInitialized: function () {
                if (window.feather) feather.replace();
            },
            onTranslated: function () {
                if (window.feather) feather.replace();
            }
        });

    } catch (e) {
        console.error("Popular Products Error:", e);
        if (window.Notiflix && Notiflix.Notify) {
            Notiflix.Notify.failure(e.message, { position: "center-top" });
        }
    }
}

async function loadCategories() {
    const container = document.querySelector(".categoryCarousel");

    if (!container) {
        console.warn("Element .categoryCarousel not found on this page.");
        return;
    }

    try {
        const res = await fetch("api/data/categories");

        if (!res.ok) {
            throw new Error("Failed to fetch categories");
        }

        const data = await res.json();
        // Adjust array key according to your API response format
        const categories = Array.isArray(data) ? data : (data.categories || data.data || []);

        // Clean up any existing Owl Carousel instance safely
        if ($(container).hasClass('owl-loaded')) {
            $(container).trigger('destroy.owl.carousel');
            $(container).removeClass('owl-loaded owl-drag');
            $(container).find('.owl-stage-outer').children().unwrap();
        }

        // Render Cards
        container.innerHTML = categories.map(cat => {
            const iconName = cat.icon || 'book-open';
            const count = cat.bookCount || cat.count || 0;
            const countText = `${count.toLocaleString()} ${count === 1 ? 'book' : 'books'}`;

            return `
                <div class="item">
                    <div class="category-card" onclick="window.location.href='categories.html?id=${cat.id}'" style="cursor: pointer;">
                        <div class="category-icon-wrapper">
                            <div class="category-icon-bg"></div>
                            <i data-feather="${iconName}" style="width:44px;height:44px;"></i>
                        </div>
                        <h5>${cat.name}</h5>
                    </div>
                </div>
            `;
        }).join('');

        // Re-initialize Feather Icons
        if (window.feather) feather.replace();

        // Re-initialize Owl Carousel
        $(container).owlCarousel({
            loop: categories.length > 4,
            margin: 20,
            nav: false,
            dots: true,
            autoplay: true,
            autoplayTimeout: 3500,
            autoplayHoverPause: true,
            responsive: {
                0: { items: 2 },
                576: { items: 3 },
                768: { items: 4 },
                992: { items: 5 },
                1200: { items: 6 }
            },
            onInitialized: function () {
                if (window.feather) feather.replace();
            },
            onTranslated: function () {
                if (window.feather) feather.replace();
            }
        });

    } catch (e) {
        console.error("Categories Loading Error:", e);
    }
}



function viewProduct(pid) {
    if (pid) {
        window.location = "details.html?pid=" + pid;
    }
}