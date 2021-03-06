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

package nl.kpmg.lcm.server.task.enrichment;

import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.backend.DataSetInformation;
import nl.kpmg.lcm.server.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.data.service.StorageService;
import nl.kpmg.lcm.server.exception.LcmException;
import nl.kpmg.lcm.server.exception.LcmValidationException;
import nl.kpmg.lcm.server.task.EnrichmentTask;
import nl.kpmg.lcm.server.task.TaskException;
import nl.kpmg.lcm.server.task.TaskResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Fills data on a MetaData object concerning the described Data.
 *
 * <p>
 * MetaData describes data. This Task will check through the appropriate backend if the data in
 * question is actually attached and/or readable from the LCM. This will update and overwrite the
 * specific piece of MetaData. The data added contains: - state : DETACHED | ATTACHED - readable :
 * UNREADABLE | READABLE - size : byte-size - update-timestamp : date
 * </p>
 * <p>
 * this will be set on the following path:
 * </p>
 * <p>
 * { "dynamic": { "data" { "state" "readable" "size" "update-timestamp" } } }
 * </p>
 *
 * @author mhoekstra
 */
public class DataEnrichmentTask extends EnrichmentTask {

  private final Logger LOGGER = LoggerFactory.getLogger(DataEnrichmentTask.class.getName());
  /**
   * The BackendService.
   */
  @Autowired
  private StorageService storageService;

  /**
   * Will store information on the data associated with a piece of MetaData.
   *
   * @param metadata the metadata to enrich
   * @param options - any option that could be useful during task execution
   * @return the result of the task
   * @throws TaskException if the backend fails
   */
  @Override
  protected final TaskResult execute(final MetaDataWrapper metadataWrapper, final Map options)
      throws TaskException {
    try {
      Backend backend = storageService.getBackend(metadataWrapper);
      if (backend == null) {
        return TaskResult.FAILURE;
      }

      DataSetInformation gatherDataSetInformation = backend.gatherDataSetInformation();

      if (!gatherDataSetInformation.isAttached()) {
        metadataWrapper.getDynamicData().setState("DETACHED");
        return TaskResult.SUCCESS;
      }
      metadataWrapper.getDynamicData().setState("ATTACHED");

      if (!gatherDataSetInformation.isReadable()) {
        metadataWrapper.getDynamicData().setReadable("UNREADABLE");
        return TaskResult.SUCCESS;
      }
      metadataWrapper.getDynamicData().setReadable("READABLE");
      metadataWrapper.getDynamicData().setSize(gatherDataSetInformation.getByteSize());
      metadataWrapper.getDynamicData().setUpdateTimestamp(gatherDataSetInformation.getModificationTime().toString());

      metaDataService.update(metadataWrapper.getMetaData().getId(), metadataWrapper.getMetaData());

      return TaskResult.SUCCESS;
    } catch (LcmValidationException ex) {
      LOGGER.warn( ex.getNotification().errorMessage());
      return TaskResult.FAILURE;
    } catch ( LcmException ex) {
      LOGGER.warn( ex.getMessage(), ex);
      return TaskResult.FAILURE;
    }
  }
}
