package andius.objects;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "conversations")
public class Conversations {

    private List<Conversation> convs = new ArrayList<>();

    public static Conversations init() throws Exception {
        InputStream file = Conversations.class.getResourceAsStream("/assets/xml/dialogs.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(Conversations.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (Conversations) jaxbUnmarshaller.unmarshal(file);
    }

    @XmlElement(name = "conversation")
    public List<Conversation> getConversations() {
        return convs;
    }

    public Conversation get(String map, String name) {
        for (Conversation c : this.convs) {
            if (c.map.contains(map) && c.name.equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

    public void setConverations(List<Conversation> c) {
        this.convs = c;
    }

    @XmlRootElement(name = "conversation")
    @XmlType(propOrder = {"map", "name", "description", "topics", "labels"})
    public static class Conversation implements Comparable {

        private String map;
        private String name;
        private String description;
        private List<Topic> topics = new ArrayList<>();
        private List<Label> labels = new ArrayList<>();

        @XmlAttribute
        public String getMap() {
            return map;
        }

        @XmlAttribute
        public String getName() {
            return name;
        }

        @XmlAttribute
        public String getDescription() {
            return description;
        }

        @XmlElement(name = "topic")
        public List<Topic> getTopics() {
            return topics;
        }

        @XmlElement(name = "label")
        public List<Label> getLabels() {
            return labels;
        }

        public void setMap(String map) {
            this.map = map;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setTopics(List<Topic> topics) {
            this.topics = topics;
        }

        public void setLabels(List<Label> labels) {
            this.labels = labels;
        }

        public Topic matchTopic(String query) {
            for (Topic t : topics) {
                StringTokenizer st = new StringTokenizer(t.getQuery(), " ");
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
            return String.format("Conversation [map=%s, name=%s, description=%s, topics=%s labels=%s]",
                    map, name, description, topics, labels);
        }

        @Override
        public int compareTo(Object o) {
            Conversation c = (Conversation) o;
            if (this.map.equals(c.getMap())) {
                return this.name.compareTo(c.getName());
            } else {
                return this.map.compareTo(c.getMap());
            }
        }

    }

    @XmlRootElement(name = "topic")
    @XmlType(propOrder = {"query", "phrase"})
    public static class Topic {

        public Topic() {

        }

        public Topic(String query, String phrase) {
            this.query = query;
            this.phrase = phrase;
        }

        private String query;
        private String phrase;

        @XmlAttribute(name = "query")
        public String getQuery() {
            return query;
        }

        @XmlAttribute(name = "phrase")
        public String getPhrase() {
            return phrase;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public void setPhrase(String phrase) {
            this.phrase = phrase;
        }

        @Override
        public String toString() {
            return String.format("Topic [query=%s, phrase=%s]",
                    query, phrase);
        }

    }

    @XmlRootElement(name = "label")
    @XmlType(propOrder = {"id", "query", "topics"})
    public static class Label {

        public Label() {

        }

        private String id;
        private String query;
        private List<Topic> topics = new ArrayList<>();

        @XmlAttribute(name = "query")
        public String getQuery() {
            return query;
        }

        @XmlAttribute(name = "id")
        public String getId() {
            return id;
        }

        @XmlElement(name = "topic")
        public List<Topic> getTopics() {
            return topics;
        }

        public void setTopics(List<Topic> topics) {
            this.topics = topics;
        }

        public Topic matchTopic(String query) {
            for (Topic t : topics) {
                StringTokenizer st = new StringTokenizer(t.getQuery(), " ");
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

        public void setId(String id) {
            this.id = id;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        @Override
        public String toString() {
            return String.format("Label [id=%s, query=%s, topics=%s]",
                    id, query, topics);
        }

    }

}
