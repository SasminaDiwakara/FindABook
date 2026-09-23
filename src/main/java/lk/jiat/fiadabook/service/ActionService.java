package lk.jiat.fiadabook.service;

import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Context;
import lk.jiat.fiadabook.dto.CartItem;
import lk.jiat.fiadabook.dto.DataDTO;
import lk.jiat.fiadabook.entity.*;
import lk.jiat.fiadabook.util.AppUtil;
import lk.jiat.fiadabook.util.HibernateUtil;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.xml.crypto.Data;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;

public class ActionService {


    public String addToWishList(int pid, @Context HttpServletRequest request) {

        JsonObject jsonObject = new JsonObject();
        String message = "";
        boolean status = false;

        HttpSession session = request.getSession();
        Session dbSess = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = dbSess.beginTransaction();

        if (session != null && session.getAttribute("user") != null) {
            Users user = (Users) session.getAttribute("user");
            Users dbUser = dbSess.merge(user);
            Books books = dbSess.get(Books.class, pid);

            Wishlist isExistWishlist = dbSess.createQuery("FROM Wishlist w " +
                            "WHERE w.users =:users AND  w.books=:books", Wishlist.class)
                    .setParameter("users", dbUser)
                    .setParameter("books", books)
                    .getSingleResultOrNull();

            if (isExistWishlist != null) {
                int rows = dbSess.createQuery("DELETE  FROM  Wishlist w " +
                                "WHERE w.books=:books AND w.users=:users")
                        .setParameter("users", dbUser)
                        .setParameter("books", books)
                        .executeUpdate();

                message = "Removed from Wishlist !";

                System.out.println(rows);

            } else {
                Wishlist wishlist = new Wishlist();
                wishlist.setBooks(books);
                wishlist.setUsers(dbUser);
                dbSess.persist(wishlist);
                message = "Added to Wishlist !";
            }
            status = true;
            tx.commit();

        } else {
            tx.rollback();
            message = "Please log to your account";
        }

        dbSess.close();

        jsonObject.addProperty("message", message);
        jsonObject.addProperty("status", status);

        return AppUtil.GSON.toJson(jsonObject);

    }

    public String addToCart(int pid, @Context HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        String message = "";
        boolean status = false;

        HttpSession session = request.getSession();

        Session dbSess = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = dbSess.beginTransaction();

        Books books = dbSess.get(Books.class, pid);

        Users user = (Users) session.getAttribute("user");

        if (user != null) {

            Users realUser = dbSess.merge(user);

            Cart dbCart = dbSess.createQuery("FROM Cart c WHERE c.users=:users AND c.Books=:books", Cart.class)
                    .setParameter("users", realUser)
                    .setParameter("books", books)
                    .getSingleResultOrNull();

            if (dbCart == null) {
                Cart cart = new Cart();
                cart.setQty(1);
                cart.setUsers(realUser);
                cart.setBooks(books);

                dbSess.persist(cart);

            } else {
                dbCart.setQty(dbCart.getQty() + 1);
                dbSess.merge(dbCart);

            }
            tx.commit();

        } else {

            Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute("cart");

            if (cart == null) {
                cart = new HashMap<>();
            }

            if (cart.containsKey(pid)) {
                cart.get(pid).setQty(
                        cart.get(pid).getQty() + 1

                );
//                System.out.println(cart.get(pid).getQty());
//                System.out.println(cart.get(pid).getTitle());

            } else {
                CartItem cartItem = new CartItem();
                cartItem.setPid(pid);
                cartItem.setPrice(books.getPrice());
                cartItem.setTitle(books.getTitle());
                cartItem.setQty(1);
                cart.put(pid, cartItem);

                session.setAttribute("cart", cart);

            }

        }

        message = "Product added to cart !";
        dbSess.close();


        status = true;

        jsonObject.addProperty("message", message);
        jsonObject.addProperty("status", status);

        return AppUtil.GSON.toJson(jsonObject);
    }

    public String removeItem(int pid, @Context HttpServletRequest request) {

        JsonObject jsonObject = new JsonObject();
        String message = "";
        boolean status = false;

        Session dbSess = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = dbSess.beginTransaction();
        HttpSession session = request.getSession();

        if (session != null && session.getAttribute("user") != null) {
            Users user = (Users) session.getAttribute("user");

            Users dbUser = dbSess.merge(user);
            Books books = dbSess.get(Books.class, pid);

            int rows = dbSess.createQuery("DELETE FROM Cart c WHERE c.users=:users AND c.Books =:books")
                    .setParameter("users", dbUser)
                    .setParameter("books", books)
                    .executeUpdate();
            transaction.commit();
            System.out.println(rows);

            message = "Cart Item removed !";
            status = true;


        } else {
            message = "Please log to your account";
        }

        dbSess.close();

        jsonObject.addProperty("message", message);
        jsonObject.addProperty("status", status);

        return AppUtil.GSON.toJson(jsonObject);
    }

    public String updateCart(int pid, int qty, @Context HttpServletRequest request) {

        JsonObject jsonObject = new JsonObject();
        String message = "";
        boolean status = false;

//        System.out.println(pid+" qty"+qty);

        Session dbSess = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = dbSess.beginTransaction();
        HttpSession session = request.getSession();

        if (session != null && session.getAttribute("user") != null) {

            Users user = (Users) session.getAttribute("user");
            Users dbUser = dbSess.merge(user);
            Books books = dbSess.get(Books.class, pid);

            Cart cart = dbSess.createQuery("FROM Cart c WHERE c.Books=:books AND c.users=:user", Cart.class)
                    .setParameter("books", books)
                    .setParameter("user", dbUser)
                    .getSingleResultOrNull();

            if (cart != null) {
                cart.setQty(qty);
                dbSess.merge(cart);
                transaction.commit();
                status = true;

            } else {
                message = "Something went wrong while updating";
            }


        } else {
            message = "Please login before proceed?";


        }


        dbSess.close();

        jsonObject.addProperty("message", message);
        jsonObject.addProperty("status", status);

        return AppUtil.GSON.toJson(jsonObject);

    }


    public String authenticateBuy(HttpServletRequest request, DataDTO dataDTO) {

        JsonObject jsonObject = new JsonObject();
        String message = "";
        boolean status = false;
        Session session = HibernateUtil.getSessionFactory().openSession();

        Users user = (Users) request.getSession().getAttribute("user");
        Books book = session.get(Books.class, dataDTO.getPid());

        if (user != null) {
            Users dbUser = session.merge(user);
            if (dbUser.getAddresses().isEmpty()) {
                message = "Fill your address details before continue!";
            } else if (book == null) {
                message = "Some data is missing?";
            } else if (dataDTO.getQty() <= 0) {
                message = "Invalid order quantity!";
            } else {

                DataDTO data = new DataDTO();

                data.setEmail(dbUser.getEmail());
                data.setFirst_name(dbUser.getFirstName());
                data.setLast_name(dbUser.getLastName());

                data.setPid(book.getId());
                data.setQty(dataDTO.getQty());

                double amount = (dataDTO.getQty() * book.getPrice()) + dataDTO.getShipping();
                data.setAmount(amount);

                data.setBook_name(book.getTitle());

                Address address = session.createQuery("FROM Address a WHERE a.users=:user", Address.class)
                        .setParameter("user", dbUser)
                        .getSingleResultOrNull();

                if (address != null) {
                    data.setAddress(address.getLineOne() + "," + address.getLineTwo());
                    data.setCity_name(address.getCity().getName());
                    data.setMobile(address.getMobile());
                }

                String[] dataMD5 = giveDataMD5(amount);

                data.setMerchant_id(dataMD5[0]);
                data.setOrder_id(dataMD5[1]);
                data.setHash(dataMD5[2]);

                jsonObject.add("data", AppUtil.GSON.toJsonTree(data));

                message = "success";
                status = true;

            }

        } else {
            message = "Log into your Account!";
        }
        session.close();

        jsonObject.addProperty("message", message);
        jsonObject.addProperty("status", status);

        return AppUtil.GSON.toJson(jsonObject);
    }

    public static String[] giveDataMD5(double amount) {

        String MerchantId = "1221315";
        String merchantSecret = "ODQ2MjM1ODE0Mjc5MDE1MjYxMTM3NzU4MjU5NDgyOTUwOTEwMTgz";
        String orderID = AppUtil.generateCode();
        String currency = "LKR";
        DecimalFormat df = new DecimalFormat("0.00");
        String amountFormatted = df.format(amount);
        String hash = getMd5(MerchantId + orderID + amountFormatted + currency + getMd5(merchantSecret));

        return new String[]{MerchantId, orderID, hash};
    }

    public static String getMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext.toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String completeBuy(HttpServletRequest request, DataDTO dataDTO) {
        JsonObject jsonObject = new JsonObject();
        String message = "";
        boolean status = false;
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        Users user = (Users) request.getSession().getAttribute("user");
        Books book = session.get(Books.class, dataDTO.getPid());

        if (user != null) {
            Users dbUser = session.merge(user);

            Address address = session.createQuery("FROM Address a WHERE a.users=:users", Address.class)
                    .setParameter("users", user)
                    .getSingleResultOrNull();

            if (dbUser.getAddresses().isEmpty()) {
                message = "Fill your address details before continue!";
            } else if (book == null) {
                message = "Some data is missing?";
            } else if (dataDTO.getQty() <= 0) {
                message = "Invalid order quantity!";
            } else {

                try {
                    Orders order = new Orders();
                    order.setUsers(dbUser);
                    Status paymentState = session.get(Status.class, 11);
                    Status trackingState = session.get(Status.class, 4);
                    order.setTrack(trackingState);
                    order.setStatus(paymentState);
                    order.setAddress(address);
                    order.setOrder_id(dataDTO.getOrder_id());
                    saveOrderItems(order, dataDTO, book, session);
                    tx.commit();

                    status = true;
                    message = "success";

                } catch (HibernateException e) {
                    tx.rollback();
                    message = e.getMessage();
                }

            }


        } else {
            message = "Something went wrong!";
        }

        session.close();

        jsonObject.addProperty("message", message);
        jsonObject.addProperty("status", status);

        return AppUtil.GSON.toJson(jsonObject);
    }

    public String authenticateCheckout(HttpServletRequest request, double total) {

        JsonObject jsonObject = new JsonObject();
        String message = "";
        boolean status = false;
        Session session = HibernateUtil.getSessionFactory().openSession();

        Users user = (Users) request.getSession().getAttribute("user");

        if (user != null) {
            Users dbUser = session.merge(user);
            if (dbUser.getAddresses().isEmpty()) {
                message = "Fill your address details before continue!";
            } else {

                List<Cart> cartList = session.createQuery("FROM Cart c WHERE c.users=:users", Cart.class)
                        .setParameter("users", dbUser)
                        .getResultList();

                Address address = session.createQuery("FROM Address a WHERE a.users=:user", Address.class)
                        .setParameter("user", dbUser)
                        .getSingleResultOrNull();

                DataDTO data = new DataDTO();

                int qty = cartList.stream().mapToInt(Cart::getQty).sum();

                data.setQty(qty);
                data.setAmount(total);
                data.setBook_name("Cart Items");
                if (address != null) {
                    data.setAddress(address.getLineOne() + "," + address.getLineTwo());
                    data.setCity_name(address.getCity().getName());
                    data.setMobile(address.getMobile());
                }


                data.setEmail(dbUser.getEmail());

//                String MerchantId = "1221315";
//                String merchantSecret = "ODQ2MjM1ODE0Mjc5MDE1MjYxMTM3NzU4MjU5NDgyOTUwOTEwMTgz";
//                String orderID = AppUtil.generateCode();
//                String currency = "LKR";
//                DecimalFormat df = new DecimalFormat("0.00");
//                String amountFormatted = df.format(total);
//                String hash = getMd5(MerchantId + orderID + amountFormatted + currency + getMd5(merchantSecret));

//                data.setMerchant_id(MerchantId);
//                data.setOrder_id(orderID);
//                data.setHash(hash);

                String[] dataMD5 = giveDataMD5(total);

                data.setMerchant_id(dataMD5[0]);
                data.setOrder_id(dataMD5[1]);
                data.setHash(dataMD5[2]);


                jsonObject.add("data", AppUtil.GSON.toJsonTree(data));

                message = "success";
                status = true;

            }

        } else {
            message = "Log into your Account!";
        }
        session.close();

        jsonObject.addProperty("message", message);
        jsonObject.addProperty("status", status);

        return AppUtil.GSON.toJson(jsonObject);
    }


    public String completeCartBuy(HttpServletRequest request, DataDTO dataDTO) {

        JsonObject respObj = new JsonObject();
        String message = "";
        boolean status = false;
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        Users user = (Users) request.getSession().getAttribute("user");
        try {

            if (user != null) {
                Users dbUser = session.merge(user);

                Address address = session.createQuery("FROM Address a WHERE a.users=:users", Address.class)
                        .setParameter("users", user)
                        .getSingleResultOrNull();

                if (dbUser.getAddresses().isEmpty()) {
                    message = "Fill your address details before continue!";
                } else if (dataDTO.getAmount() <= 0) {
                    message = "Invalid order Amount!";
                } else {

                    List<Cart> cartList = session.createQuery("FROM Cart c WHERE c.users=:users", Cart.class)
                            .setParameter("users", dbUser)
                            .getResultList();


                    Orders order = new Orders();

                    order.setUsers(dbUser);
                    Status paymentState = session.get(Status.class, 11);
                    Status trackingState = session.get(Status.class, 4);
                    order.setTrack(trackingState);
                    order.setStatus(paymentState);
                    order.setAddress(address);
                    order.setOrder_id(dataDTO.getOrder_id());

                    cartList.forEach(cart -> {
                        DataDTO dto = new DataDTO();
                        dto.setQty(cart.getQty());

                        saveOrderItems(order, dto, cart.getBooks(), session);
                    });

                   int rowCount =  session.createQuery("DELETE FROM Cart c WHERE c.users=:users")
                                    .setParameter("users",dbUser)
                            .executeUpdate();

                    System.out.println(rowCount);

                    tx.commit();

                    status = true;
                    message = "success";
                }

            } else {
                message = "Something went wrong!";
            }

        } catch (HibernateException e) {
            tx.rollback();
            message = e.getMessage();
        }

        session.close();

        respObj.addProperty("message", message);
        respObj.addProperty("status", status);

        return AppUtil.GSON.toJson(respObj);
    }

    public static void saveOrderItems(Orders order, DataDTO dataDTO, Books book, Session session) {

        OrderItems item = new OrderItems();
        item.setBooks(book);
        item.setQty(dataDTO.getQty());
        item.setRating(0);
        item.setOrders(order);
        book.setStock(book.getStock() - dataDTO.getQty());
        order.getOrderItems().add(item);
        session.persist(order);
        session.merge(book);

    }

    public String getCategories() {
        JsonObject jsonObject = new JsonObject();
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            List<Category> categories = session.createQuery("FROM Category", Category.class).getResultList();

            List<Map<String, Object>> list = new java.util.ArrayList<>();
            for (Category c : categories) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", c.getId());
                map.put("name", c.getName());

                Long bookCount = session.createQuery("SELECT COUNT(b) FROM Books b WHERE b.category = :category", Long.class)
                        .setParameter("category", c)
                        .getSingleResult();
                map.put("bookCount", bookCount);

                list.add(map);
            }

            jsonObject.add("categories", AppUtil.GSON.toJsonTree(list));
            jsonObject.addProperty("status", true);
        } catch (Exception e) {
            jsonObject.addProperty("status", false);
            jsonObject.addProperty("message", e.getMessage());
        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(jsonObject);
    }

    public String addCategory(String categoryName) {
        JsonObject jsonObject = new JsonObject();
        String message;
        boolean status = false;

        if (categoryName == null || categoryName.trim().isEmpty()) {
            jsonObject.addProperty("status", false);
            jsonObject.addProperty("message", "Category name is required");
            return AppUtil.GSON.toJson(jsonObject);
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();

        try {
            Category category = new Category();
            category.setName(categoryName.trim());
            session.persist(category);
            tx.commit();

            status = true;
            message = "Category added successfully";
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            message = e.getMessage();
        } finally {
            session.close();
        }

        jsonObject.addProperty("status", status);
        jsonObject.addProperty("message", message);
        return AppUtil.GSON.toJson(jsonObject);
    }


    public String getAuthors() {
        JsonObject jsonObject = new JsonObject();
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            List<Author> authors = session.createQuery("FROM Author", Author.class).getResultList();

            List<Map<String, Object>> list = new java.util.ArrayList<>();
            for (Author a : authors) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", a.getId());
                map.put("name", a.getName());

                Long bookCount = session.createQuery("SELECT COUNT(b) FROM Books b WHERE b.author = :author", Long.class)
                        .setParameter("author", a)
                        .getSingleResult();
                map.put("bookCount", bookCount);

                list.add(map);
            }

            jsonObject.add("authors", AppUtil.GSON.toJsonTree(list));
            jsonObject.addProperty("status", true);
        } catch (Exception e) {
            jsonObject.addProperty("status", false);
            jsonObject.addProperty("message", e.getMessage());
        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(jsonObject);
    }

    public String addAuthor(String authorName) {
        JsonObject jsonObject = new JsonObject();
        String message;
        boolean status = false;

        if (authorName == null || authorName.trim().isEmpty()) {
            jsonObject.addProperty("status", false);
            jsonObject.addProperty("message", "Author name is required");
            return AppUtil.GSON.toJson(jsonObject);
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();

        try {
            Author author = new Author();
            author.setName(authorName.trim());
            session.persist(author);
            tx.commit();

            status = true;
            message = "Author added successfully";
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            message = e.getMessage();
        } finally {
            session.close();
        }

        jsonObject.addProperty("status", status);
        jsonObject.addProperty("message", message);
        return AppUtil.GSON.toJson(jsonObject);
    }

    public String getDashboardStats() {
        JsonObject jsonObject = new JsonObject();
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            Long totalBooks = session.createQuery("SELECT COUNT(b) FROM Books b", Long.class).getSingleResult();
            Long totalCustomers = session.createQuery("SELECT COUNT(u) FROM Users u WHERE u.role.value = 'User'", Long.class).getSingleResult();
            Long totalOrders = session.createQuery("SELECT COUNT(o) FROM Orders o", Long.class).getSingleResult();
            Double totalRevenue = session.createQuery("SELECT SUM(oi.qty * b.price) FROM OrderItems oi JOIN oi.books b", Double.class).getSingleResult();

            jsonObject.addProperty("totalBooks", totalBooks != null ? totalBooks : 0);
            jsonObject.addProperty("totalCustomers", totalCustomers != null ? totalCustomers : 0);
            jsonObject.addProperty("totalOrders", totalOrders != null ? totalOrders : 0);
            jsonObject.addProperty("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
            jsonObject.addProperty("status", true);
        } catch (Exception e) {
            jsonObject.addProperty("status", false);
            jsonObject.addProperty("message", e.getMessage());
        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(jsonObject);
    }

    public String loadOrders() {
        JsonObject jsonObject = new JsonObject();
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            List<Orders> orders = session.createQuery("FROM Orders o JOIN FETCH o.users", Orders.class).getResultList();

            List<Map<String, Object>> orderList = new java.util.ArrayList<>();
            for (Orders o : orders) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", o.getId());
                map.put("orderId", o.getOrder_id());
                map.put("userName", o.getUsers().getFirstName() + " " + o.getUsers().getLastName());
                map.put("status", o.getStatus() != null ? o.getStatus().getValue() : "Processing");

                Object[] itemsResult = session.createQuery(
                                "SELECT SUM(oi.qty), SUM(oi.qty * b.price) FROM OrderItems oi JOIN oi.books b WHERE oi.orders = :order", Object[].class)
                        .setParameter("order", o)
                        .getSingleResult();

                if (itemsResult != null && itemsResult[0] != null) {
                    map.put("itemCount", itemsResult[0]);
                    map.put("total", itemsResult[1]);
                } else {
                    map.put("itemCount", 0);
                    map.put("total", 0.0);
                }

                orderList.add(map);
            }

            jsonObject.add("orders", AppUtil.GSON.toJsonTree(orderList));
            jsonObject.addProperty("status", true);
        } catch (Exception e) {
            jsonObject.addProperty("status", false);
            jsonObject.addProperty("message", e.getMessage());
        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(jsonObject);
    }

    public String getCustomers() {
        JsonObject jsonObject = new JsonObject();
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            List<Users> users = session.createQuery("FROM Users u WHERE u.role.name = 'CUSTOMER'", Users.class).getResultList();

            List<Map<String, Object>> customerList = new java.util.ArrayList<>();
            for (Users u : users) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", u.getId());
                map.put("name", u.getFirstName() + " " + u.getLastName());
                map.put("email", u.getEmail());
                map.put("status", u.getStatus() != null ? u.getStatus().getValue() : "Active");

                Long orderCount = session.createQuery("SELECT COUNT(o) FROM Orders o WHERE o.users = :user", Long.class)
                        .setParameter("user", u)
                        .getSingleResult();
                map.put("orderCount", orderCount);

                customerList.add(map);
            }

            jsonObject.add("customers", AppUtil.GSON.toJsonTree(customerList));
            jsonObject.addProperty("status", true);
        } catch (Exception e) {
            jsonObject.addProperty("status", false);
            jsonObject.addProperty("message", e.getMessage());
        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(jsonObject);
    }


}
