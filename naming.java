package az.alizeynalli.cleancode.naming;

public class Naming {

    public Date d; // modified date
    public List<Testcase> find(User user){}; //find all Testcases by User
    
    public Account[] AccountList; // use Suffix List, if it is really a List 

    public void deleteUser(User pUser){}; // no need for prefix p for parameters
    
    public URL r; // not everybody would think r will be url
  
    public class TableRepresent{};
    pubcli void car(){}; // unless it is constrcutor
  
    public saveUser(){}; createAccount(){}; generateContract(){};

    public class AccountFactory{} // Factory Pattern
    public class AccountObserver{} // Observer Pattern
    
    public class User; // taking "User" as a Use Case for same context
    public String userName;  
    public void editUser(User user){};

    public void saveIfUserUnder18(){};
    public class UserFormRealTimeFiller {};
    
    public int daysInYear = 360;
    public int hoursInWeek = 168;
}