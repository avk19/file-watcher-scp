package com.example;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;

public class ActiveDirectoryGroupFetcher2 {

    public static void main(String[] args) {
        String ldapURL = "ldaps://your-ldap-server:636";  // Update with your LDAP server URL
        String username = "your-username";
        String password = "your-password";
        String userDn = "CN=John Doe,CN=Users,DC=example,DC=com";  // Update with the DN of the user

        try {
            // Set up the environment for creating the initial context
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, ldapURL);
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, username);
            env.put(Context.SECURITY_CREDENTIALS, password);

            // Create the initial context
            DirContext context = new InitialDirContext(env);

            // Search for the user's groups
            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String filter = "(&(objectClass=user)(sAMAccountName=" + userDn + "))";
            NamingEnumeration<SearchResult> results = context.search("", filter, searchControls);

            while (results.hasMore()) {
                SearchResult searchResult = results.next();
                Attributes attributes = searchResult.getAttributes();
                Attribute memberOf = attributes.get("memberOf");

                if (memberOf != null) {
                    NamingEnumeration<?> groups = memberOf.getAll();

                    while (groups.hasMore()) {
                        String group = groups.next().toString();
                        System.out.println("Group: " + group);
                    }
                } else {
                    System.out.println("User doesn't belong to any groups.");
                }
            }

            // Close the context when done
            context.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
