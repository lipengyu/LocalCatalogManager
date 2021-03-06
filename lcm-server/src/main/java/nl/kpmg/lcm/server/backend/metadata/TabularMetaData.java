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

import nl.kpmg.lcm.server.data.metadata.MetaData;
import nl.kpmg.lcm.server.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.data.metadata.Wrapper;
import nl.kpmg.lcm.server.exception.LcmValidationException;
import nl.kpmg.lcm.validation.Notification;

/**
 *
 * @author shristov
 */
@Wrapper
public class TabularMetaData extends MetaDataWrapper {

  private TableDescriptionDescriptor tableDescription;
  private TableConfigurationDescriptor tableConfiguration;

  public TabularMetaData(MetaData metaData) {
    super(metaData);
    tableDescription = new TableDescriptionDescriptor(metaData);
    tableConfiguration = new TableConfigurationDescriptor(metaData);

    Notification notification = new Notification();
    tableDescription.validate(notification);
    tableConfiguration.validate(notification);

    if (notification.hasErrors()) {
      throw new LcmValidationException(notification);
    }
  }

  public TabularMetaData() {
    super();
    tableDescription = new TableDescriptionDescriptor(metaData);
    tableConfiguration = new TableConfigurationDescriptor(metaData);
  }

  public final void setTableDescription(final TableDescriptionDescriptor value) {
    metaData.set(value.getSectionName(), value.getMap());
  }

  public final TableDescriptionDescriptor getTableDescription() {
    return tableDescription;
  }

  public final void setTableConfiguration(final TableConfigurationDescriptor value) {
    metaData.set(value.getSectionName(), value.getMap());
  }

  public final TableConfigurationDescriptor getTableConfiguration() {
    return tableConfiguration;
  }

}
