import javax.annotation.Nonnull;
import javax.annotation.RegEx;

// this is a DTO used in a Jersey endpoint
public class User {

    @RegEx("^[a-zA-Z0-9]*$")
    private String firstName;

    private String lastName;

    @Nonnull
    private String email;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}