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

package nl.kpmg.lcm.ui.view;

import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import nl.kpmg.lcm.client.ClientException;
import nl.kpmg.lcm.rest.types.TaskDescriptionsRepresentation;
import nl.kpmg.lcm.rest.types.TaskScheduleRepresentation;
import nl.kpmg.lcm.rest.types.UserGroupsRepresentation;
import nl.kpmg.lcm.rest.types.UsersRepresentation;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.RestClientService;
import nl.kpmg.lcm.ui.view.administration.RemoteLcmPanel;
import nl.kpmg.lcm.ui.view.administration.StoragePanel;
import nl.kpmg.lcm.ui.view.administration.TasksPanel;
import nl.kpmg.lcm.ui.view.administration.UsersPanel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


/****
 *
 * @author mhoekstra
 */
@Component
@SpringView(name = AdministrationViewImpl.VIEW_NAME)
public class AdministrationViewImpl extends VerticalLayout implements AdministrationView {

  /**
   * The linkable name of this view.
   */
  public static final String VIEW_NAME = "administration";

  /**
   * The service for interacting with the backend.
   */
  @Autowired
  private RestClientService restClientService;

  /**
   * The auto wired main UI component.
   */
  @Autowired
  private UI ui;

  private final TabSheet tabsheet = new TabSheet();

  private UserGroupsRepresentation userGroups;
  private UsersRepresentation users;
  private TaskScheduleRepresentation taskSchedule;
  private TaskDescriptionsRepresentation tasks;

  private TasksPanel tasksPanel;
  private StoragePanel storagePanel;
  private RemoteLcmPanel remoteLcmPanel;
  private UsersPanel usersPanel;

  /**
   * Builds the interface.
   */
  @PostConstruct
  public final void init() {
    HorizontalLayout root = new HorizontalLayout();
    root.setWidth("100%");
    root.setMargin(true);
    root.setSpacing(true);

    storagePanel = new StoragePanel(restClientService);
    storagePanel.setHeight("100%");
    tabsheet.addTab(storagePanel, "Storage");

    remoteLcmPanel = new RemoteLcmPanel(restClientService);
    tabsheet.addTab(remoteLcmPanel, "Remote LCM");

    tasksPanel = new TasksPanel();
    tabsheet.addTab(tasksPanel, "Tasks");

    usersPanel = new UsersPanel();
    tabsheet.addTab(usersPanel, "Users");

    tabsheet.setHeight("100%");
    root.addComponent(tabsheet);

    addComponent(root);
  }

  /**
   * Loads the data on presentation.
   *
   * @param event fired when the view is entered.
   */
  @Override
  public final void enter(final ViewChangeListener.ViewChangeEvent event) {
    try {
      tasks = restClientService.getTasks();
      taskSchedule = restClientService.getTaskSchedule();
      users = restClientService.getUsers();
      userGroups = restClientService.getUserGroups();

      tasksPanel.setTasks(tasks);
      tasksPanel.setTaskSchedule(taskSchedule);

      usersPanel.setUsers(users);
      usersPanel.setUserGroups(userGroups);
    } catch (AuthenticationException ex) {
      getUI().getNavigator().navigateTo("");
    } catch (ServerException se) {
      Notification.show("Cannot instantiate client HTTPS endpoint");
      getUI().getNavigator().navigateTo("");
    } catch (ClientException ex) {
      Notification.show("Couldn't fetch remote data.");
    }
  }
}
