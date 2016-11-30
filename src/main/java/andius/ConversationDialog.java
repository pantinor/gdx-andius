/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius;

/**
 *
 * @author Paul
 */
import andius.objects.Conversations.Conversation;
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
import utils.LogScrollPane;

public class ConversationDialog extends Window implements Constants {

    public static int WIDTH = 300;
    public static int HEIGHT = 400;

    Actor previousKeyboardFocus, previousScrollFocus;
    private final FocusListener focusListener;
    private final GameScreen screen;
    private final Conversation conversation;
    private final Table internalTable;
    private final TextField input;
    private final LogScrollPane scrollPane;
    private Topic previousTopic;

    protected InputListener ignoreTouchDown = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            event.cancel();
            return false;
        }
    };

    public ConversationDialog(GameScreen screen, Conversation conversation) {
        super("", Andius.skin.get("dialog", Window.WindowStyle.class));
        this.screen = screen;
        this.conversation = conversation;

        setSkin(Andius.skin);
        setModal(true);
        defaults().pad(5);

        this.internalTable = new Table(Andius.skin);
        this.internalTable.defaults().pad(5);

        add(this.internalTable).expand().fill();
        row();

        scrollPane = new LogScrollPane(Andius.skin, new Table(), WIDTH);
        scrollPane.setHeight(HEIGHT);

        input = new TextField("", Andius.skin);
        input.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField tf, char key) {

                if (key == '\r') {

                    if (tf.getText().length() == 0) {
                        hide();
                        return;
                    }

                    String query = tf.getText();
                    Topic t = conversation.matchTopic(query);
                    if (t != null) {

                        if (t.getQuery() != null && t.getQuery().equals("join")) {

                        } else {

                            scrollPane.add(t.getPhrase());
                            if (t.getQuestion() != null && t.getQuestion().length() > 0) {
                                scrollPane.add(t.getQuestion());
                            }
                        }

                        previousTopic = t;
                    } else {

                        if (previousTopic != null && previousTopic.getQuestion() != null) {
                            if (query.toLowerCase().contains("y")) {
                                scrollPane.add(previousTopic.getYesResponse());
                            } else {
                                scrollPane.add(previousTopic.getNoResponse());
                            }
                        } else {
                            scrollPane.add("That I cannot help thee with.");
                        }
                        previousTopic = null;
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

        scrollPane.add("You meet " + this.conversation.getDescription().toLowerCase() + ".");

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
