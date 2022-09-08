package fr.epsi.montpellier.wsusermanagement.actuator;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BuildInfoContributor implements InfoContributor {
    @Value("${ws-usermanagement.version}")
    private String version;
    @Value("${ldap.cloud.source}")
    private String ldapCloudVersion;

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, String > data = new HashMap<>();
        data.put("build.version", version);
        data.put("ldap.cloud.source", ldapCloudVersion);
        builder.withDetail("buildInfo", data);
    }
}
