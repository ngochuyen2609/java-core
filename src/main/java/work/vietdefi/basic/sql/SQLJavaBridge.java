package work.vietdefi.basic.sql;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.sql.*;

public class SQLJavaBridge implements ISQLJavaBridge {
    private HikariClient sqlClient;

    public SQLJavaBridge(HikariClient sqlClient) {
        this.sqlClient = sqlClient;
    }

    @Override
    public boolean checkTableExisting(String table) {
        boolean result = false;
        try (Connection connection = sqlClient.getConnection();
             ResultSet resultSet = connection.getMetaData().getTables(null, null, table, null)) {
            result = resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean createTable(String createTableSQL, String... createIndexSql) {
        boolean result = false;
        try (Connection connection = sqlClient.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement createTableStatement = connection.prepareStatement(createTableSQL)) {
                createTableStatement.executeUpdate();
                for (String indexSql : createIndexSql) {
                    try (PreparedStatement createIndexStatement = connection.prepareStatement(indexSql)) {
                        createIndexStatement.executeUpdate();
                    }
                }
                connection.commit();
                result = true;
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public JsonObject convertResultSetToJsonObject(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        JsonObject jsonObject = new JsonObject();

        if (resultSet.next()) {
            for (int i = 1; i <= columnCount; i++) {
                jsonObject.addProperty(metaData.getColumnName(i), resultSet.getObject(i).toString());
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
                jsonObject.addProperty(metaData.getColumnName(i), resultSet.getObject(i).toString());
            }
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    @Override
    public JsonObject queryOne(String query, Object... params) throws Exception {
        try (Connection connection = sqlClient.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return convertResultSetToJsonObject(resultSet);
            }
        }
    }

    @Override
    public JsonArray queryArray(String query, Object... params) throws Exception {
        try (Connection connection = sqlClient.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return convertResultSetToJsonArray(resultSet);
            }
        }
    }

    @Override
    public Object insert(String query, Object... params) throws Exception {
        Object generatedKey = null;
        try (Connection connection = sqlClient.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedKey = generatedKeys.getObject(1);
                    }
                }
            }
        }
        return generatedKey;
    }

    @Override
    public int update(String query, Object... params) throws Exception {
        try (Connection connection = sqlClient.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            return statement.executeUpdate();
        }
    }

    @Override
    public void close() {
        sqlClient.close();
    }
}
