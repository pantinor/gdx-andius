package andius.objects;

import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;

public class Conversations {

    public Map<String, List<Conversation>> maps = new HashMap<>();

    public static Conversations init() throws Exception {
        InputStream is = Conversations.class.getResourceAsStream("/assets/json/conversations.json");
        String json = IOUtils.toString(is);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Conversations conv = gson.fromJson(json, new TypeToken<Conversations>() {
        }.getType());
        return conv;
    }

    public List<Conversation> getConversations(String map) {
        return this.maps.get(map);
    }

    public Conversation get(String map, String name) {
        for (String m : this.maps.keySet()) {
            if (m.equals(map)) {
                for (Conversation c : this.maps.get(m)) {
                    if (c.name.equalsIgnoreCase(name)) {
                        return c;
                    }
                }
            }
        }
        return null;
    }

    public static class Conversation {

        public String name;
        public String description;
        public List<String> story;

    }

}
