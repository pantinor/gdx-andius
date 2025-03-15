
import andius.Andius;
import andius.objects.TibianSprite;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Align;
import java.awt.image.BufferedImage;
import java.io.File;
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

    final MyListItem[] items = new MyListItem[20];

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

        Table monsterList = new Table(Andius.skin);
        monsterList.align(Align.top);

        for (int i = 0; i < 20; i++) {
            items[i] = new MyListItem(i);
            monsterList.add(items[i]);
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

        TextButton makeButton = new TextButton("Write JSON", skin, "default-16");
        makeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                pack();
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

        batch.begin();
        font.draw(batch, "icon: " + selectedIcon, screenWidth - 350, screenHeight - 830);
        batch.end();

        stage.act();
        stage.draw();
    }

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Monster Atlas Tool";
        cfg.width = screenWidth;
        cfg.height = screenHeight;
        new LwjglApplication(new MonsterAtlasTool(), cfg);

    }

    public class MyListItem extends Group implements Comparable<MyListItem> {

        public final int iconId;
        public final Label id;
        public Image icon;
        public String name;

        public MyListItem(int id) {
            this.iconId = id;
            this.id = new Label("" + id, skin, "default-16");
            this.icon = new Image(TibianSprite.icon("Deadeye_Devious"));

            float x = getX();
            this.icon.setBounds(x + 3, getY() + 3, dim, dim);
            this.id.setPosition(x += dim + 5, getY() + 10);

            addActor(this.id);
            addActor(this.icon);
            this.setBounds(getX(), getY(), 150, 70);
        }

        public void swapImage(String name) {
            this.name = name;
            float x = this.icon.getX();
            float y = this.icon.getY();
            this.icon.remove();
            this.icon = new Image(TibianSprite.icon(name));
            this.icon.setBounds(x, y, dim, dim);
            addActor(this.icon);
        }

        @Override
        public int compareTo(MyListItem o) {
            return Integer.compare(this.iconId, o.iconId);
        }

        public BufferedImage convertPixmapToBufferedImage() {

            TextureRegion tr = TibianSprite.icon(this.name);

            int sx = tr.getRegionX();
            int sy = tr.getRegionY();
            int w = tr.getRegionWidth();
            int h = tr.getRegionHeight();

            if (!tr.getTexture().getTextureData().isPrepared()) {
                tr.getTexture().getTextureData().prepare();
            }

            Pixmap pixmap = tr.getTexture().getTextureData().consumePixmap();

            BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    int value = pixmap.getPixel(x + sx, y + sy);
                    Color color = new Color(value);
                    int rgba = Color.argb8888(color);
                    bufferedImage.setRGB(x, y, rgba);
                }
            }
            return bufferedImage;
        }
    }

    public void pack() {

        TexturePacker.Settings settings = new TexturePacker.Settings();
        settings.minWidth = 8;
        settings.minHeight = 8;
        settings.maxWidth = 256;
        settings.maxHeight = 4000;
        settings.paddingX = 0;
        settings.paddingY = 0;
        settings.fast = true;
        settings.pot = false;
        settings.grid = true;
        settings.edgePadding = false;
        settings.bleed = false;
        settings.debug = false;
        settings.alias = false;
        settings.useIndexes = true;

        TexturePacker tp = new TexturePacker(settings);

        try {
            for (MyListItem i : this.items) {
                tp.addImage(i.convertPixmapToBufferedImage(), "" + i.iconId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        tp.pack(new File("src/main/resources/assets/json"), "wizIcons.atlas");
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
