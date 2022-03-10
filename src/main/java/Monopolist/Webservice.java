package Monopolist;

import generated.PallierType;
import generated.ProductType;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("generic")
public class Webservice {
    Services services;
    public Webservice() {
        services = new Services();
    }

    @GET
    @Path("world")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getWorld(@Context HttpServletRequest request) {
        String username = request.getHeader("X-user");
        return Response.ok(services.getWorld(username)).build();
    }
/*
    @PUT
    @Path("product")
    @Produces({MediaType.APPLICATION_JSON})
    public Response updateProduct(String username, ProductType productType) {
        services.updateProduct(username, productType);
        return Response.ok(services.getWorld(username)).build();
    }

    @PUT
    @Path("manager")
    @Produces({MediaType.APPLICATION_JSON})
    public Response updateProductManager(String username, PallierType pallierType) {
        services.updateManager(username, pallierType);
        return Response.ok(services.getWorld(username)).build();
    }



    @PUT
    @Path("upgrade")
    @Produces({MediaType.APPLICATION_JSON})
    public Response upgrade(String username) {
        return Response.ok(services.getWorld(username)).build();
    }

    @PUT
    @Path("angelupgrade")
    @Produces({MediaType.APPLICATION_JSON})
    public Response angelupgrade(String username) {
        return Response.ok(services.getWorld(username)).build();
    }

    @DELETE
    @Path("world")
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteWorld(String username) {
        return Response.ok(services.getWorld(username)).build();
    }
    */
}

