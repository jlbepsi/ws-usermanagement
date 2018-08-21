package fr.epsi.montpellier.wsusermanagement.security.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;

import fr.epsi.montpellier.Ldap.UserLdap;

@Service
public class JwtTokenService {

    @Value("${jwt.expire.hours}")
    private int expireHours;

    @Value("${jwt.token.secret}")
    private String plainSecret;


    private static final Logger logger = LoggerFactory.getLogger(JwtTokenService.class);

    public String createToken(UserLdap user) {

        // Génération de la date d'expiration
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, expireHours);

        // Construction des attributs
        Claims claims = Jwts.claims();
        this.populate(user, claims);

        // Construction du token
        return Jwts.builder()
                .signWith(getKey())
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(calendar.getTime())
                .compact();
    }

    public Jws<Claims> validateToken(String authToken) {
        return  Jwts.parser().setSigningKey(getKey()).parseClaimsJws(authToken);
    }

    private Key getKey() {
        // Création de la clé
        byte[] encodedBytes = this.plainSecret.getBytes();
        return Keys.hmacShaKeyFor(encodedBytes);
    }

    private void populate(UserLdap user, Claims claims) {

        claims.setSubject(user.getLogin());
        claims.put("nom", user.getNom());
        claims.put("prenom", user.getPrenom());
        claims.put("mail", user.getMail());
        claims.put("classe", user.getClasse());
        claims.put("roles", String.join(",", user.getRole()));
    }

}
