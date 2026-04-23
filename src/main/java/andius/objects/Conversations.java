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
        List<Conversation> conversations = this.maps.get(map);
        if (conversations == null) {
            return null;
        }

        List<Conversation> matches = new java.util.ArrayList<>();

        for (Conversation c : conversations) {
            if (c.name.equalsIgnoreCase(name)) {
                matches.add(c);
            }
        }

        if (matches.isEmpty()) {
            return null;
        }

        return matches.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(matches.size()));
    }

    public Conversation getDebug(String map, String name) {
        System.out.println("Conversations.get() map=" + map + ", name=" + name);

        List<Conversation> conversations = this.maps.get(map);
        if (conversations == null) {
            System.out.println("No conversations found for map: " + map);
            return null;
        }

        List<Conversation> matches = new java.util.ArrayList<>();

        for (Conversation c : conversations) {
            System.out.println("Checking conversation: " + c.name);

            if (c.name.equalsIgnoreCase(name)) {
                System.out.println("Matched: " + c.name + " - " + c.description);
                matches.add(c);
            }
        }

        System.out.println("Total matches found: " + matches.size());

        if (matches.isEmpty()) {
            System.out.println("No matching conversation found for name: " + name);
            return null;
        }

        int index = java.util.concurrent.ThreadLocalRandom.current().nextInt(matches.size());
        Conversation selected = matches.get(index);

        System.out.println("Selected match index: " + index);
        System.out.println("Selected conversation: " + selected.name + " - " + selected.description);

        return selected;
    }

    public static class Conversation {

        public String name;
        public String description;
        public List<String> story;

    }

}
