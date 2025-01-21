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
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;

public class ReorderPartyDialog extends Window implements Constants {

    public static int WIDTH = 300;
    public static int HEIGHT = 400;

    Actor previousKeyboardFocus, previousScrollFocus;
    private final FocusListener focusListener;
    private final BaseScreen screen;
    private final List<CharacterRecord> playerSelection;

    protected InputListener ignoreTouchDown = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            event.cancel();
            return false;
        }
    };

    public ReorderPartyDialog(Context ctx, BaseScreen screen) {
        super("Drag and Drop to Reorder", Andius.skin.get("dialog", Window.WindowStyle.class));
        this.screen = screen;

        setSkin(Andius.skin);
        setModal(true);
        defaults().pad(10);

        TextButton close = new TextButton("X", Andius.skin);
        getTitleTable().add(close).height(getPadTop());
        close.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event.toString().equals("touchDown")) {
                    hide();
                }
                return false;
            }
        });

        this.playerSelection = new List<>(Andius.skin, "default-12");
        this.playerSelection.setItems(ctx.players());

        ScrollPane sp = new ScrollPane(this.playerSelection, Andius.skin);
        add(sp).expand().fill().minWidth(200);
        row();

        DragAndDrop dragAndDrop = new DragAndDrop();
        dragAndDrop.addSource(new Source(playerSelection) {
            @Override
            public Payload dragStart(InputEvent event, float x, float y, int pointer) {
                Payload payload = new Payload();
                payload.setObject(playerSelection.getSelectedIndex());
                return payload;
            }
        });

        dragAndDrop.addTarget(new Target(playerSelection) {
            @Override
            public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
                return true;
            }

            @Override
            public void reset(Source source, Payload payload) {
            }

            @Override
            public void drop(Source source, Payload payload, float x, float y, int pointer) {
                float totalHeight = playerSelection.getItemHeight() * playerSelection.getItems().size;
                for (int idx = 0; idx < playerSelection.getItems().size; idx++) {
                    float high = totalHeight - playerSelection.getItemHeight() * idx;
                    float low = high - playerSelection.getItemHeight();
                    if (y <= high && y >= low) {
                        CharacterRecord c1 = ctx.players()[(int)payload.getObject()];
                        CharacterRecord c2 = ctx.players()[idx];
                        ctx.players()[idx] = c1;
                        ctx.players()[(int)payload.getObject()] = c2;
                        playerSelection.setItems(ctx.players());
                    }
                }
            }
        });

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
                if (isModal() && stage != null && stage.getRoot().getChildren().size > 0 && stage.getRoot().getChildren().peek() == ReorderPartyDialog.this) {
                    Actor newFocusedActor = event.getRelatedActor();
                    if (newFocusedActor != null && !newFocusedActor.isDescendantOf(ReorderPartyDialog.this) && !(newFocusedActor.equals(previousKeyboardFocus) || newFocusedActor.equals(previousScrollFocus))) {
                        event.cancel();
                    }
                }
            }
        };

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
        stage.setKeyboardFocus(playerSelection);
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
