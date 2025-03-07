
import andius.Andius;
import andius.objects.TibianSprite;
import andius.objects.Monster;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.apache.commons.io.IOUtils;
import utils.AutoFocusScrollPane;

public class MonsterAtlasTool extends InputAdapter implements ApplicationListener {

    Batch batch;

    static int screenWidth = 1920;
    static int screenHeight = 1008;

    int dim = 64;

    BitmapFont font;

    Stage stage;
    Skin skin;

    String selectedIcon;
    MyListItem selectedMonster;

    java.util.List<Monster> monsters;

    @Override
    public void create() {

        font = new BitmapFont();
        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("assets/skin/uiskin.json"));
        stage = new Stage();

        TibianSprite.init();

        Table animTable = new Table(skin);
        animTable.defaults().pad(2);
        int count = 0;
        for (String name : TibianSprite.names()) {
            Image im = new Image(TibianSprite.icon(name));
            im.setName(name);
            im.setWidth(64);
            im.setHeight(64);

            im.addListener(new ClickListener(-1) {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (event.getButton() == 0) {
                        selectedIcon = event.getTarget().getName();
                        if (selectedMonster != null) {
                            selectedMonster.monster.setIconId(selectedIcon);
                            selectedMonster.swapImage(selectedIcon);
                        }
                    }
                }
            });

            animTable.add(im);
            count++;
            if (count > 20) {
                count = 0;
                animTable.row();
            }
        }

        ScrollPane imageScrollPane = new AutoFocusScrollPane(animTable, skin);
        imageScrollPane.setScrollingDisabled(true, false);
        imageScrollPane.setBounds(0, screenHeight, screenWidth - 300, screenHeight);
        imageScrollPane.setPosition(0, 0);

        try {
            String json = IOUtils.toString(new FileInputStream(new File("src/main/resources/assets/json/bs-monsters.json")));
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            monsters = gson.fromJson(json, new TypeToken<java.util.List<Monster>>() {
            }.getType());
        } catch (Exception e) {
            //ignore
        }

        MyListItem[] items = new MyListItem[monsters.size()];
        int x = 0;
        for (Monster m : monsters) {
            items[x] = new MyListItem(m);
            x++;
        }

        Table monsterList = new Table(Andius.skin);
        monsterList.align(Align.top);

        for (MyListItem i : items) {
            monsterList.add(i);
            monsterList.row();
        }

        monsterList.addListener(new ClickListener(-1) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (event.getButton() == 0) {
                    if (event.getTarget().getParent() instanceof MyListItem) {
                        selectedMonster = (MyListItem) event.getTarget().getParent();
                    }
                }
            }
        });

        ScrollPane scrollPane = new AutoFocusScrollPane(monsterList, skin);
        scrollPane.setScrollingDisabled(true, false);

        TextButton makeButton = new TextButton("Write JSON", skin, "default");
        makeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                writeJson("monsters.json", monsters);
            }
        });

        Table table = new Table(skin);
        table.defaults().pad(2);
        table.add(makeButton).expandX().left().width(300);
        table.row();
        table.add(scrollPane).expandX().left().width(300).maxHeight(screenHeight);
        table.setBounds(screenWidth - 300, screenHeight, 300, 800);
        table.setPosition(screenWidth - 300, 200);

        stage.addActor(imageScrollPane);
        stage.addActor(table);

        Gdx.input.setInputProcessor(new InputMultiplexer(stage, this));

    }

    @Override

    public void render() {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act();
        stage.draw();

        batch.begin();
        font.draw(batch, "icon: " + selectedIcon, screenWidth - 350, screenHeight - 830);
        //font.draw(batch, "monster: " + selectedMonster.name, screenWidth - 350, screenHeight - 860);
        batch.end();
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
        cfg.title = "Monster Atlas Tool";
        cfg.width = screenWidth;
        cfg.height = screenHeight;
        new LwjglApplication(new MonsterAtlasTool(), cfg);

    }

    public class MyListItem extends Group implements Comparable<MyListItem> {

        public final Label name;
        public final Monster monster;
        public Image icon;

        public MyListItem(Monster m) {
            this.name = new Label(m.getName(), skin, "default");
            this.monster = m;
            this.icon = new Image(TibianSprite.icon(this.monster.getIconId().equals("0") ? "Deadeye_Devious" : this.monster.getIconId()));

            float x = getX();
            this.icon.setBounds(x + 3, getY() + 3, dim, dim);
            this.name.setPosition(x += dim + 5, getY() + 10);

            addActor(this.name);
            addActor(this.icon);
            this.setBounds(getX(), getY(), 150, 70);
        }

        public void swapImage(String id) {
            float x = this.icon.getX();
            float y = this.icon.getY();
            this.icon.remove();
            this.icon = new Image(TibianSprite.icon(id));
            this.icon.setBounds(x, y, dim, dim);
            addActor(this.icon);
        }

        @Override
        public int compareTo(MyListItem o) {
            return Integer.compare(this.monster.getMonsterId(), o.monster.getMonsterId());
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
