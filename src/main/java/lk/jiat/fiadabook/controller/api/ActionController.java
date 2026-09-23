package lk.jiat.fiadabook.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lk.jiat.fiadabook.dto.DataDTO;
import lk.jiat.fiadabook.service.ActionService;
import lk.jiat.fiadabook.util.AppUtil;

import java.util.Map;

@Path("/action")
public class ActionController {
    @Path("/dashboard-stats")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDashboardStats() {
        String responseJson = new ActionService().getDashboardStats();
        return Response.ok().entity(responseJson).build();
    }

    @Path("/categories")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCategories() {
        String responseJson = new ActionService().getCategories();
        return Response.ok().entity(responseJson).build();
    }

    @Path("/addCategories")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addCategory(String jsonData) {
        Map<String, String> data = AppUtil.GSON.fromJson(jsonData, Map.class);
        String categoryName = data != null ? data.get("categoryName") : null;
        String responseJson = new ActionService().addCategory(categoryName);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/authors")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAuthors() {
        String responseJson = new ActionService().getAuthors();
        return Response.ok().entity(responseJson).build();
    }

    @Path("/addAuthor")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addAuthor(String jsonData) {
        Map<String, String> data = AppUtil.GSON.fromJson(jsonData, Map.class);
        String authorName = data != null ? data.get("authorName") : null;
        String responseJson = new ActionService().addAuthor(authorName);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/loadOrders")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadOrders() {
        String responseJson = new ActionService().loadOrders();
        return Response.ok().entity(responseJson).build();
    }

    @Path("/customers")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCustomers() {
        String responseJson = new ActionService().getCustomers();
        return Response.ok().entity(responseJson).build();
    }

    @Path("/{pid}/addToWishList")
    @GET
    public Response addToWishList(@PathParam("pid") int pid, @Context HttpServletRequest request) {
        String response = new ActionService().addToWishList(pid, request);
        return Response.ok().entity(response).build();
    }

    @Path("/{pid}/addToCart")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response addToCart(@PathParam("pid") int pid, @Context HttpServletRequest request) {
        String response = new ActionService().addToCart(pid, request);
        return Response.ok().entity(response).build();
    }

    @Path("/{pid}/removeItem")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeItem(@PathParam("pid") int pid, @Context HttpServletRequest request) {
        String response = new ActionService().removeItem(pid, request);
        return Response.ok().entity(response).build();
    }

    @Path("/{pid}/updateCart")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCart(@PathParam("pid") int pid, @Context HttpServletRequest request) {
        int qty = Integer.parseInt(request.getParameter("itemCount"));
        String response = new ActionService().updateCart(pid, qty, request);
        return Response.ok().entity(response).build();
    }

    @Path("/authenticateBuy")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticateBuy(String JsonData, @Context HttpServletRequest request) {
        DataDTO dataDTO = AppUtil.GSON.fromJson(JsonData, DataDTO.class);
        System.out.println("Jason Data " + JsonData);
        String responseJson = new ActionService().authenticateBuy(request, dataDTO);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/completeBuy")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response completeBuy(String JsonData, @Context HttpServletRequest request) {
        DataDTO dataDTO = AppUtil.GSON.fromJson(JsonData, DataDTO.class);
        System.out.println("Jason Data " + JsonData);
        String responseJson = new ActionService().completeBuy(request, dataDTO);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/{total}/authenticateCheckout")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticateCheckout(@PathParam("total") double total, @Context HttpServletRequest request) {
        String responseJson = new ActionService().authenticateCheckout(request, total);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/completeCartBuy")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response completeCartBuy(String JsonData, @Context HttpServletRequest request) {
        DataDTO dataDTO = AppUtil.GSON.fromJson(JsonData, DataDTO.class);
        System.out.println("Jason Data " + JsonData);
        String responseJson = new ActionService().completeCartBuy(request, dataDTO);
        return Response.ok().entity(responseJson).build();
    }
}