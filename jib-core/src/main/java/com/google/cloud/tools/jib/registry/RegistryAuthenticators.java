/*
 * Copyright 2017 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.tools.jib.registry;

import com.google.cloud.tools.jib.http.Connection;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Function;
import javax.annotation.Nullable;

/** Static initializers for {@link RegistryAuthenticator}. */
public abstract class RegistryAuthenticators {

  /**
   * Gets a {@link RegistryAuthenticator} for a custom registry server and repository.
   *
   * @param serverUrl the server URL for the registry (for example, {@code gcr.io})
   * @param repository the image/repository name (also known as, namespace)
   * @return the {@link RegistryAuthenticator} to authenticate pulls/pushes with the registry, or
   *     {@code null} if no token authentication is necessary
   * @throws RegistryAuthenticationFailedException if failed to create the registry authenticator
   * @throws IOException if communicating with the endpoint fails
   * @throws RegistryException if communicating with the endpoint fails
   */
  @Nullable
  public static RegistryAuthenticator forOther(
      Function<URL, Connection> connectionFactory, String serverUrl, String repository)
      throws RegistryAuthenticationFailedException, IOException, RegistryException {
    try {
      return RegistryClient.factory(connectionFactory, serverUrl, repository)
          .newRegistryClient()
          .getRegistryAuthenticator();

    } catch (MalformedURLException ex) {
      throw new RegistryAuthenticationFailedException(ex);

    } catch (InsecureRegistryException ex) {
      // HTTP is not allowed, so just return null.
      return null;
    }
  }
}
