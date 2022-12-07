
/*
# © Copyright 2019-2022, Clinacuity Inc. All Rights Reserved.
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

package com.clinacuity.deid.service;

import com.clinacuity.base.BaseResource;
import com.clinacuity.clinideid.entity.CliniDeidLicense;
import com.clinacuity.clinideid.entity.PendingRequest;
import com.clinacuity.clinideid.entity.processing.DeidBatchProcessingFile;
import com.clinacuity.clinideid.enums.BatchUploadStatus;
import com.clinacuity.clinideid.message.ProcessingComplete;
import com.clinacuity.deid.outputAnnotators.DocumentListAnnotator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Calendar;
import java.util.UUID;

@ConfigurationProperties("clinacuity.deid")
@SuppressWarnings({"unused"})
public class DeidProperties {
    private String ec2InstanceName = "dev-4";

    private long ec2TimeOut = 3600000;

    private boolean isDemo = false;

    private boolean isAutoScaled = false;

    private String instanceId = "";

    private long lastExecution;

    private boolean busy = false;

    private ClinacuityApi api = new ClinacuityApi();

    private License license = new License();

    private AwsProperties awsProperties = new AwsProperties();

    public String getEc2InstanceName() {
        return ec2InstanceName;
    }

    public void setEc2InstanceName(String ec2InstanceName) {
        this.ec2InstanceName = ec2InstanceName;
    }

    public long getEc2TimeOut() {
        return ec2TimeOut;
    }

    public void setEc2TimeOut(long ec2TimeOut) {
        this.ec2TimeOut = ec2TimeOut;
    }

    public boolean isDemo() {
        return isDemo;
    }

    public void setDemo(boolean demo) {
        isDemo = demo;
    }

    public boolean isAutoScaled() {
        return isAutoScaled;
    }

    public void setAutoScaled(boolean autoScaled) {
        isAutoScaled = autoScaled;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public long getLastExecution() {
        return lastExecution;
    }

    public void setLastExecution(long lastExecution) {
        this.lastExecution = lastExecution;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public ClinacuityApi getApi() {
        return api;
    }

    public void setApi(ClinacuityApi api) {
        this.api = api;
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    public AwsProperties getAwsProperties() {
        return awsProperties;
    }

    public void setAwsProperties(AwsProperties awsProperties) {
        this.awsProperties = awsProperties;
    }

    public boolean validateLicense() {
        license.validate(api);
        return license.canProcessMoreFiles() && license.isLicenseUnexpired();
    }

    public int requestProcess(int amountToProcess) {
        license.request(api, amountToProcess);
        return getLicense().pendingRequest.getAmountToProcess();
    }

    public void updateLicense() {
        license.update(api);
    }

    public void completeRequest() {
        license.completeRequest(api);
    }

    public static class License {
        static final Logger logger = LogManager.getLogger();
        static final HttpHeaders headers = new HttpHeaders();

        private CliniDeidLicense licenseValue = new CliniDeidLicense();

        private PendingRequest pendingRequest = new PendingRequest();

        private int filesProcessed = 0;

        public License() {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }

        public CliniDeidLicense getValue() {
            return licenseValue;
        }

        public int getFilesProcessed() {
            return filesProcessed;
        }

        public void setFilesProcessed(int filesProcessed) {
            this.filesProcessed = filesProcessed;
        }

        boolean canProcessMoreFiles() {
            int limit = licenseValue.getFileLimit();
            return limit > 0;
        }

        boolean isLicenseUnexpired() {
            return licenseValue.getExpirationDate().after(Calendar.getInstance().getTime());
        }

        private void validate(ClinacuityApi api) {
            ObjectMapper mapper = new ObjectMapper();
            CliniDeidLicense licenseRequest = new CliniDeidLicense();

            licenseRequest.setKey(licenseValue.getKey());
            String url = api.baseUrl + api.validateLicense;

            HttpEntity<?> request;
            try {
                request = new HttpEntity<>(mapper.writeValueAsString(licenseRequest), headers);
            } catch (JsonProcessingException e) {
                logger.warn("An error occurred while creating the HttpEntity from :");
                logger.warn(e.getMessage());
                logger.warn("Creating a simple request with just the license key-value pair. . .");
                request = new HttpEntity<>(String.format("{\"key\":\"%s\"}", licenseValue.getKey()), headers);
            }

            licenseValue = sendRequest(url, request, CliniDeidLicense.class);
        }

        private void request(ClinacuityApi api, int amountToProcess) {
            pendingRequest = new PendingRequest();
            pendingRequest.setAmountToProcess(amountToProcess);
            pendingRequest.setLicenseKey(licenseValue.getKey());
            ObjectMapper mapper = new ObjectMapper();
            String url = api.baseUrl + api.requestLicense;
            logger.debug("amount: {}, key: {}, url: {}", amountToProcess, licenseValue.getKey(), url);
            try {
                pendingRequest = sendRequest(url, new HttpEntity<>(mapper.writeValueAsString(pendingRequest), headers), PendingRequest.class);
                logger.debug("{}", () -> "request result: " + pendingRequest.toString());
                logger.debug("pr.: amountToProc {}, totalProc {}, key: {}", pendingRequest.getAmountToProcess(), pendingRequest.getTotalCharsProcessed(), pendingRequest.getLicenseKey());
            } catch (JsonProcessingException e) {
                logger.error("Failed to sendRequest");
                logger.throwing(e);
                throw new RuntimeException(e);
            }
        }

        private void update(ClinacuityApi api) {
            ObjectMapper mapper = new ObjectMapper();
            String url = api.baseUrl + api.updateLicense;

            try {
                pendingRequest.setTotalCharsProcessed(DocumentListAnnotator.getTotalCharactersProcessed());
                pendingRequest = sendRequest(url, new HttpEntity<>(mapper.writeValueAsString(pendingRequest), headers), PendingRequest.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        private void completeRequest(ClinacuityApi api) {
            ObjectMapper mapper = new ObjectMapper();
            String url = api.baseUrl + api.completeRequest;

            ProcessingComplete processingComplete = new ProcessingComplete();
            processingComplete.setLicenseKey(getValue().getKey());
            processingComplete.setOriginalRequestId(pendingRequest.getId());
            processingComplete.setProcessedFiles(filesProcessed);
            processingComplete.setTotalCharsProcessed(DocumentListAnnotator.getTotalCharactersProcessed());

            HttpEntity<?> request;
            try {
                request = new HttpEntity<>(mapper.writeValueAsString(processingComplete), headers);
            } catch (JsonProcessingException e) {
                logger.warn("An error occurred while creating the HttpEntity from :");
                logger.warn(e.getMessage());
                logger.warn("Creating a simple request with just the license key-value pair. . .");
                request = new HttpEntity<>(
                        String.format("{'key''%s', 'filesProcessedCount': '%s'",
                                getValue().getKey(), getValue().getFilesProcessedCount()), headers);
            }

            sendRequest(url, request, CliniDeidLicense.class);
        }

        public void notifyApiBatchStatus(ClinacuityApi api, UUID batchGuid, BatchUploadStatus newStatus) {
            ObjectMapper mapper = new ObjectMapper();
            String url = api.baseUrl + "/process/batch/batchFile/" + batchGuid.toString();

            DeidBatchProcessingFile batchFile = new DeidBatchProcessingFile();
            batchFile.setGuid(batchGuid);
            batchFile.setStatus(newStatus);

            HttpEntity<?> request;
            try {
                request = new HttpEntity<>(mapper.writeValueAsString(batchFile), headers);

                RestTemplate template = new RestTemplate();
                template.exchange(url, HttpMethod.PUT, request, DeidBatchProcessingFile.class);
            } catch (JsonProcessingException e) {
                logger.warn("Unable to serialize batch file object as JSON");
            }
        }

        // TODO: Propagate this error to the UI
        private <K extends BaseResource> K sendRequest(String url, Object requestBody, Class<K> expectedResponse) {
            RestTemplate template = new RestTemplate();

            try {
                return template.postForObject(url, requestBody, expectedResponse);
            } catch (ResourceAccessException e) {
                logger.error("Error caught while requesting license update from server:");
                logger.error(e.getMessage());
                logger.error("The system will use cached results, if available.");
                throw e;
            }
        }
    }

    public static class ClinacuityApi {
        private String baseUrl;

        private String validateLicense;

        private String updateLicense;

        private String completeRequest;

        private String requestLicense;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            if (baseUrl.length() > 0 && baseUrl.charAt(baseUrl.length() - 1) != '/') {
                this.baseUrl = baseUrl + "/";
            }
        }

        public String getValidateLicense() {
            return validateLicense;
        }

        public void setValidateLicense(String validateLicense) {
            this.validateLicense = validateLicense;
        }

        public String getUpdateLicense() {
            return updateLicense;
        }

        public void setUpdateLicense(String updateLicense) {
            this.updateLicense = updateLicense;
        }

        public String getCompleteRequest() {
            return completeRequest;
        }

        public void setCompleteRequest(String completeRequest) {
            this.completeRequest = completeRequest;
        }

        public String getRequestLicense() {
            return requestLicense;
        }

        public void setRequestLicense(String requestLicense) {
            this.requestLicense = requestLicense;
        }
    }

    public static class AwsProperties {
        private S3Properties s3Properties = new S3Properties();

        private SqsProperties sqsProperties = new SqsProperties();

        private Ec2Properties ec2Properties = new Ec2Properties();

        public S3Properties getS3Properties() {
            return s3Properties;
        }

        public void setS3Properties(S3Properties s3Properties) {
            this.s3Properties = s3Properties;
        }

        public SqsProperties getSqsProperties() {
            return sqsProperties;
        }

        public void setSqsProperties(SqsProperties sqsProperties) {
            this.sqsProperties = sqsProperties;
        }

        public Ec2Properties getEc2Properties() {
            return ec2Properties;
        }

        public void setEc2Properties(Ec2Properties ec2Properties) {
            this.ec2Properties = ec2Properties;
        }

        public static class S3Properties {
            private String deidResultsBucketName;

            private String sqsMessagesBucket;

            private String deidLogBucketName = "clinacuity-clinideid-application-logs-test";

            private String deidLogBucketTargetPath = "backend-service/";

            private String deidLocalLogPath = "/home/ec2-user/log/DeidLog.log";

            public String getDeidResultsBucketName() {
                return deidResultsBucketName;
            }

            public void setDeidResultsBucketName(String deidResultsBucketName) {
                this.deidResultsBucketName = deidResultsBucketName;
            }

            public String getSqsMessagesBucket() {
                return sqsMessagesBucket;
            }

            public void setSqsMessagesBucket(String sqsMessagesBucket) {
                this.sqsMessagesBucket = sqsMessagesBucket;
            }

            public String getDeidLogBucketName() {
                return deidLogBucketName;
            }

            public void setDeidLogBucketName(String deidLogBucketName) {
                this.deidLogBucketName = deidLogBucketName;
            }

            public String getDeidLogBucketTargetPath() {
                return deidLogBucketTargetPath;
            }

            public void setDeidLogBucketTargetPath(String deidLogBucketTargetPath) {
                this.deidLogBucketTargetPath = deidLogBucketTargetPath;
            }

            public String getDeidLocalLogPath() {
                return deidLocalLogPath;
            }

            public void setDeidLocalLogPath(String deidLocalLogPath) {
                this.deidLocalLogPath = deidLocalLogPath;
            }
        }

        public static class SqsProperties {
            private String sqsTargetQueueUrl;

            public String getSqsTargetQueueUrl() {
                return sqsTargetQueueUrl;
            }

            public void setSqsTargetQueueUrl(String sqsTargetQueueUrl) {
                this.sqsTargetQueueUrl = sqsTargetQueueUrl;
            }
        }

        public static class Ec2Properties {
            private String autoScalingGroupName;

            public String getAutoScalingGroupName() {
                return autoScalingGroupName;
            }

            public void setAutoScalingGroupName(String autoScalingGroupName) {
                this.autoScalingGroupName = autoScalingGroupName;
            }
        }
    }
}
