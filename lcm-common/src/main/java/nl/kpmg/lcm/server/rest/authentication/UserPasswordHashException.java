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

package nl.kpmg.lcm.server.rest.authentication;

/**
 *
 * @author mhoekstra
 */
public class UserPasswordHashException extends Exception {

  public UserPasswordHashException() {}

  public UserPasswordHashException(String message) {
    super(message);
  }

  public UserPasswordHashException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

  public UserPasswordHashException(Throwable cause) {
    super(cause);
  }

  public UserPasswordHashException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
    super(arg0, arg1, arg2, arg3);
  }
}
