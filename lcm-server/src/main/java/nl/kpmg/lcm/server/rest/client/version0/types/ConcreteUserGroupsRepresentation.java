/*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package nl.kpmg.lcm.server.rest.client.version0.types;

import com.fasterxml.jackson.annotation.JsonIgnore;

import nl.kpmg.lcm.rest.types.LinkInjectable;
import nl.kpmg.lcm.rest.types.UserGroupsRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.UserGroupController;

import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import java.util.List;

import javax.ws.rs.core.Link;

/**
 * A wrapper class for a UserGroup and its links.
 *
 * @author mhoekstra
 */
public class ConcreteUserGroupsRepresentation extends UserGroupsRepresentation
    implements LinkInjectable {

  /**
   * The links the of a UserGroupsRepresentation.
   */
  @InjectLinks({
      @InjectLink(resource = UserGroupController.class, style = InjectLink.Style.ABSOLUTE,
          rel = "usergroup.overview"),
      @InjectLink(resource = UserGroupController.class, method = "createNewUserGroup",
          style = InjectLink.Style.ABSOLUTE, rel = "usergroup.create",
          type = "application/nl.kpmg.lcm.server.data.UserGroup+json")})
  @JsonIgnore
  private List<Link> injectedLinks;

  @Override
  public List<Link> getInjectedLinks() {
    return injectedLinks;
  }
}
