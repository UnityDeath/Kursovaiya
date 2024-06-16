package DB;

import view.MainWindow;

import java.sql.*;

public class DBWorker {
    public static final String PATH_TO_DB_FILE = "mydb.db";
    public static final String URL = "jdbc:sqlite:" + PATH_TO_DB_FILE;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            initializeDatabase();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public DBWorker() {
        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.createStatement().execute("PRAGMA journal_mode = WAL;");
            conn.createStatement().execute("PRAGMA busy_timeout = 5000;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void initializeDatabase() throws SQLException {
        String createSanatoriumTableSQL = "CREATE TABLE IF NOT EXISTS Sanatorium (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "city TEXT," +
                "adress TEXT," +
                "cost INTEGER,"+
                "rating INTEGER," +
                "time TEXT" +
                ");";

        String createHotelTableSQL = "CREATE TABLE IF NOT EXISTS Hotel (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "city TEXT," +
                "adress TEXT," +
                "cost_per_day INTEGER,"+
                "rating INTEGER" +
                ");";

        String createExcursionsTableSQL = "CREATE TABLE IF NOT EXISTS Excursions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "leaving_city TEXT," +
                "leaving_addres TEXT," +
                "arriving_place TEXT," +
                "depature_time TEXT," +
                "arriving_time TEXT," +
                "cost INTEGER" +
                ");";

        String createTicketTableSQL = "CREATE TABLE IF NOT EXISTS Ticket (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "number INTEGER," +
                "leaving_city TEXT," +
                "arriving_city TEXT," +
                "depature_time TEXT," +
                "arriving_time TEXT," +
                "type TEXT" +
                ");";

        try (Connection connection = DriverManager.getConnection(URL);
             Statement statement = connection.createStatement()) {
            statement.execute(createSanatoriumTableSQL);
            statement.execute(createHotelTableSQL);
            statement.execute(createExcursionsTableSQL);
            statement.execute(createTicketTableSQL);
        }
    }

    public ResultSet getAllFromDB(String DB) throws SQLException {
        String query = "SELECT * FROM " + DB;
        System.out.println("Executing query: " + query); // Логируем SQL-запрос
        Connection connection = DriverManager.getConnection(URL);
        Statement statement = connection.createStatement();
        return statement.executeQuery(query);
    }

    public static ResultSet getFromDBById(int id, String DB) throws SQLException {
        String query = "SELECT * FROM "+DB+" WHERE id = ?";
        System.out.println("Executing query: " + query); // Логируем SQL-запрос
        Connection connection = DriverManager.getConnection(URL);
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, id);

        ResultSet resultSet = null;
        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                resultSet = preparedStatement.executeQuery();
                break; // Если запрос выполнен успешно, выходим из цикла
            } catch (SQLException e) {
                if (e.getMessage().contains("database is locked")) {
                    retryCount++;
                    try {
                        Thread.sleep(1000); // Ждем перед следующей попыткой
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    e.printStackTrace();
                    break;
                }
            }
        }
        if (retryCount == maxRetries) {
            System.err.println("Failed to execute query after " + maxRetries + " attempts due to database lock.");
        }
        return resultSet;
    }


    public void addToDB(String table, Object[] values) {
        StringBuilder queryBuilder = new StringBuilder("INSERT INTO ");
        queryBuilder.append(table).append(" VALUES (");

        for (int i = 0; i < values.length; i++) {
            queryBuilder.append("?");
            if (i < values.length - 1) {
                queryBuilder.append(",");
            }
        }
        queryBuilder.append(")");

        String query = queryBuilder.toString();
        System.out.println("Executing query: " + query); // Логируем SQL-запрос

        Connection conn = null;
        PreparedStatement stmt = null;
        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                conn = DriverManager.getConnection(URL);
                stmt = conn.prepareStatement(query);
                for (int i = 0; i < values.length; i++) {
                    stmt.setObject(i + 1, values[i]);
                }
                stmt.executeUpdate();
                break; // Если запрос выполнен успешно, выходим из цикла
            } catch (SQLException e) {
                if (e.getMessage().contains("database is locked")) {
                    retryCount++;
                    try {
                        Thread.sleep(1000); // Ждем перед следующей попыткой
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    e.printStackTrace();
                    break;
                }
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (retryCount == maxRetries) {
            System.err.println("Failed to execute query after " + maxRetries + " attempts due to database lock.");
        }
    }


    public void deleteFromDBById(int id, String table) throws SQLException {
        String query = "DELETE FROM " + table + " WHERE id = ?";
        System.out.println("Executing query: " + query); // Логируем SQL-запрос

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        }
    }

    public static boolean checkCredentials(String login, String password) {
        String query = "SELECT * FROM Users WHERE login = ? AND password = ?";
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, login);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getIdInTablesById(int id) throws SQLException {
        String query = "SELECT idInTables FROM History WHERE id = ?";
        Connection connection = DriverManager.getConnection(URL);
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, id);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getString("idInTables");
        }
        return null; // Если запись с таким id не найдена
    }

    public static void processIdInTables(String idInTables) {
        try {
            String[] idArray = idInTables.split(" ");

            // Проходим по массиву и выполняем необходимые действия
            for (int i = 0; i < idArray.length; i++) {
                if (!idArray[i].equals("0")) {
                    switch (i) {
                        case 0:
                            String[] idTicket = idArray[i].split(",");
                            for (String s : idTicket) {
                                processTicket(Integer.parseInt(s));
                            }
                            break;
                        case 1:
                            String[] idHotel = idArray[i].split(",");
                            for (String s : idHotel) {
                                processHotel(Integer.parseInt(s));
                            }
                            break;
                        case 2:
                            String[] idExcursions = idArray[i].split(",");
                            for (String s : idExcursions) {
                                processExcursions(Integer.parseInt(s));
                            }
                            break;
                        case 3:
                            String[] idSanatorium = idArray[i].split(",");
                            for (String s : idSanatorium) {
                                processSanatorium(Integer.parseInt(s));
                            }
                            break;
                        default:
                            // Возможно, нужно добавить обработку других таблиц
                            break;
                    }
                }
            }
        } catch (NumberFormatException | SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void processTicket(int id) throws SQLException {
        ResultSet resultSet = getFromDBById(id, "Ticket");
        if (resultSet.next()) {
            Object[] rowData = {
                    resultSet.getInt("id"),
                    resultSet.getInt("number"),
                    resultSet.getString("leaving_city"),
                    resultSet.getString("arriving_city"),
                    resultSet.getString("depature_time"),
                    resultSet.getString("date"),
                    resultSet.getString("type"),
                    resultSet.getInt("cost")
            };
            // Предполагаем, что у вас есть метод addRowToTableTickets в классе MainWindow
            MainWindow.addRowToTableTickets(rowData);
        }
    }

    public static void processHotel(int id) throws SQLException {
        ResultSet resultSet = getFromDBById(id, "Hotel");
        if (resultSet.next()) {
            Object[] rowData = {
                    resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getString("city"),
                    resultSet.getString("adress"),
                    resultSet.getInt("cost_per_day"),
                    resultSet.getInt("rating")
            };
            MainWindow.addRowToTableHotels(rowData);
        }
    }

    public static void processExcursions(int id) throws SQLException {
        ResultSet resultSet = getFromDBById(id, "Excursions");
        if (resultSet.next()) {
            Object[] rowData = {
                    resultSet.getInt("id"),
                    resultSet.getString("leaving_city"),
                    resultSet.getString("leaving_addres"),
                    resultSet.getString("arriving_place"),
                    resultSet.getString("depature_time"),
                    resultSet.getString("arriving_time"),
                    resultSet.getInt("cost")
            };
            MainWindow.addRowToTableExcursions(rowData);
        }
    }

    public static void processSanatorium(int id) throws SQLException {
        ResultSet resultSet = getFromDBById(id, "Sanatorium");
        if (resultSet.next()) {
            Object[] rowData = {
                    resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getString("city"),
                    resultSet.getString("adress"),
                    resultSet.getInt("cost"),
                    resultSet.getInt("rating"),
                    resultSet.getString("time")
            };
            MainWindow.addRowToTableSanatoriums(rowData);
        }
    }

}