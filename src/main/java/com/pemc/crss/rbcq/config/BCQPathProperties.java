package com.pemc.crss.rbcq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rbcq.storage")
public class BCQPathProperties {
    private String uploadPath;      // matches upload-path in YAML
    private String processedPath;   // matches processed-path in YAML

    private String rejectedPath;

    public String getUploadPath() {
        return uploadPath;
    }

    public void setUploadPath(String uploadPath) {
        this.uploadPath = uploadPath;
    }

    public String getProcessedPath() {
        return processedPath;
    }

    public void setProcessedPath(String processedPath) {
        this.processedPath = processedPath;
    }

    public String getRejectedPath() {return  rejectedPath;}

    public void setRejectedPath(String rejectedPath){this.rejectedPath = rejectedPath;}
}
