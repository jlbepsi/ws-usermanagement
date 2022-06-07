package fr.epsi.montpellier.wsusermanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.epsi.montpellier.Ldap.UserLdap;
import fr.epsi.montpellier.wsusermanagement.security.api.controller.OptionsChangeBTS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.internal.bytebuddy.matcher.ElementMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.epsi.montpellier.Ldap.LdapManager;
import fr.epsi.montpellier.wsusermanagement.security.api.controller.UsersController;
import fr.epsi.montpellier.wsusermanagement.security.service.LdapManagerService;
import fr.epsi.montpellier.wsusermanagement.security.amqp.RabbitMQSender;
import fr.epsi.montpellier.wsusermanagement.security.service.JwtTokenService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// https://www.baeldung.com/integration-testing-in-spring
// https://www.baeldung.com/spring-mvc-test-exceptions
// https://www.baeldung.com/spring-security-method-security#testing-method-security


@WebMvcTest(value = UsersController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
// @WebMvcTest(value = UsersController.class)
@TestPropertySource(locations = "/test.properties")
@Import(LdapPropertiesTest.class)
public class UsersControllerMockTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private LdapPropertiesTest ldapProperties;

    @MockBean
    private JwtTokenService jwtTokenService;
    @MockBean
    private RabbitMQSender rabbitMQSenderer;
    @MockBean
    private LdapManagerService ldapManagerService;

    private LdapManager ldapManager;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setup() throws Exception {
        assertThat(ldapProperties.getAdresseIP()).isEqualTo("localhost");

        this.ldapManager = new LdapManager(ldapProperties.getAdresseIP(), ldapProperties.getPort(), ldapProperties.getAdminlogin(),
                ldapProperties.getAdminPassword(), ldapProperties.getBaseDN(), ldapProperties.getOuUtilisateurs(),
                ldapProperties.getOuGroups(), ldapProperties.getGroupeEtudiants());
    }

    @Test
    public void listUsers() throws Exception {
        given(this.ldapManagerService.getManager())
                .willReturn(this.ldapManager);

        this.mockMvc.perform(get("/api/users/classe/b2"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(greaterThan(10))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN", "ADMIN"})
    public void importUsersLdap() throws Exception {
        given(this.ldapManagerService.getManager())
                .willReturn(this.ldapManager);

        List<UserLdap> list = new ArrayList<>();
        // ctor UserLdap(String login, String nom, String prenom, String motDePasse, String classe, String mail, String role)
        list.add(new UserLdap("test.import1", "IMPORT1", "test", "123456", "B2", "test.import1@test.fr", "ROLE_USER"));
        list.add(new UserLdap("test.import2", "IMPORT2", "test", "123456", "B3", "test.import2@test.fr", "ROLE_USER"));
        list.add(new UserLdap("test.import3", "IMPORT3", "test", "123456", "B2", "test.import3@test.fr", "ROLE_USER"));
        String json = mapper.writeValueAsString(list);

        // @RequestBody List<UserLdap> usersDetails
        this.mockMvc.perform(
                    post("/api/users/imports")
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].status", greaterThan(0)))
                .andExpect(jsonPath("$[1].status", greaterThan(0)))
                .andExpect(jsonPath("$[2].status", greaterThan(0)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN", "ADMIN"})
    public void changeBtsUsersLDAP() throws Exception {
        given(this.ldapManagerService.getManager())
                .willReturn(this.ldapManager);

        OptionsChangeBTS optionsChangeBTS = new OptionsChangeBTS();
        optionsChangeBTS.setLogins(new ArrayList<String>(Arrays.asList("test.bts1", "test.bts2")));
        optionsChangeBTS.setBts(true);
        optionsChangeBTS.setBtsparcours("SISR");
        String json = mapper.writeValueAsString(optionsChangeBTS);

        // @RequestBody OptionsChangeBTS optionsChangeBTS
        this.mockMvc.perform(
                put("/api/users/changebts")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status", greaterThan(0)))
                .andExpect(jsonPath("$[1].status", greaterThan(0)));
    }
}
