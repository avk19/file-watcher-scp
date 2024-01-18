import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ActiveDirectoryGroupMemberFetcher {

    public static void main(String[] args) {
        String baseUsername = "yourBaseUsername";
        String baseDomainName = "yourBaseDomain";

        String groupName = "YourADGroupName";

        List<String> users = fetchUsersForADGroup(baseUsername, baseDomainName, groupName);

        System.out.println("Users in Active Directory Group " + groupName + ":");
        for (String user : users) {
            System.out.println(user);
        }
    }

    public static List<String> fetchUsersForADGroup(String baseUsername, String baseDomainName, String groupName) {
        String ldapsUrl = "ldaps://" + baseDomainName + ":636"; // LDAPS URL with port 636

        LdapContext context = null;
        try {
            context = ldapOperation(ldapsUrl);
            bindWithCredentials(context, baseUsername, getPassword(baseUsername, baseDomainName), baseDomainName);

            return getUsersForADGroup(context, groupName);

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

    private static List<String> getUsersForADGroup(LdapContext context, String groupName) throws NamingException {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<SearchResult> results = context.search(
                "CN=Users,DC=yourBaseDomain,DC=com",  // Adjust the base DN as per your AD structure
                "(&(objectClass=user)(memberOf=CN=" + groupName + ",CN=Users,DC=yourBaseDomain,DC=com))",
                searchControls
        );

        List<String> users = new ArrayList<>();
        while (results.hasMore()) {
            SearchResult entry = results.next();
            Attribute sAMAccountNameAttribute = entry.getAttributes().get("sAMAccountName");
            String sAMAccountName = sAMAccountNameAttribute.get().toString();
            users.add(sAMAccountName);
        }

        return users;
    }

    private static String getPassword(String username, String domainName) {
        // Implement a secure way to fetch the password (e.g., from a configuration file, environment variable, etc.)
        return "yourPassword";
    }
}
