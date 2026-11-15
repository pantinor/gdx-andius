package andius.dialogs;

import andius.Andius;
import andius.BaseScreen;
import andius.Constants;
import andius.Context;
import andius.WizardryDungeonScreen;
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
        super("Marching order", Andius.HUD.getSkin().get(Window.WindowStyle.class));
        this.screen = screen;

        setSkin(Andius.HUD.getSkin());
        pad(36, 18, 18, 18);
        setModal(true);
        defaults().pad(10);

        TextButton close = new TextButton("Close", Andius.HUD.getSkin());
        getTitleTable().add(close).width(64).height(26).padRight(8);
        close.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event.toString().equals("touchDown")) {
                    hide();
                }
                return false;
            }
        });

        this.playerSelection = new List<>(Andius.HUD.getSkin(), "default-12-padded-clear");
        this.playerSelection.setItems(ctx.players());

        ScrollPane sp = new ScrollPane(this.playerSelection, Andius.HUD.getSkin());
        sp.setFlickScroll(false);
        add(sp).expand().fill().width(310).height(220);
        row();
        add(new com.badlogic.gdx.scenes.scene2d.ui.Label("Drag a name to change the marching order.", Andius.HUD.getSkin()));
        row();

        DragAndDrop dragAndDrop = new DragAndDrop();
        dragAndDrop.setDragTime(0);
        dragAndDrop.addSource(new Source(playerSelection) {
            @Override
            public Payload dragStart(InputEvent event, float x, float y, int pointer) {
                int index = rowAt(x, y);
                if (index < 0) {
                    return null;
                }
                Payload payload = new Payload();
                payload.setObject(index);
                com.badlogic.gdx.scenes.scene2d.ui.Label preview = new com.badlogic.gdx.scenes.scene2d.ui.Label(
                        playerSelection.getItems().get(index).name, Andius.HUD.getSkin(), "default-16");
                preview.pack();
                payload.setDragActor(preview);
                return payload;
            }

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer, Payload payload, Target target) {
                if (target == null && payload != null) {
                    playerSelection.setSelectedIndex((int) payload.getObject());
                }
            }
        });

        dragAndDrop.addTarget(new Target(playerSelection) {
            @Override
            public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
                int index = rowAt(x, y);
                if (index < 0) {
                    return false;
                }
                playerSelection.setSelectedIndex(index);
                return true;
            }

            @Override
            public void reset(Source source, Payload payload) {
            }

            @Override
            public void drop(Source source, Payload payload, float x, float y, int pointer) {
                int destination = rowAt(x, y);
                if (destination < 0) {
                    return;
                }
                int from = (int) payload.getObject();
                CharacterRecord[] players = ctx.players();
                CharacterRecord moving = players[from];
                if (from < destination) {
                    System.arraycopy(players, from + 1, players, from, destination - from);
                } else if (from > destination) {
                    System.arraycopy(players, destination, players, destination + 1, from - destination);
                }
                players[destination] = moving;
                playerSelection.setItems(players);
                playerSelection.setSelectedIndex(destination);
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

    private int rowAt(float x, float y) {
        if (x < 0 || x >= playerSelection.getWidth() || y < 0 || y >= playerSelection.getHeight()) {
            return -1;
        }
        return playerSelection.getItemIndexAt(y);
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

        if (screen instanceof WizardryDungeonScreen) {
            WizardryDungeonScreen wds = (WizardryDungeonScreen) screen;
            wds.setInputProcessor();
        } else if (screen instanceof andius.TmxDungeonScreen dungeon) {
            dungeon.setInputProcessor();
        } else {
            Gdx.input.setInputProcessor(new InputMultiplexer(getStage(), screen));
        }
    }

}
