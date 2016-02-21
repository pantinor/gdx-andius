package andius;

import andius.objects.Conversations;
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

public class Andius extends Game {

    public static final int SCREEN_WIDTH = 1024;
    public static final int SCREEN_HEIGHT = 768;

    public static final int MAP_VIEWPORT_DIM = 624;

    public static final Context CONTEXT = new Context();
    
    public static Texture backGround;
    public static TextureAtlas heroesAtlas;
    public static TextureAtlas mapAtlas;
    
    public static Array<TextureAtlas.AtlasRegion> moongateTextures = new Array<>();
    
    public static BitmapFont font;
    public static BitmapFont smallFont;
    public static BitmapFont largeFont;
    public static BitmapFont ultimaFont;

    public static Andius mainGame;
    //public static StartScreen startScreen;

    public static Skin skin;

    public static boolean playMusic = true;
    public static float musicVolume = 0.1f;
    public static Music music;

    public static Animation explosionLarge;
    public static Animation explosion;
    public static Animation cloud;
    
    public static Animation avatar_warrior_red;
    public static Animation avatar_warrior_blue;
    public static Animation avatar_wizard_red;
    public static Animation avatar_wizard_blue;

    public static Animation marker_red;
    public static Animation marker_blue;
    public static Animation marker_white;


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

        parameter.size = 18;
        font = generator.generateFont(parameter);

        parameter.size = 12;
        smallFont = generator.generateFont(parameter);
        smallFont.setColor(Color.YELLOW);
        
        parameter.size = 24;
        largeFont = generator.generateFont(parameter);

        generator.dispose();

        generator = new FreeTypeFontGenerator(Gdx.files.classpath("assets/fonts/ultima.ttf"));
        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 96;
        ultimaFont = generator.generateFont(parameter);

        parameter.size = 48;
        BitmapFont smallUltimaFont = generator.generateFont(parameter);

        generator.dispose();

        skin = new Skin(Gdx.files.classpath("assets/skin/uiskin.json"));
        skin.remove("default-font", BitmapFont.class);
        skin.add("default-font", font, BitmapFont.class);
        skin.add("journal", font, BitmapFont.class);
        skin.add("death-screen", largeFont, BitmapFont.class);
        skin.add("ultima", ultimaFont, BitmapFont.class);
        skin.add("small-ultima", smallUltimaFont, BitmapFont.class);

        Label.LabelStyle ls = skin.get("default", Label.LabelStyle.class);
        ls.font = font;
        TextButton.TextButtonStyle tbs = skin.get("default", TextButton.TextButtonStyle.class);
        tbs.font = font;
        TextButton.TextButtonStyle tbswood = skin.get("wood", TextButton.TextButtonStyle.class);
        tbswood.font = font;
        SelectBox.SelectBoxStyle sbs = skin.get("default", SelectBox.SelectBoxStyle.class);
        sbs.font = font;
        sbs.listStyle.font = font;
        CheckBox.CheckBoxStyle cbs = skin.get("default", CheckBox.CheckBoxStyle.class);
        cbs.font = font;
        List.ListStyle lis = skin.get("default", List.ListStyle.class);
        lis.font = font;
        TextField.TextFieldStyle tfs = skin.get("default", TextField.TextFieldStyle.class);
        tfs.font = font;

        //hud = new LogDisplay();
        try {

            backGround = new Texture(Gdx.files.classpath("assets/data/frame.png"));

//            TextureRegion[][] trs = TextureRegion.split(new Texture(Gdx.files.classpath("assets/graphics/Portraits.gif")), 56, 64);
//            for (int row = 0; row < 13; row++) {
//                for (int col = 0; col < 16; col++) {
//                    //faceTiles[row * 16 + col] = trs[row][col];
//                }
//            }

            heroesAtlas = new TextureAtlas(Gdx.files.classpath("assets/data/heroes-atlas.txt"));
            mapAtlas = new TextureAtlas(Gdx.files.classpath("assets/data/map-atlas.txt"));
            moongateTextures = mapAtlas.findRegions("moongate");
            
            avatar_warrior_red = new Animation(.4f,mapAtlas.findRegions("avatar_warrior_red"));
//
//            hitTile = standardAtlas.findRegion("hit_flash");
//            magicHitTile = standardAtlas.findRegion("magic_flash");
//            missTile = standardAtlas.findRegion("miss_flash");
//            corpse = standardAtlas.findRegion("corpse");
            
            
//            TextureAtlas tmp = new TextureAtlas(Gdx.files.classpath("assets/data/explosion-atlas.txt"));
//            Array<TextureAtlas.AtlasRegion> ar = tmp.findRegions("expl");
//            explosion = new Animation(.2f, ar);
//
//            tmp = new TextureAtlas(Gdx.files.classpath("assets/data/Exp_type_B.atlas"));
//            ar = tmp.findRegions("im");
//            explosionLarge = new Animation(.1f, ar);
//
//            tmp = new TextureAtlas(Gdx.files.classpath("assets/data/cloud-atlas.txt"));
//            ar = tmp.findRegions("cloud");
//            cloud = new Animation(.2f, ar);

//            baseTileSet = (TileSet) Utils.loadXml("assets/xml/tileset-base.xml", TileSet.class);
//            baseTileSet.setMaps();
//
//            u4TileSet = (TileSet) Utils.loadXml("assets/xml/u4-tileset-base.xml", TileSet.class);
//            u4TileSet.setMaps();
//
//            maps = (MapSet) Utils.loadXml("assets/xml/maps.xml", MapSet.class);
//            maps.init(baseTileSet);
//            maps.init(MapType.combat, u4TileSet);//set combat maps with the u4 tile set
//            maps.init(MapType.shrine, u4TileSet);//set combat maps with the u4 tile set
//
//            vendorClassSet = (VendorClassSet) Utils.loadXml("assets/xml/vendor.xml", VendorClassSet.class);
//            vendorClassSet.init();
//
//            weapons = (WeaponSet) Utils.loadXml("assets/xml/weapons.xml", WeaponSet.class);
//            armors = (ArmorSet) Utils.loadXml("assets/xml/armors.xml", ArmorSet.class);
//            creatures = (CreatureSet) Utils.loadXml("assets/xml/creatures.xml", CreatureSet.class);
//            creatures.init();
//            weapons.init();
//            armors.init();
            
            Constants.Map.init();
            Constants.Moongate.init();
            Constants.Heroes.init(heroesAtlas);
            Conversations.init();
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        mainGame = this;
//        startScreen = new StartScreen();

        setScreen(Constants.Map.WORLD.getScreen());
    }

    public static class CloudDrawable extends Actor {

        float stateTime;

        @Override
        public void draw(Batch batch, float parentAlpha) {
            stateTime += Gdx.graphics.getDeltaTime();
            batch.draw(Andius.cloud.getKeyFrame(stateTime, false), getX(), getY(), 64, 64);
        }
    }

    public static class ExplosionDrawable extends Actor {

        float stateTime;

        @Override
        public void draw(Batch batch, float parentAlpha) {
            stateTime += Gdx.graphics.getDeltaTime();
            batch.draw(Andius.explosion.getKeyFrame(stateTime, false), getX(), getY(), 64, 64);
        }
    }

    public static class ExplosionLargeDrawable extends Actor {

        float stateTime;

        @Override
        public void draw(Batch batch, float parentAlpha) {
            stateTime += Gdx.graphics.getDeltaTime();
            batch.draw(Andius.explosionLarge.getKeyFrame(stateTime, false), getX(), getY(), 192, 192);
        }
    }

}
