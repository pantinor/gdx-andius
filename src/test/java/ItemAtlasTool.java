
import andius.objects.Item;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Comparator;
import org.apache.commons.io.IOUtils;

public class ItemAtlasTool extends InputAdapter implements ApplicationListener {

    Batch batch;

    static int screenWidth = 1920;
    static int screenHeight = 1008;

    int dim = 64;

    BitmapFont font;

    Stage stage;
    Skin skin;

    int selectedIcon;
    MyListItem selectedItem;

    java.util.List<Item> items;

    @Override
    public void create() {

        font = new BitmapFont();

        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("assets/skin/uiskin.json"));
        stage = new Stage();

        final List<MyListItem> list = new List<>(skin);

        TextureRegion[][] itemTextures = TextureRegion.split(new Texture(Gdx.files.classpath("assets/data/inventory.png")), 44, 44);

        Table animTable = new Table(skin);
        animTable.defaults().pad(2);
        for (int y = 0; y < itemTextures.length; y++) {
            for (int x = 0; x < itemTextures[0].length; x++) {
                Image im = new Image(itemTextures[y][x]);
                im.setName("" + (itemTextures[0].length * y + x));
                im.setWidth(44);
                im.setHeight(44);

                im.addListener(new ClickListener(-1) {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (event.getButton() == 0) {
                            selectedIcon = Integer.parseInt(event.getTarget().getName());
                            if (selectedItem != null) {
                                selectedItem.icon = selectedIcon;
                                items.get(selectedItem.id).iconID = selectedIcon;
                            }
                        }
                    }
                });

                animTable.add(im);
            }
            animTable.row();
        }

        ScrollPane imageScrollPane = new AutoFocusScrollPane(animTable, skin);
        imageScrollPane.setScrollingDisabled(true, false);
        imageScrollPane.setBounds(0, screenHeight, screenWidth - 300, screenHeight);
        imageScrollPane.setPosition(0, 0);

        try {
            String json = IOUtils.toString(new FileInputStream(new File("src/main/resources/assets/json/kod-items.json")));
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            items = gson.fromJson(json, new TypeToken<java.util.List<Item>>() {
            }.getType());

            Collections.sort(items, new Comparator<Item>() {
                @Override
                public int compare(Item it1, Item it2) {
                    if (it1.type == it2.type) {
                        return Long.compare(it1.cost, it2.cost);
                    }
                    return Long.compare(it1.type.hashCode(), it2.type.hashCode());
                }
            });

        } catch (Exception e) {
            //ignore
        }

        MyListItem[] listItems = new MyListItem[items.size()];
        int x = 0;
        for (Item i : items) {
            listItems[x] = new MyListItem(i.id, i.name, i.iconID);
            x++;
        }
        list.setItems(listItems);

        list.addListener(new ClickListener(-1) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (event.getButton() == 0) {
                    selectedItem = list.getSelected();
                }
            }
        });

        ScrollPane scrollPane = new AutoFocusScrollPane(list, skin);
        scrollPane.setScrollingDisabled(true, false);

        TextButton makeButton = new TextButton("Write JSON", skin, "default");
        makeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                writeJson("item.json", items);
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
        font.draw(batch, "item: " + selectedItem, screenWidth - 350, screenHeight - 860);
        batch.end();
    }

    private void writeJson(String file, java.util.List<Item> obj) {
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
        cfg.title = "Item Atlas Tool";
        cfg.width = screenWidth;
        cfg.height = screenHeight;
        new LwjglApplication(new ItemAtlasTool(), cfg);

    }

    public class MyListItem implements Comparable<MyListItem> {

        public final String name;
        public final int id;
        public int icon;

        public MyListItem(int id, String name, int icon) {
            this.id = id;
            this.name = name;
            this.icon = icon;
        }

        @Override
        public String toString() {
            return String.format("%s - %s", name, icon);
        }

        @Override
        public int compareTo(MyListItem o) {
            return Integer.compare(this.icon, o.icon);
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

    public class AutoFocusScrollPane extends ScrollPane {

        public AutoFocusScrollPane(Actor widget, Skin skin) {
            super(widget, skin);
            addListener(new InputListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    getStage().setScrollFocus(AutoFocusScrollPane.this);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    getStage().setScrollFocus(null);
                }
            });
        }
    }

}
