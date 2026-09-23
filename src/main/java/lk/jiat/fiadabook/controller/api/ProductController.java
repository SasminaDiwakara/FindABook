package lk.jiat.fiadabook.controller.api;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lk.jiat.fiadabook.dto.ProductDTO;
import lk.jiat.fiadabook.entity.Books;
import lk.jiat.fiadabook.service.FileUploadService;
import lk.jiat.fiadabook.service.ProductService;
import lk.jiat.fiadabook.util.AppUtil;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Path("/products")
public class ProductController {

//    @Path("/products")
//    @GET@Produces(MediaType.APPLICATION_JSON)
//    public Response loadAllUserProducts(@Context HttpServletRequest request) {
//        return Response.ok().entity("").build();
//    }

    @Path("/latest")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadLatestProducts() {
        String json = new ProductService().getLatestProducts();
        return Response.ok(json).build();
    }

    @Path("/popular")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadPopularProducts() {
        String json = new ProductService().getPopularProducts();
        return Response.ok(json).build();
    }

    @Path("/{productId}/upload-images")
    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadProductImages(@PathParam("productId") int pid
            , @FormDataParam("images[]") FormDataBodyPart formDataBodyPart
            , @Context ServletContext context) {
        List<FileUploadService.FileItem> fileItems = new ArrayList<>();
        FileUploadService fileUploadService = new FileUploadService(context);
        ProductService productService = new ProductService();
        Books books = productService.getProductById(pid);


        books.getImages().clear();

        formDataBodyPart.getParent().getBodyParts().forEach(bodyPart -> {

            InputStream inputStream = bodyPart.getEntityAs(InputStream.class);
            ContentDisposition contentDisposition = bodyPart.getContentDisposition();
            System.out.println(contentDisposition.getFileName());
            FileUploadService.FileItem fileItem  = fileUploadService.uploadFile("product/"+pid,inputStream,contentDisposition);
            fileItems.add(fileItem);
            books.getImages().add(fileItem.getFilePath());

        });

        String responseJson = new ProductService().updateProduct(books);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/addProducts")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addProduct(@FormDataParam("products") String productJson
            , @Context HttpServletRequest request) {
        ProductDTO productDTO = AppUtil.GSON.fromJson(productJson, ProductDTO.class);

        String responseJson = new ProductService().addNewProduct(productDTO, request);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/{pid}/loadProductData")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadProductData(@PathParam("pid")int pid){
        String resp = new ProductService().loadProductData(pid);

        return  Response.ok().entity(resp).build();
    }

    @Path("/{oid}/loadInvoiceData")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadInvoiceData(@PathParam("oid")int oid ,@Context HttpServletRequest request){

        String response = new ProductService().loadInvoiceData(oid , request);
        return Response.ok().entity(response).build();
    }


    private final ProductService productService = new ProductService();
    @Path("/update")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProduct(String json) {
        try {
            ProductDTO dto = AppUtil.GSON.fromJson(json, ProductDTO.class);
            return Response.ok(new ProductService().updateProduct(dto)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":false,\"message\":\"Invalid product data.\"}")
                    .build();
        }
    }

    @Path("/admin/dashboard")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response adminDashboard() {
        return Response.ok(new ProductService().getAdminDashboardStats()).build();
    }

    @Path("/admin/author/update")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAuthor(String json) {
        try {
            com.google.gson.JsonObject data = AppUtil.GSON.fromJson(json, com.google.gson.JsonObject.class);
            int id = data.has("id") ? data.get("id").getAsInt() : 0;
            String name = data.has("name") ? data.get("name").getAsString() : "";
            return Response.ok(new ProductService().updateAuthor(id, name)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":false,\"message\":\"Invalid author data.\"}")
                    .build();
        }
    }

    @Path("/admin/category/update")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCategory(String json) {
        try {
            com.google.gson.JsonObject data = AppUtil.GSON.fromJson(json, com.google.gson.JsonObject.class);
            int id = data.has("id") ? data.get("id").getAsInt() : 0;
            String name = data.has("name") ? data.get("name").getAsString() : "";
            return Response.ok(new ProductService().updateCategory(id, name)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":false,\"message\":\"Invalid category data.\"}")
                    .build();
        }
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchProducts(
            @QueryParam("q") String queryStr,
            @QueryParam("categoryId") Integer categoryId,
            @QueryParam("minPrice") Double minPrice,
            @QueryParam("maxPrice") Double maxPrice,
            @QueryParam("sort") @DefaultValue("latest") String sort,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("limit") @DefaultValue("12") int limit) {

        try {
            Map<String, Object> responseData = productService.searchProducts(queryStr, categoryId, minPrice, maxPrice, sort, page, limit);
            return Response.ok(responseData).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("error", e.getMessage())).build();
        }
    }

}
