package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;

public class DBUtils {

    private static Connection connection;
    private static String DBMS;
    private static String DBSchema;
    private static String DBUser;
    private static String DBPassword;
    private static String DBServerName;
    private static String DBPortNumber;

    public static String formatDBString() {
        return DBMS + "://" + DBServerName + ":" + DBPortNumber + "/" + DBSchema;
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void dbInit(String dbms, String dbServerName, String dbPortNumber, String dbSchema, String dbUser, String dbPassword) {
        DBMS = dbms;
        DBServerName = dbServerName;
        DBPortNumber = dbPortNumber;
        DBSchema = dbSchema;
        DBUser = dbUser;
        DBPassword = dbPassword;
    }

    public static void dbConnect() throws SQLException {
        try {
            connection = DriverManager.getConnection("jdbc:" + DBMS + "://" + DBServerName + ":" + DBPortNumber + "/" + DBSchema, DBUser, DBPassword);
            System.out.println("Successfully connected to DB " + formatDBString());
        } catch (SQLException e) {
            System.out.println("Connection Failed! " + e);
            e.printStackTrace();
            throw e;
        }
    }

    public static void dbDisconnect() throws SQLException {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
                System.out.println("Successfully disconnected from DB"); //LOG
            }
        } catch (Exception e) {
            System.out.println("Disconnect from DB error!");//LOG
            e.printStackTrace();
            throw e;
        }
    }

    public static void executeQuery(String query) {
        try {
            dbConnect();
            PreparedStatement psInsert = connection.prepareStatement(query);
            System.out.println(psInsert);
            psInsert.executeUpdate();
            psInsert.close();
        } catch (SQLException e) {
            System.out.println("DB error!\nStatement: " + query); //LOG
            e.printStackTrace();
        } finally {
            try {
                dbDisconnect();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void changeScene(ActionEvent event, String fxmlFile, String title, int sceneWidth, int sceneHeight) {
        Parent root = null;
        try {
            root = FXMLLoader.load(Objects.requireNonNull(DBUtils.class.getResource("../views/" + fxmlFile)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle(title);
        assert root != null;
        stage.setScene(new Scene(root, sceneWidth, sceneHeight));
        stage.centerOnScreen();
    }

    public static void signUpUser(ActionEvent event, String name, String username, String password, boolean isCompany, String companyName) {
        PreparedStatement psInsert = null;
        PreparedStatement psCheckUserExists = null;
        ResultSet resultSet = null;
        int companyId = 0;
        try {
            psCheckUserExists = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
            psCheckUserExists.setString(1, username);
            resultSet = psCheckUserExists.executeQuery();

            if (resultSet.isBeforeFirst()) {
                System.out.println("Username is already taken!");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Username is already taken!");
                alert.show();
            } else if (isCompany) {
                psCheckUserExists = connection.prepareStatement("SELECT * FROM companies WHERE name = ?");
                psCheckUserExists.setString(1, companyName);
                resultSet = psCheckUserExists.executeQuery();

                if (resultSet.isBeforeFirst()) {
                    while (resultSet.next()) {
                        companyId = resultSet.getInt("id");
                    }
                    psInsert = connection.prepareStatement("INSERT INTO users (name, username, password, is_admin, company_id) VALUES (?, ?, ?, ?, ?)");
                    psInsert.setString(1, name);
                    psInsert.setString(2, username);
                    psInsert.setString(3, password);
                    psInsert.setInt(4, 0);
                    psInsert.setInt(5, companyId);
                    psInsert.executeUpdate();
                    System.out.println("Created user!"); //LOG
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Congratulations!\nAccount successfully created!");
                    if (alert.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
                        changeScene(event, "log-in.fxml", "Log In", 600, 400);
                    }
                } else {
                    companyId = getNextId("companies");
                    dbConnect();
                    psInsert = connection.prepareStatement("INSERT INTO companies (id, name) values (?,?)");
                    psInsert.setInt(1, companyId);
                    psInsert.setString(2, companyName);
                    psInsert.executeUpdate();
                    System.out.println("Company created!"); //LOG
                    psInsert = connection.prepareStatement("INSERT INTO users (name, username, password, is_admin, company_id) VALUES (?, ?, ?, ?, ?)");
                    psInsert.setString(1, name);
                    psInsert.setString(2, username);
                    psInsert.setString(3, password);
                    psInsert.setInt(4, 0);
                    psInsert.setInt(5, companyId);
                    psInsert.executeUpdate();
                    System.out.println("Created user!"); //LOG
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Congratulations!\nAccount successfully created!");
                    if (alert.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
                        changeScene(event, "log-in.fxml", "Log In", 600, 400);
                    }
                }
            } else {
                psInsert = connection.prepareStatement("INSERT INTO users (name, username, password, is_admin) VALUES (?, ?, ?, ?)");
                psInsert.setString(1, name);
                psInsert.setString(2, username);
                psInsert.setString(3, password);
                psInsert.setInt(4, 0);
                psInsert.executeUpdate();
                System.out.println("Created user!"); //LOG
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Congratulations!\nAccount successfully created!");
                if (alert.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
                    changeScene(event, "log-in.fxml", "Log In", 600, 400);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (psCheckUserExists != null) {
                try {
                    psCheckUserExists.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (psInsert != null) {
                try {
                    psInsert.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void logInUser(ActionEvent event, String username, String password) {
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement("SELECT id, username, password, name, is_admin FROM users where username = ?");
            preparedStatement.setString(1, username);
            resultSet = preparedStatement.executeQuery();

            if (!resultSet.isBeforeFirst()) {
                System.out.println("User not found in the database!");//LOG
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Provided credentials are incorrect!");
                alert.show();
            } else {
                while (resultSet.next()) {
                    int retrieveId = resultSet.getInt("id");
                    String retrievePassword = resultSet.getString("password");
                    String retrievedName = resultSet.getString("name");
                    String retrievedUserName = resultSet.getString("username");
                    int retrievedIsAdmin = resultSet.getInt("is_admin");
                    if (retrievePassword.equals(password)) {
                        System.out.println("Correct password!");
                        CurrentUser.userInit(retrieveId, retrievedName, retrievedUserName, retrievePassword, retrievedIsAdmin);
                        changeScene(event, "logged-in.fxml", "Welcome", 900, 600);
                    } else {
                        System.out.println("Incorrect password!");
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("The provided credentials are incorrect!");
                        alert.show();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static ObservableList<DiscountCB> getDiscounts() {
        ObservableList<DiscountCB> discountList = FXCollections.observableArrayList();
        dbInit("mysql", "localhost", "3306", "game_shop", "root", "password");
        try {
            DBUtils.dbConnect();
            Connection connection = DBUtils.getConnection();
            PreparedStatement preparedStatement;
            ResultSet resultSet;
            preparedStatement = connection.prepareStatement("SELECT * FROM discounts ORDER BY value ASC");
            resultSet = preparedStatement.executeQuery();
            DiscountCB cb;
            while (resultSet.next()) {
                cb = new DiscountCB(resultSet.getInt("id"), resultSet.getInt("value"));
                discountList.add(cb);
            }
            resultSet.close();
        } catch (Exception e) {
            System.out.println("Select from DB error!");
            e.printStackTrace();
        } finally {
            try {
                DBUtils.dbDisconnect();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return discountList;
    }

    public static boolean userDoesExist(String username) {
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        boolean exists = false;
        try {
            dbConnect();
            preparedStatement = connection.prepareStatement("SELECT username, password, name, is_admin FROM users where username = ?");
            preparedStatement.setString(1, username);
            resultSet = preparedStatement.executeQuery();
            exists = resultSet.isBeforeFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            teardown(resultSet, preparedStatement, null);
        }
        if (exists) {
            System.out.println("Username is already taken!");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Username is already taken!");
            alert.show();
        }
        return !exists;
    }

    public static ObservableList<User> getUsersList() {
        ObservableList<User> userList = FXCollections.observableArrayList();
        try {
            DBUtils.dbConnect();
            Connection connection = DBUtils.getConnection();
            PreparedStatement preparedStatement;
            ResultSet resultSet;
            preparedStatement = connection.prepareStatement("SELECT u.id, u.name, u.username, u.password, u.is_admin, c.name company FROM users u, companies c WHERE u.company_id = c.id union all select id, name, username, password, is_admin, '-' company from users where company_id IS NULL");
            resultSet = preparedStatement.executeQuery();
            User user;
            while (resultSet.next()) {
                user = new User(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getString("username"), resultSet.getString("password"), resultSet.getInt("is_admin"), resultSet.getString("company"));
                userList.add(user);
            }
            resultSet.close();
        } catch (Exception e) {
            System.out.println("Select from DB error!");
            e.printStackTrace();
        } finally {
            try {
                DBUtils.dbDisconnect();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return userList;
    }

    public static ObservableList<Category> getCategoryList() {
        ObservableList<Category> categoryList = FXCollections.observableArrayList();
        try {
            DBUtils.dbConnect();
            Connection connection = DBUtils.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT gc.id, gc.name, count(sc.name) sub_count FROM game_categories gc LEFT JOIN sub_categories sc on sc.category_id = gc.id GROUP BY gc.id, gc.name ORDER BY gc.name");
            ResultSet resultSet = preparedStatement.executeQuery();
            Category category;
            while (resultSet.next()) {
                category = new Category(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getInt("sub_count"));
                categoryList.add(category);
            }
            resultSet.close();
        } catch (Exception e) {
            System.out.println("Select from DB error!");
            e.printStackTrace();
        } finally {
            try {
                DBUtils.dbDisconnect();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return categoryList;
    }

    public static ObservableList<SubCategory> getSubCategoryList(int categoryId) {
        ObservableList<SubCategory> subCategoryList = FXCollections.observableArrayList();
        try {
            DBUtils.dbConnect();
            String sql;
            Connection connection = DBUtils.getConnection();
            if (categoryId != 0) {
                sql = "SELECT * FROM sub_categories WHERE category_id = " + categoryId + " ORDER BY name ASC";
            } else {
                sql = "SELECT * FROM sub_categories ORDER BY name ASC";
            }
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            SubCategory subCategory;
            while (resultSet.next()) {
                subCategory = new SubCategory(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getInt("category_id"));
                subCategoryList.add(subCategory);
            }
            resultSet.close();
        } catch (Exception e) {
            System.out.println("Select from DB error!");
            e.printStackTrace();
        } finally {
            try {
                DBUtils.dbDisconnect();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return subCategoryList;
    }

    public static ObservableList<CompanyCB> getCompanyList() {
        ObservableList<CompanyCB> companyList = FXCollections.observableArrayList();
        try {
            DBUtils.dbConnect();
            Connection connection = DBUtils.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM companies ORDER BY name ASC");
            ResultSet resultSet = preparedStatement.executeQuery();
            CompanyCB companyCB;
            while (resultSet.next()) {
                companyCB = new CompanyCB(resultSet.getInt("id"), resultSet.getString("name"));
                companyList.add(companyCB);
            }
            resultSet.close();
        } catch (Exception e) {
            System.out.println("Select from DB error!");
            e.printStackTrace();
        } finally {
            try {
                DBUtils.dbDisconnect();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return companyList;
    }

    public static ObservableList<Game> getGamesList(String condition) {
        ObservableList<Game> gameList = FXCollections.observableArrayList();
        try {
            dbInit("mysql", "localhost", "3306", "game_shop", "root", "password");
            DBUtils.dbConnect();
            Connection connection = DBUtils.getConnection();
            PreparedStatement preparedStatement;
            ResultSet resultSet;
            preparedStatement = connection.prepareStatement("SELECT DISTINCT g.id, g.title, gc.name genre, sc.name sub_genre, g.developer, g.release_date, g.platform, g.price, d.value, g.owner_id  from games g, game_categories gc, discounts d, game_user_ref gur, sub_categories sc WHERE gc.id = g.category_id AND sc.id = g.sub_category_id AND d.id = g.discount_id " + condition);
            resultSet = preparedStatement.executeQuery();
            Game games;
            while (resultSet.next()) {
                games = new Game(resultSet.getInt("id"), resultSet.getString("title"), resultSet.getString("genre"), resultSet.getString("sub_genre"), resultSet.getString("developer"), resultSet.getString("release_date"), resultSet.getString("platform"), resultSet.getDouble("price"), resultSet.getInt("value"), resultSet.getInt("owner_id"));
                gameList.add(games);
            }
            resultSet.close();
        } catch (Exception e) {
            System.out.println("Select from DB error!");
            e.printStackTrace();
        } finally {
            try {
                DBUtils.dbDisconnect();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return gameList;
    }

    public static ObservableList<Game> getGamesListAccess() {
        ObservableList<Game> gameList = FXCollections.observableArrayList();
        try {
            DBUtils.dbConnect();
            Connection connection = DBUtils.getConnection();
            PreparedStatement preparedStatement;
            ResultSet resultSet;
            if (!CurrentUser.getIsAdmin()) {
                preparedStatement = connection.prepareStatement("SELECT DISTINCT g.id, g.title, gc.name genre, sc.name sub_genre, g.developer, g.release_date, g.platform, g.price, d.value, g.owner_id  from games g, game_categories gc, discounts d, game_user_ref gur, sub_categories sc WHERE gc.id = g.category_id AND sc.id = g.sub_category_id AND d.id = g.discount_id AND g.owner_id = " + CurrentUser.getId());
            } else {
                preparedStatement = connection.prepareStatement("SELECT DISTINCT g.id, g.title, gc.name genre, sc.name sub_genre, g.developer, g.release_date, g.platform, g.price, d.value, g.owner_id  from games g, game_categories gc, discounts d, game_user_ref gur, sub_categories sc WHERE gc.id = g.category_id AND sc.id = g.sub_category_id AND d.id = g.discount_id");
            }
            resultSet = preparedStatement.executeQuery();
            Game games;
            while (resultSet.next()) {
                games = new Game(resultSet.getInt("id"), resultSet.getString("title"), resultSet.getString("genre"), resultSet.getString("sub_genre"), resultSet.getString("developer"), resultSet.getString("release_date"), resultSet.getString("platform"), resultSet.getDouble("price"), resultSet.getInt("value"), resultSet.getInt("owner_id"));
                gameList.add(games);
            }
            resultSet.close();
        } catch (Exception e) {
            System.out.println("Select from DB error!");
            e.printStackTrace();
        } finally {
            try {
                DBUtils.dbDisconnect();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return gameList;
    }

    public static ObservableList<User> getUsersAccess(int gameId, String condition) {
        ObservableList<User> userList = FXCollections.observableArrayList();
        try {
            DBUtils.dbConnect();
            Connection connection = DBUtils.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT id, name, username, is_admin, company_id  FROM users WHERE id " + condition + " (SELECT user_id FROM game_user_ref WHERE game_id = " + gameId + ") AND is_admin = 0");
            ResultSet resultSet = preparedStatement.executeQuery();
            User users;
            while (resultSet.next()) {
                users = new User(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getString("username"), null, resultSet.getInt("is_admin"), "");
                userList.add(users);
            }
            resultSet.close();
        } catch (Exception e) {
            System.out.println("Select from DB error!");
            e.printStackTrace();
        } finally {
            try {
                DBUtils.dbDisconnect();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return userList;
    }

    public static int getNextId(String tableName) {
        int value = 0;
        try {
            dbConnect();
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT MAX(id) id FROM " + tableName);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                value = resultSet.getInt("id") + 1;
            }
            teardown(resultSet, preparedStatement, null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static boolean isCategoryDeleteValid(int categoryId) {
        boolean ret = true;
        try {
            dbConnect();
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM games WHERE category_id = " + categoryId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.isBeforeFirst()) {
                teardown(resultSet, preparedStatement, null);
                ret = true;
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Unable to delete category, because it is in use!");
                alert.show();
                teardown(resultSet, preparedStatement, null);
                ret = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ret;
    }

    // public static boolean isSubCategoryDeleteValid(int subCategoryId) {
    //     boolean ret = true;
    //     try {
    //         dbConnect();
    //         Connection connection = getConnection();
    //         PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM games WHERE sub_category_id = " + subCategoryId);
    //         ResultSet resultSet = preparedStatement.executeQuery();

    //         if (!resultSet.isBeforeFirst()) {
    //             teardown(resultSet, preparedStatement, null);
    //             ret = true;
    //         } else {
    //             Alert alert = new Alert(Alert.AlertType.ERROR);
    //             alert.setContentText("Unable to delete sub-category, because it is in use!");
    //             alert.show();
    //             teardown(resultSet, preparedStatement, null);
    //             ret = false;
    //         }
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }

    //     return ret;
    // }

    public static ArrayList<Game> getGames(String condition) throws Exception {
        ArrayList<Game> gameList = new ArrayList<>();
        Game games;
        dbInit("mysql", "localhost", "3306", "game_shop", "root", "password");
        dbConnect();
        Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT g.id, g.title, gc.name, sc.name sub_genre, g.developer, g.release_date, g.platform, g.price, d.value, g.owner_id FROM games g, game_categories gc, discounts d, sub_categories sc " + condition + " AND sc.id = g.sub_category_id and gc.id = g.category_id and d.id = g.discount_id");
        System.out.println(preparedStatement);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            games = new Game(resultSet.getInt("id"), resultSet.getString("title"), resultSet.getString("name"), resultSet.getString("sub_genre"), resultSet.getString("developer"), resultSet.getString("release_date"), resultSet.getString("platform"), resultSet.getDouble("price"), resultSet.getInt("value"), resultSet.getInt("owner_id"));
            gameList.add(games);
        }
        resultSet.close();
        DBUtils.dbDisconnect();
        return gameList;
    }

    public static void insertGame(String title, String genre, String subGenre, String developer, String releaseDate, String platform, int discountId) {
        dbInit("mysql", "localhost", "3306", "game_shop", "root", "password");
        DBUtils.executeQuery("INSERT INTO games (title, category_id, sub_category_id, developer, release_date, platform, discount_id) VALUES ('" + title + "', '" + genre + "', '" + subGenre + "', '" + developer + "', str_to_date('" + releaseDate + "', '%Y-%c-%d'), '" + platform + "'," + discountId + ")");
    }

    public static void deleteGame(int id) {
        dbInit("mysql", "localhost", "3306", "game_shop", "root", "password");
        DBUtils.executeQuery("DELETE FROM games WHERE id = " + id);
    }

    public static void updateGame(String title, String genre, String subGenre, Double price, String developer, String releaseDate, String platform, int discountId, int id) {
        dbInit("mysql", "localhost", "3306", "game_shop", "root", "password");
        DBUtils.executeQuery("UPDATE games SET title = '" + title + "',  category_id = " + genre + ", sub_category_id = " + subGenre + ", price = "+price+", developer= '" + developer + "', release_date = str_to_date('" + releaseDate + "', '%Y-%c-%d'), platform = '" + platform + "' WHERE id = " + id);
    }

    public static void updateGamePrice(String price, int id) {
        dbInit("mysql", "localhost", "3306", "game_shop", "root", "password");
        DBUtils.executeQuery("UPDATE games SET price = " + Double.parseDouble(price) + " WHERE id = " + id);
    }

    public static ArrayList<User> getUsers(String condition) throws Exception {
        ArrayList<User> userList = new ArrayList<>();
        User user;
        dbInit("mysql", "localhost", "3306", "game_shop", "root", "password");
        dbConnect();
        Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users " + condition);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            user = new User(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getString("username"), resultSet.getString("password"), resultSet.getInt("is_admin"), null);
            userList.add(user);
        }
        resultSet.close();
        DBUtils.dbDisconnect();
        return userList;
    }

    // public static void insertUser(String name, String username, String password, int isAdmin) {
    //     dbInit("mysql", "localhost", "3306", "game_shop", "root", "password");
    //     DBUtils.executeQuery("INSERT INTO users (name, username, password, is_admin) VALUES ('" + name + "', '" + username + "', '" + password + "', " + isAdmin + ")");
    // }

    // public static void deleteUser(int id) {
    //     dbInit("mysql", "localhost", "3306", "game_shop", "root", "password");
    //     DBUtils.executeQuery("DELETE FROM users WHERE id = " + id);
    // }

    // public static void updateUser(String name, String username, String password, int isAdmin, int id) {
    //     dbInit("mysql", "localhost", "3306", "game_shop", "root", "password");
    //     DBUtils.executeQuery("UPDATE users SET name = '" + name + "',  username = '" + username + "', password= '" + password + "', is_admin = " + isAdmin + " WHERE id = " + id);
    // }

    public static User getUserByLoginData(String username, String password) {
        dbInit("mysql", "localhost", "3306", "game_shop", "root", "password");
        try {
            dbConnect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ResultSet resultSet = null;
        User user = null;
        PreparedStatement preparedStatement;
        try {
            preparedStatement = connection.prepareStatement("SELECT id, username, password, name, is_admin, company_id FROM users where username = ? AND password = ? ");
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int retrieveId = resultSet.getInt("id");
                String retrievePassword = resultSet.getString("password");
                String retrievedName = resultSet.getString("name");
                String retrievedUserName = resultSet.getString("username");
                int retrievedIsAdmin = resultSet.getInt("is_admin");
                // int retrievedCompanyId = resultSet.getInt("company_id");
                user = new User(retrieveId, retrievedName, retrievedUserName, retrievePassword, retrievedIsAdmin, null);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return user;
    }

    public static void teardown(ResultSet resultSet, PreparedStatement preparedStatement, PreparedStatement preparedStatement2) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (preparedStatement2 != null) {
            try {
                preparedStatement2.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        try {
            dbDisconnect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // public static ObservableList<IsAdmin> getIsAdminList() {
    //     ObservableList<IsAdmin> isAdmins = FXCollections.observableArrayList();
    //     IsAdmin isAdmin;
    //     IsAdmin isAdmin1;
    //     isAdmin = new IsAdmin(0, "No");
    //     isAdmin1 = new IsAdmin(1, "Yes");
    //     isAdmins.add(isAdmin);
    //     isAdmins.add(isAdmin1);
    //     return isAdmins;
    // }
}
