
import andius.objects.Conversations;
import andius.objects.Conversations.Conversation;
import org.testng.annotations.Test;

public class ObjectsTestNG {

    @Test
    public void testScript() throws Exception {
        Conversations convs = Conversations.init();
        for (Conversation c : convs.getConversations()) {
            System.out.println(c);
        }
    }

}
