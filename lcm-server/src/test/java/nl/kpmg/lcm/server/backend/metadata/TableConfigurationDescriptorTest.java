/*
  * Copyright 2017 KPMG N.V. (unless otherwise stated).
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
package nl.kpmg.lcm.server.backend.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import nl.kpmg.lcm.server.data.metadata.MetaData;
import nl.kpmg.lcm.validation.Notification;

import org.apache.metamodel.util.FileHelper;
import org.junit.Test;

import java.util.Map;

/**
 *
 * @author shristov
 */
public class TableConfigurationDescriptorTest {
  @Test
  public void testConstruction() {
    String encoding = FileHelper.UTF_16_ENCODING;

    MetaData metaData = new MetaData();
    TableConfigurationDescriptor tableConfiguration = new TableConfigurationDescriptor(metaData);

    tableConfiguration.setEncoding(encoding);
    assertEquals(encoding, tableConfiguration.getEncoding());

    Map map = metaData.get(tableConfiguration.getSectionName());
    assertNotNull(map);
    assertEquals(1, map.size());
  }

  @Test
  public void testBlankConstruction() {

    MetaData metaData = new MetaData();
    TableConfigurationDescriptor tableConfiguration = new TableConfigurationDescriptor(metaData);

    assertNull(tableConfiguration.getEncoding());
  }

  @Test
  public void testValidate() {
    String encoding = FileHelper.UTF_16_ENCODING;

    MetaData metaData = new MetaData();
    TableConfigurationDescriptor tableConfiguration = new TableConfigurationDescriptor(metaData);

    tableConfiguration.setEncoding(encoding);
    Notification notification = new Notification();
    tableConfiguration.validate(notification);
    assertFalse(notification.hasErrors());
  }
}