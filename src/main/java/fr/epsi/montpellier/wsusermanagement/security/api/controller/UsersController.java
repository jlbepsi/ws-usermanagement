package fr.epsi.montpellier.wsusermanagement.security.api.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import fr.epsi.montpellier.Ldap.UserLdap;
import fr.epsi.montpellier.wsusermanagement.ResourceNotFoundException;
import fr.epsi.montpellier.wsusermanagement.security.service.LdapManagerService;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/api")
public class UsersController
{

    @Autowired
    private LdapManagerService ldapManagerService;

    // Get all UserLdap
    @GetMapping(path="/users")
    public @ResponseBody Iterable<UserLdap> getAllUserLdaps() {
        return ldapManagerService.getManager().listUsers();
    }

    // Get all UserLdap of a class
    @GetMapping("/users/classe/{id}")
    public @ResponseBody Iterable<UserLdap> getUserLdapsByClass(@PathVariable(value = "id") String classe) {
        return ldapManagerService.getManager().listUsers(classe);
    }


    // Get a single UserLdap
    @GetMapping("/users/{id}")
    public @ResponseBody UserLdap getUserLdapById(@PathVariable(value = "id") String login) {
        UserLdap user = ldapManagerService.getManager().getUser(login);
        if (user == null)
            throw new ResourceNotFoundException("UserLdaps", "id", login);

        return user;
    }

    // Add a UserLdap
    //TODO : Réactiver autorisation @Secured( {"ROLE_SUPER_ADMIN"} )
    @PostMapping("/users")
    //@ResponseStatus(HttpStatus.CREATED)
    //public @ResponseBody UserLdap addUserLdap(@Valid @RequestBody UserLdap userDetails) {
    public ResponseEntity<Void> addUserLdap(@Valid @RequestBody UserLdap userDetails) {

        try {
            ldapManagerService.getManager().addUser(userDetails);

            URI location = ServletUriComponentsBuilder.fromCurrentRequest().path(
                    "/{id}").buildAndExpand(userDetails.getLogin()).toUri();

            // HTTP Status Code 201: Created
            return ResponseEntity.created(location).build();
            //return userDetails;
        } catch (Exception ex) {
            throw new ResourceNotFoundException("addUserLdap", "usersDetails", "");
        }
    }

    // Update a UserLdap
    //TODO : Réactiver autorisation @Secured( {"ROLE_SUPER_ADMIN"})
    @PutMapping("/users/{login}")
    public @ResponseBody UserLdap updateUserLdap(@PathVariable(value = "login") String login,
                                                 @Valid @RequestBody UserLdap usersDetails) {

        try {
            ldapManagerService.getManager().updateUser(login, usersDetails);
            // HTTP Status Code 200: Ok
            return usersDetails;
        } catch (Exception ex) {
            throw new ResourceNotFoundException("updateUserLdap", "usersDetails", "");
        }
    }

    //TODO : Réactiver autorisation @Secured( {"ROLE_SUPER_ADMIN"})
    @PutMapping("/users/password/{login}")
    public @ResponseBody UserLdap updateUserPasswordLdap(@PathVariable(value = "login") String login,
                                                 @Valid @RequestBody UserLdap usersDetails) {

        try {
            ldapManagerService.getManager().updateUserPassword(login, usersDetails.getMotDePasse());
            // HTTP Status Code 200: Ok
            return usersDetails;
        } catch (Exception ex) {
            throw new ResourceNotFoundException("updateUserLdap", "usersDetails", "");
        }
    }



    // Import many UserLdap
    //TODO : Réactiver autorisation @Secured( {"ROLE_SUPER_ADMIN"} )
    @PostMapping("/users/imports")
    public @ResponseBody
    List<UserImportReport> importUsersLdap(@Valid @RequestBody List<UserLdap> usersDetails) {

        List<UserImportReport> resultats = new ArrayList<>();

        for (UserLdap userImport : usersDetails) {
            // Recherche de l'utilisateur
            UserLdap user = ldapManagerService.getManager().getUser(userImport.getLogin());
            try {
                if (user == null) {
                    // Ajout
                    ldapManagerService.getManager().addUser(userImport);
                    resultats.add(new UserImportReport(userImport.getLogin(), 1, "Créé"));
                } else {
                    // Modification
                    ldapManagerService.getManager().updateUser(userImport.getLogin(), userImport);
                    resultats.add(new UserImportReport(userImport.getLogin(), 2, "Mis à jour"));
                }
            } catch (Exception ex) {
                resultats.add(new UserImportReport(userImport.getLogin(), -1, "Erreur: " + ex.getMessage()));
            }
        }

        // HTTP Status Code 200: Ok
        return resultats;
    }

    // deactive a list of login
    //TODO : Réactiver autorisation @Secured( {"ROLE_SUPER_ADMIN"})
    @PutMapping("/users/deactivatelist")
    public @ResponseBody
    List<UserImportReport> deactivateUsersLdap(@Valid @RequestBody List<String> usersLogin) {

        // HTTP Status Code 200: Ok
        return internalActivateUsersLdap(usersLogin, false);
    }

    //TODO : Réactiver autorisation @Secured( {"ROLE_SUPER_ADMIN"})
    @PutMapping("/users/deactivate/{login}")
    public ResponseEntity<Void>  deactivateUserLdap(@PathVariable(value = "login") String login) {

        try {
            ldapManagerService.getManager().deactivateUser(login);
            // HTTP Status Code 200: Ok
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    // active a UserLdap
    //TODO : Réactiver autorisation @Secured( {"ROLE_SUPER_ADMIN"})
    @PutMapping("/users/activate/{login}")
    public ResponseEntity<Void>  activateUserLdap(@PathVariable(value = "login") String login) {

        try {
            ldapManagerService.getManager().activateUser(login);

            // HTTP Status Code 200: Ok
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }
    // deactive a list of login
    //TODO : Réactiver autorisation @Secured( {"ROLE_SUPER_ADMIN"})
    @PutMapping("/users/activatelist")
    public @ResponseBody
    List<UserImportReport> activateUsersLdap(@Valid @RequestBody List<String> usersLogin) {

        // HTTP Status Code 200: Ok
        return internalActivateUsersLdap(usersLogin, true);
    }

    // Delete a UserLdap
    //TODO : Réactiver autorisation @Secured( {"ROLE_SUPER_ADMIN"} )
    @DeleteMapping("/users/{login}")
    public ResponseEntity<Void> deleteUserLdap(@PathVariable(value = "login") String login) {

        try {
            ldapManagerService.getManager().deleteUser(login);
            // HTTP Status Code 204: No Content
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
            //throw new ResourceNotFoundException("updateUserLdap", "usersDetails", "");
        }
    }

    // Delete many UserLdap
    //TODO : Réactiver autorisation @Secured( {"ROLE_SUPER_ADMIN"} )
    @DeleteMapping("/users/list")
    public @ResponseBody
    List<UserImportReport> deleteUsersLdap(@Valid @RequestBody List<String> usersLogin) {

        List<UserImportReport> resultats = new ArrayList<>();

        for (String login : usersLogin) {
            // Recherche de l'utilisateur
            UserLdap user = ldapManagerService.getManager().getUser(login);
            try {
                if (user == null) {
                    resultats.add(new UserImportReport(login, 1, "Non trouvé"));
                } else {
                    ldapManagerService.getManager().deleteUser(login);
                    resultats.add(new UserImportReport(login, 2, "Supprimé"));
                }
            } catch (Exception ex) {
                resultats.add(new UserImportReport(login, -1, "Erreur: " + ex.getMessage()));
            }
        }

        return resultats;
    }

    // Bascule tous les utilisateurs dans la classe NA
    @PutMapping("/users/setuserstona")
    public ResponseEntity<Void>  setUsersToNA() {

        try {
            ldapManagerService.getManager().setUsersToNA();

            // HTTP Status Code 200: Ok
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            throw new ResourceNotFoundException("updateUserLdap", "setUsersToNA", "");
        }
    }

    private List<UserImportReport> internalActivateUsersLdap(List<String> usersLogin, boolean active) {

        List<UserImportReport> resultats = new ArrayList<>();

        for (String login : usersLogin) {
            // Recherche de l'utilisateur
            UserLdap user = ldapManagerService.getManager().getUser(login);
            try {
                if (user != null) {
                    if (active) {
                        ldapManagerService.getManager().activateUser(login);
                        resultats.add(new UserImportReport(login, 1, "Utilisateur activé"));
                    } else {
                        ldapManagerService.getManager().deactivateUser(login);
                        resultats.add(new UserImportReport(login, 1, "Utilisateur désactivé"));
                    }
                }
            } catch (Exception ex) {
                resultats.add(new UserImportReport(login, -1, "Erreur: " + ex.getMessage()));
            }
        }

        return resultats;
    }
}
