import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.Hashtable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ActiveDirectoryGroupFetcher {

    public static void main(String[] args) {
        String baseUsername = "yourBaseUsername";
        String baseDomainName = "yourBaseDomain";
        
        String searchUsername = "yourSearchUsername";
        String searchDomainName = "yourSearchDomain";

        List<String> groups = fetchActiveDirectoryGroups(baseUsername, baseDomainName, searchUsername, searchDomainName);

        System.out.println("Active Directory Groups for User " + searchUsername + " in Domain " + searchDomainName + ":");
        groups.forEach(System.out::println);
    }

    public static List<String> fetchActiveDirectoryGroups(String baseUsername, String baseDomainName, String searchUsername, String searchDomainName) {
        String ldapsUrl = "ldaps://" + baseDomainName + ":636"; // LDAPS URL with port 636

        return ldapOperation(ldapsUrl)
                .andThen(bindWithCredentials(baseUsername, getPassword(baseUsername, baseDomainName), baseDomainName))
                .andThen(searchForGroups(searchUsername))
                .apply(null); // Null is passed as the initial LdapContext, as it is created in the ldapOperation function
    }

    private static Function<LdapContext, List<String>> ldapOperation(String ldapsUrl) {
        return context -> {
            try {
                Hashtable<String, Object> env = new Hashtable<>();
                env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
                env.put(Context.PROVIDER_URL, ldapsUrl);
                env.put(Context.SECURITY_PROTOCOL, "ssl");

                LdapContext ldapContext = new InitialLdapContext(env, null);
                return ldapContext;
            } catch (NamingException e) {
                throw new RuntimeException("LDAPS Connection Error: " + e.getMessage(), e);
            }
        };
    }

    private static Function<LdapContext, List<String>> bindWithCredentials(String username, String password, String domainName) {
        return context -> {
            try {
                context.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
                context.addToEnvironment(Context.SECURITY_PRINCIPAL, username + "@" + domainName);
                context.addToEnvironment(Context.SECURITY_CREDENTIALS, password);

                return context;
            } catch (NamingException e) {
                throw new RuntimeException("LDAP Bind Error: " + e.getMessage(), e);
            }
        };
    }

    private static Function<LdapContext, List<String>> searchForGroups(String searchUsername) {
        return context -> {
            try {
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
            } catch (NamingException e) {
                throw new RuntimeException("LDAP Search Error: " + e.getMessage(), e);
            }
        };
    }

    private static List<String> getAttributeValues(Attribute attribute) throws NamingException {
        NamingEnumeration<?> attributeValues = attribute.getAll();
        List<String> values = new java.util.ArrayList<>();

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
