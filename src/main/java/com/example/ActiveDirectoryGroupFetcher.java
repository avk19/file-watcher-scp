import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ActiveDirectoryGroupFetcher {

    public static void main(String[] args) {
        String baseUsername = "yourBaseUsername";
        String baseDomainName = "yourBaseDomain";

        String searchUsername = "yourSearchUsername";
        String searchDomainName = "yourSearchDomain";

        List<String> groups = fetchActiveDirectoryGroups(baseUsername, baseDomainName, searchUsername, searchDomainName);

        System.out.println("Active Directory Groups for User " + searchUsername + " in Domain " + searchDomainName + ":");
        for (String group : groups) {
            System.out.println(group);
        }
    }

    public static List<String> fetchActiveDirectoryGroups(String baseUsername, String baseDomainName, String searchUsername, String searchDomainName) {
        String ldapsUrl = "ldaps://" + baseDomainName + ":636"; // LDAPS URL with port 636

        LdapContext context = null;
        try {
            context = ldapOperation(ldapsUrl);
            bindWithCredentials(context, baseUsername, getPassword(baseUsername, baseDomainName), baseDomainName);

            return searchForGroups(context, searchUsername);

        } catch (NamingException e) {
            throw new RuntimeException("Error accessing Active Directory: " + e.getMessage(), e);

        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException e) {
                    // Handle close exception if necessary
                }
            }
        }
    }

    private static LdapContext ldapOperation(String ldapsUrl) throws NamingException {
        Hashtable<String, Object> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapsUrl);
        env.put(Context.SECURITY_PROTOCOL, "ssl");

        return new InitialLdapContext(env, null);
    }

    private static void bindWithCredentials(LdapContext context, String username, String password, String domainName) throws NamingException {
        context.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
        context.addToEnvironment(Context.SECURITY_PRINCIPAL, username + "@" + domainName);
        context.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
    }

    private static List<String> searchForGroups(LdapContext context, String searchUsername) throws NamingException {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<SearchResult> results = context.search(
                "CN=Users,DC=yourBaseDomain,DC=com",  // Adjust the base DN as per your AD structure
                "(&(objectClass=user)(sAMAccountName=" + searchUsername + "))",
                searchControls
        );

        if (results.hasMore()) {
            SearchResult entry = results.next();
            Attribute memberOfAttribute = entry.getAttributes().get("memberOf");

            return getAttributeValues(memberOfAttribute);
        } else {
            throw new RuntimeException("User " + searchUsername + " not found in Active Directory");
        }
    }

    private static List<String> getAttributeValues(Attribute attribute) throws NamingException {
        NamingEnumeration<?> attributeValues = attribute.getAll();
        List<String> values = new ArrayList<>();

        while (attributeValues.hasMore()) {
            String value = (String) attributeValues.next();
            values.add(value);
        }

        return values;
    }

    private static String getPassword(String username, String domainName) {
        // Implement a secure way to fetch the password (e.g., from a configuration file, environment variable, etc.)
        return "yourPassword";
    }
}
