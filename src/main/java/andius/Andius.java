package andius;

import andius.objects.Conversations;
import andius.objects.Icons;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
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
import java.util.HashMap;
import utils.Hud;

public class Andius extends Game {

    public static final int SCREEN_WIDTH = 1024;
    public static final int SCREEN_HEIGHT = 768;

    public static final int MAP_VIEWPORT_DIM = 624;

    public static Context CTX;

    public static Texture backGround;
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

    public static java.util.Map<Color, Animation> EXPLMAP = new HashMap<>();

    public static Animation<TextureRegion> world_scr_avatar;
    public static Animation<TextureRegion> game_scr_avatar;

    public static Hud HUD;
    public static Conversations CONVERSATIONS;

    public static TextureRegion[] faceTiles = new TextureRegion[6 * 6];

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

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.classpath("assets/fonts/sansblack.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 12;
        smallFont = generator.generateFont(parameter);

        parameter.size = 16;
        hudLogFont = generator.generateFont(parameter);

        parameter.size = 18;
        font = generator.generateFont(parameter);

        parameter.size = 24;
        largeFont = generator.generateFont(parameter);

        parameter.size = 72;
        titleFont = generator.generateFont(parameter);

        generator.dispose();

        generator = new FreeTypeFontGenerator(Gdx.files.classpath("assets/fonts/ultima.ttf"));
        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 24;
        BitmapFont smallUltimaFont = generator.generateFont(parameter);

        generator.dispose();

        generator = new FreeTypeFontGenerator(Gdx.files.classpath("assets/fonts/gnuolane.ttf"));
        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 18;
        font = generator.generateFont(parameter);

        generator.dispose();

        skin = new Skin(Gdx.files.classpath("assets/skin/uiskin.json"));
        skin.remove("default-font", BitmapFont.class);
        skin.add("default-font", font, BitmapFont.class);
        skin.add("larger-font", largeFont, BitmapFont.class);
        skin.add("title-font", titleFont, BitmapFont.class);
        skin.add("small-font", smallFont, BitmapFont.class);
        skin.add("small-ultima", smallUltimaFont, BitmapFont.class);
        skin.add("hud-log", hudLogFont, BitmapFont.class);

        {
            Label.LabelStyle ls = skin.get("default", Label.LabelStyle.class);
            ls.font = font;
            Label.LabelStyle ls2 = skin.get("hudLogFont", Label.LabelStyle.class);
            ls2.font = hudLogFont;
            Label.LabelStyle ls3 = skin.get("hudSmallFont", Label.LabelStyle.class);
            ls3.font = smallFont;
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

            Icons.init();
            TibianSprite.init();

            mapAtlas = new TextureAtlas(Gdx.files.classpath("assets/data/map-atlas.txt"));
            moongateTextures = mapAtlas.findRegions("moongate");

            world_scr_avatar = new Animation(.4f, mapAtlas.findRegions("avatar_warrior_red"));
            game_scr_avatar = TibianSprite.animation("Knight_Knight_Male");

            TextureRegion[][] expl = TextureRegion.split(new Texture(Gdx.files.classpath("assets/data/uf_FX.png")), 24, 24);
            EXPLMAP.put(Color.GRAY, new Animation(.1f, getTextureArray(expl, 0, 0)));
            EXPLMAP.put(Color.BLUE, new Animation(.1f, getTextureArray(expl, 0, 5)));
            EXPLMAP.put(Color.RED, new Animation(.1f, getTextureArray(expl, 1, 0)));
            EXPLMAP.put(Color.GREEN, new Animation(.1f, getTextureArray(expl, 1, 5)));
            EXPLMAP.put(Color.PURPLE, new Animation(.1f, getTextureArray(expl, 4, 5)));
            EXPLMAP.put(Color.YELLOW, new Animation(.1f, getTextureArray(expl, 5, 5)));

            //static initializer
            WizardryData.class.getClass();

            Constants.Moongate.init();
            CONVERSATIONS = Conversations.init();

        } catch (Exception e) {
            e.printStackTrace();
        }

        mainGame = this;
        startScreen = new StartScreen();
        setScreen(startScreen);

    }

    private Array<TextureRegion> getTextureArray(TextureRegion[][] expl, int x, int y) {
        Array<TextureRegion> arr = new Array<>();
        arr.add(expl[x][y]);
        arr.add(expl[x][y + 1]);
        arr.add(expl[x][y + 2]);
        arr.add(expl[x][y + 3]);
        arr.add(expl[x][y + 4]);
        return arr;
    }

    public static class ExplosionDrawable extends Actor {

        private float stateTime;
        private final Animation<TextureRegion> anim;

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
