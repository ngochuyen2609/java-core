package work.vietdefi.basic;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.*;
import work.vietdefi.basic.clean.services.common.SimpleResponse;
import work.vietdefi.basic.clean.services.user.UserService;
import work.vietdefi.basic.sql.HikariClient;
import work.vietdefi.basic.sql.ISQLJavaBridge;
import work.vietdefi.basic.sql.SQLJavaBridge;

import java.io.IOException;


import static org.junit.jupiter.api.Assertions.*;


/**
 * Unit tests for UserService.
 * Tests are run against the 'test_user' table, which is dropped after tests complete.
 */
public class UserServiceTest {


    private static ISQLJavaBridge bridge;
    private static UserService userService;
    private static final String TEST_TABLE = "test_user";


    /**
     * Set up resources before all tests.
     * Initialize the SQL bridge and create an instance of UserService.
     */
    @BeforeAll
    static void setup() throws IOException {
        HikariClient hikariClient = new HikariClient("config/sql/databases.json");
        bridge = new SQLJavaBridge(hikariClient); // Create an instance of SqlJavaBridge
        // Initialize the UserService with the test table.
        userService = new UserService(bridge, TEST_TABLE);
    }


    /**
     * Clean up resources after all tests.
     * Drop the 'test_user' table to ensure a clean slate.
     */
    @AfterAll
    static void teardown() throws Exception {
        bridge.update("DROP TABLE IF EXISTS " + TEST_TABLE);
    }


    /**
     * Test user registration.
     * Verify that a user can be successfully registered.
     */
    @Test
    void testRegisterUser() {
        String username = "testUser";
        String password = "password123";

        JsonObject response = userService.register(username, password);
        System.out.println("Register response: " + response);
        assertNotNull(response);
        assertTrue(SimpleResponse.isSuccess(response));


        JsonObject data = response.getAsJsonObject("d");
        assertEquals(username, data.get("username").getAsString());
        assertNotNull(data.get("token").getAsString());
    }

    @Test
    void testRegisterUser_UsernameExists() {
        String username = "testUser";
        String password = "password123";


        JsonObject response = userService.register(username, password);
        assertNotNull(response);
        assertEquals(0, response.get("e").getAsInt(), "Expected success code 0 for new username.");


        JsonObject response2 = userService.register(username, password);
        assertNotNull(response2);
        assertEquals(10, response2.get("e").getAsInt(), "Expected error code 10 for existing username.");
    }

    @Test
    void testLoginUser_UsernameDoesNotExist() {
        String nonExistentUsername = "nonExistentUser";
        String password = "password123";
        JsonObject response = userService.login(nonExistentUsername, password);

        assertNotNull(response);
        assertEquals(10, response.get("e").getAsInt(), "Expected error code 10 for non-existent username.");
    }

    @Test
    void testLoginUser_IncorrectPassword() {
        String username1 = "testUser";
        String password1 = "password123";
        JsonObject response1 = userService.register(username1, password1);

        String username2 = "testUser";
        String incorrectPassword2 = "wrongPassword";
        JsonObject response2 = userService.login(username2, incorrectPassword2);

        assertNotNull(response2);
        assertEquals(11, response2.get("e").getAsInt(), "Expected error code 11 for incorrect password.");
    }

    @Test
    void testAuthorization_InvalidToken() {
        String invalidToken = "aaaaaaaaaaaaa";

        JsonObject response = userService.authorization(invalidToken);
        assertNotNull(response);
        assertEquals(10, response.get("e").getAsInt(), "Expected error code 10 for invalid token.");
    }

    @Test
    void testAuthorization_ExpiredToken() {
        try {
            String username = "testUser";
            String password = "password123";

            JsonObject response = userService.register(username, password);
            assertNotNull(response);
            assertTrue(SimpleResponse.isSuccess(response));


            JsonObject data = response.getAsJsonObject("d");
            assertEquals(username, data.get("username").getAsString());
            assertNotNull(data.get("token").getAsString());

            String expiredToken = data.get("token").getAsString();
            long pastTime = System.currentTimeMillis() - 3600000;
            String query = new StringBuilder("UPDATE ")
                    .append(TEST_TABLE)
                    .append(" SET token_expired = ? WHERE username = ?")
                    .toString();
            bridge.update(query, pastTime, username);

            JsonObject response2 = userService.authorization(expiredToken);

            assertNotNull(response2);
            assertEquals(12, response2.get("e").getAsInt(), "Expected error code 11 for expired token.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Test user login.
     * Verify that a user with valid credentials can log in.
     */
    @Test
    void testLoginUser() {
        String username = "testUser";
        String password = "password123";
        userService.register(username, password);


        JsonObject response = userService.login(username, password);


        assertNotNull(response);
        assertTrue(SimpleResponse.isSuccess(response));
        JsonObject data = response.getAsJsonObject("d");
        assertEquals(username, data.get("username").getAsString());
        assertNotNull(data.get("token").getAsString());
    }


    /**
     * Test token authorization.
     * Verify that a valid token allows authorization.
     */
    @Test
    void testAuthorization() {
        String username = "testUser";
        String password = "password123";
        JsonObject loginResponse = userService.login(username, password);
        String token = loginResponse.getAsJsonObject("d").get("token").getAsString();


        JsonObject authResponse = userService.authorization(token);
        assertTrue(SimpleResponse.isSuccess(authResponse));


        JsonObject fakeResponse = userService.authorization("fake_token");
        assertFalse(SimpleResponse.isSuccess(fakeResponse));
    }
}


