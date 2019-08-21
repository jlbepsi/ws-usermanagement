package fr.epsi.montpellier.wsusermanagement.security.service;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import fr.epsi.montpellier.Ldap.UserLdap;


@Service
public class JwtTokenService {

    @Value("${jwt.expire.hours}")
    private int expireHours;

    @Value("${jwt.token.private_key}")
    private String privateKeyRessource;
    @Value("${jwt.token.public_key}")
    private String publicKeyRessource;


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
                /*.signWith(getKey())*/
                .signWith(getPrivateKey(), SignatureAlgorithm.RS256)
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(calendar.getTime())
                .compact();
    }

    public Jws<Claims> validateToken(String authToken) {
        return  Jwts.parser().setSigningKey(getPublicKey()).parseClaimsJws(authToken);
    }

    private void populate(UserLdap user, Claims claims) {

        claims.setSubject(user.getLogin());
        claims.put("nom", user.getNom());
        claims.put("prenom", user.getPrenom());
        claims.put("mail", user.getMail());
        claims.put("classe", user.getClasse());
        claims.put("roles", String.join(",", user.getRole()));
        claims.put("bts", user.isBts());
        claims.put("btsparcours", user.getBtsParcours());
        claims.put("btsnumero", user.getBtsNumero());
    }

    private Key getPrivateKey() {
        // Création de la clé
        byte[] privateKey = readPrivateKey();
        if (privateKey != null) {
            try {
                PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKey);

                KeyFactory kf = KeyFactory.getInstance("RSA");
                return kf.generatePrivate(spec);
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }
        return null;
    }

    private Key getPublicKey() {
        // Création de la clé
        byte[] publicKey = readPublicKey();
        if (publicKey != null) {
            try {
                X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKey);

                KeyFactory kf = KeyFactory.getInstance("RSA");
                return kf.generatePublic(spec);
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }
        return null;
    }

    private byte[] readPrivateKey() {
        return readKey(privateKeyRessource);
    }
    private byte[] readPublicKey() {
        return readKey(publicKeyRessource);
    }
    private byte[] readKey(String ressourceName) {
        try {
            ClassPathResource resource = new ClassPathResource(ressourceName);
            InputStream stream = resource.getInputStream();
            return getBytesFromInputStream(stream);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }

        return null;
    }
    private static byte[] getBytesFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xFFFF];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
            os.write(buffer, 0, len);
        }
        return os.toByteArray();
    }
}
