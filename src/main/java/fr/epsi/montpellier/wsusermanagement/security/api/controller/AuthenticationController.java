package fr.epsi.montpellier.wsusermanagement.security.api.controller;


import fr.epsi.montpellier.wsusermanagement.security.service.LdapManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

import fr.epsi.montpellier.wsusermanagement.security.model.AuthenticationRequest;
import fr.epsi.montpellier.wsusermanagement.security.service.JwtTokenService;
import fr.epsi.montpellier.Ldap.UserLdap;



@RestController
@RequestMapping("/api/auth")
public class AuthenticationController
{

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private LdapManagerService ldapManagerService;

    @PostMapping("/signin")
    public ResponseEntity authenticate(@Valid @RequestBody AuthenticationRequest authenticationRequest) {

        // Authentification openLDAP
        UserLdap user = ldapManagerService.getManager().authenticateUser(authenticationRequest.username, authenticationRequest.password);
        if (user != null) {
            String token = jwtTokenService.createToken(user);
            return ResponseEntity.ok().body(token);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(HttpStatus.UNAUTHORIZED.getReasonPhrase());
    }

}
