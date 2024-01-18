package com.example;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.List;

public class SpringActiveDirectoryGroupFetcher2 {

    public static void main(String[] args) {
        String ldapURL = "ldaps://your-ldap-server:636";  // Update with your LDAP server URL
        String domainName = "your-domain";  // Update with your domain name
        String username = "your-username";  // Update with the CN or username
        String password = "your-password";  // Update with the password
        String userDn = "CN=" + username + ",CN=Users,DC=" + domainName + ",DC=com";

        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapURL);
        contextSource.setUserDn(userDn);
        contextSource.setPassword(password);
        contextSource.afterPropertiesSet();

        LdapTemplate ldapTemplate = new LdapTemplate(contextSource);

        String filter = "(&(objectClass=user)(sAMAccountName=" + username + "))";
        List<String> groups = ldapTemplate.search("", filter, new MemberOfAttributeMapper());

        if (groups.isEmpty()) {
            System.out.println("User doesn't belong to any groups.");
        } else {
            for (String group : groups) {
                System.out.println("Group: " + group);
            }
        }
    }

    private static class MemberOfAttributeMapper implements AttributesMapper<List<String>> {
        @Override
        public List<String> mapFromAttributes(Attributes attributes) throws NamingException {
            Attribute memberOf = attributes.get("memberOf");
            return LdapUtils.toList(memberOf.getAll());
        }
    }
}