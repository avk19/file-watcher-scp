import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.AttributesMapperCallbackHandler;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapName;

public class LdapExample {

    private final LdapTemplate ldapTemplate;

    public LdapExample(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public String getNameInNamespace(String samAccountName) {
        String filter = "(sAMAccountName=" + samAccountName + ")";
        LdapName nameInNamespace = ldapTemplate.searchForObject("", filter, (AttributesMapper<LdapName>) attrs -> {
            // Assuming the "nameInNamespace" is retrieved from the "Attributes"
            return new LdapName(attrs.get("nameInNamespace").get().toString());
        });

        return nameInNamespace != null ? nameInNamespace.toString() : null;
    }

    public static void main(String[] args) {
        // Assuming you have an instance of LdapTemplate
        LdapTemplate ldapTemplate = getYourLdapTemplateInstance();
        LdapExample ldapExample = new LdapExample(ldapTemplate);

        // Replace "yourSamAccountName" with the actual SAM account name
        String nameInNamespace = ldapExample.getNameInNamespace("yourSamAccountName");
        System.out.println("Name in Namespace: " + nameInNamespace);
    }

    private static LdapTemplate getYourLdapTemplateInstance() {
        // Implement logic to create and configure your LdapTemplate instance
        // ...
        return null;
    }
}
