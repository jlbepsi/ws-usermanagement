package fr.epsi.montpellier.wsusermanagement.security.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import fr.epsi.montpellier.Ldap.LdapManager;

@Service
public class LdapManagerService {

    @Value("${adresse_ip}")
    private String adresseIP;

    @Value("${ldap_port}")
    private String port;

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

    @Value("${groupe_etudiants}")
    private String groupeEtudiants;

    @Value("${users_ldap_directory}")
    private String usersLdapDirectory;

    private LdapManager manager;

    public LdapManagerService() {
    }

    public LdapManager getManager() {
        return this.manager;
    }

    @PostConstruct
    private void buildManager() throws Exception {
        System.out.println("Construction de LdapManagerService, AdresseIP=" + adresseIP);

        try {
            manager = new LdapManager(adresseIP, port, adminLogin, adminPassword, baseDN, ouUtilisateurs, ouGroups, groupeEtudiants);
            if (usersLdapDirectory != null)
                manager.setUsersLdapDirectory(usersLdapDirectory);
        } catch (Exception ex) {
            throw new Exception("LdapManager", ex);
        }
    }
}
