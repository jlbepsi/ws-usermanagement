package fr.epsi.montpellier.wsusermanagement.security.model;

import fr.epsi.montpellier.Ldap.UserLdap;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

public class ClassesInfo {
    public Collection<ClasseInfo> allClasses;
    public Collection<ClasseBTSInfo> bts;

    private final Map<String, ClasseInfo> _classes;
    private final Map<String, ClasseBTSInfo> _bts;

    public ClassesInfo() {
        _classes = new HashMap<>();
        _bts = new HashMap<>();

        // On ajoute les classes B1 et B2
        String[] classes = {"B1", "B2"};
        for (String classe : classes) {
            ClasseBTSInfo classeBtsInfo = new ClasseBTSInfo();
            classeBtsInfo.nom = classe;
            classeBtsInfo.effectif = 0;
            classeBtsInfo.bts = 0;
            _bts.put(classe, classeBtsInfo);
        }
    }

    public void addUser(UserLdap userLdap) {
        // Recherche de la classe
        ClasseInfo classeInfo = _classes.get(userLdap.getClasse());
        if (classeInfo == null) {
            // On ajoute l'info
            classeInfo = new ClasseInfo();
            classeInfo.nom = userLdap.getClasse();
            classeInfo.effectif = 1;
            _classes.put(userLdap.getClasse(), classeInfo);
        } else  {
            classeInfo.effectif++;
        }
        // Si c'est un B1 ou B2
        if (userLdap.getClasse().equals("B1") || userLdap.getClasse().equals("B2")) {
            ClasseBTSInfo classeBtsInfo = _bts.get(userLdap.getClasse());
            if (classeBtsInfo != null) {
                classeBtsInfo.effectif++;
                if (userLdap.isBts()) {
                    classeBtsInfo.bts++;
                }
            }
        }
    }

    public void buildLists() {
        allClasses = _classes.values();
        bts = _bts.values();
    }
}
