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

package nl.kpmg.lcm.server.backend;

import nl.kpmg.lcm.server.backend.exception.BackendException;
import nl.kpmg.lcm.server.backend.exception.BadMetaDataException;
import nl.kpmg.lcm.server.backend.exception.DataSourceValidationException;
import nl.kpmg.lcm.server.backend.metatadata.CsvMetaData;
import nl.kpmg.lcm.server.backend.storage.CsvStorage;
import nl.kpmg.lcm.server.data.ContentIterator;
import nl.kpmg.lcm.server.data.Data;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.validation.Notification;

import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.csv.CsvDataContext;
import org.apache.metamodel.csv.CsvWriter;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.FileHelper;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mhoekstra
 */
@BackendSource(type = "csv")
public class BackendCsvImpl extends AbstractBackend {

  private static final Logger logger = Logger.getLogger(BackendCsvImpl.class.getName());
  private File dataSourceFile = null;
  private final CsvMetaData csvMetaData;

  /**
   *
   * @param backendStorage valid storage. This storage name must be extracted from @metaData object
   *        and then the storage object loaded be loaded .
   * @param metaData - valid @metaData that representing CSV data source.
   * @throws BadMetaDataException when the @metaData is null or it is not consistent.
   * @throws nl.kpmg.lcm.server.backend.exception.DataSourceValidationException
   */
  public BackendCsvImpl(Storage backendStorage, MetaData metaData)
      throws DataSourceValidationException, BackendException {
    super(metaData);
    String storagePath = new CsvStorage(backendStorage).getStoragePath();
    dataSourceFile = createDataSourceFile(storagePath);
    this.csvMetaData = new CsvMetaData(metaData);
  }

  @Override
  protected String getSupportedUriSchema() {
    return "csv";
  }

  private UpdateableDataContext createDataContext() throws BackendException {
    if (metaData == null) {
      throw new IllegalStateException("MetaData parameter could not be null");
    }

    CsvConfiguration csvConfiguration = csvMetaData.getConfiguration();

    if (!dataSourceFile.exists()) {
      throw new DataSourceValidationException(
          "Unable to find data source file! FilePath" + dataSourceFile.getPath());
    }
    return (CsvDataContext) DataContextFactory.createCsvDataContext(dataSourceFile,
        csvConfiguration);
  }


  private File createDataSourceFile(String storagePath) throws DataSourceValidationException {
    if (dataSourceFile != null) {
      return dataSourceFile;
    }

    File baseDir = new File(storagePath);
    URI dataUri = getDataUri();

    String filePath = dataUri.getPath();
    dataSourceFile = new File(storagePath + filePath);

    Notification notification = new Notification();
    FilePathValidator.validate(baseDir, dataSourceFile, notification);

    if (notification.hasErrors()) {
      throw new DataSourceValidationException(notification.errorMessage());
    }

    return dataSourceFile;
  }

  @Override
  public DataSetInformation gatherDataSetInformation() throws BackendException {
    DataSetInformation info = new DataSetInformation();
    try {
      info.setUri(dataSourceFile.getCanonicalPath());
      info.setAttached(dataSourceFile.exists());
      if (dataSourceFile.exists()) {

        info.setByteSize(dataSourceFile.length());
        info.setModificationTime(new Date(dataSourceFile.lastModified()));
      }
    } catch (IOException ex) {
      logger.log(Level.SEVERE, "Unable to get info about datasource: " + dataSourceFile.getPath(),
          ex);
      throw new BackendException(ex);
    }

    return info;
  }

  /**
   * Method to store some content on a data storage backend.
   *
   * @param content {@link ContentIterator} that should be stored.
   * @param forceOverwrite - indicates how to proceed if the content already exists - in case of
   *        true the content is written no matter if already persists or not - in case it is set to
   *        false then the content is written only when it doesn't exist - in case it is set to
   *        false and the content already exists then BackendExceptionis thrown
   * @throws BackendException if - the URI is not valid or it is not possible to reach the storage.
   *         - @forceUpdateIfExists is false and the content already exists.
   */
  @Override
  public void store(ContentIterator content, DataTransformationSettings transformationSettings,
      boolean forceOverwrite) throws BackendException {
    DataSetInformation dataSetInformation = gatherDataSetInformation();
    if (dataSetInformation.isAttached() && !forceOverwrite) {
      throw new BackendException("Data set is already attached, won't overwrite.");
    }

    try (Writer writer = FileHelper.getBufferedWriter(dataSourceFile);) {

      CsvConfiguration configuration = csvMetaData.getConfiguration();
      CsvWriter csvWriter = new CsvWriter(configuration);
      int rowNumber = 1;
      while (content.hasNext()) {

        Map row = content.next();

        if (rowNumber == configuration.getColumnNameLineNumber()) {
          Object[] lineAsObjectValues = (Object[]) row.keySet().toArray(new Object[] {});
          String[] lineAsStringValues = toStringArray(lineAsObjectValues);
          String columnLine = csvWriter.buildLine(lineAsStringValues);
          writer.write(columnLine);
          rowNumber++;
        }

        Object[] lineAsObjectValues = (Object[]) row.values().toArray(new Object[] {});
        String[] lineAsStringValues = toStringArray(lineAsObjectValues);
        String line = csvWriter.buildLine(lineAsStringValues);
        writer.write(line);
        rowNumber++;
      }

      writer.flush();
    } catch (IOException ex) {
      logger.log(Level.SEVERE, "Error occured during saving information!", ex);
    }
  }

  private String[] toStringArray(Object[] lineAsObjectValues) {
    String[] lineAsStringValues = new String[lineAsObjectValues.length];
    for (int i = 0; i < lineAsObjectValues.length; i++) {
      lineAsStringValues[i] = lineAsObjectValues[i].toString();
    }
    return lineAsStringValues;
  }

  /**
   * Method to read some content from a data storage backend.
   *
   * @return {@link DataSet} with all the data specified in the @metaData object passed during
   *         initialization.
   * @throws BackendException if the URI is not valid or it is not possible to reach the storage.
   */
  @Override
  public Data read() throws BackendException {
    UpdateableDataContext dataContext = createDataContext();
    Schema schema = dataContext.getDefaultSchema();
    if (schema.getTableCount() == 0) {
      return null;
    }
    Table table = schema.getTables()[0];
    DataSet result = dataContext.query().from(table).selectAll().execute();
    csvMetaData.addColumnsDescription(table.getColumns());
    return new Data(csvMetaData.getMetaData(), new DataSetContentIterator(result));
  }

  @Override
  public boolean delete() throws BackendException {
    throw new UnsupportedOperationException("Not supported yet."); // To change body of generated
                                                                   // methods, choose Tools |
                                                                   // Templates.
  }

  @Override
  protected void extraValidation(MetaData metaData, Notification notification) {


  }

  @Override
  public void free() {

  }

}
