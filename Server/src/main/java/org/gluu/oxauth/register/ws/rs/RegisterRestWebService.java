/*
 * Janssen Project software is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.register.ws.rs;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * Provides interface for register REST web services.
 *
 * @author Javier Rojas Blum
 * @author Yuriy Zabrovarnyy
 * @version 0.1, 01.11.2012
 */
public interface RegisterRestWebService {

    /**
     * In order for an OpenID Connect client to utilize OpenID services for a user, the client needs to register with
     * the OpenID Provider to acquire a client ID and shared secret.
     *
     * @param requestParams   request parameters
     * @param httpRequest     http request object
     * @param securityContext An injectable interface that provides access to security related information.
     * @return response
     */
    @POST
    @Path("/register")
    @Produces({MediaType.APPLICATION_JSON})
    Response requestRegister(
            String requestParams,
            @Context HttpServletRequest httpRequest,
            @Context SecurityContext securityContext);

    /**
     * This operation updates the Client Metadata for a previously registered client.
     *
     * @param requestParams   request parameters
     * @param clientId        client id
     * @param authorization   Access Token that is used at the Client Configuration Endpoint
     * @param httpRequest     http request object
     * @param securityContext An injectable interface that provides access to security related information.
     * @return response
     */

    @PUT
    @Path("register")
    @Produces({MediaType.APPLICATION_JSON})
    Response requestClientUpdate(
            String requestParams,
            @QueryParam("client_id")
            String clientId,
            @HeaderParam("Authorization") String authorization,
            @Context HttpServletRequest httpRequest,
            @Context SecurityContext securityContext);

    /**
     * This operation retrieves the Client Metadata for a previously registered client.
     *
     * @param clientId        Unique Client identifier.
     * @param securityContext An injectable interface that provides access to security related information.
     * @return response
     */
    @GET
    @Path("/register")
    @Produces({MediaType.APPLICATION_JSON})
    Response requestClientRead(
            @QueryParam("client_id")
            String clientId,
            @HeaderParam("Authorization") String authorization,
            @Context HttpServletRequest httpRequest,
            @Context SecurityContext securityContext);

    /**
     * This operation removes the Client Metadata for a previously registered client.
     *
     * @param clientId        Unique Client identifier.
     * @param securityContext An injectable interface that provides access to security related information.
     * @return If a client has been successfully deprovisioned, the authorization
     * server responds with an HTTP 204 No Content message.
     * <p>
     * If the registration access token used to make this request is not
     * valid, the server responds with HTTP 401 Unauthorized.
     * <p>
     * If the client does not exist on this server, the server responds
     * with HTTP 401 Unauthorized.
     * <p>
     * If the client is not allowed to delete itself, the server
     * responds with HTTP 403 Forbidden.
     */
    @DELETE
    @Path("/register")
    @Produces({MediaType.APPLICATION_JSON})
    Response delete(
            @QueryParam("client_id") String clientId,
            @HeaderParam("Authorization") String authorization,
            @Context HttpServletRequest httpRequest,
            @Context SecurityContext securityContext);
}