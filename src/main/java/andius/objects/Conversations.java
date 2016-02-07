package andius.objects;

import andius.Constants;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "conversations")
public class Conversations {

    public static final String[] STANDARD_QUERY = {"job", "health", "look", "name", "give", "join"};

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

    public void setConverations(List<Conversation> c) {
        this.convs = c;
    }

    @XmlRootElement(name = "conversation")
    public static class Conversation {

        private Constants.Map map;
        private String name;
        private String pronoun;
        private String description;
        private List<Topic> topics = new ArrayList<>();

        @XmlAttribute
        @XmlJavaTypeAdapter(MapAdapter.class)
        public Constants.Map getMap() {
            return map;
        }

        @XmlAttribute
        public String getName() {
            return name;
        }

        @XmlAttribute
        public String getPronoun() {
            return pronoun;
        }

        @XmlAttribute
        public String getDescription() {
            return description;
        }

        @XmlElement(name = "topic")
        public List<Topic> getTopics() {
            return topics;
        }

        public void setMap(Constants.Map map) {
            this.map = map;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setPronoun(String pronoun) {
            this.pronoun = pronoun;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setTopics(List<Topic> topics) {
            this.topics = topics;
        }

        public static boolean isStandardQuery(String query) {
            for (String st : STANDARD_QUERY) {
                if (query.toLowerCase().contains(st)) {
                    return true;
                }
            }
            return false;
        }

        public Topic matchTopic(String query) {
            for (Topic t : topics) {
                if (query.toLowerCase().contains(t.getQuery().toLowerCase().trim())) {
                    return t;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return String.format("Conversation [map=%s, name=%s, pronoun=%s, description=%s, topics=%s]",
                    map, name, pronoun, description, topics);
        }

    }

    public static class MapAdapter extends XmlAdapter<String, Constants.Map> {

        @Override
        public String marshal(Constants.Map t) {
            return t.toString();
        }

        @Override
        public Constants.Map unmarshal(String val) {
            return Constants.Map.valueOf(val);
        }
    }

    @XmlRootElement(name = "topic")
    public static class Topic {

        private String query;
        private String phrase;
        private String question;
        private String yesResponse;
        private String noResponse;

        @XmlAttribute(name = "query")
        public String getQuery() {
            return query;
        }

        @XmlAttribute(name = "phrase")
        public String getPhrase() {
            return phrase;
        }

        @XmlAttribute(name = "question")
        public String getQuestion() {
            return question;
        }

        @XmlAttribute(name = "yes")
        public String getYesResponse() {
            return yesResponse;
        }

        @XmlAttribute(name = "no")
        public String getNoResponse() {
            return noResponse;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public void setPhrase(String phrase) {
            this.phrase = phrase;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public void setYesResponse(String yesResponse) {
            this.yesResponse = yesResponse;
        }

        public void setNoResponse(String noResponse) {
            this.noResponse = noResponse;
        }

        @Override
        public String toString() {
            return String.format("Topic [query=%s, phrase=%s, question=%s, yesResponse=%s, noResponse=%s]",
                    query, phrase, question, yesResponse, noResponse);
        }

    }

}
