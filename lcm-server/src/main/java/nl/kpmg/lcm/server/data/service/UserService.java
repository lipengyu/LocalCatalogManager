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

package nl.kpmg.lcm.server.data.service;

import jersey.repackaged.com.google.common.collect.Lists;

import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.data.dao.UserDao;
import nl.kpmg.lcm.server.rest.authentication.UserPasswordHashException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * @author mhoekstra
 */
@Service
public class UserService {

  @Autowired
  private UserDao userDao;

  public List<User> findAll() {
    return Lists.newLinkedList(userDao.findAll());
  }

  public UserDao getUserDao() {
    return userDao;
  }

  public void updateUser(String userId, User modifiedUser) throws UserPasswordHashException {
    User user = userDao.findOne(userId);

    if (modifiedUser.getName() != null) {
      user.setName(modifiedUser.getName());
    }

    if (modifiedUser.getNewPassword() != null) {
      user.setNewPassword(modifiedUser.getNewPassword());
    }
    if (modifiedUser.getRole() != null) {
      user.setRole(modifiedUser.getRole());
    }

    userDao.save(user);
  }
}
