package lk.jiat.fiadabook.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.Context;
import lk.jiat.fiadabook.dto.CartItem;
import lk.jiat.fiadabook.dto.UserDTO;
import lk.jiat.fiadabook.entity.*;
import lk.jiat.fiadabook.mail.VerificationMail;
import lk.jiat.fiadabook.provider.MailServiceProvider;
import lk.jiat.fiadabook.util.AppUtil;
import lk.jiat.fiadabook.util.HibernateUtil;
import lk.jiat.fiadabook.validation.Validator;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;


public class UserService {


    public String addNewUser(UserDTO userDTO) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

//        message = userDTO.getFirstName();
//
        if (userDTO.getFirstName() == null) {
            message = "First name is Required !";
        } else if (userDTO.getFirstName().isEmpty()) {
            message = "First name can't be Empty ";
        } else if (userDTO.getLastName() == null) {
            message = "Last name is Required";
        } else if (userDTO.getLastName().isEmpty()) {
            message = "Last name can't be Empty ";
        } else if (userDTO.getEmail() == null) {
            message = "Email is Required";
        } else if (userDTO.getEmail().isBlank()) {
            message = "Email cant be Empty";
        } else if (!userDTO.getEmail().matches(Validator.EMAIL_VALIDATION)) {
            message = "Invalid Email";
        } else if (userDTO.getPassword() == null) {
            message = "Password is highly required";
        } else if (userDTO.getPassword().isBlank()) {
            message = "Password can't be Empty";
        } else if (!userDTO.getPassword().matches(Validator.PASSWORD_VALIDATION)) {
            message = "Please provide a valid password \n" +
                    "The password must be at least 8 characters to the maximum of 15 \n" +
                    "At least one Upper case , special character and Digits with Lowercases !";
        } else if (userDTO.getConfirmPassword().isBlank()) {
            message = "Password can't be Empty";
        } else if (!userDTO.getConfirmPassword().matches(userDTO.getPassword())) {
            message = "Passwords do not match.";
        } else {

            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            Users singleUser = hibernateSession.createNamedQuery("Users.getByEmail", Users.class)
                    .setParameter("email", userDTO.getEmail())
                    .getSingleResultOrNull();

            if (singleUser != null) {
                message = "This is already exists ! Please use another account";

            } else {

                Users u = new Users();
                u.setFirstName(userDTO.getFirstName());
                u.setLastName(userDTO.getLastName());
                u.setEmail(userDTO.getEmail());
                u.setPassword(userDTO.getPassword());
                Role role = hibernateSession.get(Role.class, 2);
                u.setRole(role);

                String genCode = AppUtil.generateCode();
                u.setVerificationCode(genCode);

                Status statusValue = hibernateSession.get(Status.class, 4);
                u.setStatus(statusValue);


                Transaction transaction = hibernateSession.beginTransaction();
                try {
                    hibernateSession.persist(u);
                    transaction.commit();

                    MailServiceProvider.getInstance().start();
                    VerificationMail verificationMail = new VerificationMail(userDTO.getEmail(), genCode);
                    MailServiceProvider.getInstance().sendMail(verificationMail);

                    message = "Account created successfully" +
                            "Verification code has sent to your Email , check your mail";
                    status = true;


                } catch (HibernateException e) {
                    transaction.rollback();
                    message = "Account creation Failed. Please try again later !";
                }

            }
            hibernateSession.close();
        }
        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);

        return AppUtil.GSON.toJson(responseObject);
    }

    public String userLogin(UserDTO userDTO, @Context HttpServletRequest request,
                            @Context HttpServletResponse response) {

//        JsonObject responseObj = new JsonObject();
//        boolean status = false;
//        String message = "";
//
////         user authentication part...
//
//        if (userDTO.getEmail() == null) {
//            message = "Email is Required";
//        } else if (userDTO.getEmail().isBlank()) {
//            message = "Email cant be Empty";
//        } else if (!userDTO.getEmail().matches(Validator.EMAIL_VALIDATION)) {
//            message = "Invalid Email";
//        } else if (userDTO.getPassword() == null) {
//            message = "Password is highly required";
//        } else if (userDTO.getPassword().isBlank()) {
//            message = "Password can't be Empty";
//        } else if (!userDTO.getPassword().matches(Validator.PASSWORD_VALIDATION)) {
//            message = "Please provide a valid password \n" +
//                    "The password must be at least 8 characters to the maximum of 15 \n" +
//                    "At least one Upper case , special character and Digits with Lowercases !";
//        } else {
//
//            if (userDTO.isRemember()) {
//
//            } else {
//
//            }
//            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
//            Users singleUser = hibernateSession.createNamedQuery("Users.getByEmail", Users.class)
//                    .setParameter("email", userDTO.getEmail())
//                    .getSingleResultOrNull();
//
//            if (singleUser == null) {
//                message = "User account not found ?";
//
//            } else {
//                Status verifyStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
//                        .setParameter("value", String.valueOf(Status.Type.VERIFIED))
//                        .getSingleResult();
//
//                if (!singleUser.getPassword().equals(userDTO.getPassword())) {
//                    message = "Something went wrong , please check your login details.";
//                } else if (!singleUser.getStatus().equals(verifyStatus)) {
//                    message = "Your account not verified , please verify before Login.";
//                } else {
////                    HttpSession httpSession = request.getSession();
////                    httpSession.setAttribute("user", singleUser);
////                    status = true;
////                    message = "Login Successful";
//                    if (singleUser.getPassword().equals(userDTO.getPassword())
//                            && singleUser.getStatus().equals(verifyStatus)) {
//
//                        HttpSession httpSession = request.getSession();
//                        httpSession.setAttribute("user", singleUser);
//
//                        // REMEMBER ME LOGIC (NO DB STORAGE)
//                        if (userDTO.isRemember()) {
//
//                            // Token: lightweight, safe, no password stored
//                            String token = singleUser.getEmail() + ":" + System.currentTimeMillis();
//
//                            String encoded = Base64.getEncoder().encodeToString(token.getBytes());
//
//                            Cookie rememberCookie = new Cookie("remember_me", encoded);
//                            rememberCookie.setHttpOnly(true);
//                            rememberCookie.setPath("/");
//                            rememberCookie.setMaxAge(60 * 60 * 24 * 30); // 30 days
//
//                            // add cookie to response
//                            request.getSession().getServletContext()
//                                    .getResponse().addCookie(rememberCookie);
//                        }
//
//                        status = true;
//                        message = "Login Successful";
//                    }
//
//                }
//
//            }
//
//            hibernateSession.close();
//        }
//
//
//        responseObj.addProperty("status", status);
//        responseObj.addProperty("message", message);
//        return AppUtil.GSON.toJson(responseObj);

        JsonObject responseObj = new JsonObject();
        boolean status = false;
        String message = "";

        // VALIDATIONS
        if (userDTO.getEmail() == null) {
            message = "Email is required";
        } else if (userDTO.getEmail().isBlank()) {
            message = "Email cannot be empty";
        } else if (!userDTO.getEmail().matches(Validator.EMAIL_VALIDATION)) {
            message = "Invalid email format";
        } else if (userDTO.getPassword() == null) {
            message = "Password is required";
        } else if (userDTO.getPassword().isBlank()) {
            message = "Password cannot be empty";
        } else if (!userDTO.getPassword().matches(Validator.PASSWORD_VALIDATION)) {
            message = "Invalid password format";
        } else {

            Session hSession = HibernateUtil.getSessionFactory().openSession();
            Transaction tx = hSession.beginTransaction();
            Users singleUser = hSession.createNamedQuery("Users.getByEmail", Users.class)
                    .setParameter("email", userDTO.getEmail())
                    .getSingleResultOrNull();

            if (singleUser == null) {
                message = "User account not found";

            } else {

                Status verifyStatus = hSession.createNamedQuery("Status.findByValue", Status.class)
                        .setParameter("value", String.valueOf(Status.Type.ACTIVE))
                        .getSingleResult();

                if (!singleUser.getPassword().equals(userDTO.getPassword())) {
                    message = "Incorrect login details";

                } else if (!singleUser.getStatus().equals(verifyStatus)) {
                    message = "Please verify your account before logging in";

                } else {

                    HttpSession httpSession = request.getSession();
                    httpSession.setAttribute("user", singleUser);

                    Map<Integer, CartItem> cart = (Map<Integer, CartItem>) httpSession.getAttribute("cart");

                    if (cart != null && !cart.isEmpty()) {
                        mergeCart(hSession, singleUser, cart);
                        tx.commit();
                    }

                    if (userDTO.isRemember()) {

                        String token = singleUser.getEmail() + ":" + System.currentTimeMillis();
                        String encoded = Base64.getEncoder().encodeToString(token.getBytes());

                        Cookie rememberCookie = new Cookie("remember_me", encoded);
                        rememberCookie.setHttpOnly(true);
                        rememberCookie.setSecure(true);
                        rememberCookie.setPath("/");
                        rememberCookie.setMaxAge(60 * 60 * 24 * 30);

                        response.addCookie(rememberCookie);

                    }

                    status = true;
                    message = "Login Successful";
                }
            }

            hSession.close();
        }

        responseObj.addProperty("status", status);
        responseObj.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObj);

    }


    public String verify(UserDTO userDAO) {

        JsonObject responseObj = new JsonObject();
        boolean status = false;
        String message = "";

        if (userDAO.getEmail() == null) {
            message = "Email is Required to verify you ?";
        } else if (userDAO.getEmail().isBlank()) {
            message = "Email can't be Empty";
        } else if (!userDAO.getEmail().matches(Validator.EMAIL_VALIDATION)) {
            message = "Invalid Email address";
        } else if (userDAO.getVerificationCode() == null) {
            message = "Verification code not found !";
        } else if (userDAO.getVerificationCode().isBlank()) {
            message = "Can't complete this process without Verification Code !";
        } else if (!userDAO.getVerificationCode().matches(Validator.VERIFICATION_CODE_VALIDATION)) {
            message = "Please provide a valid verification code !";
        } else {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Users user =
                    session.createQuery("FROM Users u " +
                                    "WHERE u.email = :email " +
                                    "AND u.verificationCode=:verificationCode ", Users.class)
                            .setParameter("email", userDAO.getEmail())
                            .setParameter("verificationCode", userDAO.getVerificationCode())
                            .getSingleResult();

            if (user == null) {
                message = "Account not found . Please register first!";
            } else {

                Status verifiedStatus = session.createNamedQuery("Status.findByValue", Status.class)
                        .setParameter("value", String.valueOf(Status.Type.ACTIVE))
                        .getSingleResult();

                if (user.getStatus().equals(verifiedStatus)) {
                    message = "Account already verified !";

                } else {
                    user.setStatus(verifiedStatus);
                    Transaction transaction = session.beginTransaction();

                    try {
                        session.merge(user);
                        transaction.commit();
                        status = true;
                        message = "Account verification Completed !";
                    } catch (HibernateException e) {
                        transaction.rollback();
                        message = "Something went wrong . Verification process failed ?";

                    }

                }

            }

            session.close();
        }

        responseObj.addProperty("status", status);
        responseObj.addProperty("message", message);

        return AppUtil.GSON.toJson(responseObj);
    }


    private void mergeCart(Session dbSess, Users user, Map<Integer, CartItem> cartItems) {

        Cart dbCart = (Cart) dbSess.createQuery("FROM Cart c WHERE c.users =: user")
                .setParameter("user", user)
                .getSingleResultOrNull();

        cartItems.forEach((integer, cartItem) -> {

            Books books = dbSess.get(Books.class, cartItem.getPid());

            if (dbCart != null && dbCart.getBooks().equals(books)) {
                dbCart.setQty(dbCart.getQty() + 1);
                dbSess.merge(dbCart);
            } else {
                Cart cart = new Cart();
                cart.setUsers(user);
                cart.setBooks(books);
                cart.setQty(1);

                dbSess.persist(cart);
            }

        });


    }


    public String adminLogin(UserDTO userDTO, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        JsonObject responseObj = new JsonObject();
        boolean status = false;
        String message = "";

        // VALIDATIONS
        if (userDTO.getEmail() == null) {
            message = "Email is required";
        } else if (userDTO.getEmail().isBlank()) {
            message = "Email cannot be empty";
        } else if (!userDTO.getEmail().matches(Validator.EMAIL_VALIDATION)) {
            message = "Invalid email format";
        } else if (userDTO.getPassword() == null) {
            message = "Password is required";
        } else if (userDTO.getPassword().isBlank()) {
            message = "Password cannot be empty";
        } else if (!userDTO.getPassword().matches(Validator.PASSWORD_VALIDATION)) {
            message = "Invalid password format";
        } else {

            Session hSession = HibernateUtil.getSessionFactory().openSession();
            Users singleUser = hSession.createNamedQuery("Users.getByEmail", Users.class)
                    .setParameter("email", userDTO.getEmail())
                    .getSingleResultOrNull();

            if (singleUser == null) {
                message = "User account not found";

            } else {

                Status verifyStatus = hSession.createNamedQuery("Status.findByValue", Status.class)
                        .setParameter("value", String.valueOf(Status.Type.VERIFIED))
                        .getSingleResult();

                Role userRole = hSession.createNamedQuery("Role.findByValue", Role.class)
                        .setParameter("value", String.valueOf(Role.Type.ADMIN))
                        .getSingleResult();

                if (!singleUser.getPassword().equals(userDTO.getPassword())) {
                    message = "Incorrect login details";

                } else if (!singleUser.getStatus().equals(verifyStatus)) {
                    message = "Please verify your account before logging in";

                } else if (!singleUser.getRole().equals(userRole)) {
                    message = "Not An ADMIN Account";

                } else {

                    HttpSession httpSession = request.getSession();
                    httpSession.setAttribute("user", singleUser);

                    status = true;
                    message = "Login Successful";
                }
            }

            hSession.close();
        }

        responseObj.addProperty("status", status);
        responseObj.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObj);

    }


    public String verifyUser(UserDTO userDTO) {

        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

//        System.out.println(userDTO.getVerificationCode());
//        System.out.println(userDTO.getEmail());

        if (userDTO.getEmail() == null) {
            message = "Email is Required to verify you ?";
        } else if (userDTO.getEmail().isBlank()) {
            message = "Email address can't be Empty";
        } else if (!userDTO.getEmail().matches(Validator.EMAIL_VALIDATION)) {
            message = "Invalid Email address";
        } else if (userDTO.getVerificationCode() == null) {
            message = "Verification code not found !";
        } else if (userDTO.getVerificationCode().isBlank()) {
            message = "Can't complete this process without Verification Code !";
        } else if (!userDTO.getVerificationCode().matches(Validator.VERIFICATION_CODE_VALIDATION)) {
            message = "Please provide a valid verification code !";
        } else {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Users users = session.createQuery("FROM Users u WHERE u.email = :email AND u.verificationCode=:verificationCode", Users.class)
                    .setParameter("email", userDTO.getEmail())
                    .setParameter("verificationCode", userDTO.getVerificationCode())
                    .getSingleResult();

            if (users == null) {
                message = "Account not found . Please register first!";
            } else {
                Status verifiedStatus = session.createNamedQuery("Status.findByValue", Status.class)
                        .setParameter("value", String.valueOf(Status.Type.ACTIVE))
                        .getSingleResult();

                if (users.getStatus().equals(verifiedStatus)) {
                    message = "Account already verified !";

                } else {
                    users.setStatus(verifiedStatus);
                    users.setVerificationCode("");
                    Transaction transaction = session.beginTransaction();

                    try {
                        session.merge(users);
                        transaction.commit();
                        status = true;
                        message = "Account verification Completed !";
                    } catch (HibernateException e) {
                        transaction.rollback();
                        message = "Something went wrong . Verification process failed ?";

                    }

                }

            }

            session.close();
        }


        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

}