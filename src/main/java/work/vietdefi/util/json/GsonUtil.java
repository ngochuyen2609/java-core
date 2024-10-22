package work.vietdefi.util.json;

import com.google.gson.*;

import java.util.HashSet;
import java.util.Set;

/**
 * GsonUtil is a utility class that provides a static instance of
 * IGsonConverter for JSON conversion operations.
 * This class centralizes the Gson-related functionality, allowing
 * for easy access and reusability throughout the application.
 */
public class GsonUtil {
    public static Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .create();
    public static Gson beautyGson = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    public static final GsonConverter gsonConverter = new GsonConverter();

    // chuyển đổi một Object sang chuỗi JSON
    public static String toJsonString(Object object) {
        return gsonConverter.toJsonString(object);
    }

    //chuyển đổi chuỗi JSON thành đối tượng Java
    public static <T> T fromJsonString(String jsonString, Class<T> clazz) {
        return gsonConverter.fromJsonString(jsonString, clazz);
    }

    //chuyển đổi một chuỗi JSON thành đối tượng JsonElement
    public static JsonElement toJsonElement(String jsonString) {
        return gsonConverter.toJsonElement(jsonString);
    }

    //chuyển đổi một Object sang JsonElement
    public static JsonElement toJsonElement(Object object) {
        return gsonConverter.toJsonElement(object);
    }

    //chuyển đổi JsonElement thành chuỗi JSON
    public static String fromJsonElementToString(JsonElement jsonElement) {
        return gsonConverter.fromJsonElementToString(jsonElement);
    }

    //chuyển đổi JsonElement thành đối tượng Java
    public static <T> T fromJsonElement(JsonElement jsonElement, Class<T> clazz) {
        return gsonConverter.fromJsonElement(jsonElement, clazz);
    }

    public static JsonObject toJsonObject(String data) {
        try {
            return gson.fromJson(data, JsonObject.class);
        }catch (Exception e){
            return null;
        }
    }

    public static JsonArray toJsonArray(String data) {
        try {
            return gson.fromJson(data, JsonArray.class);
        }catch (Exception e){
            return null;
        }
    }
    public static Set<String> toSet(JsonArray array) {
        Set<String> set = new HashSet<>();
        for(int i = 0; i < array.size(); i++){
            set.add(array.get(i).toString());
        }
        return set;
    }
}

