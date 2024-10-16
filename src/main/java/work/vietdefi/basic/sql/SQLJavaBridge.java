package work.vietdefi.basic.sql;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.sql.*;

public class SQLJavaBridge implements ISQLJavaBridge {
    private final HikariClient sqlClient;

    public SQLJavaBridge(HikariClient sqlClient) {
        this.sqlClient = sqlClient;
    }

    @Override
    public boolean checkTableExisting(String table) {
        String query = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?";
        try (Connection connection = sqlClient.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, table);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public boolean createTable(String createTableSQL, String... createIndexSql) {
        boolean result = false;
        Connection connection = null;
        PreparedStatement[] listSt = null;

        try {
            connection = sqlClient.getConnection();
            connection.setAutoCommit(false);

            listSt = new PreparedStatement[createIndexSql.length + 1];
            listSt[0] = connection.prepareStatement(createTableSQL);
            listSt[0].executeUpdate();

            for (int i = 1; i <= createIndexSql.length; i++) {
                listSt[i] = connection.prepareStatement(createIndexSql[i - 1]);
                listSt[i].executeUpdate();
            }

            connection.commit();
            result = true;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (listSt != null) {
                for (PreparedStatement ps : listSt) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    @Override
    public JsonObject convertResultSetToJsonObject(ResultSet resultSet) throws SQLException {
        JsonObject jsonObject = new JsonObject();
        if (resultSet.next()) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                jsonObject.addProperty(columnName, resultSet.getObject(i).toString());
            }
        }
        return jsonObject;
    }

    @Override
    public JsonArray convertResultSetToJsonArray(ResultSet resultSet) throws SQLException {
        JsonArray jsonArray = new JsonArray();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (resultSet.next()) {
            JsonObject jsonObject = new JsonObject();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                jsonObject.addProperty(columnName, resultSet.getObject(i).toString());
            }
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    @Override
    public JsonObject queryOne(String query, Object... params) throws Exception {
        try (Connection connection = sqlClient.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            setParameters(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                return convertResultSetToJsonObject(resultSet);
            }
        }
    }

    @Override
    public JsonArray queryArray(String query, Object... params) throws Exception {
        try (Connection connection = sqlClient.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            setParameters(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                return convertResultSetToJsonArray(resultSet);
            }
        }
    }

    @Override
    public Object insert(String query, Object... params) throws Exception {
        try (Connection connection = sqlClient.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            setParameters(statement, params);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getObject(1);
                }
            }
        }
        return null;
    }

    @Override
    public int update(String query, Object... params) throws Exception {
        try (Connection connection = sqlClient.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            setParameters(statement, params);
            return statement.executeUpdate();
        }
    }

    @Override
    public void close() {
        sqlClient.close();
    }

    private void setParameters(PreparedStatement statement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }
}