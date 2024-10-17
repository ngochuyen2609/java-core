package work.vietdefi.basic;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import work.vietdefi.basic.json.IJacksonConverter;
import work.vietdefi.basic.json.JacksonConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the JacksonConverter class, which implements the IJacksonConverter interface.
 */
public class JacksonConverterTest {

    private IJacksonConverter jacksonConverter;

    @BeforeEach
    public void setUp() {
        jacksonConverter = new JacksonConverter();
    }

    // Test object for serialization and deserialization
    public static class TestObject {
        public String name;
        public int age;

        public TestObject(String name, int age) {
            this.name = name;
            this.age = age;
        }

        // Default constructor for deserialization
        public TestObject() {}

        // Override equals to verify object values
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestObject that = (TestObject) obj;
            return age == that.age && name.equals(that.name);
        }
    }

    @Test
    public void testToJsonString() throws JsonProcessingException {
        TestObject obj = new TestObject("Sunny", 7);
        String jsonString = jacksonConverter.toJsonString(obj);
        assertEquals("{\"name\":\"Sunny\",\"age\":7}", jsonString);
    }

    @Test
    public void testFromJsonString() throws JsonProcessingException {
        String jsonString = "{\"name\":\"Sunny\",\"age\":7}";
        TestObject obj = jacksonConverter.fromJsonString(jsonString, TestObject.class);
        assertEquals(new TestObject("Sunny", 7), obj);
    }

    @Test
    public void testToJsonNodeFromObject() throws JsonProcessingException {
        TestObject obj = new TestObject("Sunny", 7);
        JsonNode jsonNode = jacksonConverter.toJsonNode(obj);
        assertEquals("{\"name\":\"Sunny\",\"age\":7}", jsonNode.toString());
    }

    @Test
    public void testToJsonNodeFromString() throws JsonProcessingException {
        String jsonString = "{\"name\":\"Sunny\",\"age\":7}";
        JsonNode jsonNode = jacksonConverter.toJsonNode(jsonString);
        assertEquals("{\"name\":\"Sunny\",\"age\":7}", jsonNode.toString());
    }

    @Test
    public void testFromJsonNodeToString() throws JsonProcessingException {
        JsonNode jsonNode = jacksonConverter.toJsonNode("{\"name\":\"Sunny\",\"age\":7}");
        String jsonString = jacksonConverter.fromJsonNodeToString(jsonNode);
        assertEquals("{\"name\":\"Sunny\",\"age\":7}", jsonString);
    }

    @Test
    public void testFromJsonNodeToObject() throws JsonProcessingException {
        JsonNode jsonNode = jacksonConverter.toJsonNode("{\"name\":\"Sunny\",\"age\":7}");
        TestObject obj = jacksonConverter.fromJsonNode(jsonNode, TestObject.class);
        assertEquals(new TestObject("Sunny", 7), obj);
    }
}