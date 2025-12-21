package andius;

import andius.objects.TibianSprite;
import andius.objects.Conversations;
import andius.objects.Icons;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import utils.Hud;

public class Andius extends Game {

    public static final int SCREEN_WIDTH = 1024;
    public static final int SCREEN_HEIGHT = 768;

    public static final int MAP_VIEWPORT_DIM = 624;

    public static Context CTX;

    public static Texture backGround;
    public static TextureAtlas mapAtlas;

    public static BitmapFont font12;
    public static BitmapFont font14;
    public static BitmapFont font16;
    public static BitmapFont font18;
    public static BitmapFont font24;
    public static BitmapFont font72;

    public static Andius mainGame;
    public static StartScreen startScreen;

    public static Skin skin;

    public static boolean playMusic = true;
    public static float musicVolume = 0.1f;
    public static Music music;

    public static Animation<TextureRegion> world_scr_avatar;
    public static TextureRegion game_scr_avatar;

    public static Hud HUD;
    public static Conversations CONVERSATIONS;

    public static TextureRegion[] u1Tiles = new TextureRegion[9 * 6];
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
        font12 = generator.generateFont(parameter);

        parameter.size = 14;
        font14 = generator.generateFont(parameter);

        parameter.size = 16;
        font16 = generator.generateFont(parameter);

        parameter.size = 18;
        font18 = generator.generateFont(parameter);

        parameter.size = 24;
        font24 = generator.generateFont(parameter);

        parameter.size = 72;
        font72 = generator.generateFont(parameter);

        generator.dispose();

        generator = new FreeTypeFontGenerator(Gdx.files.classpath("assets/fonts/ultima.ttf"));
        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 24;
        BitmapFont smallUltimaFont = generator.generateFont(parameter);

        generator.dispose();

        skin = new Skin(Gdx.files.classpath("assets/skin/uiskin.json"));
        skin.remove("default-font", BitmapFont.class);
        skin.add("font12", font12, BitmapFont.class);
        skin.add("font14", font14, BitmapFont.class);
        skin.add("font16", font16, BitmapFont.class);
        skin.add("font24", font24, BitmapFont.class);
        skin.add("font72", font72, BitmapFont.class);
        skin.add("small-ultima", smallUltimaFont, BitmapFont.class);

        skin.get("default-12", Label.LabelStyle.class).font = font12;
        skin.get("default-12", TextButton.TextButtonStyle.class).font = font12;
        skin.get("default-12-red", TextButton.TextButtonStyle.class).font = font12;
        skin.get("default-12-green", TextButton.TextButtonStyle.class).font = font12;
        skin.get("default-12-yellow", TextButton.TextButtonStyle.class).font = font12;
        skin.get("default-12", CheckBox.CheckBoxStyle.class).font = font12;
        skin.get("default-12-padded", List.ListStyle.class).font = font12;
        skin.get("default-12-padded-clear", List.ListStyle.class).font = font12;
        
        skin.get("default-14", Label.LabelStyle.class).font = font14;
        
        skin.get("default", Label.LabelStyle.class).font = font16;
        skin.get("default-16", Label.LabelStyle.class).font = font16;
        skin.get("default", TextButton.TextButtonStyle.class).font = font16;
        skin.get("default-16", TextButton.TextButtonStyle.class).font = font16;
        skin.get("default-16-red", TextButton.TextButtonStyle.class).font = font16;
        skin.get("default-16-green", TextButton.TextButtonStyle.class).font = font16;
        skin.get("default-16-yellow", TextButton.TextButtonStyle.class).font = font16;
        skin.get("default-16", CheckBox.CheckBoxStyle.class).font = font16;

        skin.get("default-24", Label.LabelStyle.class).font = font24;
        skin.get("default-24", TextButton.TextButtonStyle.class).font = font24;
        skin.get("default-24-red", TextButton.TextButtonStyle.class).font = font24;
        skin.get("default-24-green", TextButton.TextButtonStyle.class).font = font24;
        skin.get("default-24-yellow", TextButton.TextButtonStyle.class).font = font24;

        skin.get("default-16", SelectBox.SelectBoxStyle.class).font = font16;
        skin.get("default-16", SelectBox.SelectBoxStyle.class).listStyle.font = font16;
        skin.get("default-16-dark-background", SelectBox.SelectBoxStyle.class).font = font16;
        skin.get("default-16-dark-background", SelectBox.SelectBoxStyle.class).listStyle.font = font16;
        skin.get("default-16", List.ListStyle.class).font = font16;
        skin.get("default-16-padded", List.ListStyle.class).font = font16;
        skin.get("default-16-padded-clear", List.ListStyle.class).font = font16;
        skin.get("default-16", TextField.TextFieldStyle.class).font = font16;

        HUD = new Hud();

        try {

            backGround = new Texture(Gdx.files.classpath("assets/data/frame.png"));

            TextureRegion[][] trs = TextureRegion.split(new Texture(Gdx.files.classpath("assets/data/ultima1-tiles.png")), 16, 16);
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 9; col++) {
                    u1Tiles[row * 9 + col] = trs[row][col];
                }
            }

            trs = TextureRegion.split(new Texture(Gdx.files.classpath("assets/data/uf_portraits_example.png")), 48, 48);
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 6; col++) {
                    faceTiles[row * 6 + col] = trs[row][col];
                }
            }

            Icons.init();
            TibianSprite.init();

            world_scr_avatar = new Animation(.4f, u1Tiles[10], u1Tiles[11]);
            game_scr_avatar = TibianSprite.icon("Bandit");

            WizardryData.class.getClass();

            CONVERSATIONS = Conversations.init();

        } catch (Exception e) {
            e.printStackTrace();
        }

        mainGame = this;
        startScreen = new StartScreen();
        setScreen(startScreen);

    }

}
