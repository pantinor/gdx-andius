package andius;

import static andius.Andius.mainGame;
import static andius.WizardryData.WER4_CHARS;
import static andius.WizardryData.WER_ITEMS;
import andius.objects.DoGooder;
import andius.objects.Item;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import utils.AutoFocusScrollPane;
import utils.LogScrollPane;

public class Wiz4RewardScreen implements Screen, Constants {

    private final Stage stage;
    public final CharacterRecord player;
    public final DoGooder opponent;
    private final AutoFocusScrollPane itemsScroll;
    private final LogScrollPane logs;
    private final TextButton take;
    private final TextButton exit;

    private static final int TABLE_HEIGHT = 750;
    private static final int LISTING_WIDTH = 300;
    private static final int LOG_WIDTH = 370;
    private static final int LOG_HEIGHT = 400;

    public Wiz4RewardScreen(CharacterRecord player, DoGooder opponent) {
        this.opponent = opponent;
        this.player = player;

        this.stage = new Stage();
        this.stage.setDebugAll(true);

        Table logTable = new Table(Andius.skin);
        logTable.setBackground("log-background");
        this.logs = new LogScrollPane(Andius.skin, logTable, LOG_WIDTH);
        this.logs.setBounds(320, 10, LOG_WIDTH, LOG_HEIGHT);

        List<ItemLabel> items = new List(Andius.skin);
        this.itemsScroll = new AutoFocusScrollPane(items, Andius.skin);
        this.itemsScroll.setScrollingDisabled(true, false);

        for (int id : opponent.partyMembers) {
            DoGooder dg = WER4_CHARS.get(id);
            for (int it : dg.items) {
                Item i = WER_ITEMS.get(it);
                ItemLabel label = new ItemLabel(i);
                items.getItems().add(label);
            }
        }

        this.itemsScroll.setBounds(700, 10, LISTING_WIDTH, TABLE_HEIGHT);

        this.exit = new TextButton("LEAVE", Andius.skin, "red-larger");
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                mainGame.setScreen(Map.WIZARDRY4.getScreen());
            }
        });
        this.exit.setBounds(400, 450, 75, 40);

        this.take = new TextButton("TAKE", Andius.skin, "red-larger");
        this.take.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                ItemLabel l = items.getSelected();
                if (l != null) {
                    player.inventory.add(l.item);
                }
            }
        });
        this.take.setBounds(500, 450, 75, 40);

        this.stage.addActor(this.itemsScroll);
        this.stage.addActor(this.logs);
        this.stage.addActor(exit);
        this.stage.addActor(take);

    }

    private class ItemLabel extends Label {

        final Item item;

        public ItemLabel(Item it) {
            super(it.name, Andius.skin, "default");
            this.item = it;
        }

        @Override
        public String toString() {
            return this.item.name;
        }

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }

}
