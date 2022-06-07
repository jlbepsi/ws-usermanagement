package fr.epsi.montpellier.wsusermanagement.security.api.controller;


//      SWAGGER2: https://springframework.guru/spring-boot-restful-api-documentation-with-swagger-2/
//                https://www.baeldung.com/swagger-2-documentation-for-spring-rest-api

import fr.epsi.montpellier.wsusermanagement.security.amqp.RabbitMQSender;
import fr.epsi.montpellier.wsusermanagement.security.model.AuthenticationRequest;
import fr.epsi.montpellier.wsusermanagement.security.model.ClassesInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.logging.log4j.LogManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import org.apache.logging.log4j.Logger;

import javax.naming.NamingException;
import javax.validation.Valid;
import fr.epsi.montpellier.Ldap.UserLdap;
import fr.epsi.montpellier.wsusermanagement.security.service.LdapManagerService;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/api")
@Api(value = "Web API pour la gestion des utilisateurs")
public class UsersController
{
    // @Autowired
    private final RabbitMQSender rabbitMQSenderer;
    // @Autowired
    private final LdapManagerService ldapManagerService;

    // Logger
    private static final Logger logger = LogManager.getLogger("WSUserManagement");

    // @Autowired by Spring Boot
    public UsersController(LdapManagerService ldapManagerService, RabbitMQSender rabbitMQSenderer) {
        this.ldapManagerService = ldapManagerService;
        this.rabbitMQSenderer = rabbitMQSenderer;
    }


    /** Retourne la liste de tous les utilisateurs
     * La liste est composée d'objet UserLdap
     *
     * @return Liste d'objets UserLdap
     * @see UserLdap
     */
    @GetMapping(value="/users")
    @ApiOperation(value = "Liste tous les utilisateurs",
            notes = "Also returns a link to retrieve all students with rel - all-students")
    public Iterable<UserLdap> getAllUsersLdap() {
        return ldapManagerService.getManager().listUsers();
    }

    /** Retourne la liste de tous les utilisateurs d'une classe
     * La liste est composée d'objet UserLdap
     *
     * @param classe Nom de la classe des utilisateurs. Exemple: B1, B2, B3, ...
     *
     * @return Liste d'objets UserLdap
     * @see UserLdap
     */
    @GetMapping(value = "/users/classe/{id}")
    @ApiOperation(value = "Find student by id",
            notes = "Also returns a link to retrieve all students with rel - all-students")
    @ApiParam()
    public Iterable<UserLdap> getUserLdapByClass(@PathVariable(value = "id") String classe) {
        return ldapManagerService.getManager().listUsers(classe);
    }

    /** Retourne la liste de tous les utilisateurs
     * La liste est composée d'objet UserLdap
     *
     * @return Liste d'objets UserLdap
     * @see UserLdap
     */
    @GetMapping(value="/users/classesinfo")
    public ClassesInfo getClassesInfo() {
        ClassesInfo info = new ClassesInfo();
        List<UserLdap> list = ldapManagerService.getManager().listUsers();
        for (UserLdap user : list) {
            info.addUser(user);
        }
        info.buildLists();

        return info;
    }


    /** Retourne l'utilisateur associé au login
     *
     * @param login Login de l'utilisateur : prenom.nom
     *
     * @return Un objet UserLdap
     * @see UserLdap
     */
    @GetMapping(value = "/users/{id}")
    public UserLdap getUserLdapById(@PathVariable(value = "id") String login) {
        UserLdap user = ldapManagerService.getManager().getUser(login);
        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "L'utilisateur n'a pas été trouvé");
        }
        return user;
    }

    /** Ajoute un utilisateur
     *
     * Seul le rôle SUPER_ADMIN est autorisé
     *
     * @param userDetails Objet UserLdap contenant les informations à ajouter
     * @return Le status HTTP 201 - CREATED avec l'url de la ressource créée si tout est ok
     *      *         Le status HTTP BAD_REQUEST sinon
     * @see UserLdap
     */
    @Secured( {"ROLE_SUPER_ADMIN"} )
    @PostMapping(value = "/users")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> addUserLdap(@Valid @RequestBody UserLdap userDetails) {

        try {
            internalAddUser(userDetails);

            URI location = ServletUriComponentsBuilder.fromCurrentRequest().path(
                    "/{id}").buildAndExpand(userDetails.getLogin()).toUri();

            // HTTP Status Code 201: Created
            return ResponseEntity.created(location).build();
            //return userDetails;
        } catch (Exception ex) {
            logError( String.format("add(%s)", userDetails.getLogin()), ex);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("addUserLdad, user: %s", userDetails.getDescription()));
        }
    }

    /** Met à jour les informations d'un utilisateur
     *
     * Seul le rôle SUPER_ADMIN est autorisé
     *
     *  Les informations suivantes sont prises en compte:
     * <ul>
     *     <li>Nom</li>
     *     <li>Prénom</li>
     *     <li>Classe</li>
     *     <li>Rôle</li>
     *     <li>BTS (booléen indiquant si l'utilsateur passe le BTS)</li>
     *     <li>BTS Parcours</li>
     *     <li>BTS Numéro</li>
     *     <li>Le groupe de la classe</li>
     * </ul>
     *
     * @param login Login de l'utilisateur : prenom.nom
     * @param userDetails Objet UserLdap contenant les informations à modifier
     *
     * @return Un objet UserLdap avec le status HTTP 200 si tout est ok
     *         Le status HTTP NOT_FOUND ou BAD_REQUEST sinon
     * @see UserLdap
     */
    @Secured( {"ROLE_SUPER_ADMIN"})
    @PutMapping(value = "/users/{login}")
    public UserLdap updateUserLdap(@PathVariable(value = "login") String login,
                                                 @Valid @RequestBody UserLdap userDetails) {

        try {
            if (ldapManagerService.getManager().updateUser(userDetails.getLogin(), userDetails)) {
                // HTTP Status Code 200: Ok
                return userDetails;
            }
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "L'utilisateur n'a pas été trouvé");

        } catch (Exception ex) {
            logError( String.format("update(%s)", userDetails.getLogin()), ex);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("updateUserLdap, user: %s", userDetails.getDescription()));
        }
    }

    /** Change le mot de passe de l'utilisateur
     *
     * Seul le rôle SUPER_ADMIN est autorisé
     *
     * @param login Login de l'utilisateur : prenom.nom
     * @param request Login et mot de passe de l'utilisateur
     *
     * @return Le status HTTP 200 - OK si le mot de passe a été créé, l'erreur sinon
     */
    @Secured( {"ROLE_SUPER_ADMIN"})
    @PutMapping(value = "/users/password/{login}")
    public ResponseEntity<Void>  updateUserPasswordLdap(@PathVariable(value = "login") String login,
                                                        @Valid @RequestBody AuthenticationRequest request) {

        boolean success = false;

        try {
            success = ldapManagerService.getManager().updateUserPassword(request.username, request.password);
        } catch (Exception ex) {
            logError(String.format("updatePassword(%s)", login), ex);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("updateUserLdap, user.login: %s", login));
        }

        return success ?
            ResponseEntity.ok().build() :
            ResponseEntity.notFound().build();
    }


    /** Importation de plusieurs utilisateurs
     *
     * Seul le rôle SUPER_ADMIN est autorisé
     *
     * @param usersDetails La liste d'objet UserLdap à importer
     * @return La liste d'objets UserReport contenant le résultat de l'importation
     * @see UserLdap
     */
    @Secured( {"ROLE_SUPER_ADMIN"} )
    @PostMapping(value = "/users/imports")
    public @ResponseBody
    List<UserReport> importUsersLdap(@Valid @RequestBody List<UserLdap> usersDetails) {

        List<UserReport> resultats = new ArrayList<>();

        for (UserLdap userImport : usersDetails) {
            // Recherche de l'utilisateur
            UserLdap user = ldapManagerService.getManager().getUser(userImport.getLogin());
            try {
                // Fixe le BTS
                // Par défaut, les utilisateurs B1 et B2 font le BTS, option SLAM
                if (userImport.getClasse().equalsIgnoreCase("B1") || userImport.getClasse().equalsIgnoreCase("B2")) {
                    // Si l'option BTS n'est pas activé ...
                    if (! userImport.isBts()) {
                        // ... on la fixe à l'option SLAM
                        userImport.setBts(true);
                        userImport.setBtsParcours("SLAM");
                        userImport.setBtsNumero("0");
                    }
                }

                if (user == null) {
                    // Ajout
                    internalAddUser(userImport);
                    resultats.add(new UserReport(userImport.getLogin(), 1, "Créé"));
                } else {
                    // Modification
                    ldapManagerService.getManager().updateUser(userImport.getLogin(), userImport);
                    resultats.add(new UserReport(userImport.getLogin(), 2, "Mis à jour"));
                }
            } catch (Exception ex) {
                logError(String.format("Import(%s): ", userImport.getLogin()), ex);
                resultats.add(new UserReport(userImport.getLogin(), -1, "Erreur: " + ex.getMessage()));
            }
        }

        // HTTP Status Code 200: Ok
        return resultats;
    }

    @Secured( {"ROLE_SUPER_ADMIN"})
    @PutMapping(value = "/users/deactivatelist")
    public @ResponseBody
    List<UserReport> deactivateUsersLdap(@Valid @RequestBody List<String> usersLogin) {

        // HTTP Status Code 200: Ok
        return internalActivateUsersLdap(usersLogin, false);
    }

    @Secured( {"ROLE_SUPER_ADMIN"})
    @PutMapping(value = "/users/deactivate/{login}")
    public ResponseEntity<Void>  deactivateUserLdap(@PathVariable(value = "login") String login) {

        try {
            ldapManagerService.getManager().deactivateUser(login);
            // HTTP Status Code 200: Ok
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            logError( String.format("deactivate(%s)", login), ex);
            return ResponseEntity.notFound().build();
        }
    }

    // active a UserLdap
    @Secured( {"ROLE_SUPER_ADMIN"})
    @PutMapping(value = "/users/activate/{login}")
    public ResponseEntity<Void> activateUserLdap(@PathVariable(value = "login") String login) {

        try {
            ldapManagerService.getManager().activateUser(login);

            // HTTP Status Code 200: Ok
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            logError( String.format("activate(%s)", login), ex);
            return ResponseEntity.notFound().build();
        }
    }

    // deactive a list of login
    @Secured( {"ROLE_SUPER_ADMIN"})
    @PutMapping(value = "/users/activatelist")
    public @ResponseBody
    List<UserReport> activateUsersLdap(@Valid @RequestBody List<String> usersLogin) {

        // HTTP Status Code 200: Ok
        return internalActivateUsersLdap(usersLogin, true);
    }

    // deactive a list of login
    @Secured( {"ROLE_SUPER_ADMIN"})
    @PutMapping(value = "/users/changebts")
    public @ResponseBody
    List<UserReport> changeBtsUsersLDAP(@Valid @RequestBody OptionsChangeBTS optionsChangeBTS) {

        List<UserReport> resultats = new ArrayList<>();

        for (String login : optionsChangeBTS.getLogins()) {
            // Recherche de l'utilisateur
            UserLdap user = ldapManagerService.getManager().getUser(login);
            try {
                if (user == null) {
                    resultats.add(new UserReport(login, -2, "Non trouvé"));
                } else {
                    user.setBts(optionsChangeBTS.isBts());
                    user.setBtsParcours(optionsChangeBTS.getBtsparcours());
                    // Si l'utilisateur a déjà un numéro, on l'efface ??
                    user.setBtsNumero("0");
                    ldapManagerService.getManager().updateUser(user.getLogin(), user);

                    resultats.add(new UserReport(login, 1, "Mise à jour"));
                }
            } catch (Exception ex) {
                resultats.add(new UserReport(login, -1, "Erreur: " + ex.getMessage()));
            }
        }

        return resultats;
    }

    // Delete a UserLdap
    @Secured( {"ROLE_SUPER_ADMIN"} )
    @DeleteMapping(value = "/users/{login}")
    public ResponseEntity<Void> deleteUserLdap(@PathVariable(value = "login") String login) {
        boolean success = false;

        try {
            if (internalRemoveUser(login)) {
                success = true;
            }

        } catch (Exception ex) {
            logError( String.format("delete(%s)", login), ex);
        }
        return (success ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build());
    }

    // Delete many UserLdap
    @Secured( {"ROLE_SUPER_ADMIN"} )
    @DeleteMapping(value = "/users/list")
    public @ResponseBody
    List<UserReport> deleteUsersLdap(@Valid @RequestBody List<String> usersLogin) {
        List<UserReport> resultats = new ArrayList<>();

        for (String login : usersLogin) {
            // Recherche de l'utilisateur
            UserLdap user = ldapManagerService.getManager().getUser(login);
            if (user == null) {
                resultats.add(new UserReport(login, -2, "Non trouvé"));
            } else {
                try {
                    if (internalRemoveUser(login)) {
                        resultats.add(new UserReport(login, 1, "Supprimé"));
                    } else {
                        resultats.add(new UserReport(login, -2, "Non trouvé"));
                    }
                } catch (Exception ex) {
                    logError( String.format("delete(%s)", login), ex);
                    resultats.add(new UserReport(login, -1, "Erreur: " + ex.getMessage()));
                }
            }
        }

        return resultats;
    }

    // Bascule la classe précédente des utilisateurs dans la classe NA
    @Secured( {"ROLE_SUPER_ADMIN"})
    @PutMapping(value = "/users/setclassnatousers")
    public ResponseEntity<Void>  setNAClassToUsers() {

        try {
            ldapManagerService.getManager().setNAClassToUsers();

            // HTTP Status Code 200: Ok
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            logError( ex);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "setNAClassToUsers");
        }
    }

    // Bascule tous les utilisateurs dans la classe NA
    @Secured( {"ROLE_SUPER_ADMIN"})
    @PutMapping(value = "/users/setuserstona")
    public ResponseEntity<Void>  setUsersToNA() {

        try {
            ldapManagerService.getManager().setUsersToNA();

            // HTTP Status Code 200: Ok
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            logError(ex);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "setUsersToNA");
        }
    }

    private List<UserReport> internalActivateUsersLdap(List<String> usersLogin, boolean active) {

        List<UserReport> resultats = new ArrayList<>();

        for (String login : usersLogin) {
            // Recherche de l'utilisateur
            UserLdap user = ldapManagerService.getManager().getUser(login);
            try {
                if (user != null) {
                    if (active) {
                        ldapManagerService.getManager().activateUser(login);
                        resultats.add(new UserReport(login, 1, "Utilisateur activé"));
                    } else {
                        ldapManagerService.getManager().deactivateUser(login);
                        resultats.add(new UserReport(login, 1, "Utilisateur désactivé"));
                    }
                }
            } catch (Exception ex) {
                resultats.add(new UserReport(login, -1, "Erreur: " + ex.getMessage()));
            }
        }

        return resultats;
    }


    /** Ajoute un utilsateur et envoie un message AMQP
     *
     * @param user Objet utilisateur LDAP à ajouter
     * @throws NamingException si erreur
     */
    private void internalAddUser(UserLdap user) throws Exception {
        logMessage("addUserLDAP: " + user.getDescription());
        ldapManagerService.getManager().addUser(user);
        // Send AMQP Message
        rabbitMQSenderer.sendAddMessage(user);
    }
    /** Supprimer un utilsateur d'après son login et envoie un message AMQP
     *
     * @param login Login de l'utilisateur LDAP à supprimer
     * @throws NamingException si erreur
     */
    private boolean internalRemoveUser(String login) throws NamingException {
        logMessage(String.format("removeUser: %s", login));
        if (ldapManagerService.getManager().deleteUser(login)) {
            // Send AMQP Message
            rabbitMQSenderer.sendDeleteMessage(login);
            return true;
        }
        return false;
    }


    private void logError(Exception exception) {
        logger.error(exception);
    }
    private void logError(String message, Exception exception) {
        logger.error(message, exception);
    }

    private void logMessage(String message) {
        logger.info(message);
    }
}
