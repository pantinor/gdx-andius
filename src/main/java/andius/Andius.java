package andius;

import andius.objects.UltimaSprite;
import andius.objects.Conversations;
import andius.objects.Icons;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import utils.FreeTypeSkinLoader;
import utils.Hud;
import static utils.Utils.CLASSPTH_RSLVR;

public class Andius extends Game {

    public static final int SCREEN_WIDTH = 1024;
    public static final int SCREEN_HEIGHT = 768;
    public static final int MAP_VIEWPORT_DIM = 624;
    
    public static Context CTX;
    public static Texture backGround;
    public static TextureAtlas mapAtlas;
    public static AssetManager assetManager = new AssetManager(CLASSPTH_RSLVR);
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
    public static TextureRegion[] faceTiles = new TextureRegion[16 * 13];

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

        InternalFileHandleResolver resolver = new InternalFileHandleResolver();
        assetManager.setLoader(Skin.class, new FreeTypeSkinLoader(resolver));

        FreeTypeSkinLoader.FreeTypeSkinLoaderParameter skinParam
                = new FreeTypeSkinLoader.FreeTypeSkinLoaderParameter("assets/skin/uiskin.atlas");

        assetManager.load("assets/skin/uiskin.json", Skin.class, skinParam);
        assetManager.finishLoading();

        skin = assetManager.get("assets/skin/uiskin.json", Skin.class);

        font12 = skin.get("font12", BitmapFont.class);
        font14 = skin.get("font14", BitmapFont.class);
        font16 = skin.get("font16", BitmapFont.class);
        font18 = skin.get("font18", BitmapFont.class);
        font24 = skin.get("font24", BitmapFont.class);
        font72 = skin.get("font72", BitmapFont.class);

        HUD = new Hud();

        try {

            backGround = new Texture(Gdx.files.classpath("assets/data/frame.png"));

            TextureRegion[][] trs = TextureRegion.split(new Texture(Gdx.files.classpath("assets/data/ultima1-tiles.png")), 16, 16);
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 9; col++) {
                    u1Tiles[row * 9 + col] = trs[row][col];
                }
            }

            trs = TextureRegion.split(new Texture(Gdx.files.classpath("assets/data/u6-portraits.png")), 56, 64);
            for (int row = 0; row < 13; row++) {
                for (int col = 0; col < 16; col++) {
                    faceTiles[row * 16 + col] = trs[row][col];
                }
            }

            Icons.init();
            UltimaSprite.init();

            world_scr_avatar = new Animation<>(.4f, u1Tiles[10], u1Tiles[11]);
            game_scr_avatar = u1Tiles[10];

            WizardryData.class.getClass();
            WizardryData.initItemScenarioIds();

            CONVERSATIONS = Conversations.init();

        } catch (Exception e) {
            e.printStackTrace();
        }

        mainGame = this;
        startScreen = new StartScreen();
        setScreen(startScreen);

    }

}
