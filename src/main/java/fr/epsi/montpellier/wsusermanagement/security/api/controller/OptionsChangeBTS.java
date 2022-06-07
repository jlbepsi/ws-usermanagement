package fr.epsi.montpellier.wsusermanagement.security.api.controller;

import java.util.List;

import fr.epsi.montpellier.Ldap.UserLdap;


public class OptionsChangeBTS {
    private List<String> logins;
    private boolean bts;
    private String btsparcours;


    public OptionsChangeBTS() {
    }

    public List<String> getLogins() {
        return logins;
    }

    public void setLogins(List<String> logins) {
        this.logins = logins;
    }

    public boolean isBts() {
        return bts;
    }

    public void setBts(boolean bts) {
        this.bts = bts;
    }

    public String getBtsparcours() {
        return btsparcours;
    }

    public void setBtsparcours(String btsparcours) {
        this.btsparcours = btsparcours;
    }
}
