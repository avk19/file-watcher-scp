package com.example;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

public class ActiveDirectoryGroupFetcher1 {

    public static void main(String[] args) {
        // Replace with your AD information
        String domain = "yourDomain";
        String username = "yourUsername";
        String password = "yourPassword";
        String ldapURL = "ldaps://yourADServer:636";

        // User information
        String userCommonName = "John Doe"; // Change this to the user's common name

        // Perform group retrieval
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, ldapURL);
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, username + "@" + domain);
            env.put(Context.SECURITY_CREDENTIALS, password);

            LdapContext context = new InitialLdapContext(env, null);

            // Search for the user's DN (Distinguished Name) based on the common name
            String userDN = getUserDN(context, userCommonName);

            if (userDN != null) {
                // Search for groups of the specified user
                String filter = "(member=" + userDN + ")";
                SearchControls controls = new SearchControls();
                controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

                NamingEnumeration<SearchResult> results = context.search("", filter, controls);

                while (results.hasMore()) {
                    SearchResult result = results.next();
                    String groupName = result.getName();
                    System.out.println("Group: " + groupName);
                }

                context.close();
            } else {
                System.out.println("User not found");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getUserDN(LdapContext context, String commonName) throws Exception {
        String filter = "(&(objectClass=user)(cn=" + commonName + "))";
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<SearchResult> results = context.search("", filter, controls);

        if (results.hasMore()) {
            SearchResult result = results.next();
            return result.getNameInNamespace();
        }

        return null;
    }
}
