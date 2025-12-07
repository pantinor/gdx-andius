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

    protected static final int WIDTH = 300;
    protected static final int HEIGHT = 200;

    protected final BaseScreen screen;
    protected final TextField input;
    protected final LogScrollPane scrollPane;

    public Dialog(Context ctx, BaseScreen screen) {
        super("", Andius.skin.get("dialog", Window.WindowStyle.class));
        this.screen = screen;

        setSkin(Andius.skin);
        setWidth(WIDTH);
        setHeight(HEIGHT);

        pad(10);

        scrollPane = new LogScrollPane(Andius.skin, new Table(), WIDTH);
        scrollPane.setHeight(HEIGHT);

        input = new TextField("", Andius.skin, "default-16");

        getContentTable().add(scrollPane).maxWidth(WIDTH).width(WIDTH);
        getContentTable().row();
        getContentTable().add(input).maxWidth(WIDTH).width(WIDTH);
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
        } else {
            Gdx.input.setInputProcessor(new InputMultiplexer(screen, getStage()));
        }
    }

}
