/*
 * Copyright 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.tools.jib.registry.json;

import com.google.cloud.tools.jib.json.JsonTemplateMapper;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

/** */
public class ErrorResponseTemplateTest {

  @Test
  public void testSimpleError() throws IOException {
    String content =
        "{\"errors\":[{\"code\":\"MANIFEST_INVALID\",\"message\":\"manifest invalid\"}]}";
    ErrorResponseTemplate errorResponse =
        JsonTemplateMapper.readJson(content, ErrorResponseTemplate.class);
    Assert.assertNotNull(errorResponse.getErrors());
    Assert.assertEquals(1, errorResponse.getErrors().size());
    ErrorEntryTemplate error = errorResponse.getErrors().get(0);
    Assert.assertEquals("MANIFEST_INVALID", error.getCode());
    Assert.assertEquals("manifest invalid", error.getMessage());
    Assert.assertNull(error.getDetail());
  }

  @Test
  public void testMultipleErrors() throws IOException {
    // synthetic example
    String content =
        "{\"errors\":["
            + "{\"code\":\"MANIFEST_INVALID\",\"message\":\"manifest invalid\"},"
            + "{\"code\":\"INVALID_REQUEST\",\"detail\":{},\"message\":\"Invalid request\"}"
            + "]}";
    ErrorResponseTemplate errorResponse =
        JsonTemplateMapper.readJson(content, ErrorResponseTemplate.class);
    Assert.assertNotNull(errorResponse.getErrors());
    Assert.assertEquals(2, errorResponse.getErrors().size());
    {
      ErrorEntryTemplate error = errorResponse.getErrors().get(0);
      Assert.assertEquals("MANIFEST_INVALID", error.getCode());
      Assert.assertEquals("manifest invalid", error.getMessage());
      Assert.assertNull(error.getDetail());
    }
    {
      ErrorEntryTemplate error = errorResponse.getErrors().get(1);
      Assert.assertEquals("INVALID_REQUEST", error.getCode());
      Assert.assertEquals("Invalid request", error.getMessage());
      Assert.assertNotNull(error.getDetail());
      Assert.assertTrue(error.getDetail().isEmpty());
    }
  }

  @Test
  public void testEmptyDetail() throws IOException {
    // seen on https://github.com/GoogleContainerTools/jib/issues/534
    String content =
        "{\"errors\":[{\"code\":\"MANIFEST_INVALID\",\"message\":\"manifest invalid\",\"detail\":{}}]}";
    ErrorResponseTemplate errorResponse =
        JsonTemplateMapper.readJson(content, ErrorResponseTemplate.class);
    Assert.assertNotNull(errorResponse.getErrors());
    Assert.assertEquals(1, errorResponse.getErrors().size());
    ErrorEntryTemplate error = errorResponse.getErrors().get(0);
    Assert.assertEquals("MANIFEST_INVALID", error.getCode());
    Assert.assertEquals("manifest invalid", error.getMessage());
    Assert.assertNotNull(error.getDetail());
    Assert.assertTrue(error.getDetail().isEmpty());
  }

  @Test
  public void testDetailWithMessage() throws IOException {
    // seen on https://github.com/GoogleContainerTools/jib/issues/698
    String content =
        "{\"errors\":[{\"code\":\"MANIFEST_INVALID\",\"detail\":{\"message\":\"manifest schema version not supported\"},\"message\":\"manifest invalid\"}]}";
    ErrorResponseTemplate errorResponse =
        JsonTemplateMapper.readJson(content, ErrorResponseTemplate.class);
    Assert.assertNotNull(errorResponse.getErrors());
    Assert.assertEquals(1, errorResponse.getErrors().size());
    ErrorEntryTemplate error = errorResponse.getErrors().get(0);
    Assert.assertEquals("MANIFEST_INVALID", error.getCode());
    Assert.assertEquals("manifest invalid", error.getMessage());
    Assert.assertNotNull(error.getDetail());
    Assert.assertEquals(1, error.getDetail().size());
    Assert.assertTrue(error.getDetail().containsKey("message"));
    Assert.assertEquals("manifest schema version not supported", error.getDetail().get("message"));
  }

  @Test
  public void testDetailWithDescription() throws IOException {
    // seen on https://github.com/GoogleContainerTools/jib/issues/534
    String content =
        " {\"errors\":[{\"code\":\"MANIFEST_INVALID\",\"message\":\"manifest invalid\",\"detail\":{\"description\":\"null\"}}]}";
    ErrorResponseTemplate errorResponse =
        JsonTemplateMapper.readJson(content, ErrorResponseTemplate.class);
    Assert.assertNotNull(errorResponse.getErrors());
    Assert.assertEquals(1, errorResponse.getErrors().size());
    ErrorEntryTemplate error = errorResponse.getErrors().get(0);
    Assert.assertEquals("MANIFEST_INVALID", error.getCode());
    Assert.assertEquals("manifest invalid", error.getMessage());
    Assert.assertNotNull(error.getDetail());
    Assert.assertEquals(1, error.getDetail().size());
    Assert.assertTrue(error.getDetail().containsKey("description"));
    Assert.assertEquals("null", error.getDetail().get("description"));
  }
}
