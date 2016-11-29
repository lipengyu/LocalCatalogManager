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

package nl.kpmg.lcm.server.backend.storage;

import java.util.HashMap;
import java.util.Map;
import nl.kpmg.lcm.server.backend.exception.BackendException;
import nl.kpmg.lcm.server.data.Storage;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class CsvStorageTest {
    
 @Test
  public void testCreateStorage() throws BackendException {
    Storage correctStorage = new Storage();
    correctStorage.setName("csv-sotrage");
    Map options = new HashMap();
    options.put("storagePath", "/tmp");
    correctStorage.setOptions(options);

    CsvStorage hiveStorage = new CsvStorage(correctStorage);
    Assert.assertNotNull(hiveStorage);
  }
  
   @Test(expected = BackendException.class)
  public void testValidateStorageMissingStoragePath() throws BackendException {
    Storage incrrectStorage = new Storage();
    incrrectStorage.setName("csv-sotrage");
    Map options = new HashMap();
    incrrectStorage.setOptions(options);

    new CsvStorage(incrrectStorage);
  }
}
