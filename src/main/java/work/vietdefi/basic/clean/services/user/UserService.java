package work.vietdefi.basic.clean.services.user;


import com.google.gson.JsonObject;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import work.vietdefi.basic.clean.services.common.SimpleResponse;
import work.vietdefi.basic.sql.ISQLJavaBridge;
import org.apache.commons.codec.digest.DigestUtils;

import java.math.BigInteger;


/**
 * UserService is an implementation of the IUserService interface.
 * It manages user operations such as registration, login, and authorization,
 * and interacts with the database via the ISQLJavaBridge.
 */
public class UserService implements IUserService {

    // A bridge to interact with the underlying SQL database.
    private ISQLJavaBridge bridge;

    // The name of the table where user data is stored.
    private String table;


    /**
     * Constructs a new UserService with the given SQL bridge and table name.
     * If the specified table does not exist, it is automatically created.
     *
     * @param bridge The ISQLJavaBridge used to interact with the database.
     * @param table  The name of the user table in the database.
     */
    public UserService(ISQLJavaBridge bridge, String table) {
        this.bridge = bridge;
        this.table = table;

        // Check if the table exists, and create it if necessary.
        if (!bridge.checkTableExisting(table)) {
            createTable();
        }
    }


    /**
     * Creates the user table with the appropriate schema and indexes.
     * The schema includes columns for user ID, username, password, token,
     * and token expiration time.
     */
    private void createTable() {
        String createTableSQL = ("CREATE TABLE IF NOT EXISTS table_name ("
                + "user_id BIGINT PRIMARY KEY AUTO_INCREMENT," // Unique ID for each user.
                + "username VARCHAR(64) UNIQUE,"               // Username, must be unique.
                + "password VARCHAR(2048),"                    // Encrypted password.
                + "token VARCHAR(2048),"                       // Authentication token.
                + "token_expired BIGINT DEFAULT 0"             // Expiration timestamp for the token.
                + ")").replace("table_name", table);


        // SQL to create a unique index on the username column.
        String indexSQL1 = "CREATE UNIQUE INDEX table_name_username_uindex ON table_name (username)"
                .replace("table_name", table);


        // SQL to create an index on the token column.
        String indexSQL2 = "CREATE INDEX table_name_token_index ON table_name (token)"
                .replace("table_name", table);


        // Execute the SQL statements to create the table and indexes.
        bridge.createTable(createTableSQL, indexSQL1, indexSQL2);
    }


    /**
     * Registers a new user with the provided username and password.
     *
     * @param username The username for the new user.
     * @param password The password for the new user (should be encrypted before storage).
     * @return A JsonObject indicating the result of the registration (e.g., success or error).
     */
    @Override
    public JsonObject register(String username, String password) {
        try {
            String query = new StringBuilder("SELECT * FROM ")
                    .append(table)
                    .append(" WHERE username = ?")
                    .toString();
            boolean exist = bridge.queryExist(query, username);
            System.out.println("Checking existence for username: " + username + ", exist: " + exist);
            if (exist) {
                return SimpleResponse.createResponse(10);
            }

            query = new StringBuilder("INSERT INTO ")
                    .append(table)
                    .append(" (username, password, token, token_expired) VALUES (?, ?, ?, ?)")
                    .toString();
            String hashedPassword = DigestUtils.sha512Hex(password);

            long create_time = System.currentTimeMillis();
            String token = RandomStringUtils.randomAlphanumeric(8);
            long token_expired = create_time;

            BigInteger generatedKey = (BigInteger) bridge.insert(query, username, hashedPassword, token, token_expired);

            System.out.println("generatedKey: " + generatedKey);
            JsonObject response = get(generatedKey.longValue());

            System.out.println("response: " + response);
            if (response == null) {
                System.out.println("Response is null for generated key: " + generatedKey);
                return SimpleResponse.createResponse(1);
            }

            JsonObject data = response.getAsJsonObject("d");
            System.out.println("Data: " + data);
            return login(data);
        } catch (Exception e) {
            e.printStackTrace();
            return SimpleResponse.createResponse(1);
        }
    }

    public JsonObject login(JsonObject data) {
        try {
            // Tạo token ngẫu nhiên
            String token = RandomStringUtils.randomAlphanumeric(16);
            long tokenExpired = System.currentTimeMillis() + 3600000;

            String query = new StringBuilder("UPDATE ")
                    .append(table)
                    .append(" SET token = ?, token_expired = ? WHERE user_id = ?")
                    .toString();
            bridge.update(query, token, tokenExpired, data.get("user_id").getAsInt());

            data.addProperty("token", token);
            data.addProperty("token_expired", tokenExpired);

            data.remove("password");

            return SimpleResponse.createResponse(0, data);
        } catch (Exception e) {
            e.printStackTrace();
            return SimpleResponse.createResponse(1);
        }
    }


    /**
     * Logs in a user by validating the provided username and password.
     *
     * @param username The username of the user attempting to log in.
     * @param password The corresponding password for authentication.
     * @return A JsonObject containing the login result (e.g., token or error message).
     */
    @Override
    public JsonObject login(String username, String password) {
        try{
            String query = new StringBuilder("SELECT * FROM ")
                    .append(table)
                    .append(" WHERE username = ?")
                    .toString();
            JsonObject data = bridge.queryOne(query, username);
            if(data == null) {
                return SimpleResponse.createResponse(10);
            }
            String storedPassword = data.get("password").getAsString();
            String hashedPassword = DigestUtils.sha512Hex(password);
            if(!storedPassword.equals(hashedPassword)){
                return SimpleResponse.createResponse(11);
            }
            return login(data);
        }catch (Exception e){
            return SimpleResponse.createResponse(1);
        }
    }




    /**
     * Authorizes a user by validating the provided authentication token.
     *
     * @param token The token to be verified for authorization.
     * @return A JsonObject indicating whether the token is valid or expired, along with any relevant data.
     */
    @Override
    public JsonObject authorization(String token) {
        try{
            String query = new StringBuilder("SELECT * FROM ")
                    .append(table)
                    .append(" WHERE  token = ?")
                    .toString();
            JsonObject data = bridge.queryOne(query, token);
            if(data == null){
                return SimpleResponse.createResponse(10);
            }

            long token_expired = data.get("token_expired").getAsLong();
            long now = System.currentTimeMillis();
            if(token_expired < now){
                return SimpleResponse.createResponse(12, data);
            }
            data.remove("password");
            return SimpleResponse.createResponse(0, data);
        }catch (Exception e){

            return SimpleResponse.createResponse(1);
        }
    }

    @Override
    public JsonObject get(long user_id) {
        try{
            String query = new StringBuilder("SELECT * FROM ")
                    .append(table)
                    .append(" WHERE user_id = ?")
                    .toString();
            JsonObject json = bridge.queryOne(query, BigInteger.valueOf(user_id));
            if(json == null){
                return SimpleResponse.createResponse(10);
            }
            json.remove("password");
            return SimpleResponse.createResponse(0, json);
        }catch (Exception e){
            return SimpleResponse.createResponse(1);
        }
    }
}

