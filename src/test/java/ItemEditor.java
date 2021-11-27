
import andius.objects.Item;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.IOUtils;

public class ItemEditor extends InputAdapter implements ApplicationListener {

    static int screenWidth = 1024;
    static int screenHeight = 768;
    int dim = 44;
    int canvasGridWidth;
    int canvasGridHeight;
    
    Batch batch;
    BitmapFont font;
    Stage stage;
    Skin skin;

    List<Item> items;
    Item selectedItem;

    TextureRegion[] invIcons = new TextureRegion[67 * 12];
    Image selectedImage;

    @Override
    public void create() {

        font = new BitmapFont();
        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("assets/skin/uiskin.json"));
        stage = new Stage();

        FileHandle fh = Gdx.files.classpath("assets/data/inventory.png");
        Texture tx = new Texture(fh);
        canvasGridWidth = tx.getWidth() / dim;
        canvasGridHeight = tx.getHeight() / dim;

        Table icons = new Table(skin);
        icons.defaults().pad(2);
        TextureRegion[][] inv = TextureRegion.split(tx, 44, 44);
        for (int row = 0; row < canvasGridHeight; row++) {
            for (int col = 0; col < canvasGridWidth; col++) {
                Image img = new Image(inv[row][col]);
                img.setName("" + (row * canvasGridWidth + col));
                icons.add(img);
                invIcons[row * canvasGridWidth + col] = inv[row][col];
            }
            icons.row();
        }

        icons.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event.toString().equals("touchDown")) {
                    selectedImage = (Image) event.getTarget();
                }
                return false;
            }
        }
        );

        try {
            InputStream is = this.getClass().getResourceAsStream("assets/json/items.json");
            String json = IOUtils.toString(is);//new FileInputStream(new File("assets/json/items.json")));

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            items = gson.fromJson(json, new TypeToken<java.util.List<Item>>() {
            }.getType());
            
            Collections.sort(items);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Table itemTable = new Table(skin);

        for (Item it : items) {
            itemTable.add(new Image(invIcons[it.iconID]));
            Label l = new Label(it.toString(), skin);
            l.setUserObject(it);
            itemTable.add(l);
            itemTable.row();
        }

        itemTable.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event.toString().equals("touchDown") && event.getTarget() instanceof Label) {
                    
                    selectedItem = (Item) event.getTarget().getUserObject();
                    int iconId = selectedImage != null ? Integer.parseInt(selectedImage.getName()) : 0;
                    selectedItem.iconID = iconId;
                    
                    Cell cell = itemTable.getCell(event.getTarget());
                    int row = cell.getRow();
                    for (int i=0;i<itemTable.getCells().size;i++) {
                        Cell c2 = itemTable.getCells().get(i);
                        if (c2.getRow() == row && c2.getColumn() == 0) {
                            c2.setActor(new Image(invIcons[iconId]));
                        }
                    }
                }
                return false;
            }
        }
        );

        ScrollPane iconsPane = new ScrollPane(icons, skin);
        ScrollPane itemsPane = new ScrollPane(itemTable, skin);

        TextButton makeButton = new TextButton("Make Atlas", skin, "default");
        makeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                writeJson("items.json", items);
            }
        }
        );

        Table table = new Table(skin);
        table.defaults().pad(2);

        table.add(iconsPane).minWidth(600).minHeight(700).align(Align.top);
        table.add(itemsPane).minWidth(200).minHeight(700).align(Align.top);
        table.row();

        table.add();
        table.add(makeButton).minWidth(100).expandX().align(Align.top);
        table.row();

        table.setX(20);
        table.setY(50);
        table.setWidth(1000);
        table.setHeight(700);

        stage.addActor(table);

        Gdx.input.setInputProcessor(new InputMultiplexer(stage, this));

    }

    @Override
    public void render() {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();

        if (selectedImage != null) {
            selectedImage.getDrawable().draw(batch, 650, 700, 44, 44);
        }

        batch.end();

        stage.act();
        stage.draw();
    }

    private void writeJson(String file, List<Item> obj) {
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
        cfg.title = "Item Editor";
        cfg.width = screenWidth;
        cfg.height = screenHeight;
        new LwjglApplication(new ItemEditor(), cfg);

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void dispose() {

    }

    @Override
    public void resume() {

    }

}
