package fr.epsi.montpellier.wsusermanagement.security.service;

import fr.epsi.montpellier.Ldap.LdapManager;
import fr.epsi.montpellier.wsusermanagement.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class LdapManagerService {

    @Value("${adresse_ip}")
    private String adresseIP;

    @Value("${admin_login}")
    private String adminLogin;

    @Value("${admin_password}")
    private String adminPassword;

    @Value("${base_dn}")
    private String baseDN;

    @Value("${ou_utilisateurs}")
    private String ouUtilisateurs;

    @Value("${ou_groups}")
    private String ouGroups;

    private LdapManager manager;

    public LdapManagerService() {
    }

    public LdapManager getManager() {
        return this.manager;
    }

    @PostConstruct
    private void buildManager() {
        try {
            manager = new LdapManager(adresseIP, adminLogin, adminPassword, baseDN, ouUtilisateurs, ouGroups);
        } catch (Exception ex) {
            throw new ResourceNotFoundException("LdapManager", "config file", "");
        }
    }
}
