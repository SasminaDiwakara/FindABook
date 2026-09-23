package lk.jiat.fiadabook.service;

import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Context;
import lk.jiat.fiadabook.dto.*;
import lk.jiat.fiadabook.entity.*;
import lk.jiat.fiadabook.util.AppUtil;
import lk.jiat.fiadabook.util.HibernateUtil;
import lk.jiat.fiadabook.validation.Validator;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProfileService {

    public String loadAddress(@Context HttpServletRequest request) {
        JsonObject responseObj = new JsonObject();
        HttpSession session = request.getSession();

        if (session != null && session.getAttribute("user") != null) {
            Users user = (Users) session.getAttribute("user");

            responseObj.addProperty("name", user.getFirstName() + " " + user.getLastName());
            responseObj.addProperty("email", user.getEmail());

            Session hibernateSess = HibernateUtil.getSessionFactory().openSession();

            List<Address> addressList =
                    hibernateSess.createQuery("FROM Address a " +
                                    "WHERE a.users=: user", Address.class)
                            .setParameter("user", user)
                            .getResultList();

            List<JsonObject> addresses = new ArrayList<>();
            for (Address a : addressList) {
                JsonObject jo = new JsonObject();
                jo.addProperty("lineOne", a.getLineOne());
                jo.addProperty("lineTwo", a.getLineTwo());
                jo.addProperty("mobile", a.getMobile());
                jo.addProperty("cityId", a.getCity().getId());
                jo.addProperty("cityName", a.getCity().getName());
                jo.addProperty("isPrimary", a.isPrimary());

                addresses.add(jo);
            }


            responseObj.add("addresses", AppUtil.GSON.toJsonTree(addresses));

            hibernateSess.close();

        }

        return AppUtil.GSON.toJson(responseObj);
    }

    public String updateProfile(UserDTO userDTO, @Context HttpServletRequest request) {

        JsonObject responseJson = new JsonObject();
        boolean status = false;
        String message = "";

        System.out.println("Current Password : " + userDTO.getFirstName());

        if (userDTO.getFirstName() == null) {
            message = "First name is Required !";
        } else if (userDTO.getFirstName().isEmpty()) {
            message = "First name can't be Empty ";
        } else if (userDTO.getLastName() == null) {
            message = "Last name is Required";
        } else if (userDTO.getLastName().isEmpty()) {
            message = "Last name can't be Empty ";
        } else if (userDTO.getLineOne() == null && userDTO.getLineOne().isBlank()) {
            message = "Address Line one id Required !";
        } else if (userDTO.getLineTwo() == null && userDTO.getLineTwo().isBlank()) {
            message = "Line Two cant be empty ?";
        } else if (userDTO.getCityId() == 0) {
            message = "Please select a city .";
        } else if (userDTO.getPostalCode() == null && userDTO.getPostalCode().isBlank()) {
            message = "Please add your city/towns postal code.";
        } else if (!userDTO.getPostalCode().matches(Validator.POSTALCODE_VALIDATION)) {
            message = "Invalid postal Code";
        } else if (userDTO.getMobile() == null && userDTO.getMobile().isBlank()) {
            message = "Mobile number is required.";
        } else if (!userDTO.getMobile().matches(Validator.MOBILE_VALIDATION)) {
            message = "Invalid mobile number , please check again!";
//        } else if (userDTO.getPassword() == null) {
//            message = "Password is highly required.";
//        } else if (userDTO.getPassword().isBlank()) {
//            message = "Password can't be Empty";
//        } else if (!userDTO.getPassword().matches(Validator.PASSWORD_VALIDATION)) {
//            message = "Please provide a valid password \n" +
//                    "The password must be contain least 8 characters to the maximum of 15 \n" +
//                    "At least one Upper case , special character and Digits with Lowercases !";
//        } else if (!userDTO.getNewPassword().isBlank()) {
//            if (userDTO.getNewPassword() != null) {
//                message = "New Password can't be Empty";
//            } else if (!userDTO.getNewPassword().matches(Validator.PASSWORD_VALIDATION)) {
//                message = "Please provide a valid password \n" +
//                        "New password must be contain least 8 characters to the maximum of 15 \n" +
//                        "At least one Upper case , special character and Digits with Lowercases !";
//
//            } else if (userDTO.getConfirmPassword().isBlank()) {
//                message = "Confirm Password can't be Empty";
//
//            } else if (!userDTO.getConfirmPassword().equals(userDTO.getNewPassword())) {
//                message = "Confirm password do not match with new password";
//            }

        } else {

            HttpSession httpSession = request.getSession(false);

            if (httpSession == null || httpSession.getAttribute("user") == null) {
                message = "Please login again";

            } else {

                Users sessUser = (Users) httpSession.getAttribute("user");

                Session session = HibernateUtil.getSessionFactory().openSession();
                Users dbUser = session.createNamedQuery("Users.getByEmail", Users.class)
                        .setParameter("email", sessUser.getEmail())
                        .getSingleResult();

                dbUser.setFirstName(userDTO.getFirstName());
                dbUser.setLastName(userDTO.getLastName());
//                dbUser.setPassword(!userDTO.getConfirmPassword().isBlank() ? userDTO.getConfirmPassword() : userDTO.getPassword());


//                Address currentAddress = session.createQuery("FROM Address a WHERE a.users=:users AND a.isPrimary=true", Address.class)
//                        .setParameter("users", dbUser)
//                        .getSingleResult();
//
//                if (currentAddress == null) {
//                    currentAddress = new Address();
//                    currentAddress.setUsers(dbUser);
//                    currentAddress.setPrimary(true);
//
//                }

//                NoResultException throws currnt address


//                currentAddress.setLineOne(userDTO.getLineOne());
//                currentAddress.setLineTwo(userDTO.getLineTwo());
//                currentAddress.setPostalCode(userDTO.getPostalCode());
//                currentAddress.setMobile(userDTO.getMobile());
//
//                City city = session.find(City.class, userDTO.getCityId());
//                currentAddress.setCity(city);
//
//                Transaction transaction = session.beginTransaction();
//                try {
////                    session.merge(currentAddress);
//                    session.merge(dbUser);
//                    transaction.commit();
//                    httpSession.setAttribute("user", dbUser);
//                    status = true;
//                    message = "Profile Update successfully";
//                } catch (HibernateException e) {
//                    transaction.rollback();
//                    message = "Profile updating process failed";
//                }


                Address currentAddress;

                List<Address> addresses = session.createQuery(
                                "FROM Address a WHERE a.users = :users AND a.isPrimary = true",
                                Address.class
                        )
                        .setParameter("users", dbUser)
                        .getResultList();

                if (addresses.isEmpty()) {
                    currentAddress = new Address();
                    currentAddress.setUsers(dbUser);
                    currentAddress.setPrimary(true);
                } else {
                    currentAddress = addresses.get(0);
                }

                currentAddress.setLineOne(userDTO.getLineOne());
                currentAddress.setLineTwo(userDTO.getLineTwo());
                currentAddress.setPostalCode(userDTO.getPostalCode());
                currentAddress.setMobile(userDTO.getMobile());

                City city = session.find(City.class, userDTO.getCityId());
                currentAddress.setCity(city);

                Transaction transaction = session.beginTransaction();
                try {
                    session.merge(dbUser);
                    session.merge(currentAddress);
                    transaction.commit();

                    httpSession.setAttribute("user", dbUser);
                    status = true;
                    message = "Profile updated successfully";

                } catch (HibernateException e) {
                    transaction.rollback();
                    message = "Profile updating process failed";
                }

                session.close();

            }

        }


        //profile updating part....


        responseJson.addProperty("status", status);
        responseJson.addProperty("message", message);

        return AppUtil.GSON.toJson(responseJson);
    }

    public String userData(@Context HttpServletRequest request) {

        JsonObject responseObj = new JsonObject();

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            // User is NOT logged in
            responseObj.addProperty("logged", false);
            return AppUtil.GSON.toJson(responseObj);
        }

        Users user = (Users) session.getAttribute("user");

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setPassword(user.getPassword());

        try (Session hibernateSess = HibernateUtil.getSessionFactory().openSession()) {
            List<Address> addressList =
                    hibernateSess.createQuery("FROM Address  a " +
                                    "WHERE a.users=:users", Address.class)
                            .setParameter("users", user)
                            .getResultList();

            Address primaryAddress = null;

            for (Address address : addressList) {
                if (address.isPrimary()) {
                    primaryAddress = address;
                    break;
                }
            }


            if (primaryAddress != null) {
                userDTO.setLineOne(primaryAddress.getLineOne());
                userDTO.setLineTwo(primaryAddress.getLineTwo());
                userDTO.setMobile(primaryAddress.getMobile());
                userDTO.setPostalCode(primaryAddress.getPostalCode());
                userDTO.setPrimary(primaryAddress.isPrimary());
                userDTO.setCityId(primaryAddress.getCity().getId());
                userDTO.setCityName(primaryAddress.getCity().getName());


            }

        } catch (HibernateException e) {
            throw new RuntimeException(e.getMessage());
        }


        LocalDateTime createdAt = user.getCreatedAt();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MMMM");
        String sinceAt = createdAt.format(formatter);
        userDTO.setSinceAt(sinceAt);

//        responseObj.add("user", AppUtil.GSON.toJsonTree(userDTO));


        responseObj.addProperty("logged", true);
        responseObj.add("user", AppUtil.GSON.toJsonTree(userDTO));
        return AppUtil.GSON.toJson(responseObj);


    }


    public String loadCart(@Context HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        String message = "";
        boolean status = false;

        HttpSession session = request.getSession();
        Session dbSess = HibernateUtil.getSessionFactory().openSession();

        if (session != null && session.getAttribute("user") != null) {

            Users user = (Users) session.getAttribute("user");
            Users dbUser = dbSess.merge(user);

            List<Cart> cartList = dbSess.createQuery("FROM Cart c " +
                            "WHERE c.users=:user " +
                            "ORDER BY c.id DESC ", Cart.class)
                    .setParameter("user", dbUser).getResultList();

            if (cartList.isEmpty()) {
                message = "empty";
            } else {
                List<cartDAO> cartDAOS = new ArrayList<>();

                cartList.forEach(cart -> {

                    Books books = cart.getBooks();
                    cartDAO cartDAO = new cartDAO();
                    cartDAO.setPid(books.getId());
                    cartDAO.setCartQty(cart.getQty());
                    cartDAO.setTitle(books.getTitle());
                    cartDAO.setPrice(books.getPrice());
                    cartDAO.setAuthor(books.getAuthor().getName());
                    cartDAO.setImages(books.getImages());
                    cartDAO.setProductQty(books.getStock());

                    cartDAOS.add(cartDAO);
                });
                jsonObject.add("cartItems", AppUtil.GSON.toJsonTree(cartDAOS));
                status = true;
            }

            dbSess.close();

        } else {
            message = "Please login to your account ?";
        }

        jsonObject.addProperty("message", message);
        jsonObject.addProperty("status", status);

        return AppUtil.GSON.toJson(jsonObject);
    }

    public String loadWishlistProducts(@Context HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        boolean status = false;
        String message = "";
        HttpSession session = request.getSession();
        Session dbSess = HibernateUtil.getSessionFactory().openSession();

//                System.out.println("ok");
        if (session != null && session.getAttribute("user") != null) {
            Users user = (Users) session.getAttribute("user");
//                System.out.println("ok");


            Users dbUser = dbSess.merge(user);
            List<Books> wishlistBooks =
                    dbSess.createQuery("SELECT w.books FROM Wishlist w " +
                                    "WHERE w.users=:user " +
                                    "ORDER BY w.id DESC ", Books.class)
                            .setParameter("user", dbUser)
                            .getResultList();

            System.out.println(dbUser.getLastName());

            if (!wishlistBooks.isEmpty()) {

                List<ProductDTO> productDAOList = new ArrayList<>();
                for (Books book : wishlistBooks) {
                    ProductDTO productDAO = getProductDAO(book);
                    productDAOList.add(productDAO);
                }
                jsonObject.add("wProducts", AppUtil.GSON.toJsonTree(productDAOList));
                status = true;
            } else {
                message = "empty";
            }

        } else {
            message = "Log into your account !";
        }

        dbSess.close();

        jsonObject.addProperty("status", status);
        jsonObject.addProperty("message", message);

        return AppUtil.GSON.toJson(jsonObject);

    }

    public String loadOrders(@Context HttpServletRequest request) {

        JsonObject jsonObject = new JsonObject();
        boolean status = false;
        String message = "";
        HttpSession session = request.getSession();
        Session dbSess = HibernateUtil.getSessionFactory().openSession();

        if (session != null && session.getAttribute("user") != null) {
//            System.out.println("ok");
            Users user = (Users) session.getAttribute("user");

            Users dbUser = dbSess.merge(user);

            List<Orders> orderList =
                    dbSess.createQuery("FROM Orders o " +
                                    "WHERE o.users=:users " +
                                    "ORDER BY  o.createdAt DESC ", Orders.class)
                            .setParameter("users", dbUser)
                            .getResultList();

//            System.out.println(orders.getOrder_id());
            List<OrderDTO> ordersData = new ArrayList<>();

            orderList.forEach(order -> {

                List<OrderItems> orderItems = dbSess.createQuery("FROM OrderItems oi " +
                                "WHERE oi.orders=:orders", OrderItems.class)
                        .setParameter("orders", order)
                        .getResultList();

                OrderDTO singleOrder = new OrderDTO();

                singleOrder.setOrder_id(order.getOrder_id());
                singleOrder.setCreatedAt(order.getCreatedAt().toString());
                singleOrder.setInv_id(order.getId());
                singleOrder.setTrackState(order.getTrack().getValue());

                ArrayList<OrderDetails> orderDetailList = new ArrayList<>();

                orderItems.forEach(orderItem -> {
                    OrderDetails orderDetails = new OrderDetails();
                    orderDetails.setTitle(orderItem.getBooks().getTitle());
                    orderDetails.setAuthor(orderItem.getBooks().getAuthor().getName());
                    orderDetails.setPrice(orderItem.getBooks().getPrice());
                    orderDetails.setImages(orderItem.getBooks().getImages());
                    orderDetails.setQty(orderItem.getQty());
                    orderDetailList.add(orderDetails);
//                System.out.println(orderItem.getBooks().getTitle());
                });

                singleOrder.setOrderDetails(orderDetailList);
                ordersData.add(singleOrder);
            });

            jsonObject.add("orders", AppUtil.GSON.toJsonTree(ordersData));
            status = true;

        } else {
            message = "Log into your account !";
        }

        dbSess.close();

        jsonObject.addProperty("status", status);
        jsonObject.addProperty("message", message);

        return AppUtil.GSON.toJson(jsonObject);

    }

    private static ProductDTO getProductDAO(Books books) {
        ProductDTO productDAO = new ProductDTO();
        productDAO.setId(books.getId());
        productDAO.setTitle(books.getTitle());
        productDAO.setPrice(books.getPrice());
        productDAO.setStock(books.getStock());
        productDAO.setAuthor(books.getAuthor().getName());
        productDAO.setCategoryName(books.getCategory().getName());
        productDAO.setDescription(books.getDescription());
        productDAO.setImage(books.getImages());
        return productDAO;
    }

}