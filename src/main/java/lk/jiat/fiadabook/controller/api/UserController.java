package lk.jiat.fiadabook.controller.api;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.*;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lk.jiat.fiadabook.annotation.IsUser;
import lk.jiat.fiadabook.dto.PwResetDTO;
import lk.jiat.fiadabook.dto.UserDTO;
import lk.jiat.fiadabook.entity.Users;
import lk.jiat.fiadabook.mail.ForgotPasswordMail;
import lk.jiat.fiadabook.mail.VerificationMail;
import lk.jiat.fiadabook.provider.MailServiceProvider;
import lk.jiat.fiadabook.util.AppUtil;
import lk.jiat.fiadabook.service.UserService;
import lk.jiat.fiadabook.util.HibernateUtil;
import lk.jiat.fiadabook.validation.Validator;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.validator.constraints.ParameterScriptAssert;
import org.w3c.dom.ls.LSOutput;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


@Path("/users")
public class UserController {

    //    @IsUser
    @Path("/logout")
    @GET
    public Response logout(@Context HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        //        System.out.println(session);

        if (session != null && session.getAttribute("user") != null) {
            session.invalidate();
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewAccount(String JsonData) {
//        System.out.println("Login controller hit");
//        System.out.println("Received JSON: " + JsonData);
        UserDTO userDTO = AppUtil.GSON.fromJson(JsonData, UserDTO.class);
        String responseJson = new UserService().addNewUser(userDTO);
        return Response.ok().entity(responseJson).build();
    }

//    @Path("/login")
//    @POST
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//
//    public Response login(String JsonData, @Context HttpServletRequest request, @Context HttpServletResponse response) {
//        UserDTO userDTO = AppUtil.GSON.fromJson(JsonData, UserDTO.class);
//        System.out.println("Jason Data "+JsonData);
//        String responseJson = new UserService().userLogin(userDTO, request, response);
//        return Response.ok().entity(responseJson).build();
//    }

    @Path("/login")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(String JsonData, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        UserDTO userDTO = AppUtil.GSON.fromJson(JsonData, UserDTO.class);
        System.out.println("Jason Data " + JsonData);
        String responseJson = new UserService().userLogin(userDTO, request, response);
        return Response.ok().entity(responseJson).build();
    }


    @Path("/loadCookies")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadCookies(@Context HttpServletRequest request) {

        JsonObject response = new JsonObject();
        boolean status = false;

        String email = "";

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("remember_me".equals(c.getName())) {
                    try {
                        String decoded = new String(Base64.getDecoder().decode(c.getValue()));

                        String[] parts = decoded.split(":");

                        if (parts.length == 2) {
                            email = parts[0];
                        }

                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid cookie value");
                    }
                }
            }
        }

        status = true;

        response.addProperty("email", email);
        response.addProperty("status", status);

        String responseObj = AppUtil.GSON.toJson(response);

        return Response.ok().entity(responseObj).build();
    }

    @Path("/adminLogin")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response adminLogin(String JsonData, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        UserDTO userDTO = AppUtil.GSON.fromJson(JsonData, UserDTO.class);
        System.out.println("Jason Data " + JsonData);
        String responseJson = new UserService().adminLogin(userDTO, request, response);
        return Response.ok().entity(responseJson).build();
    }

    @Path("{email}/forgotPassword")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response forgotPassword(@PathParam("email") String email) {

        JsonObject resp = new JsonObject();
        String message = "";
        boolean status = false;

        if (email == null) {
            message = "Please enter your email ?";
        } else if (!email.matches(Validator.EMAIL_VALIDATION)) {

            message = "Invalid email address , recheck again!";
        } else {

            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            Transaction tx = hibernateSession.beginTransaction();
            try {
                Users singleUser = hibernateSession.createNamedQuery("Users.getByEmail", Users.class)
                        .setParameter("email", email)
                        .getSingleResultOrNull();

                if (singleUser != null) {

                    String gencode = AppUtil.generateCode();
                    singleUser.setVerificationCode(gencode);
                    hibernateSession.merge(singleUser);

                    tx.commit();

                    MailServiceProvider.getInstance().start();
                    ForgotPasswordMail forgotPasswordMail = new ForgotPasswordMail(email, gencode);
                    MailServiceProvider.getInstance().sendMail(forgotPasswordMail);

                    message = "Verification code has Sent !";
                    status = true;

                } else {
                    message = "No user found for this Email address ?";
                }
            } catch (HibernateException e) {
                tx.rollback();
                throw new RuntimeException(e);
            }
            hibernateSession.close();

        }

        resp.addProperty("status", status);
        resp.addProperty("message", message);


        return Response.ok().entity(AppUtil.GSON.toJson(resp)).build();

    }

    @Path("/resetPassword")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetPassword(String jsonData) {

//        System.out.println(jsonData);
        PwResetDTO resetDTO = AppUtil.GSON.fromJson(jsonData, PwResetDTO.class);
        System.out.println(resetDTO.getNewPassword());

        JsonObject resp = new JsonObject();
        String message = "";
        boolean status = false;

        if (resetDTO.getEmail().isBlank()) {
            message = "Please enter your email ?";
        } else if (!resetDTO.getEmail().matches(Validator.EMAIL_VALIDATION)) {
            message = "Invalid email address , recheck again!";
        } else if (resetDTO.getCode().isBlank()) {
            message = "Please enter your Verification Code ?";
        } else if (!resetDTO.getCode().matches(Validator.VERIFICATION_CODE_VALIDATION)) {
            message = "Invalid verification code!";
        } else if (resetDTO.getNewPassword().isEmpty()) {
            message = "Your new password is required ?";
        } else if (!resetDTO.getNewPassword().matches(Validator.PASSWORD_VALIDATION)) {
            message = "Please provide a valid password \n" +
                    "The password must be at least 8 characters to the maximum of 15 \n" +
                    "At least one Upper case , special character and Digits with Lowercases !";
        } else {

            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            Transaction tx = hibernateSession.beginTransaction();

            try {
                Users singleUser = hibernateSession.createQuery("FROM Users u" +
                                " WHERE u.email=:email " +
                                "AND u.verificationCode=:verifyCode ", Users.class)
                        .setParameter("email", resetDTO.getEmail())
                        .setParameter("verifyCode", resetDTO.getCode())
                        .getSingleResultOrNull();

                if (singleUser != null) {

                    if (singleUser.getPassword().equals(resetDTO.getNewPassword())) {
                        message = "This is your old password.  \n Try Login ?";
                    } else {
                        singleUser.setPassword(resetDTO.getNewPassword());
                        hibernateSession.merge(singleUser);
                        tx.commit();

                        message = "Password reset complete!";
                        status = true;

                    }

                } else {
                    message = "No user found for this Email address ?";
                }
            } catch (HibernateException e) {
                tx.rollback();
                throw new RuntimeException(e);
            }
            hibernateSession.close();
        }

        resp.addProperty("status", status);
        resp.addProperty("message", message);

        return Response.ok().entity(AppUtil.GSON.toJson(resp)).build();

    }

    @Path("/verify")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response verify(String jsonData) {
//        System.out.println("Jason Data"+jsonData);
        UserDTO userDAO = AppUtil.GSON.fromJson(jsonData, UserDTO.class);
        String response = new UserService().verify(userDAO);

        return Response.ok().entity(response).build();
    }

    @Path("/{email}/resendCode")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response resendCode(@PathParam("email") String email) {
//        System.out.println(email);

        JsonObject resp = new JsonObject();
        String message = "";
        boolean status = false;

        if (email == null) {
            message = "Please enter your email ?";
        } else if (!email.matches(Validator.EMAIL_VALIDATION)) {

            message = "Invalid email address , recheck again!";
        } else {


            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            Transaction tx = hibernateSession.beginTransaction();
            try {
                Users singleUser = hibernateSession.createNamedQuery("Users.getByEmail", Users.class)
                        .setParameter("email", email)
                        .getSingleResultOrNull();

                if (singleUser != null) {

                    String gencode = AppUtil.generateCode();
                    singleUser.setVerificationCode(gencode);
                    hibernateSession.merge(singleUser);

                    tx.commit();
                    //mail is sending from here
                    MailServiceProvider.getInstance().start();
                    VerificationMail verificationMail = new VerificationMail(email, gencode);
                    MailServiceProvider.getInstance().sendMail(verificationMail);

                    message = "Verification code has Sent !";
                    status = true;

                } else {
                    message = "No user found for this Email address ?";
                }
            } catch (HibernateException e) {
                tx.rollback();
                throw new RuntimeException(e);
            }
            hibernateSession.close();

        }

        resp.addProperty("status", status);
        resp.addProperty("message", message);


        return Response.ok().entity(AppUtil.GSON.toJson(resp)).build();
    }

}
