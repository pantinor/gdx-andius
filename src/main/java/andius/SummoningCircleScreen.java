package andius;

import andius.objects.Sound;
import andius.objects.Sounds;
import static andius.Andius.SCREEN_HEIGHT;
import static andius.Andius.SCREEN_WIDTH;
import static andius.Andius.mainGame;
import andius.WizardryData.SummoningCircle;
import andius.objects.Monster;
import andius.objects.MutableMonster;
import andius.objects.SaveGame;
import andius.objects.SaveGame.CharacterRecord;
import andius.objects.Spells;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import utils.AutoFocusScrollPane;
import utils.FrameMaker;
import utils.Utils;

public class SummoningCircleScreen implements Screen, Constants {

    private final Stage stage;
    public final CharacterRecord player;
    private final SummoningCircle summoningId;
    private final Batch batch;
    private final SelectBox<MonsterItem> select1, select2, select3;
    private final List<MonsterItem> monsters;
    private final AutoFocusScrollPane monstersScroll;
    private final TextButton summon;
    private final TextButton exit;
    private final Texture background;

    public SummoningCircleScreen(CharacterRecord player, SummoningCircle summoningId) {
        this.summoningId = summoningId;
        this.player = player;
        this.batch = new SpriteBatch();
        this.stage = new Stage();

        if (!player.summoningCircles.contains(summoningId)) {
            player.summoningCircles.add(summoningId);
            player.level = summoningId.ordinal() + 1;
            player.str = Utils.adjustValueMax(player.str, 1, 18);
            player.intell = Utils.adjustValueMax(player.intell, 1, 18);
            player.piety = Utils.adjustValueMax(player.piety, 1, 18);
            player.vitality = Utils.adjustValueMax(player.vitality, 1, 18);
            player.agility = Utils.adjustValueMax(player.agility, 1, 18);
            player.luck = Utils.adjustValueMax(player.luck, 1, 18);
            player.maxhp = player.level * 10;
            for (int i = 0; i <= 6; i++) {
                if (i + 1 > player.level) {
                    break;
                }
                player.magePoints[i] = 9;
            }
            player.knownSpells.clear();
            if (player.magePoints[0] > 0) {
                for (int i = 1; i <= 4; i++) {
                    player.knownSpells.add(Spells.values()[i - 1]);
                }
            }
            if (player.magePoints[1] > 0) {
                for (int i = 5; i <= 6; i++) {
                    player.knownSpells.add(Spells.values()[i - 1]);
                }
            }
            if (player.magePoints[2] > 0) {
                for (int i = 7; i <= 8; i++) {
                    player.knownSpells.add(Spells.values()[i - 1]);
                }
            }
            if (player.magePoints[3] > 0) {
                for (int i = 9; i <= 11; i++) {
                    player.knownSpells.add(Spells.values()[i - 1]);
                }
            }
            if (player.magePoints[4] > 0) {
                for (int i = 12; i <= 14; i++) {
                    player.knownSpells.add(Spells.values()[i - 1]);
                }
            }
            if (player.magePoints[5] > 0) {
                for (int i = 15; i <= 18; i++) {
                    player.knownSpells.add(Spells.values()[i - 1]);
                }
            }
            if (player.magePoints[6] > 0) {
                for (int i = 19; i <= 21; i++) {
                    player.knownSpells.add(Spells.values()[i - 1]);
                }
            }
        }

        for (int i = 0; i <= 6; i++) {
            if (i + 1 > player.level) {
                break;
            }
            player.magePoints[i] = 9;
        }

        this.player.adjustHP(this.player.maxhp);

        FrameMaker fm = new FrameMaker(SCREEN_WIDTH, SCREEN_HEIGHT, new Color(0x080808ff));

        this.monsters = new List(Andius.skin, "default-16");
        this.monstersScroll = new AutoFocusScrollPane(this.monsters, Andius.skin);
        this.monstersScroll.setScrollingDisabled(true, false);

        Array<MonsterItem> ms = new Array<>();
        for (MutableMonster mm : player.summonedMonsters) {
            ms.add(new MonsterItem((Monster) mm.baseType()));
        }

        this.monsters.setItems(ms);

        Array<MonsterItem> arr = new Array<>();
        for (int i = this.summoningId.low(); i <= this.summoningId.high(); i++) {
            arr.add(new MonsterItem(WizardryData.Scenario.WER.monsters().get(i)));
        }

        select1 = new SelectBox<>(Andius.skin, "default-16");
        select2 = new SelectBox<>(Andius.skin, "default-16");
        select3 = new SelectBox<>(Andius.skin, "default-16");

        select1.setItems(arr);
        select1.setSelected(arr.first());
        select2.setItems(arr);
        select2.setSelected(arr.first());
        select3.setItems(arr);
        select3.setSelected(arr.first());

        this.summon = new TextButton("SUMMON", Andius.skin, "default-16");
        this.summon.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                summon();
            }
        });

        this.exit = new TextButton("LEAVE", Andius.skin, "default-16");
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                mainGame.setScreen(Map.WIZARDRY4.getScreen());
            }
        });
        this.exit.setBounds(400, 450, 80, 40);

        Texture t = new Texture(Gdx.files.classpath("assets/data/pentagram.png"));
        t.getTextureData().prepare();
        fm.drawPixmap(t.getTextureData().consumePixmap(), 100, 0);

        int x = 280;
        fm.setBounds(this.select1, x, 530, 130, 20);
        fm.setBounds(this.select2, x += 150, 530, 130, 20);
        fm.setBounds(this.select3, x += 150, 530, 130, 20);
        this.summon.setBounds(x += 150, 530 - 4, 100, 30);
        this.exit.setBounds(x, 485, 100, 30);

        fm.setBounds(this.monstersScroll, 750, 100, 200, 200);

        this.stage.addActor(this.monstersScroll);
        this.stage.addActor(this.select1);
        this.stage.addActor(this.select2);
        this.stage.addActor(this.select3);

        this.stage.addActor(summon);
        this.stage.addActor(exit);

        this.background = fm.build();

    }

    public void summon() {

        Sounds.play(Sound.SPIRITS);

        Monster m1 = this.select1.getSelected().m;
        Monster m2 = this.select2.getSelected().m;
        Monster m3 = this.select3.getSelected().m;

        int roll1 = m1.getGroupSize().roll();
        int roll2 = m2.getGroupSize().roll();
        int roll3 = m3.getGroupSize().roll();

        this.player.summonedMonsters.clear();

        for (int i = 1; i <= roll1; i++) {
            this.player.summonedMonsters.add(new MutableMonster(m1));
        }

        for (int i = 1; i <= roll2; i++) {
            this.player.summonedMonsters.add(new MutableMonster(m2));
        }

        for (int i = 1; i <= roll3; i++) {
            this.player.summonedMonsters.add(new MutableMonster(m3));
        }

        Array<MonsterItem> ms = new Array<>();
        for (MutableMonster mm : player.summonedMonsters) {
            ms.add(new MonsterItem((Monster) mm.baseType()));
        }

        this.monsters.setItems(ms);

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(this.background, 0, 0);

        Andius.font24.draw(batch, "The " + this.summoningId.display() + " Level Pentagram Glows...", 350, 700);
        Andius.font24.draw(batch, "and the Gate Opens!", 350, 670);
        Andius.font24.draw(batch, "Call Forth 3 Groups of Monsters!", 300, 620);

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

    private class MonsterItem {

        private final Monster m;

        public MonsterItem(Monster m) {
            this.m = m;
        }

        @Override
        public String toString() {
            return this.m.name.toUpperCase();
        }

    }

}
