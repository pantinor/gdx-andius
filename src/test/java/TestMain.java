
import andius.Andius;
import andius.Constants;
import andius.Context;
import andius.WizardryCombatScreen;
import andius.dialogs.ConversationDialog;
import andius.objects.Monster;
import andius.objects.MutableMonster;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import utils.AutoFocusScrollPane;

public class TestMain extends Game {

    private TextureAtlas iconAtlas;

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "test";
        cfg.width = 1024;
        cfg.height = 768;
        new LwjglApplication(new TestMain(), cfg);
    }

    @Override
    public void create() {

        try {

            Andius a = new Andius();
            a.create();

            this.iconAtlas = new TextureAtlas(Gdx.files.classpath("assets/json/wizIcons.atlas"));

            //Constants.Map.CAVE.init();
            TestScreen t = new TestScreen();

            //t.stage.setDebugAll(true);

            //ConversationDialog d = new ConversationDialog(new Context(), null, Andius.CONVERSATIONS.get("Bordermarch", "Dupre"));
            //d.show(t.stage);
            Table monstersTable = new Table(Andius.skin);
            monstersTable.top().left();
            AutoFocusScrollPane monstersScroll = new AutoFocusScrollPane(monstersTable, Andius.skin);
            monstersScroll.setScrollingDisabled(true, false);

            for (Monster m : Constants.Map.WIZARDRY1.scenario().monsters()) {
                monstersTable.add(new MonsterListing(m)).pad(3);
                monstersTable.row();
            }

            monstersScroll.setBounds(712, 14, 300, 740 - 35);

            t.stage.addActor(monstersScroll);

            setScreen(t);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class TestScreen implements Screen {

        Stage stage = new Stage();

        @Override
        public void show() {
            Gdx.input.setInputProcessor(stage);
        }

        @Override
        public void render(float f) {
            Gdx.gl.glClearColor(0, 0, 0, 0);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            stage.act();
            stage.draw();
        }

        @Override
        public void resize(int i, int i1) {
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

    private class MonsterListing extends Group {

        final Image icon;
        final Label l1;
        final Label l2;
        final Label l3;
        final Monster m;

        MonsterListing(Monster m) {
            this.m = m;

            AtlasRegion ar = iconAtlas.findRegion("" + this.m.getIconId());

            if (ar == null) {
                ar = iconAtlas.findRegion("0");
            }

            this.icon = new Image(ar);

            this.l1 = new Label("xxx", Andius.skin, "default-16");
            this.l2 = new Label("xxx", Andius.skin, "default-16");
            this.l3 = new Label("xxx", Andius.skin, "default-16");
            this.icon.setPosition(220, 2);

            String d1 = String.format("%s  LVL %d", m.name.toUpperCase(), m.getLevel());
            this.l1.setText(d1);

            addActor(this.icon);
            addActor(this.l1);
            addActor(this.l2);
            addActor(this.l3);

            float x = 3;
            this.l1.setBounds(x, 17 * 2, 300, 17);
            this.l2.setBounds(x, 17 * 1, 300, 17);
            this.l3.setBounds(x, 17 * 0, 300, 17);

            this.setSize(300, 17 * 3f);

        }

    }

}
