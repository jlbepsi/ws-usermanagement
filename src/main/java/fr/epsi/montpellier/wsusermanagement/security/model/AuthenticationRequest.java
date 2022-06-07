package fr.epsi.montpellier.wsusermanagement.security.model;

import javax.validation.constraints.NotEmpty;

public class AuthenticationRequest {
    @NotEmpty
    public String username;
    @NotEmpty
    public String password;
    public String role;
}
