package fr.epsi.montpellier.wsusermanagement.security.api.controller;


import fr.epsi.montpellier.wsusermanagement.security.service.LdapManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import fr.epsi.montpellier.Ldap.UserLdap;
import fr.epsi.montpellier.wsusermanagement.ResourceNotFoundException;


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

    // Update a UserLdap
    @Secured( {"ROLE_SUPER_ADMIN"})
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


    // Add a UserLdap
    @Secured( {"ROLE_SUPER_ADMIN"} )
    @PostMapping("/users")
    public @ResponseBody UserLdap addUserLdap(@Valid @RequestBody UserLdap usersDetails) {

        try {
            ldapManagerService.getManager().addUser(usersDetails);
            return usersDetails;
        } catch (Exception ex) {
            throw new ResourceNotFoundException("addUserLdap", "usersDetails", "");
        }
    }

    // Delete a UserLdap
    @Secured( {"ROLE_SUPER_ADMIN"} )
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
