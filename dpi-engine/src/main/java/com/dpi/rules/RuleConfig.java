package com.dpi.rules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/**
 * JSON configuration for blocking rules.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleConfig {

    @JsonProperty("blocked_domains")
    private List<String> blockedDomains = Collections.emptyList();

    @JsonProperty("blocked_ips")
    private List<String> blockedIps = Collections.emptyList();

    @JsonProperty("blocked_apps")
    private List<String> blockedApps = Collections.emptyList();

    @JsonProperty("blocked_ports")
    private List<Integer> blockedPorts = Collections.emptyList();

    public List<String> getBlockedDomains() { return blockedDomains; }
    public void setBlockedDomains(List<String> blockedDomains) { this.blockedDomains = blockedDomains != null ? blockedDomains : Collections.emptyList(); }

    public List<String> getBlockedIps() { return blockedIps; }
    public void setBlockedIps(List<String> blockedIps) { this.blockedIps = blockedIps != null ? blockedIps : Collections.emptyList(); }

    public List<String> getBlockedApps() { return blockedApps; }
    public void setBlockedApps(List<String> blockedApps) { this.blockedApps = blockedApps != null ? blockedApps : Collections.emptyList(); }

    public List<Integer> getBlockedPorts() { return blockedPorts; }
    public void setBlockedPorts(List<Integer> blockedPorts) { this.blockedPorts = blockedPorts != null ? blockedPorts : Collections.emptyList(); }
}
