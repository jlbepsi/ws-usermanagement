package fr.epsi.montpellier.wsusermanagement.security.model;

import fr.epsi.montpellier.Ldap.UserLdap;
import org.apache.catalina.User;

public class UserLdapDTO extends UserLdap {

    public UserLdapDTO() {
        super();
    }
    public UserLdapDTO(String login, String nom, String prenom, String motDePasse, String classe, String mail) {
        this(login, nom, prenom, motDePasse, classe, mail, "ROLE_USER");
    }

    public UserLdapDTO(String login, String nom, String prenom, String motDePasse, String classe, String mail, String role) {
        super(login, nom, prenom, motDePasse, classe, mail, role);
    }
}
