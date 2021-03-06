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

package nl.kpmg.lcm.server.data;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class JsonReaderContentIterator implements ContentIterator {
  private JsonReader reader = null;
  private static final Logger logger = LoggerFactory.getLogger(JsonReaderContentIterator.class.getName());

  public JsonReaderContentIterator(JsonReader reader) throws IOException {
    this.reader = reader;
    this.reader.beginArray();
  }

  @Override
  public boolean hasNext() {
    try {
      return reader.hasNext();
    } catch (IOException ex) {
      logger.warn(String.format("reader.hasNext() threw and exception. %s", ex.getMessage()));
      return false;
    }
  }

  @Override
  public Map next() {
    Gson gson = new Gson();
    try {
      Map next = null;
      if (reader.hasNext()) {
        next = gson.fromJson(reader, Map.class);
      }
      return next;
    } catch (IOException ex) {
      logger.warn(String.format(
          "reader.hasNext() threw and exception while reading next element. %s", ex.getMessage()));
      return null;
    }
  }

  @Override
  public void close() throws IOException {
    reader.endArray();
    reader.close();
  }

  @Override
  public Iterator<Map> iterator() {
    return this;
  }
}
