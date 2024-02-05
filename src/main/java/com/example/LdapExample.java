import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.LdapOperationsCallback;
import javax.naming.Name;
import javax.naming.directory.DirContext;

public class LdapExample {

    private final LdapTemplate ldapTemplate;

    public LdapExample(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public String getNameInNamespace(String username) {
        return ldapTemplate.executeReadOnly((LdapOperationsCallback<String>) ldapOperations -> {
            DirContext dirContext = ldapOperations.getContextSource().getContext(username, "yourPassword");
            Name name = dirContext.getNameInNamespace();
            return name.toString();
        });
    }

    public static void main(String[] args) {
        // Assuming you have an instance of LdapTemplate
        LdapTemplate ldapTemplate = getYourLdapTemplateInstance();
        LdapExample ldapExample = new LdapExample(ldapTemplate);

        // Replace "yourUsername" with the actual username
        String nameInNamespace = ldapExample.getNameInNamespace("yourUsername");
        System.out.println("Name in Namespace: " + nameInNamespace);
    }

    private static LdapTemplate getYourLdapTemplateInstance() {
        // Implement logic to create and configure your LdapTemplate instance
        // ...
        return null;
    }
}
