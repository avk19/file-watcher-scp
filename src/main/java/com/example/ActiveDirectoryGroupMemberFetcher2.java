package com.example;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.List;

public class ActiveDirectoryGroupMemberFetcher2 {

    public static void main(String[] args) {
        String ldapURL = "ldaps://your-ldap-server:636";  // Update with your LDAP server URL
        String username = "your-domain\\your-username";  // Replace with your domain and username
        String password = "your-password";
        String groupName = "YourGroupName";  // Replace with the target group name
        String domainName = "your-domain";  // Replace with your domain name

        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapURL);
        contextSource.setUserDn(username);
        contextSource.setPassword(password);
        contextSource.afterPropertiesSet();

        LdapTemplate ldapTemplate = new LdapTemplate(contextSource);

        String filter = "(&(objectClass=user)(memberOf=CN=" + groupName + ",DC=" + domainName + ",DC=com))";
        List<String> users = ldapTemplate.search("", filter, new UserNameAttributeMapper());

        if (users.isEmpty()) {
            System.out.println("No users found in the group.");
        } else {
            for (String user : users) {
                System.out.println("User: " + user);
            }
        }
    }

    private static class UserNameAttributeMapper implements AttributesMapper<List<String>> {
        @Override
        public List<String> mapFromAttributes(Attributes attributes) throws NamingException {
            Attribute sAMAccountName = attributes.get("sAMAccountName");
            return LdapUtils.toList(sAMAccountName.getAll());
        }
    }
}
