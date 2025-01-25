package andius;

import static andius.Andius.SCREEN_HEIGHT;
import static andius.Andius.SCREEN_WIDTH;
import static andius.Andius.mainGame;
import static andius.WizardryData.WER4_CHARS;
import static andius.WizardryData.WER_ITEMS;
import andius.objects.DoGooder;
import andius.objects.Item;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import utils.AutoFocusScrollPane;
import utils.FrameMaker;
import utils.LogScrollPane;

public class Wiz4RewardScreen implements Screen, Constants {

    private final Stage stage;
    public final CharacterRecord player;
    public final DoGooder opponent;
    private final Batch batch;
    private final List<ItemLabel> items;
    private final AutoFocusScrollPane itemsScroll;
    private final LogScrollPane logs;
    private final TextButton take;
    private final TextButton exit;
    private final Texture background;

    private static final int TABLE_HEIGHT = 740;
    private static final int LISTING_WIDTH = 300;
    private static final int LOG_WIDTH = 370;
    private static final int LOG_HEIGHT = 400;

    public Wiz4RewardScreen(CharacterRecord player, DoGooder opponent) {
        this.opponent = opponent;
        this.player = player;
        this.batch = new SpriteBatch();
        this.stage = new Stage();
        this.stage.setDebugAll(true);

        FrameMaker fm = new FrameMaker(SCREEN_WIDTH, SCREEN_HEIGHT);

        Table logTable = new Table(Andius.skin);
        this.logs = new LogScrollPane(Andius.skin, logTable, LOG_WIDTH);

        this.items = new List(Andius.skin, "default-16");
        this.itemsScroll = new AutoFocusScrollPane(this.items, Andius.skin);
        this.itemsScroll.setScrollingDisabled(true, false);

        for (int id : opponent.partyMembers) {
            DoGooder dg = WER4_CHARS.get(id);
            for (int it : dg.items) {
                Item i = WER_ITEMS.get(it);
                ItemLabel label = new ItemLabel(i);
                items.getItems().add(label);
            }
        }

        this.exit = new TextButton("LEAVE", Andius.skin, "default-16");
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                mainGame.setScreen(Map.WIZARDRY4.getScreen());
            }
        });
        this.exit.setBounds(400, 450, 80, 40);

        this.take = new TextButton("TAKE", Andius.skin, "default-16");
        this.take.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                take();
            }
        });
        this.take.setBounds(500, 450, 80, 40);

        fm.setBounds(this.logs, 326, 14, LOG_WIDTH, LOG_HEIGHT);
        fm.setBounds(this.itemsScroll, 712, 14, LISTING_WIDTH, TABLE_HEIGHT);

        this.stage.addActor(this.itemsScroll);
        this.stage.addActor(this.logs);
        this.stage.addActor(exit);
        this.stage.addActor(take);

        this.background = fm.build();

    }

    private void take() {
        ItemLabel l = this.items.getSelected();
        if (l != null) {
            player.inventory.add(l.item);
            this.items.getItems().removeValue(l, true);
        }
    }

    private class ItemLabel extends Label {

        final Item item;

        public ItemLabel(Item it) {
            super(it.name, Andius.skin, "default-16");
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

        batch.begin();
        batch.draw(this.background, 0, 0);
        batch.end();

        stage.act();
        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            mainGame.setScreen(Map.WIZARDRY4.getScreen());
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            take();
        }
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
