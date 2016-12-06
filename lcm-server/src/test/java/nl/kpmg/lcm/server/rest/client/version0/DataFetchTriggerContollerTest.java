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

package nl.kpmg.lcm.server.rest.client.version0;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;

import nl.kpmg.lcm.server.LcmBaseServerTest;
import nl.kpmg.lcm.server.ServerException;

import org.junit.After;
import org.junit.Test;

import java.util.logging.Logger;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import nl.kpmg.lcm.rest.types.MetaDataRepresentation;
import nl.kpmg.lcm.rest.types.MetaDatasRepresentation;
import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.backend.exception.BackendException;
import nl.kpmg.lcm.server.backend.exception.BackendNotImplementedException;
import nl.kpmg.lcm.server.data.Data;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.RemoteLcm;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.server.data.TaskDescription;
import nl.kpmg.lcm.server.data.service.StorageService;

import nl.kpmg.lcm.server.rest.authentication.BasicAuthenticationManager;

import nl.kpmg.lcm.server.data.service.TaskDescriptionService;
import nl.kpmg.lcm.server.data.service.exception.MissingStorageException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;

public class DataFetchTriggerContollerTest extends LcmBaseServerTest {

  @Autowired
  private TaskDescriptionService taskDescriptionService;

  private static final String AUTH_USER_HEADER = "LCM-Authentication-User";
  private static final String METADATA_CONTENT_TYPE =
      "application/nl.kpmg.lcm.server.data.MetaData+json";
  private static final String METADATA_PATH = "client/v0/local";
  private static final String TRIGGER_PATH = "client/v0/remote";
  private static final Logger LOG = Logger.getLogger(DataFetchTriggerContollerTest.class.getName());
  private static final String CSV_SCHEME = "csv";
  private static final String CSV_STORAGE_PATH = System.getProperty("java.io.tmpdir");
  private static final String CSV_STORAGE_NAME = "csv-storage";
  private static final String CSV_FILE_NAME = "temp.csv";
  private static final String CSV_STORAGE_URI =
      CSV_SCHEME + "://" + CSV_STORAGE_NAME + "/" + CSV_FILE_NAME;
  private static final File CSV_FILE = new File(CSV_STORAGE_PATH + File.separator + CSV_FILE_NAME);
  private static final String LCM_PATH = "client/v0/remote";
  private static final String LCM_CONTENT_TYPE =
      "application/nl.kpmg.lcm.server.data.RemoteLcm+json";

  private int dataLen;
  private int headerLen;

  @Autowired
  private StorageService storageService;

  /**
   * Always clean up DB.
   *
   * @throws ServerException
   */
  @After
  public void afterTest() throws ServerException {
    getWebTarget().path("client/logout").request().header(AUTH_USER_HEADER, "admin")
        .header(BasicAuthenticationManager.BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin)
        .post(null);
    List<TaskDescription> tdList = taskDescriptionService.findAll();
    for (TaskDescription td : tdList) {
      if (td != null) {
        taskDescriptionService.getTaskDescriptionDao().delete(td);
      }
    }
  }

  @Before
  public void beforeTest() throws ServerException {

  }

  @AfterClass
  public static void afterClass() {}

  @BeforeClass
  public static void beforeClass() {}

  @Test
  @Ignore
  public void testTrigger() throws ServerException, MissingStorageException, BackendException,
      BackendNotImplementedException, IOException {
    // Client finds the id of the remote lcm that contains the data she wants
    RemoteLcm lcm = getLCMId();
    // Client disovers after metadata id from the remote lcm
    MetaData md = createStorgaeAndPostMetadata();

    // Sends a request to local lcm to fetch the data and metadata
    postTrigger(lcm.getId(), md.getId(), 200);

    // In the end we should have a task scheduled
    List<TaskDescription> tdList = taskDescriptionService.findAll();
    assertNotNull(tdList);
    assertTrue(tdList.size() > 0);
    assertNotNull(tdList.get(0));
  }

  @Test
  public void testNonExistingLcm() throws ServerException, MissingStorageException,
      BackendException, BackendNotImplementedException, IOException {
    // Client disovers after metadata id from the remote lcm
    MetaData md = createStorgaeAndPostMetadata();
    // Sends a request to local lcm to fetch the data and metadata
    postTrigger("non-existing-lcm", md.getId(), 404);
  }

  @Test
  @Ignore
  public void testNonExistingMetadata() throws ServerException, MissingStorageException,
      BackendException, BackendNotImplementedException, IOException {
    RemoteLcm lcm = getLCMId();
    // Sends a request to local lcm to fetch the data and metadata
    postTrigger(lcm.getId(), "non-existing-metadata", 404);
  }

  private MetaData createStorgaeAndPostMetadata() throws BackendNotImplementedException,
      MissingStorageException, BackendException, IOException, ServerException {
    Storage csvStorage = new Storage();
    csvStorage.setName(CSV_STORAGE_NAME);
    Map options = new HashMap();
    options.put("storagePath", CSV_STORAGE_PATH);
    csvStorage.setOptions(options);

    MetaData metadata = new MetaData();
    metadata.setDataUri(CSV_STORAGE_URI);

    storageService.getStorageDao().save(csvStorage);
    Backend backend = storageService.getBackend(metadata);

    generateCsvTestFile(CSV_FILE);
    Data data = backend.read();
    assertNotNull(data);
    postMeadata(metadata, 200);
    metadata = getMetadata(200).get(0).getItem();
    return metadata;
  }

  private void postMeadata(MetaData metadata, int expected) throws ServerException {
    Entity<MetaData> entity = Entity.entity(metadata, METADATA_CONTENT_TYPE);

    Response resp = getWebTarget().path(METADATA_PATH).request().header(AUTH_USER_HEADER, "admin")
        .header(BasicAuthenticationManager.BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin)
        .post(entity);

    assertEquals(expected, resp.getStatus());
  }

  private void postTrigger(String lcmId, String metadataId, int expected) throws ServerException {
    Response resp = getWebTarget().path(TRIGGER_PATH).path(lcmId).path("metadata").path(metadataId)
        .request().header(AUTH_USER_HEADER, "admin")
        .header(BasicAuthenticationManager.BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin)
        .post(null);
    assertEquals(expected, resp.getStatus());
  }

  private RemoteLcm getLCMId() throws ServerException {
    RemoteLcm lcm = new RemoteLcm();
    lcm.setId("uid" + 0);
    lcm.setUrl(getWebTarget().getUri().toString());
    postLcm(lcm, 200);

    return lcm;
  }

  private void postLcm(RemoteLcm lcm, int expected) throws ServerException {
    Entity<RemoteLcm> entity = Entity.entity(lcm, LCM_CONTENT_TYPE);
    Response resp = getWebTarget().path(LCM_PATH).request().header(AUTH_USER_HEADER, "admin")
        .header(BasicAuthenticationManager.BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin)
        .post(entity);
    assertEquals(expected, resp.getStatus());
  }

  private List<MetaDataRepresentation> getMetadata(int expected) throws ServerException {

    Invocation.Builder req =
        getWebTarget().path(METADATA_PATH).request().header(AUTH_USER_HEADER, "admin")
            .header(BasicAuthenticationManager.BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin);

    Response response = req.get();
    assertEquals(expected, response.getStatus());
    List<MetaDataRepresentation> items =
        response.readEntity(MetaDatasRepresentation.class).getItems();

    return items;
  }

  private void generateCsvTestFile(File f) throws IOException {
    headerLen = 5;
    dataLen = 10;
    try (PrintWriter out = new PrintWriter(f)) {
      for (int i = 0; i < headerLen; i++) {
        if (i < headerLen - 1) {
          out.print("Header" + i + ",");
        } else {
          out.print("Header" + i + "\n");
        }
      }

      for (int i = 0; i < dataLen; i++) {
        for (int j = 0; j < headerLen; j++) {
          if (j < headerLen - 1) {
            out.print(i + "" + j + ",");
          } else {
            out.print(i + "" + j + "\n");
          }
        }
      }
    }
  }

}