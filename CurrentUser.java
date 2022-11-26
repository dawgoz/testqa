package sample;

public class CurrentUser {
    private static int id;
    private static String name;
    private static String userName;
    private static String password;
    private static boolean isAdmin;

    public static void userInit(int usrId, String usrName, String usrUserName, String usrPassword, int usrAdmin) {
        id = usrId;
        name = usrName;
        userName = usrUserName;
        password = usrPassword;
        isAdmin = usrAdmin == 1;
    }

    public static boolean getIsAdmin() {
        return isAdmin;
    }

    public static String getName() {
        return name;
    }

    public static String getUserName() {
        return userName;
    }

    public static String getPassword() {
        return password;
    }

    public static int getId() {
        return id;
    }

    public static void setId(int id) {
        CurrentUser.id = id;
    }
}
