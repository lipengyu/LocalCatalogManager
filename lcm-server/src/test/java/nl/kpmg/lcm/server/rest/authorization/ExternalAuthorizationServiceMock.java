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
package nl.kpmg.lcm.server.rest.authorization;

import nl.kpmg.lcm.server.rest.authentication.Roles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author shristov
 */
public   class ExternalAuthorizationServiceMock extends AbstractAutorizationService {
  private static String resourceNameEmptyPermissions = PermissionCheckerTest.class.getName() + ".testExternalCheckUnauthorized";
  private static String resourceNameCorrectPermissions = PermissionCheckerTest.class.getName() + ".testExternalCheck";

    @Override
    protected Map<String, List<String>> loadAuthorizationMap() {
      Map<String, List<String>> map = new HashMap();
      List permissionList1 = new ArrayList();
      permissionList1.add(Roles.API_USER);
      permissionList1.add(Roles.ADMINISTRATOR);
      map.put(resourceNameEmptyPermissions, new ArrayList());
      map.put(resourceNameCorrectPermissions, permissionList1);

      return map;
    }
  }
