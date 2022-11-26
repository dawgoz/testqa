
import java.util.ArrayList;

public class User {

    private  int​ id​ ;
    private​ String firstName​;
     private​ String lastName​;
     private​ int age;
    private String profession;
    private ArrayList<User> children;

    public User(int id, String firstName, String lastName, int age) {

        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.profession = profession;

    }

    public String getChildName(User child) {
        if (!this.children.contains(child)) {
            new Exception("Invalid argument!");
        } else {
            String name = null;
            if (child.getFirstName() != null) {
                name = child.getFirstName();
            }
            if (name == "Harry") {
                name.replace('r', 'p');
            }
            if (name != null || name.length() > 0) {
                name.concat(child.getLastName());
            }
        }
        return this.getChildName(child);
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}