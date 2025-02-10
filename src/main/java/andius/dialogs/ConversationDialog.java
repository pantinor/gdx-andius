package andius.dialogs;

import andius.Andius;
import andius.Constants;
import andius.Context;
import andius.GameScreen;
import andius.objects.Conversations.Conversation;
import andius.objects.Conversations.Label;
import andius.objects.Conversations.Topic;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import java.util.TreeMap;
import utils.LogScrollPane;

public class ConversationDialog extends Window implements Constants {

    public static int WIDTH = 300;
    public static int HEIGHT = 400;

    Actor previousKeyboardFocus, previousScrollFocus;
    private final FocusListener focusListener;
    private final GameScreen screen;
    private final Conversation conv;
    private final Table internalTable;
    private final TextField input;
    private final LogScrollPane scrollPane;
    private Label previousLabel;

    protected InputListener ignoreTouchDown = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            event.cancel();
            return false;
        }
    };

    public ConversationDialog(Context ctx, GameScreen screen, Conversation conv) {
        super("", Andius.skin.get("dialog", Window.WindowStyle.class));
        this.screen = screen;
        this.conv = conv;

        setSkin(Andius.skin);
        setModal(true);
        defaults().pad(5);

        this.internalTable = new Table(Andius.skin);
        this.internalTable.defaults().pad(5);

        add(this.internalTable).expand().fill();
        row();

        scrollPane = new LogScrollPane(Andius.skin, new Table(Andius.skin), WIDTH);
        scrollPane.setHeight(HEIGHT);

        input = new TextField("", Andius.skin, "default-16");
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
                            Wrapper w = new Wrapper(lt.getPhrase());
                            recurseLabels(w);
                            previousLabel = w.active;
                            scrollPane.add(w.phrase);
                        } else {

                            lt = previousLabel.matchTopic("default");
                            if (lt != null) {
                                Wrapper w = new Wrapper(lt.getPhrase());
                                recurseLabels(w);
                                previousLabel = w.active;
                                scrollPane.add(w.phrase);
                            } else {
                                scrollPane.add("That I cannot help thee with.");
                                previousLabel = null;
                            }
                            
                        }

                    } else if (query.contains("name")) {

                        scrollPane.add(conv.getName());

                    } else {

                        Topic t = conv.matchTopic(query);
                        if (t != null) {
                            Wrapper w = new Wrapper(t.getPhrase());
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

        internalTable.add(scrollPane).maxWidth(WIDTH).width(WIDTH);
        internalTable.row();
        internalTable.add(input).maxWidth(WIDTH).width(WIDTH);

        focusListener = new FocusListener() {
            @Override
            public void keyboardFocusChanged(FocusListener.FocusEvent event, Actor actor, boolean focused) {
                if (!focused) {
                    focusChanged(event);
                }
            }

            @Override
            public void scrollFocusChanged(FocusListener.FocusEvent event, Actor actor, boolean focused) {
                if (!focused) {
                    focusChanged(event);
                }
            }

            private void focusChanged(FocusListener.FocusEvent event) {
                Stage stage = getStage();
                if (isModal() && stage != null && stage.getRoot().getChildren().size > 0 && stage.getRoot().getChildren().peek() == ConversationDialog.this) {
                    Actor newFocusedActor = event.getRelatedActor();
                    if (newFocusedActor != null && !newFocusedActor.isDescendantOf(ConversationDialog.this) && !(newFocusedActor.equals(previousKeyboardFocus) || newFocusedActor.equals(previousScrollFocus))) {
                        event.cancel();
                    }
                }
            }
        };

        Topic greeting = conv.matchTopic("greeting");
        if (greeting != null) {
            Wrapper w = new Wrapper(greeting.getPhrase());
            recurseLabels(w);
            previousLabel = w.active;
            scrollPane.add(w.phrase);
        }
        
        if (conv.getDescription() != null) {
            Wrapper w = new Wrapper(conv.getDescription());
            recurseLabels(w);
            previousLabel = w.active;
            scrollPane.add("You meet " + w.phrase);
        }

    }

    private static class Wrapper {

        String phrase;
        Label active;

        public Wrapper(String phrase) {
            this.phrase = phrase;
        }
    }

    private void recurseLabels(Wrapper w) {

        if (w.phrase.contains("%") && this.conv.getLabels() != null) {

            java.util.Map<Integer, Label> order = new TreeMap<>();

            for (Label label : this.conv.getLabels()) {
                int x = w.phrase.indexOf("%" + label.getId() + "%");
                if (x >= 0) {
                    order.put(x, label);
                }
            }

            if (!order.isEmpty()) {

                for (Label l : order.values()) {
                    w.phrase = w.phrase.replace("%" + l.getId() + "%", l.getQuery());
                    w.active = l;
                }

                recurseLabels(w);
            }
        }

    }

    public void show(Stage stage) {

        clearActions();

        removeCaptureListener(ignoreTouchDown);

        previousKeyboardFocus = null;
        Actor actor = stage.getKeyboardFocus();
        if (actor != null && !actor.isDescendantOf(this)) {
            previousKeyboardFocus = actor;
        }

        previousScrollFocus = null;
        actor = stage.getScrollFocus();
        if (actor != null && !actor.isDescendantOf(this)) {
            previousScrollFocus = actor;
        }

        pack();

        stage.addActor(this);
        stage.setKeyboardFocus(input);
        stage.setScrollFocus(this);

        Gdx.input.setInputProcessor(stage);

        Action action = sequence(Actions.alpha(0), Actions.fadeIn(0.4f, Interpolation.fade));
        addAction(action);

        setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));
    }

    public void hide() {
        Action action = sequence(fadeOut(0.4f, Interpolation.fade), Actions.removeListener(ignoreTouchDown, true), Actions.removeActor());

        Stage stage = getStage();

        if (stage != null) {
            removeListener(focusListener);
        }

        if (action != null) {
            addCaptureListener(ignoreTouchDown);
            addAction(sequence(action, Actions.removeListener(ignoreTouchDown, true), Actions.removeActor()));
        } else {
            remove();
        }

        Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
    }

}
