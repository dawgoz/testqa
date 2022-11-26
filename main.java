
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author 56130
 */
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