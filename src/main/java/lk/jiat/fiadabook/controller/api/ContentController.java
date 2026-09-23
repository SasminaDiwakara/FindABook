package lk.jiat.fiadabook.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lk.jiat.fiadabook.dto.AuthorDTO;
import lk.jiat.fiadabook.dto.CategoryDTO;
import lk.jiat.fiadabook.service.CityService;
import lk.jiat.fiadabook.service.ProductService;
import lk.jiat.fiadabook.service.ProfileService;
import lk.jiat.fiadabook.util.AppUtil;

@Path("/data")
public class ContentController {
    @Path("/cities")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadCities(){
        String loadAllCities = new CityService().loadCities();
        return Response.ok().entity(loadAllCities).build();
    }

    @Path("/categories")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadCategories(String JsonData, @Context HttpServletRequest request , @Context HttpServletResponse response) {
        String loadAllCategories = new ProductService().loadCatogeries();
        return Response.ok(loadAllCategories).build();
    }

    @Path("/addCategories")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addNewCategories(String jsonData) {

        CategoryDTO categoryDTO = AppUtil.GSON.fromJson(jsonData, CategoryDTO.class);
        String responseJson = new ProductService().addNewCategory(categoryDTO);
        return Response.ok(responseJson).build();
    }

    @Path("/authors")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadAuthors(String JsonData, @Context HttpServletRequest request , @Context HttpServletResponse response) {
        String loadAllAuthors = new ProductService().loadAuthors();
        return Response.ok(loadAllAuthors).build();
    }

    @Path("/addAuthor")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addNewAuthor(String jsonData) {

        AuthorDTO authorDTO = AppUtil.GSON.fromJson(jsonData, AuthorDTO.class);
        String responseJson = new ProductService().addNewAuthor(authorDTO);
        return Response.ok(responseJson).build();
    }


    @Path("/loadCart")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadCart(@Context HttpServletRequest request){

        String response = new ProfileService().loadCart(request);
        return Response.ok().entity(response).build();
    }


    @Path("/loadWishlistProducts")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadWishlistProducts(@Context HttpServletRequest request){

        String resp = new ProfileService().loadWishlistProducts(request);

        return  Response.ok().entity(resp).build();
    }

    @Path("/loadOrders")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadOrders(@Context HttpServletRequest request){

        String resp = new ProfileService().loadOrders(request);
        System.out.println(resp);
        return  Response.ok().entity(resp).build();
    }



}

