/*
 * Copyright 2016 KPMG N.V. (unless otherwise stated).
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

package nl.kpmg.lcm.server.data.service;

import nl.kpmg.lcm.client.HttpsClientFactory;
import nl.kpmg.lcm.configuration.ClientConfiguration;
import nl.kpmg.lcm.rest.types.MetaDatasRepresentation;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.data.RemoteLcm;
import nl.kpmg.lcm.server.exception.LcmException;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteMetaDataRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteMetaDatasRepresentation;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.Response;

/**
 *
 * @author shristov
 */
@Service
public class RemoteMetaDataService {

  private static final Logger logger = LoggerFactory.getLogger(RemoteMetaDataService.class.getName());
  private final String remoteMetaDataPath = "remote/v0/metadata?search=";
  @Autowired
  private ClientConfiguration configuration;

  @Autowired
  private RemoteLcmService remoteLcmService;

  // TODO once the Authorization/Auhtenticaion model is implemented this part must be refactored
  // After the refactoring ther emust be used a user which is used only for remote calls
  private String adminUser;
  private String adminPassword;

  @Value("${lcm.server.adminUser}")
  public final void setAdminUser(final String adminUser) {
    this.adminUser = adminUser;
  }

  @Value("${lcm.server.adminPassword}")
  public final void setAdminPassword(final String adminPassword) {
    this.adminPassword = adminPassword;
  }

  private MetaDatasRepresentation fetchRemoteLcmMetadata(RemoteLcm remoteLcm, String searchString) {
    configuration.setTargetHost(remoteLcm.getDomain());
    configuration.setTargetPort(remoteLcm.getPort().toString());
    String fetchUrl = buildRemoteUrl(remoteLcm) + "/" + remoteMetaDataPath + searchString;

    HttpAuthenticationFeature credentials =
        HttpAuthenticationFeature.basicBuilder().credentials(adminUser, adminPassword).build();

    HttpsClientFactory clientFactory = new HttpsClientFactory(configuration, credentials);

    Response response = null;
    try {
      response = clientFactory.createWebTarget(fetchUrl).request().get();
      if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
        MetaDatasRepresentation datasRepresentation =
            response.readEntity(MetaDatasRepresentation.class);
        return datasRepresentation;
      }
    } catch (ServerException ex) {
      logger.error( "Unable to make request. Error " + ex.getMessage());
      MetaDatasRepresentation emptObject = new MetaDatasRepresentation();
      emptObject.setItems(Collections.emptyList());

      return emptObject;
    }

    MetaDatasRepresentation emptObject = new MetaDatasRepresentation();
    emptObject.setItems(Collections.emptyList());

    return emptObject;
  }

  private String buildRemoteUrl(RemoteLcm lcm) {
    String url = String.format("%s://%s", lcm.getProtocol(), lcm.getDomain());
    if (lcm.getPort() != null) {
      url += ":" + lcm.getPort();
    }
    return url;
  }

  public MetaDatasRepresentation getMetaDatasRepresentation(String scope, String searchString) {
    Stream<RemoteLcm> registeredLcms = remoteLcmService.findAll().stream();
    if (scope.equals("all")) {
      Stream<MetaDatasRepresentation> remoteMetadatas =
          registeredLcms.map(rl -> fetchRemoteLcmMetadata(rl, searchString));
      return concretize(remoteMetadatas);
    }
    Optional<RemoteLcm> targetLcm =
        registeredLcms.filter(rl -> rl.getId().equals(scope)).findFirst();
    if (targetLcm.isPresent())
      return concretize(Stream.of(fetchRemoteLcmMetadata(targetLcm.get(), searchString)));
    else
      throw new LcmException(
          String.format("Unknown LCM remote peer: %s. Make sure to add it via the API.", scope),
          Response.Status.BAD_REQUEST);
  }

  protected MetaDatasRepresentation concretize(Stream<MetaDatasRepresentation> mdrss) {
    ConcreteMetaDatasRepresentation metaDatasRepresentation = new ConcreteMetaDatasRepresentation();
    mdrss.filter(mdrs -> mdrs.getItems() != null)
        .forEach(mdrs -> concretize(mdrs, metaDatasRepresentation));
    return metaDatasRepresentation;
  }

  private MetaDatasRepresentation concretize(MetaDatasRepresentation mdrs,
      ConcreteMetaDatasRepresentation cmdr) {
    List metadatas =
        mdrs.getItems().stream().map(mdr -> mdr.getItem()).collect(Collectors.toList());
    cmdr.addRepresentedItems(ConcreteMetaDataRepresentation.class, metadatas);

    return cmdr;
  }
}
