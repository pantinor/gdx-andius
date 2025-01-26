package andius;

import andius.objects.SaveGame.CharacterRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
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

public class MalorDialog extends Window implements Constants {

    public static int WIDTH = 300;
    public static int HEIGHT = 150;

    Actor previousKeyboardFocus, previousScrollFocus;
    private final FocusListener focusListener;
    private final BaseScreen mapScreen;
    private final Table internalTable;
    private final TextField input;
    private final LogScrollPane logPane;

    protected InputListener ignoreTouchDown = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            event.cancel();
            return false;
        }
    };

    public MalorDialog(CharacterRecord caster, BaseScreen sc) {
        super("", Andius.skin.get("dialog", Window.WindowStyle.class));
        this.mapScreen = sc;

        setSkin(Andius.skin);
        setModal(true);
        defaults().pad(5);

        this.internalTable = new Table(Andius.skin);
        this.internalTable.defaults().pad(5);

        add(this.internalTable).expand().fill();
        row();

        logPane = new LogScrollPane(Andius.skin, new Table(), WIDTH);
        logPane.setHeight(HEIGHT);

        input = new TextField("", Andius.skin, "default-16");
        input.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField tf, char key) {
                if (key == '\r') {

                    if (tf.getText().length() == 0) {
                        hide();
                        return;
                    }

                    String coordinates = tf.getText().trim();

                    String regex = "^-?\\d+,-?\\d+,-?\\d+$";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(coordinates);

                    if (matcher.matches()) {
                        String[] coords = coordinates.split(",");
                        int northsouth = Integer.parseInt(coords[0]);
                        int eastwest = Integer.parseInt(coords[1]);
                        int vertical = Integer.parseInt(coords[2]);
                        mapScreen.teleport(vertical, northsouth, eastwest);
                        hide();
                        input.setTextFieldListener(null);
                    } else {
                        logPane.add("Nothing is happening");
                    }
                }
            }
        });

        internalTable.add(logPane).maxWidth(WIDTH).width(WIDTH);
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
                if (isModal() && stage != null && stage.getRoot().getChildren().size > 0 && stage.getRoot().getChildren().peek() == MalorDialog.this) {
                    Actor newFocusedActor = event.getRelatedActor();
                    if (newFocusedActor != null && !newFocusedActor.isDescendantOf(MalorDialog.this) && !(newFocusedActor.equals(previousKeyboardFocus) || newFocusedActor.equals(previousScrollFocus))) {
                        event.cancel();
                    }
                }
            }
        };
        
        logPane.add("Enter the desired number of steps one wishes to travel as East (+) West (-), North (-) South (+) and vertical directions as X,Y,Z.");
        Vector3 v = new Vector3();
        mapScreen.getCurrentMapCoords(v);
        logPane.add("Current coordinates are");
        logPane.add(String.format("East/West [%d]", (int) v.x));
        logPane.add(String.format("North/South [%d]", (int) v.y));
        logPane.add(String.format("Up/Down [%d]", (int) v.z));
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

        Gdx.input.setInputProcessor(new InputMultiplexer(mapScreen, stage));
    }

}
