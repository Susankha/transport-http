/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.http2.http2forwardedextension;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.util.TestUtil;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

public class Http2ForwardedDefaultTestCase extends Http2ForwardedTestUtil {

    @BeforeClass
    public void setUp() throws InterruptedException {
        super.setUp(getSenderConfigs());
    }

    private SenderConfiguration getSenderConfigs() {
        SenderConfiguration senderConfiguration = new SenderConfiguration();
        senderConfiguration.setHttpVersion("2.0");
        senderConfiguration.setForceHttp2(true);
        return senderConfiguration;
    }

    @Test
    public void testSingleHeader() {
        try {
            HttpCarbonMessage response = send(new DefaultHttpHeaders());
            assertNull(response.getHeader(Constants.FORWARDED));

            response = send(new DefaultHttpHeaders()
                    .set(Constants.FORWARDED, "for=192.0.2.43;by=203.0.113.60;proto=http;host=example.com"));
            assertEquals(response.getHeader(Constants.FORWARDED),
                    "for=192.0.2.43;by=203.0.113.60;proto=http;host=example.com");

            response = send(new DefaultHttpHeaders().set(Constants.X_FORWARDED_FOR, "192.0.2.43, 203.0.113.60"));
            assertEquals(response.getHeader(Constants.X_FORWARDED_FOR), "192.0.2.43, 203.0.113.60");

        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running postTest", e);
        }
    }

    @Test
    public void testMultipleHeaders() {
        try {
            DefaultHttpHeaders headers1 = new DefaultHttpHeaders();
            headers1.set(Constants.FORWARDED, "by=203.0.113.60;proto=http;host=example.com");
            headers1.set(Constants.X_FORWARDED_FOR, "123.34.24.67");

            HttpCarbonMessage response = send(headers1);
            assertEquals(response.getHeader(Constants.FORWARDED), "by=203.0.113.60;proto=http;host=example.com");
            assertEquals(response.getHeader(Constants.X_FORWARDED_FOR), "123.34.24.67");

            DefaultHttpHeaders headers2 = new DefaultHttpHeaders();
            headers2.set(Constants.X_FORWARDED_FOR, "123.34.24.67");
            headers2.set(Constants.X_FORWARDED_HOST, "www.abc.com");

            response = send(headers2);
            assertEquals(response.getHeader(Constants.X_FORWARDED_FOR), "123.34.24.67");
            assertNull(response.getHeader(Constants.X_FORWARDED_BY));
            assertEquals(response.getHeader(Constants.X_FORWARDED_HOST), "www.abc.com");
            assertNull(response.getHeader(Constants.X_FORWARDED_PROTO));

        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running postTest", e);
        }
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {

    }
}
