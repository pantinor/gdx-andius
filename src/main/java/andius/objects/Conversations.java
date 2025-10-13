package andius.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
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
        public List<Topic> topics = new ArrayList<>();
        public List<Label> labels = new ArrayList<>();

        public Conversation() {

        }

        public Conversation(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public Topic matchTopic(String query) {
            for (Topic t : topics) {
                StringTokenizer st = new StringTokenizer(t.query, " ");
                while (st.hasMoreTokens()) {
                    String tok = st.nextToken().trim().toLowerCase();
                    if (tok.equals("or")) {
                        continue;
                    }
                    if (query.toLowerCase().contains(tok)) {
                        return t;
                    }
                }
            }
            return null;
        }

    }

    public static class Topic {

        public String query;
        public String phrase;

        public Topic() {
        }

        public Topic(String query, String phrase) {
            this.query = query;
            this.phrase = phrase;
        }

        @Override
        public String toString() {
            return "Topic{" + "query=" + query + ", phrase=" + phrase + '}';
        }

    }

    public static class Label {

        public String id;
        public String query;
        public List<Topic> topics = new ArrayList<>();

        public Topic matchTopic(String query) {
            for (Topic t : topics) {
                StringTokenizer st = new StringTokenizer(t.query, " ");
                while (st.hasMoreTokens()) {
                    String tok = st.nextToken().trim().toLowerCase();
                    if (tok.equals("or")) {
                        continue;
                    }
                    if (query.toLowerCase().contains(tok)) {
                        return t;
                    }
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return "Label{" + "id=" + id + ", query=" + query + ", topics=" + topics + '}';
        }

    }
}
