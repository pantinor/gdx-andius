package andius;

import andius.WizardryData.MazeCell;
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

public class RiddleDialog extends Window implements Constants {

    public static int WIDTH = 300;
    public static int HEIGHT = 300;

    Actor previousKeyboardFocus, previousScrollFocus;
    private final FocusListener focusListener;
    private final WizardryDungeonScreen screen;
    private final MazeCell cell;
    private final Table internalTable;
    private final TextField input;
    private final LogScrollPane scrollPane;

    protected InputListener ignoreTouchDown = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            event.cancel();
            return false;
        }
    };

    public RiddleDialog(Context ctx, WizardryDungeonScreen screen, MazeCell cell) {
        super("", Andius.skin.get("dialog", Window.WindowStyle.class));
        this.screen = screen;
        this.cell = cell;

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

                    String answer = tf.getText().trim();

                    if (answer.equalsIgnoreCase(cell.riddleAnswer)) {
                        scrollPane.add("Correct!");
                        cell.riddleAnswer = "answered";
                        hide();
                    } else {
                        scrollPane.add(cell.message.getText());
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
                if (isModal() && stage != null && stage.getRoot().getChildren().size > 0 && stage.getRoot().getChildren().peek() == RiddleDialog.this) {
                    Actor newFocusedActor = event.getRelatedActor();
                    if (newFocusedActor != null && !newFocusedActor.isDescendantOf(RiddleDialog.this) && !(newFocusedActor.equals(previousKeyboardFocus) || newFocusedActor.equals(previousScrollFocus))) {
                        event.cancel();
                    }
                }
            }
        };

        scrollPane.add(this.cell.message.getText());
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
