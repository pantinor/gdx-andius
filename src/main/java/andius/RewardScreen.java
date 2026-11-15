package andius;

import andius.objects.Sound;
import andius.objects.Sounds;
import static andius.Andius.mainGame;
import andius.Constants.Status;
import andius.objects.ClassType;
import andius.objects.Item;
import andius.objects.Reward;
import andius.objects.Reward.RewardDetails;
import andius.objects.Reward.TrapType;
import andius.objects.SaveGame.CharacterRecord;
import andius.objects.Spells;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import java.util.ArrayList;
import java.util.Random;
import utils.LogScrollPane;
import utils.Utils;
import utils.ManagementUi;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import static utils.SkinUtils.GOLD;
import static utils.SkinUtils.INK;
import static utils.SkinUtils.MUTED;

public class RewardScreen implements Screen, Constants {

    private final Context context;
    private final Map map;
    private final Reward reward;

    private final ManagementUi ui = new ManagementUi();
    private final Texture chestTexture;
    private Label chestStatus, actionHint, memberDetails;
    private Image portrait;
    private TextButton leaveButton;
    private final Stage stage;

    private final List<String> action;
    private final Label pselLabel;
    private final List<CharacterRecord> playerSelection;
    private final SelectBox<TrapType> trapSelection;

    private final LogScrollPane logs;
    private final TextButton go;

    private TrapType trap;
    protected Random rand = new Random();
    private final java.util.List<CharacterRecord> whoTried = new java.util.ArrayList<>();
    private boolean chestOpened = false;
    private boolean chestDisarmed = false;

    public RewardScreen(Context context, Map map, int rewardId) {
        this.context = context;
        this.map = map;
        this.reward = map.scenario().rewards().get(rewardId);
        this.trap = this.reward.getTrap();

        this.chestTexture = new Texture(Gdx.files.classpath("assets/data/rewards.png"));
        this.stage = new Stage(new FitViewport(Andius.SCREEN_WIDTH, Andius.SCREEN_HEIGHT));
        //this.stage.setDebugAll(true);

        this.trapSelection = new SelectBox<>(ui.skin);
        this.trapSelection.setItems(TrapType.values());

        this.action = new List<>(ui.skin);
        this.action.setItems(new String[]{"OPEN", "INSPECT", "CALFO", "DISARM"});
        this.playerSelection = new List<>(ui.skin) {
            @Override
            public String toString(CharacterRecord player) {
                return player.name + " / " + player.classType;
            }
        };
        this.playerSelection.setItems(context.players());

        this.go = new TextButton("Open chest", ui.skin);
        this.go.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                CharacterRecord player = playerSelection.getSelected();
                if (player == null) {
                    return;
                }
                if (action.getSelected().equals("OPEN")) {
                    open(player);
                } else if (action.getSelected().equals("INSPECT")) {
                    inspect(player);
                } else if (action.getSelected().equals("CALFO")) {
                    calfo(player);
                } else if (action.getSelected().equals("DISARM")) {
                    disarm(player, trapSelection.getSelected());

                }
            }
        });

        this.pselLabel = new Label("", ui.skin, "emphasis");
        this.logs = new LogScrollPane(ui.skin, new Table(), 304, "default");
        buildLayout();
    }

    private void buildLayout() {
        ui.label(stage, "Treasure discovered", "default-24", GOLD, 24, 714, 700, 34);
        ui.label(stage, "Secure the chest and claim the spoils of your expedition.", "default", MUTED, 24, 696, 700, 20);
        ui.panel(stage, "01  /  The chest", "Choose your approach", 24, 288);
        Image chest = new Image(new TextureRegion(chestTexture, 270, 0, 115, 108));
        chest.setScaling(Scaling.none);
        chest.setBounds(104, 488, 115, 108);
        chest.setTouchable(Touchable.disabled);
        stage.addActor(chest);
        chestStatus = ui.label(stage, "Unopened chest", "emphasis", GOLD, 36, 450, 264, 24);
        action.setBounds(36, 280, 264, (float) Math.ceil(action.getPrefHeight()));
        stage.addActor(action);
        ui.label(stage, "SUSPECTED TRAP", "default", GOLD, 36, 244, 264, 20);
        trapSelection.setBounds(36, 204, 264, 34);
        stage.addActor(trapSelection);
        actionHint = ui.description(stage, 36, 108, 264, 82);
        ui.panel(stage, "02  /  Loot journal", "Findings, traps, and recovered treasure", 328, 328);
        logs.setBounds(340, 108, 304, 516);
        logs.setFadeScrollBars(false);
        stage.addActor(logs);
        ui.panel(stage, "03  /  The company", "Choose who will act", 672, 328);
        playerSelection.setBounds(684, 428, 304, 196);
        stage.addActor(playerSelection);
        portrait = new Image();
        portrait.setScaling(Scaling.fit);
        portrait.setBounds(684, 328, 76, 76);
        portrait.setTouchable(Touchable.disabled);
        stage.addActor(portrait);
        pselLabel.setBounds(772, 372, 216, 24);
        pselLabel.setEllipsis(true);
        stage.addActor(pselLabel);
        memberDetails = ui.description(stage, 684, 108, 304, 204);
        go.setBounds(24, 24, 172, 38);
        ui.primary(go);
        stage.addActor(go);
        leaveButton = new TextButton("Leave treasure", ui.skin);
        leaveButton.setBounds(850, 24, 150, 38);
        leaveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                leave();
            }
        });
        stage.addActor(leaveButton);
        log("A chest lies before the company. Choose your approach carefully.");
    }

    private void refreshUI() {
        CharacterRecord player = playerSelection.getSelected();
        String choice = action.getSelected();
        chestStatus.setText(chestOpened ? "Treasure recovered" : chestDisarmed ? "Chest secured" : "Unopened chest");
        leaveButton.setText(chestOpened ? "Continue journey" : "Leave treasure");
        action.getSelection().setDisabled(chestOpened);
        trapSelection.setDisabled(chestOpened || !"DISARM".equals(choice));
        go.setDisabled(chestOpened || player == null || player.isDisabled()
                || "INSPECT".equals(choice) && whoTried.contains(player)
                || "CALFO".equals(choice) && !player.canCast(Spells.CALFO)
                || "DISARM".equals(choice) && chestDisarmed);
        go.setText(switch (choice) {
            case "INSPECT" ->
                "Inspect chest";
            case "CALFO" ->
                "Cast Calfo";
            case "DISARM" ->
                "Disarm trap";
            default ->
                "Open chest";
        });
        actionHint.setText(chestOpened ? "The spoils have been distributed among the able members of your company."
                : switch (choice) {
            case "INSPECT" ->
                "Look for a trap. Each adventurer may inspect the chest once.";
            case "CALFO" ->
                "Spend a Calfo spell use to secure the chest with magic.";
            case "DISARM" ->
                "Select the suspected trap, then attempt to disarm it. A mistake may trigger the trap.";
            default ->
                "Open the chest and collect its contents. An active trap will be triggered.";
        });
        if (player == null) {
            return;
        }
        portrait.setDrawable(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                andius.objects.Portraits.get(player)));
        pselLabel.setText(player.name);
        memberDetails.setText("Level " + player.level + " " + player.race + " " + player.classType
                + "\nHealth  " + player.hp + " / " + player.maxhp + "\nCondition  " + (player.isDead() ? "DEAD" : player.status)
                + "\nDisarm chance  " + (player.classType == ClassType.THIEF || player.classType == ClassType.NINJA ? 75 : 10) + "%"
                + "\nGold carried  " + player.gold + "\n\nCalfo  /  " + (player.canCast(Spells.CALFO) ? "Available" : "Unavailable"));
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

        if (!this.chestDisarmed) {
            releaseTrapAffect(player);
        }

        java.util.List<CharacterRecord> lastMenStanding = new ArrayList<>();
        for (CharacterRecord c : this.context.players()) {
            if (!c.isDisabled()) {
                lastMenStanding.add(c);
            }
        }

        int goldAmt = this.reward.goldAmount();
        for (CharacterRecord c : lastMenStanding) {
            c.adjustGold(goldAmt / lastMenStanding.size());
            log(String.format("%s found %d gold.", c.name.toUpperCase(), goldAmt / lastMenStanding.size()));
        }

        if (!lastMenStanding.isEmpty()) {
            for (RewardDetails d : reward.getRewardDetails()) {
                if (d.itemReward != null) {
                    int odds = d.odds;
                    int roll = rand.nextInt(101);
                    if (roll <= odds) {
                        CharacterRecord picked = lastMenStanding.get(rand.nextInt(lastMenStanding.size()));
                        int itemId = Utils.getRandomBetween(d.itemReward.min, d.itemReward.min + d.itemReward.range);
                        Item found = this.map.scenario().items().get(itemId).clone();
                        picked.inventory.add(found);
                        log(String.format("%s finds a %s.", picked.name.toUpperCase(), found.genericName));
                    }
                }
            }
            Sounds.play(Sound.POSITIVE_EFFECT);
            this.chestOpened = true;
        }

    }

    private void releaseTrapAffect(CharacterRecord player) {
        switch (this.trap) {
            case NONE -> {
                log("Nothing happened...");
            }
            case TELEPORTER -> {
                log("The party was teleported!"); //TODO
                Sounds.play(Sound.WAVE);
            }
            case ALARM -> {
                log("You triggered a noisy alarm that can attract attention!");
                Sounds.play(Sound.TRIGGER);//TODO combat
            }
            case POISON_NEEDLE -> {
                log("Jabbed by poisoned needle!");
                player.status.set(Status.POISONED, 1);
                Sounds.play(Sound.POISON_EFFECT);
            }
            case GAS_BOMB -> {
                log("A gas bomb explodes in your face!");
                for (CharacterRecord c : this.context.players()) {
                    if (rand.nextInt(21) > c.luck) {
                        c.status.set(Status.POISONED, 1);
                    }
                }
                Sounds.play(Sound.POISON_EFFECT);
            }
            case ANTI_MAGE -> {
                for (CharacterRecord c : this.context.players()) {
                    if (c.classType == ClassType.MAGE || c.classType == ClassType.BISHOP || c.classType == ClassType.SAMURAI) {
                        if (rand.nextInt(21) > c.luck) {
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
            }
            case ANTI_PRIEST -> {
                for (CharacterRecord c : this.context.players()) {
                    if (c.classType == ClassType.PRIEST || c.classType == ClassType.LORD) {
                        if (rand.nextInt(21) > c.luck) {
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
            }
            case BOLT -> {
                log("Bolts come flying out of the wall!");
                int damage = rand.nextInt(8) + 1;
                player.adjustHP(-damage);
                Sounds.play(Sound.CROSSBOW);
            }
        }
        this.trap = TrapType.NONE;
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

        log(String.format("%s inspected..", player.name.toUpperCase()));

        if (rand.nextInt(101) < chance) {
            log(String.format("and finds %s!", this.trap));
        } else if (rand.nextInt(21) > player.agility) {
            releaseTrapAffect(player);
        } else {
            log(String.format("and thinks it is %s.", TrapType.values()[rand.nextInt(TrapType.values().length)].toString()));
        }
    }

    private void calfo(CharacterRecord player) {
        if (player.isDisabled()) {
            Sounds.play(Sound.NEGATIVE_EFFECT);
            log("Cannot cast calfo!");
            return;
        }
        if (!player.canCast(Spells.CALFO)) {
            Sounds.play(Sound.NEGATIVE_EFFECT);
            log("Cannot cast calfo!");
            return;
        }

        log("Calfo disarmed " + this.trap);
        this.trap = TrapType.NONE;
        this.chestDisarmed = true;
        Sounds.play(Sound.TRIGGER);
        player.decrMagicPts(Spells.CALFO);
    }

    private void disarm(CharacterRecord player, TrapType attemptingTrap) {
        if (player.isDisabled()) {
            Sounds.play(Sound.NEGATIVE_EFFECT);
            log("Cannot disarm!");
            return;
        }
        int chance = (player.classType == ClassType.THIEF || player.classType == ClassType.NINJA ? 75 : 10);
        if (attemptingTrap == this.trap) {
            if (rand.nextInt(101) <= chance) {
                log(player.name.toUpperCase() + " disarmed " + this.trap + "!");
                this.chestDisarmed = true;
                this.trap = TrapType.NONE;
                Sounds.play(Sound.TRIGGER);
            } else if (rand.nextInt(101) > player.agility * 5) {
                log(player.name.toUpperCase() + " failed to disarm " + this.trap + "!");
                releaseTrapAffect(player);
            }
        } else {
            log(player.name.toUpperCase() + " disarmed " + attemptingTrap + " but released " + this.trap + " instead!");
            releaseTrapAffect(player);
        }
    }

    private void leave() {
        mainGame.setScreen(this.map.getScreen());
    }

    private void log(String s) {
        this.logs.add(s);
        this.map.getScreen().log(s);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float f) {
        Gdx.gl.glClearColor(INK.r, INK.g, INK.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        refreshUI();
        stage.getViewport().apply();
        stage.act(Math.min(f, .1f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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
        stage.dispose();
        chestTexture.dispose();
        ui.dispose();
    }

}
