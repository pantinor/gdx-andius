package andius;

import andius.objects.Icons;
import andius.objects.Conversations;
import andius.objects.Item;
import andius.objects.Monster;
import andius.objects.Reward;
import andius.objects.Reward.RewardElement;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Array;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.util.HashMap;
import org.apache.commons.io.IOUtils;
import utils.Hud;

public class Andius extends Game {

    public static final int SCREEN_WIDTH = 1024;
    public static final int SCREEN_HEIGHT = 768;

    public static final int MAP_VIEWPORT_DIM = 624;

    public static Context CTX;

    public static Texture backGround;
    public static TextureAtlas heroesAtlas;
    public static TextureAtlas mapAtlas;

    public static Array<TextureAtlas.AtlasRegion> moongateTextures = new Array<>();

    public static BitmapFont font;
    public static BitmapFont smallFont;
    public static BitmapFont largeFont;
    public static BitmapFont hudLogFont;
    public static BitmapFont titleFont;

    public static Andius mainGame;
    public static StartScreen startScreen;

    public static Skin skin;

    public static boolean playMusic = true;
    public static float musicVolume = 0.1f;
    public static Music music;

    public static Animation explGray;
    public static Animation explGreen;
    public static Animation explRed;
    public static Animation explBlue;

    public static Animation world_scr_avatar;
    public static Animation game_scr_avatar;

    public static Hud HUD;

    public static TextureRegion[] faceTiles = new TextureRegion[6 * 6];
    public static TextureRegion[] invIcons = new TextureRegion[67 * 12];

    public static java.util.List<Item> ITEMS;
    public static final java.util.Map<String, Item> ITEMS_MAP = new HashMap<>();

    public static java.util.List<Monster> MONSTERS;
    public static final java.util.Map<String, Monster> MONSTER_MAP = new HashMap<>();

    public static java.util.List<Reward> REWARDS;

    public static void main(String[] args) {

        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Andius";
        cfg.width = SCREEN_WIDTH;
        cfg.height = SCREEN_HEIGHT;
        cfg.addIcon("assets/data/icon.png", Files.FileType.Classpath);
        new LwjglApplication(new Andius(), cfg);

    }

    @Override
    public void create() {

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.classpath("assets/fonts/gnuolane.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 16;
        hudLogFont = generator.generateFont(parameter);

        parameter.size = 18;
        font = generator.generateFont(parameter);

        parameter.size = 24;
        largeFont = generator.generateFont(parameter);

        parameter.size = 72;
        titleFont = generator.generateFont(parameter);

        generator.dispose();

        skin = new Skin(Gdx.files.classpath("assets/skin/uiskin.json"));
        skin.remove("default-font", BitmapFont.class);
        skin.add("default-font", font, BitmapFont.class);
        skin.add("larger-font", largeFont, BitmapFont.class);
        skin.add("title-font", titleFont, BitmapFont.class);
        smallFont = skin.get("verdana-10", BitmapFont.class);
        skin.add("small-font", smallFont, BitmapFont.class);

        {
            Label.LabelStyle ls = skin.get("default", Label.LabelStyle.class);
            ls.font = font;
            TextButton.TextButtonStyle tbs = skin.get("default", TextButton.TextButtonStyle.class);
            tbs.font = font;
            TextButton.TextButtonStyle tbsred = skin.get("red", TextButton.TextButtonStyle.class);
            tbsred.font = font;
            TextButton.TextButtonStyle tbsbr = skin.get("brown", TextButton.TextButtonStyle.class);
            tbsbr.font = font;
            SelectBox.SelectBoxStyle sbs = skin.get("default", SelectBox.SelectBoxStyle.class);
            sbs.font = font;
            sbs.listStyle.font = font;
            CheckBox.CheckBoxStyle cbs = skin.get("default", CheckBox.CheckBoxStyle.class);
            cbs.font = font;
            List.ListStyle lis = skin.get("default", List.ListStyle.class);
            lis.font = font;
            TextField.TextFieldStyle tfs = skin.get("default", TextField.TextFieldStyle.class);
            tfs.font = font;
        }
        {
            Label.LabelStyle ls = skin.get("larger", Label.LabelStyle.class);
            ls.font = largeFont;
            TextButton.TextButtonStyle tbs = skin.get("larger", TextButton.TextButtonStyle.class);
            tbs.font = largeFont;
            TextButton.TextButtonStyle tbsred = skin.get("red-larger", TextButton.TextButtonStyle.class);
            tbsred.font = largeFont;
            TextButton.TextButtonStyle tbsbr = skin.get("brown-larger", TextButton.TextButtonStyle.class);
            tbsbr.font = largeFont;
            SelectBox.SelectBoxStyle sbs = skin.get("larger", SelectBox.SelectBoxStyle.class);
            sbs.font = largeFont;
            sbs.listStyle.font = largeFont;
            List.ListStyle lis = skin.get("larger", List.ListStyle.class);
            lis.font = largeFont;
            TextField.TextFieldStyle tfs = skin.get("larger", TextField.TextFieldStyle.class);
            tfs.font = largeFont;
        }

        HUD = new Hud();

        try {

            backGround = new Texture(Gdx.files.classpath("assets/data/frame.png"));

            TextureRegion[][] trs = TextureRegion.split(new Texture(Gdx.files.classpath("assets/data/uf_portraits_example.png")), 48, 48);
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 6; col++) {
                    faceTiles[row * 6 + col] = trs[row][col];
                }
            }

            TextureRegion[][] inv = TextureRegion.split(new Texture(Gdx.files.classpath("assets/data/inventory.png")), 44, 44);
            Texture tx = new Texture(Gdx.files.classpath("assets/data/inventory.png"));
            for (int row = 0; row < tx.getHeight() / 44; row++) {
                for (int col = 0; col < tx.getWidth() / 44; col++) {
                    invIcons[row * tx.getWidth() / 44 + col] = inv[row][col];
                }
            }

            heroesAtlas = new TextureAtlas(Gdx.files.classpath("assets/data/heroes-atlas.txt"));
            mapAtlas = new TextureAtlas(Gdx.files.classpath("assets/data/map-atlas.txt"));
            moongateTextures = mapAtlas.findRegions("moongate");

            world_scr_avatar = new Animation(.4f, mapAtlas.findRegions("avatar_warrior_red"));
            game_scr_avatar = new Animation(.5f, heroesAtlas.findRegions("FIGHTER_RED"));

            TextureRegion[][] expl = TextureRegion.split(new Texture(Gdx.files.classpath("assets/data/uf_FX.png")), 24, 24);
            Array<TextureRegion> gray = new Array<>();
            gray.add(expl[0][0]);
            gray.add(expl[0][1]);
            gray.add(expl[0][2]);
            gray.add(expl[0][3]);
            gray.add(expl[0][4]);
            Array<TextureRegion> blue = new Array<>();
            blue.add(expl[0][5]);
            blue.add(expl[0][6]);
            blue.add(expl[0][7]);
            blue.add(expl[0][8]);
            blue.add(expl[0][9]);
            Array<TextureRegion> red = new Array<>();
            red.add(expl[1][0]);
            red.add(expl[1][1]);
            red.add(expl[1][2]);
            red.add(expl[1][3]);
            red.add(expl[1][4]);
            Array<TextureRegion> green = new Array<>();
            green.add(expl[1][5]);
            green.add(expl[1][6]);
            green.add(expl[1][7]);
            green.add(expl[1][8]);
            green.add(expl[1][9]);

            explGray = new Animation(.1f, gray);
            explBlue = new Animation(.1f, blue);
            explRed = new Animation(.1f, red);
            explGreen = new Animation(.1f, green);

            InputStream is = this.getClass().getResourceAsStream("/assets/json/items-json.txt");
            String json = IOUtils.toString(is);

            is = this.getClass().getResourceAsStream("/assets/json/rewards-json.txt");
            String json2 = IOUtils.toString(is);

            is = this.getClass().getResourceAsStream("/assets/json/monsters-json.txt");
            String json3 = IOUtils.toString(is);

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            ITEMS = gson.fromJson(json, new TypeToken<java.util.List<Item>>() {
            }.getType());
            for (Item i : ITEMS) {
                ITEMS_MAP.put(i.name, i);
            }
            REWARDS = gson.fromJson(json2, new TypeToken<java.util.List<Reward>>() {
            }.getType());
            MONSTERS = gson.fromJson(json3, new TypeToken<java.util.List<Monster>>() {
            }.getType());
            for (Monster i : MONSTERS) {
                MONSTER_MAP.put(i.name, i);
            }

            Icons.init();
            Constants.Map.init();
            Constants.Moongate.init();
            Conversations.init();

        } catch (Exception e) {
            e.printStackTrace();
        }

        mainGame = this;
        startScreen = new StartScreen();
        setScreen(startScreen);

    }

    public static class ExplosionDrawable extends Actor {

        private float stateTime;
        private final Animation anim;

        public ExplosionDrawable(Animation anim) {
            this.anim = anim;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            stateTime += Gdx.graphics.getDeltaTime();
            batch.draw(anim.getKeyFrame(stateTime, false), getX(), getY(), 24, 24);
        }
    }

}
