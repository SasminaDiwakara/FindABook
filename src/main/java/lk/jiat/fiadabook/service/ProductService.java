package lk.jiat.fiadabook.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lk.jiat.fiadabook.dto.*;
import lk.jiat.fiadabook.entity.*;
import lk.jiat.fiadabook.util.AppUtil;
import lk.jiat.fiadabook.util.HibernateUtil;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductService {

    public String loadCatogeries() {

        JsonObject responseJson = new JsonObject();
        JsonArray categoriesArray = new JsonArray();

        Session session = HibernateUtil.getSessionFactory().openSession();

        try {

            List<Object[]> results = session.createQuery(
                    "SELECT c.id, c.name, COUNT(b.id) " +
                            "FROM Category c " +
                            "LEFT JOIN Books b ON b.category.id = c.id " +
                            "GROUP BY c.id, c.name " +
                            "ORDER BY c.name ASC",
                    Object[].class
            ).getResultList();

            for (Object[] row : results) {

                JsonObject category = new JsonObject();

                category.addProperty("id", ((Number) row[0]).intValue());
                category.addProperty("name", (String) row[1]);
                category.addProperty("bookCount", ((Number) row[2]).intValue());

                categoriesArray.add(category);
            }

            responseJson.addProperty("status", true);
            responseJson.add("categories", categoriesArray);

            return AppUtil.GSON.toJson(responseJson);

        } catch (Exception e) {

            e.printStackTrace();

            responseJson.addProperty("status", false);
            responseJson.addProperty(
                    "message",
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Failed to load categories"
            );

        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(responseJson);
    }

    public String addNewCategory(CategoryDTO categoryDTO) {

        JsonObject responseJson = new JsonObject();
        boolean status = false;
        String message;

        if (categoryDTO == null ||
                categoryDTO.getCategoryName() == null ||
                categoryDTO.getCategoryName().trim().isEmpty()) {

            message = "Category name is required";
        } else {
            Session session = HibernateUtil.getSessionFactory().openSession();

            Category existing = session.createQuery(
                            "FROM Category c WHERE LOWER(c.name) = LOWER(:name)", Category.class)
                    .setParameter("name", categoryDTO.getCategoryName().trim())
                    .uniqueResult();

            if (existing != null) {
                message = "Category already exists";
            } else {
                Transaction tx = session.beginTransaction();

                Category category = new Category();
                category.setName(categoryDTO.getCategoryName().trim());

                session.persist(category);
                tx.commit();

                status = true;
                message = "Category added successfully";
            }

            session.close();
        }

        responseJson.addProperty("status", status);
        responseJson.addProperty("message", message);

        return AppUtil.GSON.toJson(responseJson);
    }

    public String loadAuthors() {

        JsonObject responseJson = new JsonObject();
        JsonArray authorsArray = new JsonArray();

        Session session = HibernateUtil.getSessionFactory().openSession();

        try {

            List<Object[]> results = session.createQuery(
                    "SELECT a.id, a.name, COUNT(b.id) " +
                            "FROM Author a " +
                            "LEFT JOIN Books b ON b.author.id = a.id " +
                            "GROUP BY a.id, a.name " +
                            "ORDER BY a.name ASC",
                    Object[].class
            ).getResultList();

            for (Object[] row : results) {

                JsonObject author = new JsonObject();

                author.addProperty("id", (Integer) row[0]);
                author.addProperty("name", (String) row[1]);
                author.addProperty("bookCount", ((Number) row[2]).longValue());

                author.addProperty("totalSales", 0.00);

                authorsArray.add(author);
            }

            responseJson.addProperty("status", true);
            responseJson.add("authors", authorsArray);

        } catch (Exception e) {

            e.printStackTrace();

            responseJson.addProperty("status", false);
            responseJson.addProperty(
                    "message",
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Failed to load authors"
            );

        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(responseJson);
    }

    public String addNewAuthor(AuthorDTO authorDTO) {

        JsonObject responseJson = new JsonObject();
        boolean status = false;
        String message;

        if (authorDTO == null ||
                authorDTO.getAuthorName() == null ||
                authorDTO.getAuthorName().trim().isEmpty()) {

            message = "Author name is required";
        } else {
            Session session = HibernateUtil.getSessionFactory().openSession();

            Author existing = session.createQuery(
                            "FROM Author c WHERE c.name = :name", Author.class)
                    .setParameter("name", authorDTO.getAuthorName().trim())
                    .uniqueResult();

            if (existing != null) {
                message = "Author already exists";
            } else {
                Transaction tx = session.beginTransaction();

                Author author = new Author();
                author.setName(authorDTO.getAuthorName().trim());

                session.persist(author);
                tx.commit();

                status = true;
                message = "Author added successfully";
            }

            session.close();
        }

        responseJson.addProperty("status", status);
        responseJson.addProperty("message", message);

        return AppUtil.GSON.toJson(responseJson);
    }


    public String updateAuthor(int authorId, String authorName) {
        JsonObject responseJson = new JsonObject();
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;

        try {
            if (authorId <= 0) {
                responseJson.addProperty("status", false);
                responseJson.addProperty("message", "Invalid author id.");
                return AppUtil.GSON.toJson(responseJson);
            }

            if (authorName == null || authorName.trim().isEmpty()) {
                responseJson.addProperty("status", false);
                responseJson.addProperty("message", "Author name is required.");
                return AppUtil.GSON.toJson(responseJson);
            }

            Author author = session.get(Author.class, authorId);
            if (author == null) {
                responseJson.addProperty("status", false);
                responseJson.addProperty("message", "Author not found.");
                return AppUtil.GSON.toJson(responseJson);
            }

            String cleanName = authorName.trim();
            Author duplicate = session.createQuery(
                            "FROM Author a WHERE LOWER(a.name)=LOWER(:name) AND a.id<>:id", Author.class)
                    .setParameter("name", cleanName)
                    .setParameter("id", authorId)
                    .uniqueResult();

            if (duplicate != null) {
                responseJson.addProperty("status", false);
                responseJson.addProperty("message", "Another author already has this name.");
                return AppUtil.GSON.toJson(responseJson);
            }

            tx = session.beginTransaction();
            author.setName(cleanName);
            session.merge(author);
            tx.commit();

            responseJson.addProperty("status", true);
            responseJson.addProperty("message", "Author updated successfully.");
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
            responseJson.addProperty("status", false);
            responseJson.addProperty("message", "Author update failed.");
        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(responseJson);
    }

    public String updateCategory(int categoryId, String categoryName) {
        JsonObject responseJson = new JsonObject();
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;

        try {
            if (categoryId <= 0) {
                responseJson.addProperty("status", false);
                responseJson.addProperty("message", "Invalid category id.");
                return AppUtil.GSON.toJson(responseJson);
            }

            if (categoryName == null || categoryName.trim().isEmpty()) {
                responseJson.addProperty("status", false);
                responseJson.addProperty("message", "Category name is required.");
                return AppUtil.GSON.toJson(responseJson);
            }

            Category category = session.get(Category.class, categoryId);
            if (category == null) {
                responseJson.addProperty("status", false);
                responseJson.addProperty("message", "Category not found.");
                return AppUtil.GSON.toJson(responseJson);
            }

            String cleanName = categoryName.trim();
            Category duplicate = session.createQuery(
                            "FROM Category c WHERE LOWER(c.name)=LOWER(:name) AND c.id<>:id", Category.class)
                    .setParameter("name", cleanName)
                    .setParameter("id", categoryId)
                    .uniqueResult();

            if (duplicate != null) {
                responseJson.addProperty("status", false);
                responseJson.addProperty("message", "Another category already has this name.");
                return AppUtil.GSON.toJson(responseJson);
            }

            tx = session.beginTransaction();
            category.setName(cleanName);
            session.merge(category);
            tx.commit();

            responseJson.addProperty("status", true);
            responseJson.addProperty("message", "Category updated successfully.");
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
            responseJson.addProperty("status", false);
            responseJson.addProperty("message", "Category update failed.");
        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(responseJson);
    }

    public String updateProduct(ProductDTO dto) {
        JsonObject responseJson = new JsonObject();
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;

        try {
            if (dto == null || dto.getId() <= 0) {
                responseJson.addProperty("status", false);
                responseJson.addProperty("message", "Invalid product id.");
                return AppUtil.GSON.toJson(responseJson);
            }
            if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
                responseJson.addProperty("status", false);
                responseJson.addProperty("message", "Title is required.");
                return AppUtil.GSON.toJson(responseJson);
            }
            if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
                responseJson.addProperty("status", false);
                responseJson.addProperty("message", "Description is required.");
                return AppUtil.GSON.toJson(responseJson);
            }
            if (dto.getAuthor() == null || dto.getAuthor().trim().isEmpty()) {
                responseJson.addProperty("status", false);
                responseJson.addProperty("message", "Author is required.");
                return AppUtil.GSON.toJson(responseJson);
            }
            if (dto.getCategoryId() <= 0) {
                responseJson.addProperty("status", false);
                responseJson.addProperty("message", "Category is required.");
                return AppUtil.GSON.toJson(responseJson);
            }
            if (dto.getPrice() <= 0) {
                responseJson.addProperty("status", false);
                responseJson.addProperty("message", "Price must be greater than zero.");
                return AppUtil.GSON.toJson(responseJson);
            }
            if (dto.getStock() < 0) {
                responseJson.addProperty("status", false);
                responseJson.addProperty("message", "Stock cannot be negative.");
                return AppUtil.GSON.toJson(responseJson);
            }

            Books book = session.get(Books.class, dto.getId());
            if (book == null) {
                responseJson.addProperty("status", false);
                responseJson.addProperty("message", "Product not found.");
                return AppUtil.GSON.toJson(responseJson);
            }

            int authorId;
            try {
                authorId = Integer.parseInt(dto.getAuthor().trim());
            } catch (NumberFormatException e) {
                responseJson.addProperty("status", false);
                responseJson.addProperty("message", "Invalid author selected.");
                return AppUtil.GSON.toJson(responseJson);
            }

            Author author = session.get(Author.class, authorId);
            Category category = session.get(Category.class, dto.getCategoryId());

            if (author == null) {
                responseJson.addProperty("status", false);
                responseJson.addProperty("message", "Selected author was not found.");
                return AppUtil.GSON.toJson(responseJson);
            }
            if (category == null) {
                responseJson.addProperty("status", false);
                responseJson.addProperty("message", "Selected category was not found.");
                return AppUtil.GSON.toJson(responseJson);
            }

            tx = session.beginTransaction();
            book.setTitle(dto.getTitle().trim());
            book.setDescription(dto.getDescription());
            book.setPrice(dto.getPrice());
            book.setStock(dto.getStock());
            book.setAuthor(author);
            book.setCategory(category);
            session.merge(book);
            tx.commit();

            responseJson.addProperty("status", true);
            responseJson.addProperty("message", "Product updated successfully.");
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
            responseJson.addProperty("status", false);
            responseJson.addProperty("message", "Product update failed.");
        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(responseJson);
    }

    public String getAdminDashboardStats() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        JsonObject response = new JsonObject();

        try {
            Double revenue = session.createQuery(
                            "SELECT COALESCE(SUM(oi.books.price * oi.qty), 0) FROM OrderItems oi", Double.class)
                    .getSingleResult();

            Long orderCount = session.createQuery(
                    "SELECT COUNT(o) FROM Orders o", Long.class).getSingleResult();

            Long bookCount = session.createQuery(
                    "SELECT COUNT(b) FROM Books b", Long.class).getSingleResult();

            Long lowStock = session.createQuery(
                    "SELECT COUNT(b) FROM Books b WHERE b.stock <= 10", Long.class).getSingleResult();

            response.addProperty("status", true);
            response.addProperty("revenue", revenue == null ? 0.0 : revenue);
            response.addProperty("orderCount", orderCount == null ? 0 : orderCount);
            response.addProperty("bookCount", bookCount == null ? 0 : bookCount);
            response.addProperty("lowStock", lowStock == null ? 0 : lowStock);
        } catch (Exception e) {
            e.printStackTrace();
            response.addProperty("status", false);
            response.addProperty("message", "Failed to load dashboard statistics.");
        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(response);
    }

    public String updateProduct(Books books) {
        JsonObject responseObj = new JsonObject();
        boolean status = false;
        String message = "";

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        try {
            session.merge(books);
            transaction.commit();
            status = true;
            message = "success";
        } catch (HibernateException e) {
            transaction.rollback();
            throw new RuntimeException(e);
        }

        session.close();
        responseObj.addProperty("status", status);
        responseObj.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObj);

    }

    public Books getProductById(int pid) {

        Session session = HibernateUtil.getSessionFactory().openSession();
        Books books = session.find(Books.class, pid);
        session.close();
        return books;
    }


    public String addNewProduct(ProductDTO productDTO, HttpServletRequest request) {
        JsonObject responseOBJ = new JsonObject();
        boolean status = false;
        String message = "";

        if (productDTO.getTitle() == null || productDTO.getTitle().isBlank()) {
            message = "Title is required";
        } else if (productDTO.getAuthor() == null || productDTO.getAuthor().isEmpty()) {
            message = "Add a author of your book";
        } else if (productDTO.getDescription() == null || productDTO.getDescription().isBlank()) {
            message = "Add a brief summary of your book";
        } else if (productDTO.getPrice() <= 0) {
            message = "Books price is required.";
        } else if (productDTO.getStock() <= 0) {
            message = "Add the quantity of the book";
        } else {
            HttpSession session = request.getSession(false);
            if (session == null) {
                message = "Session expired ! Please login.";

            } else if (session.getAttribute("user") == null) {
                message = "Please Login";
            } else {
                org.hibernate.Session hibernateSess = HibernateUtil.getSessionFactory().openSession();

                Books book = new Books();
                book.setTitle(productDTO.getTitle());
                Author author = hibernateSess.get(Author.class, productDTO.getAuthor());
                book.setAuthor(author);
                book.setDescription(productDTO.getDescription());
                book.setPrice(productDTO.getPrice());
                book.setStock(productDTO.getStock());

                Category category = hibernateSess.find(Category.class, productDTO.getCategoryId());
                book.setCategory(category);


                Status pendingStatus = hibernateSess.createNamedQuery("Status.findByValue", Status.class)
                        .setParameter("value", String.valueOf(Status.Type.ACTIVE))
                        .getSingleResult();

                book.setStatus(pendingStatus);

                Transaction transaction = hibernateSess.beginTransaction();

                try {
                    hibernateSess.persist(book);
                    transaction.commit();
                    status = true;

                    responseOBJ.addProperty("productId", book.getId());

                } catch (Exception e) {

                    transaction.rollback();
                    e.printStackTrace();
                    message = "Product save failed";


                }


                hibernateSess.close();
            }
        }


        responseOBJ.addProperty("status", status);
        responseOBJ.addProperty("message", message);
        return AppUtil.GSON.toJson(responseOBJ);
    }

    public String loadAllProducts() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Books> books = session.createQuery("FROM Books", Books.class).getResultList();
        session.close();

        JsonObject response = new JsonObject();
        response.add("products", AppUtil.GSON.toJsonTree(books));
        return AppUtil.GSON.toJson(response);
    }

    public String getLatestProducts() {

        Session session = HibernateUtil.getSessionFactory().openSession();

        List<Books> books = session.createQuery(
                        "SELECT b FROM Books b " +
                                "JOIN FETCH b.author " +
                                "JOIN FETCH b.category " +
                                "ORDER BY b.createdAt DESC",
                        Books.class
                )
                .setMaxResults(10)
                .getResultList();

        List<ProductDTO> dtoList = books.stream().map(book -> {
            ProductDTO dto = new ProductDTO();
            dto.setId(book.getId());
            dto.setTitle(book.getTitle());
            dto.setPrice(book.getPrice());
            dto.setStock(book.getStock());
            dto.setAuthor(book.getAuthor().getName());
            dto.setCategoryId(book.getCategory().getId());
            dto.setCreatedAt(book.getCreatedAt().toString());

            if (!book.getImages().isEmpty()) {
                dto.setImage(book.getImages());
            }

            return dto;
        }).toList();

        session.close();

        JsonObject response = new JsonObject();
        response.add("products", AppUtil.GSON.toJsonTree(dtoList));

        return AppUtil.GSON.toJson(response);
    }

    public String getPopularProducts() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Books> books;

        try {
            books = session.createQuery(
                            "SELECT b FROM OrderItems oi " +
                                    "JOIN oi.books b " +
                                    "JOIN FETCH b.author " +
                                    "JOIN FETCH b.category " +
                                    "GROUP BY b.id, b.title, b.price, b.stock, b.createdAt, b.description, b.author, b.category " +
                                    "ORDER BY SUM(oi.qty) DESC",
                            Books.class
                    )
                    .setMaxResults(10)
                    .getResultList();

            if (books.isEmpty()) {
                books = session.createQuery(
                                "SELECT b FROM Books b " +
                                        "JOIN FETCH b.author " +
                                        "JOIN FETCH b.category " +
                                        "ORDER BY b.createdAt DESC",
                                Books.class
                        )
                        .setMaxResults(10)
                        .getResultList();
            }

        } catch (Exception e) {
            e.printStackTrace();
            books = new ArrayList<>();
        }

        List<ProductDTO> dtoList = books.stream().map(book -> {
            ProductDTO dto = new ProductDTO();
            dto.setId(book.getId());
            dto.setTitle(book.getTitle());
            dto.setPrice(book.getPrice());
            dto.setStock(book.getStock());
            dto.setAuthor(book.getAuthor().getName());
            dto.setCategoryId(book.getCategory().getId());
            dto.setCreatedAt(book.getCreatedAt().toString());

            if (book.getImages() != null && !book.getImages().isEmpty()) {
                dto.setImage(book.getImages());
            }

            return dto;
        }).toList();

        session.close();

        JsonObject response = new JsonObject();
        response.add("products", AppUtil.GSON.toJsonTree(dtoList));

        return AppUtil.GSON.toJson(response);
    }


    public String loadProductData(int pid) {
        JsonObject jsonObject = new JsonObject();
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            Books book = session.get(Books.class, pid);
            if (book == null) {
                jsonObject.addProperty("status", false);
                jsonObject.addProperty("message", "Product not found.");
                return AppUtil.GSON.toJson(jsonObject);
            }

            JsonObject product = new JsonObject();
            product.addProperty("id", book.getId());
            product.addProperty("title", book.getTitle());
            product.addProperty("description", book.getDescription());
            product.addProperty("price", book.getPrice());
            product.addProperty("stock", book.getStock());
            product.addProperty("authorId", book.getAuthor() == null ? 0 : book.getAuthor().getId());
            product.addProperty("author", book.getAuthor() == null ? "" : book.getAuthor().getName());
            product.addProperty("categoryId", book.getCategory() == null ? 0 : book.getCategory().getId());
            product.addProperty("categoryName", book.getCategory() == null ? "" : book.getCategory().getName());
            product.add("images", AppUtil.GSON.toJsonTree(book.getImages() == null ? new ArrayList<>() : book.getImages()));
            product.addProperty("createdAt", String.valueOf(book.getCreatedAt()));

            jsonObject.add("singleProduct", product);
            jsonObject.addProperty("status", true);
            jsonObject.addProperty("message", "Product loaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            jsonObject.addProperty("status", false);
            jsonObject.addProperty("message", "Failed to load product.");
        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(jsonObject);
    }

    public Map<String, Object> searchProducts(String queryStr, Integer categoryId, Double minPrice,
                                              Double maxPrice, String sort, int page, int limit) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            StringBuilder hql = new StringBuilder("FROM Books b WHERE 1=1 ");
            Map<String, Object> params = new HashMap<>();

            if (queryStr != null && !queryStr.trim().isEmpty()) {
                hql.append("AND (LOWER(b.title) LIKE :query OR LOWER(b.author.name) LIKE :query) ");
                params.put("query", "%" + queryStr.trim().toLowerCase() + "%");
            }

            if (categoryId != null && categoryId > 0) {
                hql.append("AND b.category.id = :categoryId ");
                params.put("categoryId", categoryId);
            }

            if (minPrice != null && minPrice >= 0) {
                hql.append("AND b.price >= :minPrice ");
                params.put("minPrice", minPrice);
            }
            if (maxPrice != null && maxPrice > 0) {
                hql.append("AND b.price <= :maxPrice ");
                params.put("maxPrice", maxPrice);
            }

            switch (sort != null ? sort : "latest") {
                case "name_asc":
                    hql.append("ORDER BY b.title ASC");
                    break;
                case "name_desc":
                    hql.append("ORDER BY b.title DESC");
                    break;
                case "price_low_high":
                    hql.append("ORDER BY b.price ASC");
                    break;
                case "price_high_low":
                    hql.append("ORDER BY b.price DESC");
                    break;
                case "popular":
                case "latest":
                default:
                    hql.append("ORDER BY b.id DESC");
                    break;
            }

            String countHql = "SELECT COUNT(b) " + hql.toString();
            Query<Long> countQuery = session.createQuery(countHql, Long.class);
            params.forEach(countQuery::setParameter);
            long totalItems = countQuery.getSingleResult();

            Query<Books> query = session.createQuery(hql.toString(), Books.class);
            params.forEach(query::setParameter);

            int firstResult = (page - 1) * limit;
            query.setFirstResult(firstResult);
            query.setMaxResults(limit);

            List<Books> booksList = query.getResultList();
            List<ProductDTO> dtos = new ArrayList<>();

            for (Books book : booksList) {
                ProductDTO dto = new ProductDTO();
                dto.setId(book.getId());
                dto.setTitle(book.getTitle());
                dto.setAuthor(book.getAuthor() != null ? book.getAuthor().getName() : "Unknown");
                dto.setPrice(book.getPrice());
                dto.setStock(book.getStock());
                dto.setCategoryId(book.getCategory() != null ? book.getCategory().getId() : 0);
                dto.setCategoryName(book.getCategory() != null ? book.getCategory().getName() : "General");
                dto.setImage(book.getImages());
                dtos.add(dto);
            }

            long totalPages = (long) Math.ceil((double) totalItems / limit);

            Map<String, Object> result = new HashMap<>();
            result.put("products", dtos);
            result.put("currentPage", page);
            result.put("totalPages", totalPages);
            result.put("totalItems", totalItems);

            return result;

        } finally {
            session.close();
        }
    }

    private List<ProductDTO> loadRelatedProducts(int categoryId) {

        Session dbSess = HibernateUtil.getSessionFactory().openSession();

        Category category = dbSess.get(Category.class, categoryId);
        Status status = dbSess.get(Status.class, 1);
        List<Books> productList = dbSess.createQuery("FROM Books b " +
                        "WHERE b.status=:status " +
                        "AND b.category=:category ORDER BY b.createdAt DESC ", Books.class)
                .setParameter("status", status)
                .setParameter("category", category)
                .setMaxResults(10)
                .getResultList();

        if (!productList.isEmpty()) {

            List<ProductDTO> productDAOS = new ArrayList<>();

            for (Books book : productList) {
                ProductDTO productDAO = getProductDAO(book);
                productDAOS.add(productDAO);
            }

            return productDAOS;

        }

        dbSess.close();

        return null;
    }

    public String loadInvoiceData(int oid, HttpServletRequest request) {

        JsonObject jsonObject = new JsonObject();
        boolean status = false;
        String message = "";
        Session dbSess = HibernateUtil.getSessionFactory().openSession();


        if (request.getSession() != null && request.getSession().getAttribute("user") != null) {

            Users sessUser = (Users) request.getSession().getAttribute("user");

            Users user = dbSess.merge(sessUser);

            if (oid != 0) {
                Orders orders = dbSess.createQuery("FROM Orders o WHERE o.users=:users AND o.order_id=:order_id", Orders.class)
                        .setParameter("users", user)
                        .setParameter("order_id", String.valueOf(oid))
                        .uniqueResult();

                System.out.println(orders.getOrder_id());

                List<OrderItems> orderItems = dbSess.createQuery("FROM OrderItems oi WHERE oi.orders=:orders", OrderItems.class)
                        .setParameter("orders", orders)
                        .getResultList();

                InvoiceDataDTO invoiceData = new InvoiceDataDTO();

                invoiceData.setFirstName(user.getFirstName());
                invoiceData.setLastName(user.getLastName());
                invoiceData.setEmail(user.getEmail());
                invoiceData.setLineOne(orders.getAddress().getLineOne());
                invoiceData.setLineTwo(orders.getAddress().getLineTwo());
                invoiceData.setMobile(orders.getAddress().getMobile());
                invoiceData.setCountry(orders.getAddress().getCountry());
                invoiceData.setCityName(orders.getAddress().getCity().getName());

                invoiceData.setShipping(400.00);
                invoiceData.setOrder_id(orders.getOrder_id());
                invoiceData.setCreatedAt(orders.getCreatedAt().toString());
                invoiceData.setInv_id(orders.getId());
                ArrayList<OrderDetails> orderDetailList = new ArrayList<>();
                orderItems.forEach(orderItem -> {

                    OrderDetails orderDetails = new OrderDetails();
                    orderDetails.setTitle(orderItem.getBooks().getTitle());
                    orderDetails.setAuthor(orderItem.getBooks().getAuthor().getName());
                    orderDetails.setPrice(orderItem.getBooks().getPrice());
                    orderDetails.setQty(orderItem.getQty());
                    orderDetailList.add(orderDetails);
                    System.out.println(orderItem.getBooks().getTitle());
                });

                invoiceData.setOrderDetails(orderDetailList);

                jsonObject.add("items",AppUtil.GSON.toJsonTree(invoiceData));

                status= true;

            } else {
                message = "Something is Missing? , Contact Admin";

            }
        } else {
            message = "Please log to your account";
        }

        dbSess.close();

        jsonObject.addProperty("status", status);
        jsonObject.addProperty("message", message);

        return AppUtil.GSON.toJson(jsonObject);

    }


    private static ProductDTO getProductDAO(Books book) {
        ProductDTO productDAO = new ProductDTO();
        productDAO.setId(book.getId());
        productDAO.setTitle(book.getTitle());
        productDAO.setPrice(book.getPrice());
        productDAO.setDescription(book.getDescription());
        productDAO.setCategoryId(book.getCategory().getId());
        productDAO.setCategoryName(book.getCategory().getName());
        productDAO.setImage(book.getImages());
        productDAO.setStock(book.getStock());
        productDAO.setCreatedAt(String.valueOf(book.getCreatedAt()));
        productDAO.setAuthor(book.getAuthor().getName());
        return productDAO;
    }


}