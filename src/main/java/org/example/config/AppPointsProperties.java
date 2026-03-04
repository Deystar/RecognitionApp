package org.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.points")
public class AppPointsProperties {

    private int peerGivingAllowance = 20;
    private int teamGivingAllowance = 20;
    private boolean allowSelfTeamAward = false;

    public int     getPeerGivingAllowance()  { return peerGivingAllowance; }
    public int     getTeamGivingAllowance()  { return teamGivingAllowance; }
    public boolean isAllowSelfTeamAward()    { return allowSelfTeamAward; }

    public void setPeerGivingAllowance(int peerGivingAllowance)   { this.peerGivingAllowance = peerGivingAllowance; }
    public void setTeamGivingAllowance(int teamGivingAllowance)   { this.teamGivingAllowance = teamGivingAllowance; }
    public void setAllowSelfTeamAward(boolean allowSelfTeamAward) { this.allowSelfTeamAward = allowSelfTeamAward; }
}
