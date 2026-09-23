let editor1 = null;
let editingProductId = 0;
let editMode = false;
let adminProducts = [];
let adminAuthors = [];
let adminCategories = [];
let adminOrders = [];

window.addEventListener("load", async () => {
    if (window.Notiflix) {
        Notiflix.Loading.dots("Loading Data...", {
            clickToClose: false,
            svgColor: "#0284c7"
        });
    }

    try {
        await getCategories();
        await getAuthor();
        await loadAllProducts();
        await loadAllOrders();

        await loadTotalUsers();

        updateDashboardStats();
    } catch (error) {
        console.error("Initialization error:", error);
        notifyFailure(error.message || "Failed to initialize admin data.");
    } finally {
        if (window.Notiflix) {
            Notiflix.Loading.remove();
        }
    }
});

/* =========================================================
   COMMON HELPERS
   ========================================================= */

function escapeHtml(text) {
    if (!text) return "";
    return String(text)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function escapeAttribute(text) {
    if (!text) return "";
    return String(text)
        .replace(/&/g, "&amp;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function escapeJs(value) {
    return String(value ?? "")
        .replace(/\\/g, "\\\\")
        .replace(/'/g, "\\'")
        .replace(/\r/g, "")
        .replace(/\n/g, "\\n");
}

function replaceFeatherIcons() {
    if (typeof feather !== "undefined" && feather.replace) {
        feather.replace();
    }
}

function notifyFailure(message) {
    if (window.Notiflix) {
        Notiflix.Notify.failure(message, { position: "right-top" });
    } else {
        console.error(message);
    }
}

function notifyWarning(message) {
    if (window.Notiflix) {
        Notiflix.Notify.warning(message, { position: "right-top" });
    } else {
        console.warn(message);
    }
}

function notifySuccess(message) {
    if (window.Notiflix) {
        Notiflix.Notify.success(message, { position: "right-top" });
    } else {
        console.log(message);
    }
}

async function readJsonResponse(response) {
    const text = await response.text();

    if (!text) {
        return {};
    }

    try {
        return JSON.parse(text);
    } catch (error) {
        console.error("Invalid JSON response:", text);
        throw new Error(
            `Server returned invalid JSON (${response.status}).`
        );
    }
}

function getProductImages(product) {
    if (!product) return [];

    if (Array.isArray(product.images)) {
        return product.images;
    }

    if (Array.isArray(product.image)) {
        return product.image;
    }

    return [];
}

function getAuthorId(product) {
    if (!product) return "0";

    return String(
        product.authorId ??
        product.author ??
        product.authorID ??
        "0"
    );
}

function getCategoryId(product) {
    if (!product) return "0";

    return String(
        product.categoryId ??
        product.categoryID ??
        product.category?.id ??
        "0"
    );
}

function getProductModal() {
    const modalElement = document.getElementById("exampleModal");

    if (!modalElement) {
        throw new Error("Product modal #exampleModal was not found.");
    }

    if (!window.bootstrap || !bootstrap.Modal) {
        throw new Error("Bootstrap Modal is not loaded.");
    }

    return {
        element: modalElement,
        instance: bootstrap.Modal.getOrCreateInstance(modalElement)
    };
}

function hideModal(modalId) {
    const el = document.getElementById(modalId);
    if (el && window.bootstrap && bootstrap.Modal) {
        const modal = bootstrap.Modal.getInstance(el);
        if (modal) {
            modal.hide();
        }
    }
}

function openManagementModal(id) {
    const el = document.getElementById(id);
    if (!el || !window.bootstrap || !bootstrap.Modal) {
        notifyFailure("Bootstrap modal is not available.");
        return null;
    }
    return bootstrap.Modal.getOrCreateInstance(el);
}

/* =========================================================
   RICH TEXT EDITOR
   ========================================================= */

function initializeProductEditor() {
    if (editor1) {
        return editor1;
    }

    const descriptionElement =
        document.getElementById("description");

    if (!descriptionElement) {
        console.warn(
            "RichTextEditor: #description element not found."
        );
        return null;
    }

    if (typeof RichTextEditor === "undefined") {
        console.error(
            "RichTextEditor library is not loaded."
        );
        return null;
    }

    try {
        editor1 = new RichTextEditor(descriptionElement);

        console.log(
            "Product RichTextEditor initialized successfully."
        );

        return editor1;
    } catch (error) {
        console.error(
            "Failed to initialize RichTextEditor:",
            error
        );

        editor1 = null;
        return null;
    }
}

function getDescriptionHtml() {
    const editor = initializeProductEditor();

    if (editor && typeof editor.getHTMLCode === "function") {
        return editor.getHTMLCode() || "";
    }

    const textarea =
        document.getElementById("description");

    return textarea ? textarea.value || "" : "";
}

function setDescriptionHtml(html) {
    const value = html || "";
    const editor = initializeProductEditor();

    if (editor && typeof editor.setHTMLCode === "function") {
        editor.setHTMLCode(value);
        return;
    }

    const textarea =
        document.getElementById("description");

    if (textarea) {
        textarea.value = value;
    }
}

document.addEventListener("DOMContentLoaded", () => {
    initializeProductEditor();

    const modal = document.getElementById("exampleModal");

    if (modal) {
        modal.addEventListener("shown.bs.modal", () => {
            initializeProductEditor();
        });
    }

    const imageUpload =
        document.getElementById("imageUpload");

    const preview =
        document.getElementById("previewImg");

    if (imageUpload && preview) {
        imageUpload.addEventListener("change", () => {
            const file = imageUpload.files?.[0];

            if (!file) {
                preview.src = "";
                preview.style.display = "none";
                return;
            }

            if (!file.type.startsWith("image/")) {
                notifyWarning("Please select a valid image file.");
                imageUpload.value = "";
                preview.src = "";
                preview.style.display = "none";
                return;
            }

            preview.src = URL.createObjectURL(file);
            preview.style.display = "block";
        });
    }
});

/* =========================================================
   STATS & DASHBOARD
   ========================================================= */

async function loadTotalUsers() {
    try {
        const response = await fetch("api/admin-data/users");
        const data = await response.json();
        const users = data.users || [];

        const element = document.getElementById("totalUsers");
        if (element) {
            element.textContent = users.length;
        }
    } catch (error) {
        console.error("Error loading users:", error);
    }
}

// async function loadAllOrders() {
//     try {
//         const response = await fetch("api/orders");
//         const data = await readJsonResponse(response);
//         adminOrders = data.orders || [];
//     } catch (error) {
//         console.error("Error loading orders:", error);
//     }
// }

function updateDashboardStats() {
    const totalBooks = Array.isArray(adminProducts) ? adminProducts.length : 0;
    const totalOrders = Array.isArray(adminOrders) ? adminOrders.length : 0;
    const totalRevenue = Array.isArray(adminOrders)
        ? adminOrders.reduce((total, order) => total + Number(order.total || 0), 0)
        : 0;

    const booksElement = document.getElementById("totalBooks");
    const ordersElement = document.getElementById("totalOrders");
    const revenueElement = document.getElementById("totalRevenue");

    if (booksElement) booksElement.textContent = totalBooks;
    if (ordersElement) ordersElement.textContent = totalOrders;
    if (revenueElement) revenueElement.textContent = "Rs" + totalRevenue.toFixed(2);
}

/* =========================================================
   CATEGORIES
   ========================================================= */

async function getCategories() {
    try {
        const response = await fetch("api/admin-data/categories");
        const data = await readJsonResponse(response);

        if (!response.ok) {
            throw new Error(data.message || "Category loading failed.");
        }

        const categories = Array.isArray(data.categories) ? data.categories : [];
        adminCategories = categories;

        const categorySelect = document.getElementById("category");
        if (categorySelect) {
            categorySelect.innerHTML = `<option value="0">Select Category</option>`;
            categories.forEach(category => {
                const option = document.createElement("option");
                option.value = category.id;
                option.textContent = category.name;
                categorySelect.appendChild(option);
            });
        }

        renderCategoriesTable(categories);
    } catch (error) {
        console.error("Category loading error:", error);
        notifyFailure(error.message);
    }
}

function renderCategoriesTable(categories) {
    const tbody = document.querySelector("#categoriesSection table.admin-table tbody");
    if (!tbody) return;

    const query = (document.getElementById("categoriesSearch")?.value || "")
        .trim().toLowerCase();

    const filtered = categories.filter(category =>
        String(category.name || "").toLowerCase().includes(query)
    );

    if (!filtered.length) {
        tbody.innerHTML = `<tr><td colspan="4" class="text-center py-4">No categories found.</td></tr>`;
        return;
    }

    tbody.innerHTML = filtered.map(category => {
        // Use backend aggregated bookCount directly or fallback to frontend check
        const bookCount = category.bookCount ?? adminProducts.filter(book =>
            String(book.categoryId ?? book.categoryID ?? book.category?.id ?? "") === String(category.id)
        ).length;

        return `
            <tr>
                <td style="font-weight:700;">${escapeHtml(category.name)}</td>
                <td>${bookCount}</td>
                <td><span class="badge badge-success">Active</span></td>
                <td>
                    <div class="action-buttons">
                        <button type="button" class="btn-icon btn-edit"
                                title="Edit category"
                                onclick="editCategory(${Number(category.id)}, '${escapeJs(category.name || "")}')">
                            <i data-feather="edit-2"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join("");

    replaceFeatherIcons();
}

function editCategory(id, name) {
    const idInput = document.getElementById("editCategoryId");
    const nameInput = document.getElementById("editCategoryName");
    if (!idInput || !nameInput) {
        notifyFailure("Category edit modal is missing from admin.html.");
        return;
    }
    idInput.value = id;
    nameInput.value = name || "";
    const modal = openManagementModal("editCategoryModal");
    if (modal) {
        modal.show();
        setTimeout(() => nameInput.focus(), 250);
    }
}

async function updateCategory() {
    const id = Number(document.getElementById("editCategoryId")?.value || 0);
    const input = document.getElementById("editCategoryName");
    const name = input?.value.trim() || "";

    if (!id || !name) {
        notifyWarning("Category name is required.");
        return;
    }

    try {
        const response = await fetch("api/products/admin/category/update", {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ id, name })
        });
        const data = await readJsonResponse(response);
        if (!response.ok || !data.status) {
            throw new Error(data.message || "Category update failed.");
        }
        notifySuccess(data.message || "Category updated successfully.");
        openManagementModal("editCategoryModal")?.hide();
        await getCategories();
        await loadAllProducts();
    } catch (error) {
        console.error("Update category error:", error);
        notifyFailure(error.message || "Category update failed.");
    }
}

async function addNewCategory() {
    const input = document.getElementById("categoryName");

    if (!input || !input.value.trim()) {
        notifyWarning("Please enter a category name.");
        return;
    }

    try {
        const response = await fetch("api/admin-data/addCategories", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ categoryName: input.value.trim() })
        });

        const data = await readJsonResponse(response);

        if (!response.ok || !data.status) {
            throw new Error(data.message || "Category adding failed.");
        }

        notifySuccess(data.message || "Category added successfully!");
        input.value = "";
        hideModal("exampleModal2");
        await getCategories();
    } catch (error) {
        console.error("Add category error:", error);
        notifyFailure(error.message);
    }
}

/* =========================================================
   AUTHORS
   ========================================================= */

async function getAuthor() {
    try {
        const response = await fetch("api/admin-data/authors");
        const data = await readJsonResponse(response);

        if (!response.ok) {
            throw new Error(data.message || "Authors loading failed.");
        }

        const authors = Array.isArray(data.authors) ? data.authors : [];
        adminAuthors = authors;

        const authorsSelect = document.getElementById("authors");
        if (authorsSelect) {
            authorsSelect.innerHTML = `<option value="0">Select Author</option>`;
            authors.forEach(author => {
                const option = document.createElement("option");
                option.value = author.id;
                option.textContent = author.name;
                authorsSelect.appendChild(option);
            });
        }

        renderAuthorsTable(authors);
    } catch (error) {
        console.error("Authors loading error:", error);
        notifyFailure(error.message);
    }
}

function renderAuthorsTable(authors) {
    const tbody = document.querySelector("#authorsSection table.admin-table tbody");
    if (!tbody) return;

    const query = (document.getElementById("authorsSearch")?.value || "")
        .trim().toLowerCase();

    const filtered = authors.filter(author =>
        String(author.name || "").toLowerCase().includes(query)
    );

    if (!filtered.length) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center py-4">No authors found.</td></tr>`;
        return;
    }

    tbody.innerHTML = filtered.map(author => {
        // Use backend aggregated bookCount directly or fallback to frontend check
        const bookCount = author.bookCount ?? adminProducts.filter(book =>
            String(book.authorId ?? book.authorID ?? book.author ?? "") === String(author.id)
        ).length;

        return `
            <tr>
                <td style="font-weight:700;">${escapeHtml(author.name)}</td>
                <td>${bookCount}</td>
                <td><span class="badge badge-success">Active</span></td>
                <td>
                    <div class="action-buttons">
                        <button type="button" class="btn-icon btn-edit"
                                title="Edit author"
                                onclick="editAuthor(${Number(author.id)}, '${escapeJs(author.name || "")}')">
                            <i data-feather="edit-2"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join("");

    replaceFeatherIcons();
}

function editAuthor(id, name) {
    const idInput = document.getElementById("editAuthorId");
    const nameInput = document.getElementById("editAuthorName");
    if (!idInput || !nameInput) {
        notifyFailure("Author edit modal is missing from admin.html.");
        return;
    }
    idInput.value = id;
    nameInput.value = name || "";
    const modal = openManagementModal("editAuthorModal");
    if (modal) {
        modal.show();
        setTimeout(() => nameInput.focus(), 250);
    }
}

async function updateAuthor() {
    const id = Number(document.getElementById("editAuthorId")?.value || 0);
    const input = document.getElementById("editAuthorName");
    const name = input?.value.trim() || "";

    if (!id || !name) {
        notifyWarning("Author name is required.");
        return;
    }

    try {
        const response = await fetch("api/products/admin/author/update", {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ id, name })
        });
        const data = await readJsonResponse(response);
        if (!response.ok || !data.status) {
            throw new Error(data.message || "Author update failed.");
        }
        notifySuccess(data.message || "Author updated successfully.");
        openManagementModal("editAuthorModal")?.hide();
        await getAuthor();
        await loadAllProducts();
    } catch (error) {
        console.error("Update author error:", error);
        notifyFailure(error.message || "Author update failed.");
    }
}

async function addNewAuthor() {
    const input = document.getElementById("authorName");

    if (!input || !input.value.trim()) {
        notifyWarning("Please enter an author name.");
        return;
    }

    try {
        const response = await fetch("api/admin-data/addAuthor", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ authorName: input.value.trim() })
        });

        const data = await readJsonResponse(response);

        if (!response.ok || !data.status) {
            throw new Error(data.message || "Author adding failed.");
        }

        notifySuccess(data.message || "Author added successfully!");
        input.value = "";
        hideModal("exampleModal1");
        await getAuthor();
    } catch (error) {
        console.error("Add author error:", error);
        notifyFailure(error.message);
    }
}

/* =========================================================
   PRODUCTS
   ========================================================= */

async function loadAllProducts() {
    try {
        const response = await fetch("api/products/search?limit=100");

        if (!response.ok) {
            throw new Error("Failed to load products.");
        }

        const data = await response.json();
        const products = data.products || data.items || [];

        adminProducts = products;

        renderProductsTable(products);
        renderStockTable(products);

        // Re-render category/author tables so counts update dynamically
        renderCategoriesTable(adminCategories);
        renderAuthorsTable(adminAuthors);

        updateDashboardStats();
    } catch (error) {
        console.error("Error loading products:", error);
        notifyFailure("Failed to load products.");
    }
}

function renderProductsTable(products) {
    const tbody = document.querySelector("#booksSection table.admin-table tbody");

    if (!tbody) return;

    if (!products.length) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center">
                    No books available.
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = products.map(book => {
        const images = getProductImages(book);
        const image = images.length ? images[0] : "assets/images/findabookLogo.png";
        const author = book.author || book.authorName || "Unknown";
        const category = book.categoryName || book.category?.name || "General";
        const price = Number(book.price || 0);
        const stock = Number(book.stock || 0);

        return `
            <tr>
                <td>
                    <div class="product-cell">
                        <img src="${escapeAttribute(image)}" class="product-image" alt="Book">
                        <div class="product-info">
                            <div class="product-name">${escapeHtml(book.title || "Untitled")}</div>
                            <div class="product-author">${escapeHtml(author)}</div>
                        </div>
                    </div>
                </td>
                <td>${escapeHtml(category)}</td>
                <td style="font-weight: 700; color: #667eea;">Rs ${price.toFixed(2)}</td>
                <td>${stock}</td>
                <td>
                    <span class="badge ${stock > 0 ? "badge-success" : "badge-danger"}">
                        ${stock > 0 ? "Active" : "Out of Stock"}
                    </span>
                </td>
                <td>
                    <div class="action-buttons">
                        <button type="button" class="btn-icon btn-edit" onclick="editProduct(${Number(book.id)})" title="Edit book">
                            <i data-feather="edit-2"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join("");

    replaceFeatherIcons();
}

function renderStockTable(products) {
    const tbody = document.querySelector("#stockSection table.admin-table tbody");
    if (!tbody) return;

    if (!products.length) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center">No inventory found.</td></tr>`;
        return;
    }

    tbody.innerHTML = products.map(book => {
        const images = getProductImages(book);
        const image = images.length ? images[0] : "assets/images/findabookLogo.png";
        const stock = Number(book.stock || 0);

        return `
            <tr>
                <td>
                    <div class="product-cell">
                        <img src="${escapeAttribute(image)}" class="product-image" alt="Book">
                        <div class="product-info">
                            <div class="product-name">${escapeHtml(book.title || "Untitled")}</div>
                        </div>
                    </div>
                </td>
                <td>${stock}</td>
                <td>
                    <span class="badge ${stock > 10 ? "badge-success" : stock > 0 ? "badge-warning" : "badge-danger"}">
                        ${stock > 10 ? "In Stock" : stock > 0 ? "Low Stock" : "Out of Stock"}
                    </span>
                </td>
                <td>
                    <div class="action-buttons">
                        <button type="button" class="btn-icon btn-edit" onclick="editProduct(${Number(book.id)})" title="Update Stock">
                            <i data-feather="edit-2"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join("");

    replaceFeatherIcons();
}

function resetProductForm() {
    editingProductId = 0;
    editMode = false;

    const form = document.getElementById("productForm");
    if (form) form.reset();

    const bookName = document.getElementById("bookName");
    const authors = document.getElementById("authors");
    const category = document.getElementById("category");
    const price = document.getElementById("price");
    const qty = document.getElementById("qty");
    const preview = document.getElementById("previewImg");
    const imageUpload = document.getElementById("imageUpload");

    if (bookName) bookName.value = "";
    if (authors) authors.value = "0";
    if (category) category.value = "0";
    if (price) price.value = "";
    if (qty) qty.value = "";
    if (imageUpload) imageUpload.value = "";

    if (preview) {
        preview.src = "";
        preview.style.display = "none";
    }

    setDescriptionHtml("");

    const title = document.getElementById("productModalLabel");
    const subtitle = document.getElementById("productModalSubtitle");
    const submitBtn = document.getElementById("submitBtn");

    if (title) title.textContent = "Add New Book";
    if (subtitle) subtitle.textContent = "Add a new book to the store inventory";
    if (submitBtn) submitBtn.textContent = "Save Product";
}

async function editProduct(productId) {
    try {
        if (!productId) {
            notifyFailure("Invalid product ID.");
            return;
        }

        editMode = true;
        editingProductId = Number(productId);

        if (window.Notiflix) {
            Notiflix.Loading.dots("Loading Product...", {
                clickToClose: false,
                svgColor: "#0284c7"
            });
        }

        const response = await fetch(`api/products/${editingProductId}/loadProductData`);
        const data = await readJsonResponse(response);

        if (!response.ok || !data.status) {
            throw new Error(data.message || "Unable to load product.");
        }

        const product = data.singleProduct;

        if (!product) {
            throw new Error("Product data was not found.");
        }

        const modalData = getProductModal();

        const bookName = document.getElementById("bookName");
        const authors = document.getElementById("authors");
        const category = document.getElementById("category");
        const price = document.getElementById("price");
        const qty = document.getElementById("qty");
        const preview = document.getElementById("previewImg");

        if (bookName) bookName.value = product.title || "";
        if (authors) authors.value = getAuthorId(product);
        if (category) category.value = getCategoryId(product);
        if (price) price.value = product.price ?? "";
        if (qty) qty.value = product.stock ?? "";

        const images = getProductImages(product);

        if (preview) {
            if (images.length) {
                preview.src = images[0];
                preview.style.display = "block";
            } else {
                preview.src = "";
                preview.style.display = "none";
            }
        }

        const title = document.getElementById("productModalLabel");
        const subtitle = document.getElementById("productModalSubtitle");
        const submitBtn = document.getElementById("submitBtn");

        if (title) title.textContent = "Edit Book";
        if (subtitle) subtitle.textContent = "Update the selected book";
        if (submitBtn) submitBtn.textContent = "Update Product";

        const description = product.description || "";

        const applyDescription = () => {
            initializeProductEditor();
            setDescriptionHtml(description);
        };

        if (editor1) {
            applyDescription();
        } else {
            modalData.element.addEventListener(
                "shown.bs.modal",
                applyDescription,
                { once: true }
            );
        }

        modalData.instance.show();
    } catch (error) {
        console.error("Edit product error:", error);
        editMode = false;
        editingProductId = 0;
        notifyFailure(error.message || "Unable to load product.");
    } finally {
        if (window.Notiflix) {
            Notiflix.Loading.remove();
        }
    }
}

async function saveProduct() {
    if (editMode) {
        await updateProduct();
    } else {
        await addNewProduct();
    }
}

async function uploadImage(productId) {
    const imageUpload = document.getElementById("imageUpload");
    if (!imageUpload || !imageUpload.files || imageUpload.files.length === 0) {
        return;
    }

    const imageFormData = new FormData();
    Array.from(imageUpload.files).forEach((file) => {
        imageFormData.append("images[]", file);
    });

    const response = await fetch(`api/products/${productId}/upload-images`, {
        method: "PUT",
        body: imageFormData
    });

    const data = await readJsonResponse(response);
    if (!response.ok || !data.status) {
        throw new Error(data.message || "Failed to upload product image.");
    }
}

async function addNewProduct() {
    const name = document.getElementById("bookName");
    const author = document.getElementById("authors");
    const category = document.getElementById("category");
    const price = document.getElementById("price");
    const qty = document.getElementById("qty");
    const imageUpload = document.getElementById("imageUpload");
    const description = getDescriptionHtml();

    if (!name || !name.value.trim()) {
        notifyWarning("Please enter a book name.");
        return;
    }

    if (!description.trim()) {
        notifyWarning("Please enter a description.");
        return;
    }

    if (!author || author.value === "0") {
        notifyWarning("Please select an author.");
        return;
    }

    if (!category || category.value === "0") {
        notifyWarning("Please select a category.");
        return;
    }

    if (!price || price.value === "") {
        notifyWarning("Please enter the price.");
        return;
    }

    if (!qty || qty.value === "") {
        notifyWarning("Please enter the stock quantity.");
        return;
    }

    if (!imageUpload || !imageUpload.files || imageUpload.files.length === 0) {
        notifyWarning("Please add a product image.");
        return;
    }

    const productObject = {
        title: name.value.trim(),
        description: description,
        author: author.value,
        categoryId: Number(category.value),
        price: Number(price.value),
        stock: Number(qty.value)
    };

    const formData = new FormData();
    formData.append("products", JSON.stringify(productObject));

    try {
        if (window.Notiflix) {
            Notiflix.Loading.dots("Adding Product...", {
                clickToClose: false,
                svgColor: "#0284c7"
            });
        }

        const response = await fetch("api/products/addProducts", {
            method: "POST",
            body: formData
        });

        const data = await readJsonResponse(response);

        if (!response.ok || !data.status) {
            throw new Error(data.message || "Failed to add product.");
        }

        if (!data.productId) {
            throw new Error("Product was created, but the server did not return productId.");
        }

        await uploadImage(data.productId);

        notifySuccess("Product added successfully!");
        hideModal("exampleModal");
        resetProductForm();

        await loadAllProducts();
        await getCategories();
        await getAuthor();
    } catch (error) {
        console.error("Add product error:", error);
        notifyFailure(error.message || "Failed to add product.");
    } finally {
        if (window.Notiflix) {
            Notiflix.Loading.remove();
        }
    }
}

async function updateProduct() {
    try {
        const bookName = document.getElementById("bookName");
        const authors = document.getElementById("authors");
        const category = document.getElementById("category");
        const price = document.getElementById("price");
        const qty = document.getElementById("qty");
        const imageUpload = document.getElementById("imageUpload");

        if (!editingProductId) {
            notifyFailure("No product selected for update.");
            return;
        }

        const description = getDescriptionHtml();

        if (!bookName || !bookName.value.trim()) {
            notifyWarning("Please enter book name.");
            return;
        }

        if (!description.trim()) {
            notifyWarning("Please enter description.");
            return;
        }

        if (!authors || authors.value === "0") {
            notifyWarning("Please select an author.");
            return;
        }

        if (!category || category.value === "0") {
            notifyWarning("Please select a category.");
            return;
        }

        if (!price || price.value === "") {
            notifyWarning("Please enter the price.");
            return;
        }

        if (!qty || qty.value === "") {
            notifyWarning("Please enter the stock quantity.");
            return;
        }

        const productObject = {
            id: editingProductId,
            title: bookName.value.trim(),
            description: description,
            author: authors.value,
            categoryId: Number(category.value),
            price: Number(price.value),
            stock: Number(qty.value)
        };

        if (window.Notiflix) {
            Notiflix.Loading.dots("Updating Product...", {
                clickToClose: false,
                svgColor: "#0284c7"
            });
        }

        const response = await fetch("api/products/update", {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(productObject)
        });

        const data = await readJsonResponse(response);

        if (!response.ok || !data.status) {
            throw new Error(data.message || "Failed to update product.");
        }

        if (imageUpload && imageUpload.files && imageUpload.files.length > 0) {
            await uploadImage(editingProductId);
        }

        notifySuccess("Product updated successfully!");
        hideModal("exampleModal");
        resetProductForm();

        await loadAllProducts();
        await getCategories();
        await getAuthor();
    } catch (error) {
        console.error("Update product error:", error);
        notifyFailure(error.message || "Failed to update product.");
    } finally {
        if (window.Notiflix) {
            Notiflix.Loading.remove();
        }
    }
}

async function loadAllOrders() {

    try {

        console.log("Loading admin orders...");

        const response = await fetch("api/admin-data/loadOrders", {
            method: "GET",
            headers: {
                "Accept": "application/json"
            }
        });

        console.log("Orders response status:", response.status);

        const text = await response.text();

        console.log("Orders raw response:", text);

        let data;

        try {
            data = JSON.parse(text);
        } catch (e) {
            throw new Error("Server did not return valid JSON.");
        }

        console.log("Orders JSON:", data);

        if (!response.ok) {
            throw new Error(
                data.message || "Failed to load orders."
            );
        }

        if (data.status === false) {
            throw new Error(
                data.message || "Failed to load orders."
            );
        }

        const orders = Array.isArray(data.orders)
            ? data.orders
            : [];

        console.log("Orders array:", orders);

        // Keep complete orders for Orders page/dashboard
        adminOrders = orders;

        // Dashboard: latest 3
        renderLatestOrders(orders);

        // Orders page: all orders
        if (typeof renderOrdersTable === "function") {
            renderOrdersTable(orders);
        }

    } catch (error) {

        console.error(
            "Load orders error:",
            error
        );

        const tbody =
            document.getElementById("latestOrdersBody");

        if (tbody) {

            tbody.innerHTML = `
                <tr>
                    <td colspan="6"
                        class="text-center text-danger">
                        Failed to load orders
                    </td>
                </tr>
            `;
        }

        if (typeof notifyFailure === "function") {

            notifyFailure(
                error.message ||
                "Failed to load dashboard orders."
            );

        } else if (window.Notiflix) {

            Notiflix.Notify.failure(
                error.message ||
                "Failed to load dashboard orders.",
                {
                    position: "right-top"
                }
            );
        }
    }
}







function renderLatestOrders(orders) {

    const tbody =
        document.getElementById("latestOrdersBody");

    if (!tbody) {

        console.error(
            "latestOrdersBody was not found."
        );

        return;
    }

    if (!Array.isArray(orders) || orders.length === 0) {

        tbody.innerHTML = `
            <tr>
                <td colspan="6"
                    class="text-center">
                    No orders found.
                </td>
            </tr>
        `;

        return;
    }

    /*
     * Sort newest first.
     *
     * If your API already returns newest first,
     * this still works correctly.
     */
    const latestOrders = [...orders]
        .sort((a, b) => {

            const dateA = new Date(
                a.createdAt ||
                a.date ||
                a.orderDate ||
                0
            ).getTime();

            const dateB = new Date(
                b.createdAt ||
                b.date ||
                b.orderDate ||
                0
            ).getTime();

            if (!isNaN(dateA) && !isNaN(dateB)) {
                return dateB - dateA;
            }

            return Number(b.id || 0) -
                Number(a.id || 0);

        })
        .slice(0, 3);


    tbody.innerHTML = latestOrders.map(order => {

        const orderId =
            order.id ||
            order.orderId ||
            "N/A";


        const customer =
            order.userName ||
            order.customerName ||
            (order.user && (order.user.name || order.user.email)) ||
            order.userEmail ||
            "Guest";


        /*
         * Your API may use different names depending
         * on the DTO.
         */
        const bookCount =
            order.itemCount ??
            order.itemsCount ??
            order.quantity ??
            order.items ??
            1;


        const date =
            order.date ||
            order.createdAt ||
            order.orderDate ||
            "N/A";


        const total =
            Number(
                order.total ??
                order.totalAmount ??
                order.amount ??
                0
            );


        const status =
            order.status ||
            "PROCESSING";


        const normalizedStatus =
            String(status)
                .trim()
                .toUpperCase();


        let badgeClass =
            "badge-warning";


        if (
            normalizedStatus === "DELIVERED" ||
            normalizedStatus === "COMPLETED"
        ) {

            badgeClass =
                "badge-success";

        } else if (
            normalizedStatus === "CANCELLED" ||
            normalizedStatus === "CANCELED"
        ) {

            badgeClass =
                "badge-danger";
        }


        return `
            <tr>

                <!-- Order ID -->
                <td style="font-weight: 600;">
                    #BKW-${escapeHtml(String(orderId))}
                </td>


                <!-- Customer -->
                <td>
                    ${escapeHtml(String(customer))}
                </td>


                <!-- Date -->
                <td>
                    ${escapeHtml(formatOrderDate(date))}
                </td>


                <!-- Number of books -->
                <td>
                    ${escapeHtml(String(bookCount))}
                </td>


                <!-- Amount -->
                <td style="
                    font-weight: 700;
                    color: #667eea;
                ">
                    Rs ${total.toFixed(2)}
                </td>


                <!-- Status -->
                <td>
                    <span class="badge ${badgeClass}">
                        ${escapeHtml(status)}
                    </span>
                </td>
            </tr>
        `;

    }).join("");


    if (window.feather) {
        feather.replace();
    }
}

function renderOrdersTable(orders) {
    // Falls back to direct tbody selection if outer wrapper selector fails
    const tbody = document.querySelector("#ordersSection table.admin-table tbody")
        || document.querySelector("table.admin-table tbody")
        || document.getElementById("adminOrdersTableBody");

    if (!tbody) {
        console.error("Orders table tbody element not found in the DOM.");
        return;
    }

    if (!Array.isArray(orders) || orders.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center py-4">No recent orders found.</td>
            </tr>
        `;
        return;
    }

    // Display the most recent 10 orders first
    const recentOrders = [...orders].reverse().slice(0, 10);

    tbody.innerHTML = recentOrders.map(order => {
        const orderId = order.id || order.orderId || "—";

        // Handle customer name
        const customer = order.userName
            || order.customerName
            || (order.user && (order.user.name || order.user.email))
            || order.userEmail
            || "Guest";

        const itemCount = order.itemCount ?? order.itemsCount ?? order.quantity ?? order.items ?? 0;

        const totalAmount = Number(order.total || order.totalAmount || order.price || 0);

        // Handle nested status object vs raw string
        const rawStatus = typeof order.status === 'object' && order.status !== null
            ? (order.status.value || order.status.name || "Pending")
            : (order.status || "Pending");

        const statusStr = String(rawStatus);

        // Handle date string
        const rawDate = order.createdAt || order.date || order.orderDate;
        const dateStr = rawDate ? new Date(rawDate).toLocaleDateString() : "—";

        const lowerStatus = statusStr.toLowerCase();
        const badgeClass = lowerStatus === "completed" || lowerStatus === "delivered"
            ? "badge-success"
            : lowerStatus === "cancelled"
                ? "badge-danger"
                : "badge-warning";

        const safeCustomer = typeof escapeHtml === "function" ? escapeHtml(customer) : customer;
        const safeStatus = typeof escapeHtml === "function" ? escapeHtml(statusStr) : statusStr;

        return `
            <tr>
                <td style="font-weight:700;">#${orderId}</td>
                <td>${safeCustomer}</td>
                <td>${dateStr}</td>
                <td>${itemCount}</td>
                <td style="font-weight: 700; color: #667eea;">Rs ${totalAmount.toFixed(2)}</td>
                <td><span class="badge ${badgeClass}">${safeStatus}</span></td>
            </tr>
        `;
    }).join("");

    if (typeof replaceFeatherIcons === "function") {
        replaceFeatherIcons();
    } else if (window.feather) {
        feather.replace();
    }
}

function formatOrderDate(dateValue) {

    if (!dateValue) {
        return "N/A";
    }

    const date = new Date(dateValue);

    if (isNaN(date.getTime())) {
        return dateValue;
    }

    return date.toLocaleString("en-US", {
        month: "short",
        day: "numeric",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    });
}