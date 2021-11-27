
import andius.objects.Icons;
import andius.objects.Monster;
import java.util.ArrayList;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.apache.commons.io.IOUtils;

public class SpriteAtlasTool extends InputAdapter implements ApplicationListener {

    Batch batch;

    static int screenWidth = 1920;
    static int screenHeight = 1008;

    int dim = 48;
    int canvasGridWidth;
    int canvasGridHeight;

    boolean initMapPosition = true;

    MyVector currentMapCoords;
    MyVector selectedMapCoords;

    BitmapFont font;
    Sprite sprBg;

    Stage stage;
    Skin skin;
    MyListItem selectedMonster;

    java.util.List<MyListItem> gridItems;
    Texture box;

    java.util.List<Monster> monsters;

    @Override
    public void create() {

        Pixmap pixmap = new Pixmap(dim, dim, Format.RGBA8888);
        pixmap.setColor(new Color(1, 1, 0, .8f));
        int w = 1;
        pixmap.fillRectangle(0, 0, w, dim);
        pixmap.fillRectangle(dim - w, 0, w, dim);
        pixmap.fillRectangle(w, 0, dim - 2 * w, w);
        pixmap.fillRectangle(w, dim - w, dim - 2 * w, w);
        box = new Texture(pixmap);
        FileHandle fh = Gdx.files.classpath("assets/data/uf_heroes.png");
        Texture tx = new Texture(fh);
        canvasGridWidth = tx.getWidth() / dim;
        canvasGridHeight = tx.getHeight() / dim;

        sprBg = new Sprite(tx, 0, 0, tx.getWidth(), tx.getHeight());

        gridItems = new ArrayList<>();

        font = new BitmapFont();
        font.setColor(Color.WHITE);

        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("assets/skin/uiskin.json"));
        stage = new Stage();

        final List<MyListItem> list = new List<>(skin);

        try {
            Icons.init();

            String json = IOUtils.toString(new FileInputStream(new File("src/main/resources/assets/json/monsters-json.txt")));

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            monsters = gson.fromJson(json, new TypeToken<java.util.List<Monster>>() {
            }.getType());

            MyListItem[] items = new MyListItem[monsters.size()];
            int x = 0;
            for (Monster m : monsters) {
                items[x] = new MyListItem(m);
                x++;
            }
            list.setItems(items);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        list.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectedMonster = list.getSelected();
            }
        });

        ScrollPane scrollPane = new ScrollPane(list, skin);
        scrollPane.setScrollingDisabled(true, false);

        TextButton makeButton = new TextButton("Make Atlas", skin, "default");
        makeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                for (Monster m : monsters) {
                    //m.name = toCamelCase(m.name);
                    //m.genericName = toCamelCase(m.genericName);
                    System.out.println(m.name+"\t"+m.genericName);
                }
                writeJson("monsters-json.txt", monsters);
            }
        });

        Table table = new Table(skin);
        table.defaults().pad(2);
        table.add(makeButton).expandX().left().width(175);
        table.row();
        table.add(scrollPane).expandX().left().width(175).maxHeight(screenHeight);
        table.setPosition(screenWidth - 175, 0);
        table.setFillParent(true);

        stage.addActor(table);

        Gdx.input.setInputProcessor(new InputMultiplexer(stage, this));

    }

    @Override
    public void render() {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        batch.draw(sprBg, 0, screenHeight - sprBg.getHeight());

        font.draw(batch, "current mouse coords: " + currentMapCoords, 10, 80);
        font.draw(batch, "selectedMapCoords: " + selectedMapCoords, 10, 60);
        try {
            int idx = selectedMapCoords.y * 40 + selectedMapCoords.x;
            font.draw(batch, "touchedIcon: " + Icons.get(idx) + " " + idx, 10, 40);
        } catch (Exception e) {

        }
        font.draw(batch, "selectedMonster: " + selectedMonster, 10, 20);

        batch.end();

        stage.act();
        stage.draw();
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {

        currentMapCoords = new MyVector(
                Math.round(screenX / dim),
                Math.round((screenHeight / dim) - ((screenHeight - screenY) / dim) - 1)
        );

        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        int x = Math.round(screenX / dim);
        int y = Math.round((screenHeight / dim) - ((screenHeight - screenY) / dim) - 1);

        if (y <= canvasGridHeight && x <= canvasGridWidth) {

            selectedMapCoords = new MyVector(x, y);

            if (selectedMonster != null) {
                int idx = selectedMapCoords.y * 40 + selectedMapCoords.x;
                //selectedMonster.monster.icon = selectedMonster.icon = Icons.get(idx);
            }

        }

        return false;
    }

    private void writeJson(String file, java.util.List<Monster> obj) {
        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.setPrettyPrinting().create();
            String json = gson.toJson(obj);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(json.getBytes("UTF-8"));
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Sprite Atlas Tool";
        cfg.width = screenWidth;
        cfg.height = screenHeight;
        new LwjglApplication(new SpriteAtlasTool(), cfg);

    }

    public static String toCamelCase(final String init) {
        if (init == null) {
            return null;
        }

        final StringBuilder ret = new StringBuilder(init.length());

        for (final String word : init.split(" ")) {
            if (!word.isEmpty()) {
                ret.append(word.substring(0, 1).toUpperCase());
                ret.append(word.substring(1).toLowerCase());
            }
            if (!(ret.length() == init.length())) {
                ret.append(" ");
            }
        }

        return ret.toString();
    }

    public class MyVector {

        private int x;
        private int y;

        private MyVector(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return String.format("%s, %s", x, y);
        }

    }

    public class MyListItem implements Comparable<MyListItem> {

        public final String name;
        public final Monster monster;
        public TextureRegion icon;

        public MyListItem(Monster m) {
            this.name = m.getName();
            this.monster = m;
            //this.icon = m.getIcon();
        }

        @Override
        public String toString() {
            return String.format("%s - %s", name, icon);
        }

        @Override
        public int compareTo(MyListItem o) {
            return Integer.compare(this.monster.getIconId(), o.monster.getIconId());
        }

    }

    @Override
    public void resize(int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub

    }

}
