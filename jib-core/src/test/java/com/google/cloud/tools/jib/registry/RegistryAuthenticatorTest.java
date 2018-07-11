/*
 * Copyright 2018 Google LLC. All rights reserved.
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
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link RegistryAuthenticator}. */
public class RegistryAuthenticatorTest {

  @Test
  public void testFromAuthenticationMethod_bearer()
      throws MalformedURLException, RegistryAuthenticationFailedException {
    RegistryAuthenticator registryAuthenticator =
        RegistryAuthenticator.fromAuthenticationMethod(
            url -> new Connection(url, null),
            "Bearer realm=\"https://somerealm\",service=\"someservice\",scope=\"somescope\"",
            "someimage");
    Assert.assertEquals(
        new URL("https://somerealm?service=someservice&scope=repository:someimage:scope"),
        registryAuthenticator.getAuthenticationUrl("scope"));
  }

  @Test
  public void testFromAuthenticationMethod_basic() throws RegistryAuthenticationFailedException {
    Assert.assertNull(
        RegistryAuthenticator.fromAuthenticationMethod(
            url -> new Connection(url, null),
            "Basic realm=\"https://somerealm\",service=\"someservice\",scope=\"somescope\"",
            "someimage"));

    Assert.assertNull(
        RegistryAuthenticator.fromAuthenticationMethod(
            url -> new Connection(url, null),
            "BASIC realm=\"https://somerealm\",service=\"someservice\",scope=\"somescope\"",
            "someimage"));

    Assert.assertNull(
        RegistryAuthenticator.fromAuthenticationMethod(
            url -> new Connection(url, null),
            "bASIC realm=\"https://somerealm\",service=\"someservice\",scope=\"somescope\"",
            "someimage"));
  }

  @Test
  public void testFromAuthenticationMethod_noBearer() {
    try {
      RegistryAuthenticator.fromAuthenticationMethod(
          url -> new Connection(url, null),
          "realm=\"https://somerealm\",service=\"someservice\",scope=\"somescope\"",
          "someimage");
      Assert.fail("Authentication method without 'Bearer ' or 'Basic ' should fail");

    } catch (RegistryAuthenticationFailedException ex) {
      Assert.assertEquals(
          "Failed to authenticate with the registry because: 'Bearer' was not found in the 'WWW-Authenticate' header, tried to parse: realm=\"https://somerealm\",service=\"someservice\",scope=\"somescope\"",
          ex.getMessage());
    }
  }

  @Test
  public void testFromAuthenticationMethod_noRealm() {
    try {
      RegistryAuthenticator.fromAuthenticationMethod(
          url -> new Connection(url, null), "Bearer scope=\"somescope\"", "someimage");
      Assert.fail("Authentication method without 'realm' should fail");

    } catch (RegistryAuthenticationFailedException ex) {
      Assert.assertEquals(
          "Failed to authenticate with the registry because: 'realm' was not found in the 'WWW-Authenticate' header, tried to parse: Bearer scope=\"somescope\"",
          ex.getMessage());
    }
  }

  @Test
  public void testFromAuthenticationMethod_noService() {
    try {
      RegistryAuthenticator.fromAuthenticationMethod(
          url -> new Connection(url, null), "Bearer realm=\"https://somerealm\"", "someimage");
      Assert.fail("Authentication method without 'service' should fail");

    } catch (RegistryAuthenticationFailedException ex) {
      Assert.assertEquals(
          "Failed to authenticate with the registry because: 'service' was not found in the 'WWW-Authenticate' header, tried to parse: Bearer realm=\"https://somerealm\"",
          ex.getMessage());
    }
  }
}
