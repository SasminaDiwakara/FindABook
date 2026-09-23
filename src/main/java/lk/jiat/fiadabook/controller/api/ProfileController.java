package lk.jiat.fiadabook.controller.api;

import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lk.jiat.fiadabook.annotation.IsUser;
import lk.jiat.fiadabook.dto.UserDTO;
import lk.jiat.fiadabook.service.ProfileService;
import lk.jiat.fiadabook.service.UserService;
import lk.jiat.fiadabook.util.AppUtil;

@Path("/profile")
public class ProfileController {

    @Path("/addresses")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadAddress(@Context HttpServletRequest request){
        String responseJson = new ProfileService().loadAddress(request);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/user-profile")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadUserProfile(@Context HttpServletRequest request) {
        String responseJson = new ProfileService().userData(request);
        return Response.ok().entity(responseJson).build();
    }


    @Path("/update-profile")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})

    public Response updateProfile(String jsonData , @Context HttpServletRequest request) {
        System.out.println("User Data" + jsonData);
        UserDTO userDTO  = AppUtil.GSON.fromJson(jsonData,UserDTO.class);
        String responseJson = new ProfileService().updateProfile( userDTO, request);
        return  Response.ok().entity(responseJson).build();
    }

}





