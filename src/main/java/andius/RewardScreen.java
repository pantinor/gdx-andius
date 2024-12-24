package andius;

import static andius.Andius.mainGame;
import andius.objects.ClassType;
import andius.objects.Item;
import andius.objects.Reward;
import andius.objects.Reward.RewardDetails;
import andius.objects.SaveGame.CharacterRecord;
import andius.objects.Spells;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import java.util.ArrayList;
import java.util.Random;
import utils.AutoFocusScrollPane;
import utils.LogScrollPane;
import utils.Utils;

public class RewardScreen implements Screen, Constants {

    public enum TrapType {
        NONE,
        POISON_NEEDLE,
        GAS_BOMB,
        BOLT,
        TELEPORTER,
        ANTI_MAGE,
        ANTI_PRIEST,
        ALARM,
        CROSSBOW_BOLT,
        EXPLODING_BOX,
        SPLINTERS,
        BLADES,
        STUNNER;
    }

    private final Context context;
    private final Map contextMap;
    private final Reward goldReward;
    private final Reward chestReward;
    private final int difficultyLevel;
    private final int expPoints;

    private final Texture hud;
    private final SpriteBatch batch;
    private final Stage stage;

    private final List<String> action;
    private final Label pselLabel;
    private final List<String> playerSelection;
    private final List<TrapType> trapSelection;

    private final LogScrollPane logs;
    private final TextButton go;

    TrapType trapType = TrapType.NONE;
    protected Random rand = new Random();
    private final java.util.List<CharacterRecord> whoTried = new java.util.ArrayList<>();
    private boolean chestOpened = false;

    private static final int LOG_AREA_WIDTH = 650;
    private static final int X_ALIGN = 280;
    private static final int ITEM_HGT = 25;

    public RewardScreen(Context context, Map contextMap, int difficultyLevel, int expPoints, int goldReward, int chestReward) {
        this.context = context;
        this.contextMap = contextMap;
        this.goldReward = contextMap.scenario().rewards().get(goldReward);
        this.chestReward = contextMap.scenario().rewards().get(chestReward);
        this.difficultyLevel = difficultyLevel;
        this.expPoints = expPoints;

        this.hud = new Texture(Gdx.files.classpath("assets/data/treasure.png"));
        this.batch = new SpriteBatch();
        this.stage = new Stage();

        int trap = this.chestReward.trapTypeFlags;
        java.util.List<TrapType> tmp = new ArrayList<>();
        for (int j = 0; j < TrapType.values().length; j++) {
            if ((trap & 0x01) != 0) {
                tmp.add(TrapType.values()[j]);
            }
            trap >>>= 1;
        }
        if (!tmp.isEmpty()) {
            this.trapType = tmp.get(rand.nextInt(tmp.size()));
        }

        final TrapType[] tt = new TrapType[TrapType.values().length - 1];
        for (int i = 1; i < TrapType.values().length; i++) {
            tt[i - 1] = TrapType.values()[i];
        }
        this.trapSelection = new List<>(Andius.skin, "default");
        this.trapSelection.setItems(new TrapType[0]);

        this.action = new List<>(Andius.skin, "larger");
        this.action.setItems(new String[]{"OPEN", "INSPECT", "CALFO", "DISARM", "LEAVE"});
        this.action.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (action.getSelected().equals("LEAVE")) {
                    pselLabel.setText("");
                } else {
                    pselLabel.setText("WHO WILL " + action.getSelected() + " ?");
                }
                if (action.getSelected().equals("DISARM")) {
                    trapSelection.setItems(tt);
                } else {
                    trapSelection.setItems(new TrapType[0]);
                }
            }
        });

        this.playerSelection = new List<>(Andius.skin, "larger");
        String[] names = new String[this.context.players().length];
        for (int i = 0; i < this.context.players().length; i++) {
            names[i] = this.context.players()[i].name.toUpperCase();
        }
        this.playerSelection.setItems(names);

        this.go = new TextButton("GO", Andius.skin, "red-larger");
        this.go.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                CharacterRecord player = null;
                for (CharacterRecord p : RewardScreen.this.context.players()) {
                    if (p.name.toUpperCase().equals(playerSelection.getSelected())) {
                        player = p;
                        break;
                    }
                }
                if (action.getSelected().equals("OPEN")) {
                    open(player);
                } else if (action.getSelected().equals("INSPECT")) {
                    inspect(player);
                } else if (action.getSelected().equals("CALFO")) {
                    calfo(player);
                } else if (action.getSelected().equals("DISARM")) {
                    disarm(player, trapSelection.getSelected());
                } else if (action.getSelected().equals("LEAVE")) {
                    leave();
                }
            }
        });

        AutoFocusScrollPane sp1 = new AutoFocusScrollPane(this.action, Andius.skin);
        AutoFocusScrollPane sp2 = new AutoFocusScrollPane(this.playerSelection, Andius.skin);
        AutoFocusScrollPane sp3 = new AutoFocusScrollPane(this.trapSelection, Andius.skin);

        sp1.setScrollingDisabled(true, false);
        sp2.setScrollingDisabled(true, false);
        sp3.setScrollingDisabled(true, false);

        Label tmp1 = new Label("A CHEST !", Andius.skin, "larger");
        Label tmp2 = new Label("YOU MAY :", Andius.skin, "larger");
        this.pselLabel = new Label("WHO WILL OPEN ?", Andius.skin, "larger");

        this.logs = new LogScrollPane(Andius.skin, new Table(), 275);
        this.logs.setBounds(X_ALIGN, Andius.SCREEN_HEIGHT - 200, LOG_AREA_WIDTH, 150);

        tmp1.setBounds(X_ALIGN, 500, 175, ITEM_HGT);
        tmp2.setBounds(X_ALIGN, 475, 175, ITEM_HGT);
        sp1.setBounds(X_ALIGN, 295, 100, 175);
        sp2.setBounds(X_ALIGN + 110, 295, 175, 175);
        pselLabel.setBounds(X_ALIGN + 110, 475, 175, ITEM_HGT);
        go.setBounds(X_ALIGN + 110 + 185, 310, 65, 40);
        sp3.setBounds(X_ALIGN + 110 + 185 + 80, 298, 155, 198);

        stage.addActor(tmp1);
        stage.addActor(tmp2);
        stage.addActor(pselLabel);
        stage.addActor(sp1);
        stage.addActor(sp2);
        stage.addActor(sp3);
        stage.addActor(go);
        stage.addActor(this.logs);
    }

    private void open(CharacterRecord player) {
        if (player.isDisabled()) {
            Sounds.play(Sound.NEGATIVE_EFFECT);
            log("Cannot open it!");
            return;
        }
        if (this.chestOpened) {
            Sounds.play(Sound.NEGATIVE_EFFECT);
            log("Already opened!");
            return;
        }
        releaseTrapAffect(player);

        java.util.List<CharacterRecord> okChars = new ArrayList<>();
        for (CharacterRecord c : this.context.players()) {
            if (!c.isDisabled()) {
                okChars.add(c);
            }
        }

        if (chestReward != null) {
            for (CharacterRecord c : okChars) {
                int goldAmt = this.chestReward.goldAmount();
                c.adjustGold(goldAmt);
                log(String.format("%s found %d gold.", c.name.toUpperCase(), goldAmt));
            }
        }

        if (chestReward != null) {
            if (!okChars.isEmpty()) {
                for (RewardDetails d : chestReward.getRewardDetails()) {
                    if (d.itemReward != null) {
                        int odds = d.odds;
                        int roll = rand.nextInt(101);
                        if (roll <= odds) {
                            CharacterRecord picked = okChars.get(rand.nextInt(okChars.size()));
                            int itemId = Utils.getRandomBetween(d.itemReward.getMin(), d.itemReward.getMax());
                            Item found = this.contextMap.scenario().items().get(itemId).clone();
                            picked.inventory.add(found);
                            log(String.format("%s finds a %s.", picked.name.toUpperCase(), found.genericName));
                        }
                    }
                }
                Sounds.play(Sound.POSITIVE_EFFECT);
                this.chestOpened = true;
            }
        }

    }

    private void releaseTrapAffect(CharacterRecord player) {
        switch (this.trapType) {
            case NONE:
                break;
            case TELEPORTER:
                log("The party was teleported!"); //TODO
                Sounds.play(Sound.GAZE);
                break;
            case ALARM:
                log("You triggered a noisy alarm that can attract attention!");
                Sounds.play(Sound.TRIGGER);
                break;
            case POISON_NEEDLE:
                log("Jabbed by poisoned needle!");
                player.status.set(Status.POISONED, 1);
                Sounds.play(Sound.POISON_EFFECT);
                break;
            case GAS_BOMB:
                log("A gas bomb explodes in your faces!");
                for (CharacterRecord c : this.context.players()) {
                    if (rand.nextInt(20) + 1 < c.luck) {
                        player.status.set(Status.POISONED, 1);
                    }
                }
                Sounds.play(Sound.POISON_EFFECT);
                break;
            case ANTI_MAGE:
                for (CharacterRecord c : this.context.players()) {
                    if (c.classType == ClassType.MAGE || c.classType == ClassType.WIZARD || c.classType == ClassType.SAMURAI) {
                        if (rand.nextInt(21) < c.luck) {
                            log("A spell has a horrible affect on the mage " + c.name);
                            if (rand.nextInt(2) == 0) {
                                c.status.set(Status.STONED, 4);
                            } else {
                                c.status.set(Status.PARALYZED, 4);
                            }
                            Sounds.play(Sound.MAGIC);
                        }
                    }
                }
                Sounds.play(Sound.ROCKS);
                break;
            case ANTI_PRIEST:
                for (CharacterRecord c : this.context.players()) {
                    if (c.classType == ClassType.CLERIC || c.classType == ClassType.LORD) {
                        if (rand.nextInt(20) + 1 < c.luck) {
                            log("A spell has a horrible affect on the cleric " + c.name);
                            if (rand.nextInt(2) == 0) {
                                c.status.set(Status.STONED, 4);
                            } else {
                                c.status.set(Status.PARALYZED, 4);
                            }
                            Sounds.play(Sound.MAGIC);
                        }
                    }
                }
                Sounds.play(Sound.ROCKS);
                break;
            case BOLT:
            case CROSSBOW_BOLT: {
                log("Bolts come flying out of the wall!");
                int damage = 0;
                for (int i = 0; i < this.difficultyLevel; i++) {
                    damage += rand.nextInt(8) + 1;
                }
                player.adjustHP(-damage);
                Sounds.play(Sound.CROSSBOW);
                break;
            }
            case EXPLODING_BOX: {
                log("The box explodes!");
                for (CharacterRecord c : this.context.players()) {
                    int damage = 0;
                    for (int i = 0; i < this.difficultyLevel; i++) {
                        damage += rand.nextInt(8) + 1;
                    }
                    c.adjustHP(-damage);
                }
                Sounds.play(Sound.EXPLOSION);
                break;
            }
            case SPLINTERS: {
                log("Splinters come flying out into your face!");
                for (CharacterRecord c : this.context.players()) {
                    int damage = 0;
                    for (int i = 0; i < this.difficultyLevel; i++) {
                        damage += rand.nextInt(6) + 1;
                    }
                    c.adjustHP(-damage);
                }
                Sounds.play(Sound.BOOM);
                break;
            }
            case BLADES: {
                log("Piercing blades slice across your body!");
                for (CharacterRecord c : this.context.players()) {
                    int damage = 0;
                    for (int i = 0; i < this.difficultyLevel; i++) {
                        damage += rand.nextInt(12) + 1;
                    }
                    c.adjustHP(-damage);
                }
                Sounds.play(Sound.BOOM);
                break;
            }
            case STUNNER:
                log("A stunning noise is emitted from the chest!");
                player.status.set(Status.PARALYZED, 4);
                Sounds.play(Sound.GAZE);
                break;
        }
        this.trapType = TrapType.NONE;
    }

    private void inspect(CharacterRecord player) {
        if (player.isDisabled()) {
            Sounds.play(Sound.NEGATIVE_EFFECT);
            log("Cannot inspect!");
            return;
        }
        if (whoTried.contains(player)) {
            Sounds.play(Sound.NEGATIVE_EFFECT);
            log("Already tried!");
            return;
        }
        whoTried.add(player);
        int chance = player.agility;
        if (player.classType == ClassType.THIEF) {
            chance = chance * 6;
        }
        if (player.classType == ClassType.NINJA) {
            chance = chance * 4;
        }
        if (chance > 95) {
            chance = 95;
        }
        if (rand.nextInt(100) + 1 < chance) {
            log(String.format("%s finds %s.", player.name.toUpperCase(), this.trapType.toString()));
        } else if (rand.nextInt(20) + 1 > player.agility) {
            releaseTrapAffect(player);
        } else {
            log(String.format("%s finds %s.", player.name.toUpperCase(), TrapType.values()[rand.nextInt(TrapType.values().length)].toString()));
        }
    }

    private void calfo(CharacterRecord player) {
        if (player.isDisabled()) {
            Sounds.play(Sound.NEGATIVE_EFFECT);
            log("Cannot cast calfo!");
            return;
        }
        if (player.clericPoints[Spells.CALFO.getLevel() - 1] < 1) {
            Sounds.play(Sound.NEGATIVE_EFFECT);
            log("Not enough spell points!");
            return;
        }
        log("Calfo disarmed " + this.trapType.toString());
        Sounds.play(Sound.TRIGGER);
        player.clericPoints[Spells.CALFO.getLevel() - 1] -= 1;
        this.trapType = TrapType.NONE;
    }

    private void disarm(CharacterRecord player, TrapType ttype) {
        if (player.isDisabled()) {
            Sounds.play(Sound.NEGATIVE_EFFECT);
            log("Cannot disarm!");
            return;
        }
        if (this.trapType == TrapType.NONE) {
            log("Nothing found to disarm!");
            return;
        }
        int mod = (player.classType == ClassType.THIEF || player.classType == ClassType.NINJA ? 1 : 0);
        if (ttype == this.trapType) {
            if (rand.nextInt(70) + 1 < player.level - this.difficultyLevel + 50 * mod) {
                log("You disarmed it!");
                this.trapType = TrapType.NONE;
                Sounds.play(Sound.TRIGGER);
            } else if (rand.nextInt(20) + 1 < player.agility) {
                log("Disarm failed!");
            }
        } else {
            releaseTrapAffect(player);
        }
    }

    private void leave() {
        mainGame.setScreen(this.contextMap.getScreen());
    }

    private void log(String s) {
        this.logs.add(s);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        java.util.List<CharacterRecord> okChars = new ArrayList<>();
        for (CharacterRecord c : this.context.players()) {
            if (!c.isDisabled()) {
                okChars.add(c);
            }
        }
        int exp = this.expPoints / okChars.size();
        for (CharacterRecord c : okChars) {
            int goldAmt = this.goldReward.goldAmount();
            c.adjustGold(goldAmt);
            c.awardXP(exp);
            log(String.format("%s found %d gold and %d experience points.", c.name.toUpperCase(), goldAmt, exp));
        }
    }

    @Override
    public void render(float f) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(this.hud, 0, 0);

        int x1 = X_ALIGN;
        int y = 245;
        Andius.largeFont.draw(batch, "Name", x1, y);
        Andius.largeFont.draw(batch, "Class", x1 += 150, y);
        Andius.largeFont.draw(batch, "Status", x1 += 200, y);
        Andius.largeFont.draw(batch, "Hit Points", x1 += 120, y);
        y -= 25;
        for (CharacterRecord c : this.context.players()) {
            x1 = X_ALIGN;
            Andius.largeFont.setColor(c.isDead() ? Color.RED : c.status.color());
            if (c.hp > 0 && c.hp < 2) {
                Andius.largeFont.setColor(Color.SALMON);
            }
            Andius.largeFont.draw(batch, c.name.toUpperCase(), x1, y);
            String d = String.format("LVL %d  %s  %s", c.level, c.race.toString(), c.classType.toString());
            Andius.largeFont.draw(batch, d, x1 += 150, y);
            Andius.largeFont.draw(batch, "" + (c.isDead() ? "DEAD" : c.status), x1 += 200, y);
            Andius.largeFont.draw(batch, String.format("%d / %d", c.hp, c.maxhp), x1 += 120, y);
            y -= 25;
        }

        Andius.largeFont.setColor(Color.WHITE);

        batch.end();
        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int i, int i1) {
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
