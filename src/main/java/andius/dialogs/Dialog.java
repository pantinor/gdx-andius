package andius.dialogs;

import andius.Andius;
import andius.BaseScreen;
import andius.Constants;
import andius.Context;
import andius.WizardryDungeonScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import utils.LogScrollPane;

public abstract class Dialog extends com.badlogic.gdx.scenes.scene2d.ui.Dialog implements Constants {

    protected static final int WIDTH = 550;
    protected static final int HEIGHT = 550;

    protected final BaseScreen screen;
    protected final TextField input;
    protected final LogScrollPane scrollPane;

    public Dialog(Context ctx, BaseScreen screen) {
        super("Encounter", Andius.HUD.getSkin().get(Window.WindowStyle.class));
        this.screen = screen;

        setSkin(Andius.HUD.getSkin());
        setSize(WIDTH, HEIGHT);

        float titlePadTop = getPadTop();
        padLeft(10);
        padRight(10);
        padBottom(10);
        padTop(titlePadTop + 10);

        scrollPane = new LogScrollPane(Andius.HUD.getSkin(), new Table(), WIDTH, "default-12");

        input = new TextField("", Andius.HUD.getSkin());

        getContentTable().defaults().maxWidth(WIDTH).width(WIDTH).growX();
        getContentTable().add(scrollPane).expandY().fillY().minHeight(0);
        getContentTable().row();
        getContentTable().add(input);
    }

    @Override
    public com.badlogic.gdx.scenes.scene2d.ui.Dialog show(Stage stage, Action action) {
        Gdx.input.setInputProcessor(stage);
        com.badlogic.gdx.scenes.scene2d.ui.Dialog d = super.show(stage, action);
        stage.setKeyboardFocus(input);
        return d;
    }

    @Override
    public void hide() {
        super.hide();
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
