package fr.epsi.montpellier.wsusermanagement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
public class LdapPropertiesTest {
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

    public String getAdminlogin() {
        return adminLogin;
    }

    public String getAdresseIP() {
        return adresseIP;
    }

    public String getPort() {
        return port;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public String getBaseDN() {
        return baseDN;
    }

    public String getOuUtilisateurs() {
        return ouUtilisateurs;
    }

    public String getOuGroups() {
        return ouGroups;
    }

    public String getGroupeEtudiants() {
        return groupeEtudiants;
    }

    public String getUsersLdapDirectory() {
        return usersLdapDirectory;
    }
}
