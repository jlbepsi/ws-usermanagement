package fr.epsi.montpellier.wsusermanagement.security.api.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

import fr.epsi.montpellier.wsusermanagement.security.model.AuthenticationRequest;
import fr.epsi.montpellier.wsusermanagement.security.service.JwtTokenService;
import fr.epsi.montpellier.wsusermanagement.security.model.AuthenticationResponse;
import fr.epsi.montpellier.wsusermanagement.security.service.LdapManagerService;

import fr.epsi.montpellier.Ldap.UserLdap;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController
{

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private LdapManagerService ldapManagerService;

    @PostMapping("/login")
    public ResponseEntity authenticate(@Valid @RequestBody AuthenticationRequest authenticationRequest) {
        AuthenticationResponse response = new AuthenticationResponse();
        response.status = false;

        System.out.printf("AuthenticationController.authenticate Login=%s, Role=%s", authenticationRequest.username, authenticationRequest.role);

        // Authentification openLDAP
        try {
            UserLdap user = ldapManagerService.getManager().authenticateUser(authenticationRequest.username, authenticationRequest.password);
            if (user != null) {
                if (authenticationRequest.role != null && !authenticationRequest.role.isEmpty()) {
                    // Si le role fourni est présent dans la liste des roles alors on accepte
                    if (user.getRole().contains(authenticationRequest.role.toUpperCase()))
                        response.status = true;
                } else {
                    response.status = true;
                }
            }

            if (response.status) {
                response.token = jwtTokenService.createToken(user);
                return ResponseEntity.ok().body(response);
            }
        } catch (Exception ex) {
            System.out.println("Erreur: " + ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(HttpStatus.UNAUTHORIZED.getReasonPhrase());
    }

}
