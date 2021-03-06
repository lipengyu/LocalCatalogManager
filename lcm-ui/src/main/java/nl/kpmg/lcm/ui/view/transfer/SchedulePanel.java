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
package nl.kpmg.lcm.ui.view.transfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import nl.kpmg.lcm.client.ClientException;
import nl.kpmg.lcm.rest.types.MetaDataRepresentation;
import nl.kpmg.lcm.rest.types.MetaDatasRepresentation;
import nl.kpmg.lcm.rest.types.RemoteLcmRepresentation;
import nl.kpmg.lcm.rest.types.RemoteLcmsRepresentation;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.data.RemoteLcm;
import nl.kpmg.lcm.server.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.RestClientService;
import nl.kpmg.lcm.ui.view.transfer.components.StartTransferWindow;

import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author shristov
 */
public class SchedulePanel extends CustomComponent {
  private RestClientService restClientService;

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SchedulePanel.class
      .getName());

  private Table remoteMetadataTable;
  private TextArea metadataDetails;
  private ComboBox remoteLcmListComboBox;
  private TextField searchField;
  private Map<String, String> remoteLcmUrlMap = new HashMap();

  public SchedulePanel(RestClientService restClientService) {
    this.restClientService = restClientService;

    HorizontalLayout searchlayout = initSearchLayout(restClientService);
    HorizontalLayout dataLayout = initDataLayout();

    VerticalLayout rootVerticalLayout = new VerticalLayout();
    rootVerticalLayout.addComponent(searchlayout);
    rootVerticalLayout.addComponent(dataLayout);

    HorizontalLayout root = new HorizontalLayout();
    root.addComponent(rootVerticalLayout);
    root.setSpacing(true);
    root.setMargin(true);
    root.setWidth("100%");
    root.setHeight("100%");
    root.setExpandRatio(rootVerticalLayout, 1f);

    setCompositionRoot(root);
  }

  private HorizontalLayout initSearchLayout(RestClientService restClientService1)
      throws UnsupportedOperationException {
    remoteLcmListComboBox = initRemoteLcmListComboBox();

    searchField = new TextField();
    searchField.addStyleName("margin-right-20");
    searchField.addStyleName("width-search-field");
    searchField.setCaption("Search criteria");
    searchField.setInputPrompt("Type search criteria");

    Button searchButton = new Button("Search");
    searchButton.addClickListener(new SearchListener());

    HorizontalLayout searchlayout = new HorizontalLayout();
    searchlayout.addComponent(remoteLcmListComboBox);
    searchlayout.addComponent(searchField);
    searchlayout.addComponent(searchButton);
    searchlayout.setComponentAlignment(searchButton, Alignment.BOTTOM_RIGHT);
    searchlayout.addStyleName("margin-bottom-20");
    return searchlayout;
  }

  private ComboBox initRemoteLcmListComboBox() throws UnsupportedOperationException {
    ComboBox remoteLcmListComboBox = new ComboBox("Remote LCMs");
    RemoteLcmsRepresentation remoteLcms;
    try {
      remoteLcms = restClientService.getRemoteLcm();
      remoteLcmListComboBox.addItem("all");
      remoteLcmListComboBox.setItemCaption("all", "All");

      for (RemoteLcmRepresentation item : remoteLcms.getItems()) {
        RemoteLcm remoteLcm = item.getItem();
        String template = "%s://%s:%s";
        String url =
            String.format(template, remoteLcm.getProtocol(), remoteLcm.getDomain(), remoteLcm
                .getPort().toString());

        remoteLcmUrlMap.put(remoteLcm.getId(), remoteLcm.getName() + " : " + url);
        remoteLcmListComboBox.addItem(remoteLcm.getId());
        remoteLcmListComboBox.setItemCaption(remoteLcm.getId(), remoteLcm.getName() + " : " + url);
        remoteLcmListComboBox.setTextInputAllowed(false);
      }
    } catch (AuthenticationException | ServerException | ClientException ex) {
      LOGGER.error("Unable to load remote LCMs! Message:" + ex.getMessage());
    }
    remoteLcmListComboBox.addStyleName("margin-right-20");
    remoteLcmListComboBox.addStyleName("width-wide-search-field");
    remoteLcmListComboBox.setRequired(true);
    remoteLcmListComboBox.setInputPrompt("Please select one");

    return remoteLcmListComboBox;
  }

  private HorizontalLayout initDataLayout() throws UnsupportedOperationException {
    VerticalLayout tableLayout = new VerticalLayout();
    remoteMetadataTable = initRemoteMetadataTable();
    tableLayout.addComponent(remoteMetadataTable);
    tableLayout.addStyleName("padding-right-20");

    VerticalLayout detailsLayout = new VerticalLayout();
    detailsLayout.setWidth("50%");
    detailsLayout.setHeight("100%");
    metadataDetails = new TextArea();
    metadataDetails.setWidth("100%");
    metadataDetails.setHeight("100%");
    detailsLayout.addComponent(metadataDetails);

    HorizontalLayout dataLayout = new HorizontalLayout();
    dataLayout.addComponent(tableLayout);
    dataLayout.addComponent(metadataDetails);
    dataLayout.setWidth("100%");

    return dataLayout;
  }


  private Table initRemoteMetadataTable() throws UnsupportedOperationException {
    Table table = new Table();
    table.setWidth("100%");
    table.addContainerProperty("Id", String.class, null);
    table.addContainerProperty("Name", String.class, null);
    table.addContainerProperty("Actions", HorizontalLayout.class, null);

    return table;
  }

  private HorizontalLayout createActionsLayout(MetaDataRepresentation item) {

    HorizontalLayout actionsLayout = new HorizontalLayout();

    Button viewButton = new Button("view");
    viewButton.setData(item);
    viewButton.addClickListener((event) -> {
      MetaDataRepresentation data = (MetaDataRepresentation) event.getButton().getData();
      try {
        String json =
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(data.getItem());
        metadataDetails.setValue(json);
      } catch (JsonProcessingException ex) {
        LOGGER.warn("Error:  unable to parse the metadata! Message: " + ex.getMessage());
        metadataDetails.setValue("Error:  unable to parse the metadata!");
      }
    });
    viewButton.addStyleName("link");

    Button transferButton = new Button("transfer");
    transferButton.setData(item);
    transferButton.addClickListener(new ClickListener() {

      @Override
      public void buttonClick(Button.ClickEvent event) {
        MetaDataRepresentation data = (MetaDataRepresentation) event.getButton().getData();
        String id = (String) remoteLcmListComboBox.getValue();
        String url = remoteLcmUrlMap.get(id);
        StartTransferWindow storageCreateWindow =
            new StartTransferWindow(restClientService, id, url, data.getItem().getId(), data
                .getItem().getName());
        UI.getCurrent().addWindow(storageCreateWindow);
      }
    });
    transferButton.addStyleName("link");

    actionsLayout.addComponent(viewButton);
    actionsLayout.addComponent(transferButton);

    return actionsLayout;
  }

  class SearchListener implements Button.ClickListener {
    @Override
    public void buttonClick(Button.ClickEvent event) {
      try {
        String id = (String) remoteLcmListComboBox.getValue();
        MetaDatasRepresentation result = restClientService.getRemoteMetadata(id);
        remoteMetadataTable.removeAllItems();
        if (result != null) {
          for (MetaDataRepresentation item : result.getItems()) {

            MetaDataWrapper metaData = new MetaDataWrapper(item.getItem());
            String json = null;
            try {
              json = new ObjectMapper().writeValueAsString(metaData.getMetaData());
            } catch (JsonProcessingException ex) {
              LOGGER.warn("Unable to convert metatda:  " + metaData.getId() + "Exception : "
                  + ex.getMessage());
              continue;
            }

            String searchText = searchField.getValue().toLowerCase();
            if (searchText.length() == 0 /* empty criteria */
                || (json != null && json.toLowerCase().indexOf(searchText) != -1)) {

              HorizontalLayout actionsLayout = createActionsLayout(item);
              remoteMetadataTable.addItem(new Object[] {metaData.getId(), metaData.getName(),
                  actionsLayout}, metaData.getId());

            }
          }
        }
      } catch (AuthenticationException | ServerException | ClientException ex) {
        LOGGER.error("Unable to load remote LCMs! Message: " + ex.getMessage());
        Notification.show("Unable to load remote LCMs!");
      }
    }
  }
}
