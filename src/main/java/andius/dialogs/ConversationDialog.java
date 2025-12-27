package andius.dialogs;

import andius.Context;
import andius.GameScreen;
import andius.objects.Conversations.Conversation;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class ConversationDialog extends Dialog {

    private final Conversation conv;
    private int index = 0;

    public ConversationDialog(Context ctx, GameScreen screen, Conversation conv) {
        super(ctx, screen);

        this.conv = conv;

        if (conv.description != null) {
            scrollPane.add("You meet " + conv.name + ". " + conv.description);
        }

        input.setTextFieldListener((TextField tf, char key) -> {
            tf.setText("");
            
            if (index >= conv.story.size()) {
                hide();
                return;
            }
            
            scrollPane.add(conv.story.get(index));
            
            index++;
        });

    }

}
