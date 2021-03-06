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
import nl.kpmg.lcm.validation.Notification;

/**
 *
 * @author shristov
 */
public class CsvConfigurationDescriptor extends TableConfigurationDescriptor {

  public CsvConfigurationDescriptor(MetaData metaData) {
    super(metaData);
  }

  public final Integer getColumnNameLine() {
    return get("column-name-line");
  }

  public final void setColumnNameLine(final Integer columnNameLine) {
    set("column-name-line", columnNameLine);
  }

  public final Character getSeparatorChar() {
    return get("separator-char");
  }

  public final void setSeparatorChar(final Character separatorChar) {
    set("separator-char", separatorChar);
  }

  public final Character getQuoteChar() {
    return get("quote-char");
  }

  public final void setQuoteChar(final Character quoteChar) {
    set("quote-char", quoteChar);
  }

  public final Character getEscapeChar() {
    return get("escape-char");
  }

  public final void setEscapeChar(final Character escapeChar) {
    set("escape-char", escapeChar);
  }

  @Override
  public void validate(Notification notification) {
    super.validate(notification);
    if (getMap().get("column-name-line") != null) {
      try {
        Integer columnNameValue = get("column-name-line");
      } catch (ClassCastException cce) {
        notification.addError("\"column-name-line\" property does not have numeric value", cce);
      }
    }

    if (getMap().get("separator-char") != null) {
      try {
        Character separatorChar = get("separator-char");
      } catch (ClassCastException cce) {
        notification.addError("\"separator-char\" property is not a char", cce);
      }
    }

    if (getMap().get("quote-char") != null) {
      try {
        Character quoteChar = get("quote-char");
      } catch (ClassCastException cce) {
        notification.addError("\"quote-char\" property is not a char", cce);
      }
    }

    if (getMap().get("escape-char") != null) {
      try {
        Character escapeChar = get("escape-char");
      } catch (ClassCastException cce) {
        notification.addError("\"escape-char\" property is not a char", cce);
      }
    }
  }

}
