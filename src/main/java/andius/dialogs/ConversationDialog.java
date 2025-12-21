package andius.dialogs;

import andius.Context;
import andius.GameScreen;
import andius.objects.Conversations.Conversation;
import andius.objects.Conversations.Label;
import andius.objects.Conversations.Topic;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import java.util.TreeMap;

public class ConversationDialog extends Dialog {

    private final Conversation conv;
    private Label previousLabel;

    public ConversationDialog(Context ctx, GameScreen screen, Conversation conv) {
        super(ctx, screen);

        this.conv = conv;

        Topic greeting = conv.matchTopic("greeting");
        if (greeting != null) {
            Wrapper w = new Wrapper(greeting.phrase);
            recurseLabels(w);
            previousLabel = w.active;
            scrollPane.add(w.phrase);
        }

        if (conv.description != null) {
            Wrapper w = new Wrapper(conv.description);
            recurseLabels(w);
            previousLabel = w.active;
            scrollPane.add("You meet " + w.phrase);
        }

        input.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField tf, char key) {

                if (key == '\r') {

                    if (tf.getText().length() == 0) {
                        hide();
                        return;
                    }

                    String query = tf.getText();

                    if (previousLabel != null) {

                        Topic lt = previousLabel.matchTopic(query);
                        if (lt != null) {
                            Wrapper w = new Wrapper(lt.phrase);
                            recurseLabels(w);
                            previousLabel = w.active;
                            scrollPane.add(w.phrase);
                        } else {

                            lt = previousLabel.matchTopic("default");
                            if (lt != null) {
                                Wrapper w = new Wrapper(lt.phrase);
                                recurseLabels(w);
                                previousLabel = w.active;
                                scrollPane.add(w.phrase);
                            } else {
                                scrollPane.add("That I cannot help thee with.");
                                previousLabel = null;
                            }

                        }

                    } else if (query.contains("name")) {
                        scrollPane.add(conv.name);
                    } else {

                        Topic t = conv.matchTopic(query);
                        if (t != null) {
                            Wrapper w = new Wrapper(t.phrase);
                            recurseLabels(w);
                            previousLabel = w.active;
                            scrollPane.add(w.phrase);
                        } else {
                            scrollPane.add("That I cannot help thee with.");
                        }
                    }

                    tf.setText("");
                }
            }
        });

    }

    private static class Wrapper {

        String phrase;
        Label active;

        public Wrapper(String phrase) {
            this.phrase = phrase;
        }
    }

    private void recurseLabels(Wrapper w) {

        if (w.phrase.contains("%") && this.conv.labels != null) {

            java.util.Map<Integer, Label> order = new TreeMap<>();

            for (Label label : this.conv.labels) {
                int x = w.phrase.indexOf("%" + label.id + "%");
                if (x >= 0) {
                    order.put(x, label);
                }
            }

            if (!order.isEmpty()) {

                for (Label l : order.values()) {
                    w.phrase = w.phrase.replace("%" + l.id + "%", l.query);
                    w.active = l;
                }

                recurseLabels(w);
            }
        }

    }

}
