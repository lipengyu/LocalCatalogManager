package nl.kpmg.lcm.server.rest.client;

import nl.kpmg.lcm.rest.types.Version0Representation;
import nl.kpmg.lcm.server.rest.authentication.Roles;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteVersion0Representation;

import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * First Implementation of the LCM Client interface.
 *
 * The client interface is the REST API to which all clients will make their requests.
 *
 * @author mhoekstra
 */
@Component
@Path("client/v0")
public class Version0 {

  /**
   *
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  public Version0Representation getIndex() {
    return new ConcreteVersion0Representation();
  }
}