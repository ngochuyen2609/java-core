package work.vietdefi.basic.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * GsonConverter is an implementation of the IGsonConverter interface,
 * using the Gson library for JSON serialization and deserialization.
 */
public class GsonConverter implements IGsonConverter {
    private final Gson gson;

    public GsonConverter() {
        this.gson = new Gson();
    }

    // Chuyển đổi một đối tượng Java thành chuỗi JSON.
    @Override
    public String toJsonString(Object object) {
        return gson.toJson(object);
    }

    // Chuyển đổi một chuỗi JSON thành một đối tượng Java của loại được chỉ định.
    @Override
    public <T> T fromJsonString(String jsonString, Class<T> clazz) {
        return gson.fromJson(jsonString, clazz);
    }

    //Chuyển đổi một chuỗi JSON thành một đối tượng JsonElement.
    //JsonElement có thể đại diện cho bất kỳ loại dữ liệu nào trong JSON
    @Override
    public JsonElement toJsonElement(String jsonString) {
        return gson.fromJson(jsonString, JsonElement.class);
    }

    //Chuyển đổi một đối tượng Java thành một đối tượng JsonElement.
    @Override
    public JsonElement toJsonElement(Object object) {
        return gson.toJsonTree(object);
    }

    //Chuyển đổi một đối tượng JsonElement thành chuỗi JSON.
    @Override
    public String fromJsonElementToString(JsonElement jsonElement) {
        return gson.toJson(jsonElement);
    }

    //Chuyển đổi một đối tượng JsonElement thành một đối tượng Java của loại được chỉ định.
    @Override
    public <T> T fromJsonElement(JsonElement jsonElement, Class<T> clazz) {
        return gson.fromJson(jsonElement, clazz);
    }
}
