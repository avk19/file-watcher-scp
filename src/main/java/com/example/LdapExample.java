import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;

import javax.naming.directory.Attributes;
import javax.naming.directory.InvalidAttributeValueException;
import javax.naming.directory.SearchControls;
import java.util.List;
import java.util.Optional;

public class LdapExample {

    private final LdapTemplate ldapTemplate;

    public LdapExample(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public Optional<String> getDnByUsername(String username) {
        String baseDn = "ou=people,dc=example,dc=com"; // Your LDAP base DN

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        List<String> distinguishedNames = ldapTemplate.search(
                baseDn,
                "(uid=" + username + ")",
                searchControls,
                (AttributesMapper<String>) attributes -> {
                    try {
                        return attributes.get("distinguishedName").get().toString();
                    } catch (InvalidAttributeValueException e) {
                        throw new RuntimeException("Error retrieving distinguishedName", e);
                    }
                }
        );

        return distinguishedNames.stream().findFirst();
    }

    public static void main(String[] args) {
        // Initialize your LdapTemplate (you need to configure your LDAP context source)
        // LdapTemplate ldapTemplate = ...

        LdapExample ldapExample = new LdapExample(ldapTemplate);

        // Example: Get the distinguishedName for a specific username
        String username = "john.doe";
        Optional<String> distinguishedName = ldapExample.getDnByUsername(username);

        distinguishedName.ifPresentOrElse(
                dn -> System.out.println("Distinguished Name for " + username + ": " + dn),
                () -> System.out.println("Username not found: " + username)
        );
    }
}
