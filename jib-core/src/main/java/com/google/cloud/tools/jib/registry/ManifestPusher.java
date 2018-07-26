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

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpStatusCodes;
import com.google.cloud.tools.jib.http.BlobHttpContent;
import com.google.cloud.tools.jib.http.Response;
import com.google.cloud.tools.jib.image.json.BuildableManifestTemplate;
import com.google.cloud.tools.jib.json.JsonTemplateMapper;
import com.google.cloud.tools.jib.registry.json.ErrorEntryTemplate;
import com.google.cloud.tools.jib.registry.json.ErrorResponseTemplate;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

/** Pushes an image's manifest. */
class ManifestPusher implements RegistryEndpointProvider<Void> {
  /**
   * @see <a
   *     href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/415">https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/415</a>
   */
  @VisibleForTesting static final int STATUS_CODE_INVALID_MEDIA_TYPE = 415;

  private final RegistryEndpointRequestProperties registryEndpointRequestProperties;
  private final BuildableManifestTemplate manifestTemplate;
  private final String imageTag;

  ManifestPusher(
      RegistryEndpointRequestProperties registryEndpointRequestProperties,
      BuildableManifestTemplate manifestTemplate,
      String imageTag) {
    this.registryEndpointRequestProperties = registryEndpointRequestProperties;
    this.manifestTemplate = manifestTemplate;
    this.imageTag = imageTag;
  }

  @Override
  public BlobHttpContent getContent() {
    return new BlobHttpContent(
        JsonTemplateMapper.toBlob(manifestTemplate), manifestTemplate.getManifestMediaType());
  }

  @Override
  public List<String> getAccept() {
    return Collections.emptyList();
  }

  @Override
  public Void handleResponse(Response response) {
    return null;
  }

  @Override
  public Void handleHttpResponseException(HttpResponseException httpResponseException)
      throws HttpResponseException, RegistryErrorException {

    // quay.io returns 415 for v2-2 manifests (not a documented status code)
    switch (httpResponseException.getStatusCode()) {
      case HttpStatusCodes.STATUS_CODE_BAD_REQUEST:
      case HttpStatusCodes.STATUS_CODE_NOT_FOUND:
      case HttpStatusCodes.STATUS_CODE_METHOD_NOT_ALLOWED:
      case STATUS_CODE_INVALID_MEDIA_TYPE:
        // The name or reference was invalid.
        ErrorResponseTemplate errorResponse = null;
        try {
          errorResponse =
              JsonTemplateMapper.readJson(
                  httpResponseException.getContent(), ErrorResponseTemplate.class);
        } catch (IOException ex) {
          // cannot parse the response content, so just rethrow
          throw httpResponseException;
        }
        RegistryErrorExceptionBuilder registryErrorExceptionBuilder =
            new RegistryErrorExceptionBuilder(getActionDescription(), httpResponseException);
        for (ErrorEntryTemplate errorEntry : errorResponse.getErrors()) {
          registryErrorExceptionBuilder.addReason(errorEntry);
        }

        throw registryErrorExceptionBuilder.build();

      default:
        return null;
    }
  }

  @Override
  public URL getApiRoute(String apiRouteBase) throws MalformedURLException {
    return new URL(
        apiRouteBase + registryEndpointRequestProperties.getImageName() + "/manifests/" + imageTag);
  }

  @Override
  public String getHttpMethod() {
    return HttpMethods.PUT;
  }

  @Override
  public String getActionDescription() {
    return "push image manifest for "
        + registryEndpointRequestProperties.getServerUrl()
        + "/"
        + registryEndpointRequestProperties.getImageName()
        + ":"
        + imageTag;
  }
}
