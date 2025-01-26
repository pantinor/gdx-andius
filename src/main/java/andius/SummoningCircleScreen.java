package andius;

import static andius.Andius.SCREEN_HEIGHT;
import static andius.Andius.SCREEN_WIDTH;
import static andius.Andius.mainGame;
import andius.WizardryData.SummoningCircle;
import andius.objects.Monster;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import utils.AutoFocusScrollPane;
import utils.FrameMaker;

public class SummoningCircleScreen implements Screen, Constants {

    private final Stage stage;
    public final CharacterRecord player;
    private final SummoningCircle summoningId;
    private final Batch batch;
    private final List<Monster> monsters;
    private final AutoFocusScrollPane monstersScroll;
    private final TextButton summon;
    private final TextButton exit;
    private final Texture background;

    private static final int TABLE_HEIGHT = 740;
    private static final int LISTING_WIDTH = 300;

    public SummoningCircleScreen(CharacterRecord player, SummoningCircle summoningId) {
        this.summoningId = summoningId;
        this.player = player;
        this.batch = new SpriteBatch();
        this.stage = new Stage();
        this.stage.setDebugAll(true);

        FrameMaker fm = new FrameMaker(SCREEN_WIDTH, SCREEN_HEIGHT);
        
        //200: Dinks, Fuzzball, Creeping Coins, Bubbly Slimes, Orcs, Level 1 Mages, Gas Clouds, Skeletons, Garian Raiders, Level 1 Priests, Zombies, Kobolds.
        //201: Creeping Cruds, Crawling Kelps, Mummies, Witches, Poltergeists, No-See-Um Swarm, Rogues, Ashers, Anacondas, Dusters, Huge Spiders, Lvl 3 Priests.
        //202: Rotting Corpses, Dragon Flies, Spirits, Harpies, Bugbears, Wererats, Ronins, Gaze Hounds, Banshees, Shades, Level 5 Priests, Looters.
        //203: Blink Dogs, Bushwackers, Moat Monsters, Strangler Vines, Giant Toads, Vorpal Bunnies, Giant Slugs, Goblin Shamans, Goblins, Cockatrics, Ogres, Priestesses
        //204: Level 3 Samurai, Grave Mists, High Corsairs, Minor Daiyamos, Lifestealers, Nightstalkers, Wights, Master Ninjas, Bishops, Werewolves, Hobgoblins, Centaurs.
        //-30480: Gargoyles, Ghasts, Komodo Dragons, Hellhounds, Priests of Fung, Masters/Dragons, Seraphim, Weretigers, Boring Beetles, Displacer Beasts, Corrosive Slimes, Gas Dragons.
        //206: Scrylls, Carriers, Myrmidons, Gorgons, Level 6 Ninjas, Dark Riders, Doppelgangers, Giant Mantises, Evil Eyes, Goblin Princes, Masters w/ Wind, Wyverns
        //207: Brass Dragons, Fiends, Will o' Wisps, Berserkers, Chimeras, Xenos, Bleebs, Rocs, Major Daimyos, Trools, Champ Samurai, Vampires.
        //-30220: Murphy's Ghosts, Manticores, Liches, Frost Giants, Fire Giants, Hamamotos, Masters/Summer, Hydrae, Succubi, Fire Drakes, Dragon Zombies, Cyclopses.
        //-30120: Greater Demons, Poison Giants, Gold Dragons, Maelifics, Vampire Lords, High Masters, Lycorgi, Black Dragons, Foaming Molds, Iron Golems, Flack, Demon Lords.

        
        //int[][] levels = new int[][];
        

        this.monsters = new List(Andius.skin, "default-16");
        this.monstersScroll = new AutoFocusScrollPane(this.monsters, Andius.skin);
        this.monstersScroll.setScrollingDisabled(true, false);

        this.exit = new TextButton("LEAVE", Andius.skin, "default-16");
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                mainGame.setScreen(Map.WIZARDRY4.getScreen());
            }
        });
        this.exit.setBounds(400, 450, 80, 40);

        this.summon = new TextButton("SUMMON", Andius.skin, "default-16");
        this.summon.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                summon();
            }
        });
        this.summon.setBounds(500, 450, 80, 40);

        fm.setBounds(this.monstersScroll, 712, 14, LISTING_WIDTH, TABLE_HEIGHT);

        this.stage.addActor(this.monstersScroll);
        this.stage.addActor(exit);
        this.stage.addActor(summon);

        this.background = fm.build();

    }
    
    public void summon() {
        
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(this.background, 0, 0);
        batch.end();

        stage.act();
        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            mainGame.setScreen(Map.WIZARDRY4.getScreen());
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            summon();
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }

}
