package work.vietdefi.basic.json;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JacksonConverter is an implementation of the IJacksonConverter interface,
 * using the Jackson library for JSON serialization and deserialization.
 */
public class JacksonConverter implements IJacksonConverter {
    private final ObjectMapper objectMapper;

    public JacksonConverter() {
        this.objectMapper = new ObjectMapper();
    }

    //Chuyển đổi một đối tượng Java thành chuỗi JSON.
    @Override
    public String toJsonString(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    //Chuyển đổi một chuỗi JSON thành một đối tượng Java.
    @Override
    public <T> T fromJsonString(String jsonString, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(jsonString, clazz);
    }

    //Chuyển chuỗi JSON thành đối tượng JsonNode
    @Override
    public JsonNode toJsonNode(String jsonString) throws JsonProcessingException {
        return objectMapper.readTree(jsonString);
    }

    //Chuyển đổi một đối tượng Java thành JsonNode
    @Override
    public JsonNode toJsonNode(Object object) throws JsonProcessingException {
        return objectMapper.valueToTree(object);
    }

    //Chuyển JsonNode thành chuỗi JSON.
    @Override
    public String fromJsonNodeToString(JsonNode jsonNode) {
        return jsonNode.toString();
    }

    //Chuyển đổi JsonNode thành đối tượng Java của lớp clazz.
    @Override
    public <T> T fromJsonNode(JsonNode jsonNode, Class<T> clazz) {
        return objectMapper.convertValue(jsonNode, clazz);
    }
}