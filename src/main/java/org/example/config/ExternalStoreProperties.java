package org.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.external-store")
public class ExternalStoreProperties {

    private boolean enabled = false;
    private String url = "";
    private String apiKey = "";

    public boolean isEnabled()  { return enabled; }
    public String  getUrl()     { return url; }
    public String  getApiKey()  { return apiKey; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setUrl(String url)          { this.url = url; }
    public void setApiKey(String apiKey)    { this.apiKey = apiKey; }
}
