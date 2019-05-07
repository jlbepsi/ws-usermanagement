package fr.epsi.montpellier.wsusermanagement.security.api.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
            return usersDetails;
        } catch (Exception ex) {
            throw new ResourceNotFoundException("updateUserLdap", "usersDetails", "");
        }
    }

    // Import a UserLdap
    //TODO : Réactiver autorisation @Secured( {"ROLE_SUPER_ADMIN"} )
    @PostMapping("/users/imports")
    public @ResponseBody
    List<UserImportReport> importUserLdap(@Valid @RequestBody List<UserLdap> usersDetails) {

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

        return resultats;
    }

    // deactive a UserLdap
    //TODO : Réactiver autorisation @Secured( {"ROLE_SUPER_ADMIN"})
    @PutMapping("/users/deactivate/{login}")
    public ResponseEntity<Void>  deactivateUserLdap(@PathVariable(value = "login") String login) {

        try {
            ldapManagerService.getManager().deactivateUser(login);
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
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a UserLdap
    //TODO : Réactiver autorisation @Secured( {"ROLE_SUPER_ADMIN"} )
    @DeleteMapping("/users/{login}")
    public ResponseEntity<Void> deleteUserLdap(@PathVariable(value = "login") String login) {

        try {
            ldapManagerService.getManager().deleteUser(login);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
            //throw new ResourceNotFoundException("updateUserLdap", "usersDetails", "");
        }
    }
}
