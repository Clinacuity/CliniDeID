
/*
# © Copyright 2019-2023, Clinacuity Inc. All Rights Reserved.
#
# This file is part of CliniDeID.
# CliniDeID is free software: you can redistribute it and/or modify it under the terms of the
# GNU General Public License as published by the Free Software Foundation,
# either version 3 of the License, or any later version.
# CliniDeID is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
# without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
# PURPOSE. See the GNU General Public License for more details.
# You should have received a copy of the GNU General Public License along with CliniDeID.
# If not, see <https://www.gnu.org/licenses/>.
# =========================================================================   
*/

package com.clinacuity.deid.service.services;

import com.clinacuity.clinideid.entity.CliniDeidLicense;
import com.clinacuity.clinideid.message.DeidClientMessage;
import com.clinacuity.clinideid.message.DeidLevel;
import com.clinacuity.clinideid.message.ProcessingComplete;
import com.clinacuity.deid.mains.DeidPipeline;
import com.clinacuity.deid.service.DeidProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UIMAException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class TestDeidServicePipeline {
    private static final Logger logger = LogManager.getLogger();
    private static final Integer mockServerPort = 9081;
    private static Date mockExpirationDate;
    private static ClientAndServer proxy;
    private static TestDeidProperties properties;

    private ClientAndServer mockServer;

    /**
     * Initializes the test DeidProperties
     */
    @BeforeClass
    public static void setup() {
        proxy = ClientAndServer.startClientAndServer();
        mockExpirationDate = new Date(Calendar.getInstance().getTime().getTime() + 3600000);
    }

    /**
     * Restarts the mock server and resets the proxy
     */
    @Before
    public void startMockServer() {
        if (properties == null) {
            properties = new TestDeidProperties();
            properties.getDeidProperties().getLicense().getValue().setKey(UUID.randomUUID());
        }

        mockServer = startClientAndServer(mockServerPort);
        proxy.reset();
    }

    /**
     * Shuts down the mock after each test
     */
    @After
    public void stopMockServer() {
        if (mockServer == null) {
            logger.error("MOCK SERVER IS NULL, WHAT!");
        } else {
            mockServer.stop();
        }
    }

    /**
     * Tests the service accepts text and de-identifies a name.
     * Does not require a license; this simulates the webapp processing text without a license
     * <p>
     * NOTE: The RNN (python) must be started externally, or else this will cause a Connection Refused exception
     * </p>
     */
    @Ignore
    @Test
    public void runPipelineWithoutLicense() {
        String originalText = "Hello, Caitlin Smith.";
        try {
            DeidServicePipeline pipeline = new DeidServicePipeline(new DeidProperties(), null);
//            pipeline.configure(DeidLevel.beyond, true, false, false, false);
            DeidClientMessage message = new DeidClientMessage();
            message.setOutputResynthesis(true);
            message.setOutputCategoryTags(false);
            message.setOutputGeneralTags(false);
            message.setIsBatch(false);
            message.setDeidLevel(DeidLevel.beyond);
            message.setMessage(originalText);
            String result = pipeline.runPipeline(message);
            if (result.equalsIgnoreCase("error") || result.startsWith("Failed to process text")) {
                fail("Error from running pipeline");
            }
            assertNotEquals(originalText, result);
        } catch (UIMAException e) {
            fail(e.getMessage());
        }
    }

    @Ignore
    @Test
    public void runPipelineWithLicenseNoRnnMiraSvm() throws JsonProcessingException {
        //TODO This needs updated with new license stuff and such from JK
        DeidPipeline.setExcludes("RNN,MIRA,SVM");
        runPipelineWithLicense();
    }

    @Test
    public void runPipelineWithoutLicenseNoRnnMiraSvm() {
        DeidPipeline.setExcludes("RNN,MIRA,SVM");
        runPipelineWithoutLicense();
    }

    /**
     * Tests the service accepts text and de-identifies a name.
     * A license (provided in test) is required; this simulates a client running a request through the API
     * <p>
     * NOTE: The RNN (python) must be started externally, or else this will cause a Connection Refused exception
     * </p>
     */
    // TODO un-ignore after DEID-265
    @Ignore
    @Test
    public void runPipelineWithLicense() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        CliniDeidLicense responseLicense = (CliniDeidLicense) SerializationUtils.clone(properties.getDeidProperties().getLicense().getValue());
        responseLicense.setExpirationDate(mockExpirationDate);
        String requestLicenseJson = mapper.writeValueAsString(properties.getDeidProperties().getLicense().getValue());
        String responseLicenseJson = mapper.writeValueAsString(responseLicense);

        // Sets the expected response for [...]/license/validate
        mockServer
                .when(request()
                        .withPath("(.*)" + properties.getDeidProperties().getApi().getValidateLicense())
                        .withBody(requestLicenseJson)
                        .withHeaders(new Header(CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON_VALUE))
                ).respond(
                response()
                        .withHeaders(new Header(CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON_VALUE))
                        .withBody(responseLicenseJson)
        );

        // TODO create pending request
//        // Sets the expected response for [...]/license/requestFiles
//        mockServer
//                .when(request()
//                        .withPath(properties.getDeidProperties().getApi().getBaseUrl()
//                                + properties.getDeidProperties().getApi().getRequestLicense())
//                ).respond(
//                response()
//                        .withHeaders(new Header(CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON_VALUE))
//                        .withBody(responseLicenseJson)
//        );
//

        // TODO set object ID
        // Sets the expected response for [...]/license/update
        ProcessingComplete processingComplete = new ProcessingComplete();
        processingComplete.setLicenseKey(properties.getDeidProperties().getLicense().getValue().getKey());
//        processingComplete.setOriginalRequestId(pendingRequest.getId());
        processingComplete.setProcessedFiles(1);
        mockServer
                .when(request()
                        .withPath("(.*)" + properties.getDeidProperties().getApi().getUpdateLicense())
                        .withBody(requestLicenseJson)
                        .withHeaders(new Header(CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON_VALUE))
                ).respond(
                response()
                        .withHeaders(new Header(CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON_VALUE))
                        .withBody(responseLicenseJson)
        );

        String originalText = "Hello, Caitlin Smith";
        try {
            DeidServicePipeline pipeline = new DeidServicePipeline(properties.getDeidProperties(), null);
            DeidClientMessage message = new DeidClientMessage();
            message.setOutputResynthesis(true);
            message.setOutputCategoryTags(false);
            message.setOutputGeneralTags(false);
            message.setIsBatch(false);
            message.setDeidLevel(DeidLevel.beyond);
            message.setMessage(originalText);
            String result = pipeline.runPipeline(message, properties.getDeidProperties().getLicense().getValue().getKey());
            if (result.equalsIgnoreCase("error")) {
                fail("Error from running pipeline");
            }
            assertNotEquals(originalText, result);
        } catch (UIMAException e) {
            logger.throwing(e);
            fail(e.getMessage());
        }
    }

    @TestConfiguration
    private static class TestDeidProperties {
        private DeidProperties deidProperties = null;

        @Bean
        public DeidProperties getDeidProperties() {
            if (deidProperties == null) {
                deidProperties = new DeidProperties();
                deidProperties.getApi().setRequestLicense("license/requestFiles");
                deidProperties.getApi().setValidateLicense("license/validate");
                deidProperties.getApi().setUpdateLicense("license/updateRequest");
                deidProperties.getApi().setCompleteRequest("license/completeRequest");
                deidProperties.getApi().setBaseUrl("https://devtest1.clinacuity.com:" /* "http://127.0.0.1:"*/ + mockServerPort.toString() + "/clinacuity/v1/clinideid/");
            }
            return deidProperties;
        }
    }
}
