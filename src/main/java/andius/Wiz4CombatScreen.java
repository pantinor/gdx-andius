package andius;

import static andius.Andius.CTX;
import static andius.Andius.SCREEN_HEIGHT;
import static andius.Andius.SCREEN_WIDTH;
import static andius.Andius.mainGame;
import static andius.Andius.startScreen;
import andius.WizardryData.MazeCell;
import static andius.WizardryData.WER4_CHARS;
import static andius.WizardryData.WER_ITEMS;
import static andius.WizardryData.nextHawkindQuote;
import andius.objects.Dice;
import andius.objects.DoGooder;
import andius.objects.HealthCursor;
import andius.objects.Item;
import andius.objects.Monster;
import andius.objects.Mutable;
import andius.objects.MutableCharacter;
import andius.objects.MutableMonster;
import andius.objects.SaveGame;
import andius.objects.SaveGame.CharacterRecord;
import andius.objects.Sound;
import andius.objects.Sounds;
import andius.objects.Spells;
import static andius.objects.Spells.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import utils.AutoFocusScrollPane;
import utils.LogScrollPane;
import utils.Utils;

public class Wiz4CombatScreen implements Screen, Constants {

    public final DoGooder opponent;
    private final Stage stage;
    private final com.badlogic.gdx.scenes.scene2d.ui.Skin combatSkin = utils.SkinUtils.createCombatSkin(Andius.skin);
    private final Texture uiPixel = Utils.fillRectangle(1, 1, Color.WHITE, 1);
    private Label encounterStatus, targetHint;
    private boolean ended;

    private final com.badlogic.gdx.scenes.scene2d.ui.List<SpellLabel> spells;
    private final Table monstersTable;
    private final Table enemiesTable;
    private final AutoFocusScrollPane monstersScroll;
    private final AutoFocusScrollPane enemiesScroll;
    private final AutoFocusScrollPane spellsScroll;

    public final List<MutableCharacter> enemies = new ArrayList<>();
    public final List<MutableMonster> monsters;
    public final CharacterRecord player;

    private final LogScrollPane logs;
    private final TextButton cast;
    private final TextButton fight;
    private final TextButton flee;
    private final TextButton exit;

    private static final Color INK = Color.valueOf("0c141d");
    private static final Color GOLD = Color.valueOf("dbbb7c");
    private static final Color MUTED = Color.valueOf("92a7b7");
    private static final int LISTING_WIDTH = 264;
    private static final int ENEMY_WIDTH = 342;
    private static final int ENEMY_HEIGHT = 88;
    private static final int LINE_HEIGHT = 18;
    private static final int LOG_WIDTH = 352;
    private final Image selected = selection();

    private final TextureAtlas iconAtlas;

    private int round = 1;
    private int suprised = 0;
    MazeCell destCell, fromCell;

    private void buildLayout() {
        int enemyBottom = enemyPanelBottom();
        panel(0, 730, SCREEN_WIDTH, 38);
        panel(8, 628, 282, 96);
        panel(8, 44, 282, 576);
        panel(298, enemyBottom, 718, 724 - enemyBottom);
        panel(298, 44, 342, enemyBottom - 52);
        panel(646, 44, 370, enemyBottom - 52);
        panel(8, 4, 1008, 36);
        caption("WIZARDRY IV / " + opponent.name.toUpperCase(), 18, 738, 704, 22, "default-16", GOLD);
        encounterStatus = caption("", 740, 738, 276, 22, "default-12", GOLD);
        caption("WERDNA", 20, 698, 262, 20, "default-16", GOLD);
        caption("SUMMONED ALLIES", 20, 594, 262, 20, "default-16", GOLD);
        String title = opponent.slogan.isEmpty() ? "ENEMY PARTY" : opponent.slogan.split("[|]")[0];
        caption(title, 310, 698, 694, 20, "default-16", GOLD);
        caption("SPELLS / ITEMS", 310, enemyBottom - 34, 318, 20, "default-16", GOLD);
        caption("BATTLE JOURNAL", 658, enemyBottom - 34, 346, 20, "default-16", GOLD);
        targetHint = caption("", 20, 10, 628, 24, "default-12", MUTED);
        styleButton(fight, false);
        styleButton(cast, true);
        styleButton(flee, false);
        styleButton(exit, true);
        spells.getSelection().clear();
    }

    private int enemyPanelBottom() {
        int rows = Math.min(4, Math.max(2, (enemies.size() + 1) / 2));
        return 724 - (rows * (ENEMY_HEIGHT + 4) + 40);
    }

    private boolean targetsPlayer(Spells spell) {
        return spell == DIOS || spell == DIAL || spell == DIALMA || spell == MADI
                || spell == MOGREF || spell == SOPIC || spell == PORFIC;
    }

    private void panel(float x, float y, float width, float height) {
        Image image = new Image(uiPixel);
        image.setColor(INK);
        image.setBounds(x, y, width, height);
        image.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        stage.addActor(image);
        image.toBack();
        Image rule = new Image(uiPixel);
        rule.setColor(GOLD.r, GOLD.g, GOLD.b, .4f);
        rule.setBounds(x, y + height - 1, width, 1);
        rule.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        stage.addActor(rule);
    }

    private Label caption(String text, float x, float y, float width, float height, String style, Color color) {
        Label label = new Label(text, combatSkin, style);
        label.setBounds(x, y, width, height);
        label.setEllipsis(true);
        label.setColor(color);
        label.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        stage.addActor(label);
        return label;
    }

    private void styleButton(TextButton button, boolean primary) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(button.getStyle());
        var pixel = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new TextureRegion(uiPixel));
        style.up = pixel.tint(Color.valueOf(primary ? "35635e" : "1c2d3c"));
        style.over = pixel.tint(Color.valueOf(primary ? "477e72" : "30495b"));
        style.down = pixel.tint(Color.valueOf("597b79"));
        style.disabled = pixel.tint(INK);
        style.fontColor = primary ? Color.WHITE : GOLD;
        style.overFontColor = style.downFontColor = style.checkedFontColor = style.fontColor;
        style.disabledFontColor = MUTED;
        button.setStyle(style);
    }

    private Image selection() {
        Image image = new Image(uiPixel) {
            @Override public void draw(Batch batch, float parentAlpha) {
                Color previous = new Color(batch.getColor());
                batch.setColor(GOLD.r, GOLD.g, GOLD.b, parentAlpha);
                batch.draw(uiPixel, getX(), getY(), ENEMY_WIDTH, 1);
                batch.draw(uiPixel, getX(), getY() + ENEMY_HEIGHT - 1, ENEMY_WIDTH, 1);
                batch.draw(uiPixel, getX(), getY(), 2, ENEMY_HEIGHT);
                batch.draw(uiPixel, getX() + ENEMY_WIDTH - 2, getY(), 2, ENEMY_HEIGHT);
                batch.setColor(previous);
            }
        };
        image.setSize(ENEMY_WIDTH, ENEMY_HEIGHT);
        image.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        return image;
    }

    private void updateStatus() {
        long alive = enemies.stream().filter(e -> !e.isDead()).count();
        encounterStatus.setText(ended ? player.isDead() ? "DEFEATED" : alive == 0 ? "VICTORY" : "WITHDRAWN"
                : "TURN " + round + " / " + alive + " ENEMIES");
        cast.setDisabled(ended || spells.getSelected() == null);
        String target = selected.getParent() instanceof DoGooderListing listing && !listing.mm.isDead()
                ? listing.mm.name() : "auto (weakest enemy)";
        SpellLabel spell = spells.getSelected();
        String ability = "Choose a spell or fight.";
        if (spell != null) {
            Spells selectedSpell = spell.spell != null ? spell.spell : spell.item.spell;
            if (targetsPlayer(selectedSpell)) target = player.name;
            if (spell.item != null) ability = spell.item.name + " / " + selectedSpell;
            else {
                int remaining = selectedSpell.getType() == andius.objects.ClassType.MAGE
                        ? player.magePoints[selectedSpell.getLevel() - 1] : player.clericPoints[selectedSpell.getLevel() - 1];
                ability = selectedSpell + " / " + selectedSpell.getType() + " " + selectedSpell.getLevel()
                        + " / " + remaining + " uses";
            }
        }
        targetHint.setText(ended ? "Encounter complete. Continue when ready."
                : "Target: " + target + " / " + ability);
    }

    public Wiz4CombatScreen(CharacterRecord player, List<MutableMonster> monsters, DoGooder opponent, MazeCell destCell, MazeCell fromCell, String initialMessage) {

        this.opponent = opponent;
        this.player = player;
        this.monsters = monsters;
        this.destCell = destCell;
        this.fromCell = fromCell;

        this.iconAtlas = new TextureAtlas(Gdx.files.classpath("assets/json/werdna_portraits.atlas"));

        Table logTable = new Table(combatSkin);
        logTable.setBackground("dimmer");
        this.logs = new LogScrollPane(combatSkin, logTable, LOG_WIDTH, "default-12");

        this.stage = new Stage(new com.badlogic.gdx.utils.viewport.FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT));
        //this.stage.setDebugAll(true);

        this.monstersTable = new Table(combatSkin);
        this.monstersTable.top().left();
        this.monstersScroll = new AutoFocusScrollPane(this.monstersTable, combatSkin);
        this.monstersScroll.setScrollingDisabled(true, false);

        this.enemiesTable = new Table(combatSkin);
        this.enemiesTable.top().left();
        this.enemiesScroll = new AutoFocusScrollPane(enemiesTable, combatSkin);
        this.enemiesScroll.setScrollingDisabled(true, false);

        this.enemiesScroll.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                com.badlogic.gdx.math.Vector2 p = enemiesTable.stageToLocalCoordinates(
                        new com.badlogic.gdx.math.Vector2(event.getStageX(), event.getStageY())
                );

                Actor hit = enemiesTable.hit(p.x, p.y, true);

                while (hit != null) {
                    if (hit instanceof DoGooderListing) {
                        return;
                    }
                    hit = hit.getParent();
                }

                selected.remove();
            }
        });

        PlayerListing playerCard = new PlayerListing(player);
        playerCard.setBounds(16, 638, LISTING_WIDTH, LINE_HEIGHT * 3);
        this.stage.addActor(playerCard);

        for (MutableMonster mm : monsters) {
            this.monstersTable.add(new MonsterListing(mm)).pad(2);
            this.monstersTable.row();
        }

        for (int id : opponent.partyMembers) {
            DoGooder dg = WER4_CHARS.get(id);
            MutableCharacter mutc = new MutableCharacter(dg);
            this.enemies.add(mutc);
            this.enemiesTable.add(new DoGooderListing(mutc)).width(ENEMY_WIDTH).pad(2);
            if (this.enemiesTable.getCells().size % 2 == 0) this.enemiesTable.row();
        }

        this.spells = new com.badlogic.gdx.scenes.scene2d.ui.List(combatSkin, "default-12-padded-clear");
        this.spellsScroll = new AutoFocusScrollPane(this.spells, combatSkin);
        this.spellsScroll.setScrollingDisabled(true, false);

        for (Spells s : player.knownSpells) {
            addSpell(s);
        }
        if (player.weapon != null && player.weapon.spell != null) {
            addSpell(player.weapon);
        }
        if (player.helm != null && player.helm.spell != null) {
            addSpell(player.helm);
        }
        if (player.armor != null && player.armor.spell != null) {
            addSpell(player.armor);
        }
        if (player.item1 != null && player.item1.spell != null) {
            addSpell(player.item1);
        }
        if (player.item2 != null && player.item2.spell != null) {
            addSpell(player.item2);
        }
        for (Item i : player.inventory) {
            if (i.spell != null) {
                addSpell(i);
            }
        }

        this.fight = new TextButton("Fight", combatSkin, "default-16");
        this.fight.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                fight();
            }
        });
        this.fight.setBounds(660, 8, 108, 28);

        this.cast = new TextButton("Cast", combatSkin, "default-16");
        this.cast.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                cast();
            }
        });
        this.cast.setBounds(776, 8, 108, 28);

        this.flee = new TextButton("Retreat", combatSkin, "default-16");
        this.flee.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                end(true);
            }
        });
        this.flee.setBounds(892, 8, 116, 28);

        this.exit = new TextButton("Continue", combatSkin, "default-16");
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (player.isDead()) {
                    Map.releaseAllMaps();
                    mainGame.setScreen(startScreen);
                } else {
                    if (pickEnemy() == null) {
                        Map.WIZARDRY4.getScreen().endCombat(true, opponent);
                        mainGame.setScreen(new Wiz4RewardScreen(player, opponent));
                    } else {
                        mainGame.setScreen(Map.WIZARDRY4.getScreen());
                    }
                }
            }
        });
        this.exit.setBounds(776, 8, 108, 28);

        this.monstersScroll.setBounds(16, 52, 272, 544);
        int enemyBottom = enemyPanelBottom();
        this.spellsScroll.setBounds(308, 52, 328, enemyBottom - 88);
        this.logs.setBounds(652, 52, LOG_WIDTH, enemyBottom - 88);
        this.enemiesScroll.setBounds(306, enemyBottom + 8, 704, 688 - enemyBottom);
        this.stage.addActor(this.fight);
        this.stage.addActor(this.flee);
        this.stage.addActor(this.cast);
        this.stage.addActor(this.logs);

        this.stage.addActor(this.monstersScroll);
        this.stage.addActor(this.enemiesScroll);
        this.stage.addActor(this.spellsScroll);

        buildLayout();

        String disp = !this.opponent.slogan.isEmpty() ? this.opponent.slogan.replace("|", "  ") : this.opponent.name;

        if (Utils.percentChance(20)) {
            this.suprised = 1;
            log("You suprised " + disp, Color.YELLOW);
        } else if (Utils.percentChance(20)) {
            this.suprised = 2;
            log("You were suprised by " + disp, Color.YELLOW);
        } else {
            this.suprised = 0;
            log("You encounter " + disp, Color.YELLOW);
        }

        if (initialMessage != null) {
            log(initialMessage, Color.RED);
        }

    }

    private void cast() {
        if (ended || spells.getSelected() == null) return;

        List<Object> shuffled = new ArrayList();

        if (this.suprised == 1) {
            shuffled.add(player);
            shuffled.addAll(this.monsters);
            shuffled.addAll(this.enemies);
        } else if (this.suprised == 2) {
            shuffled.addAll(this.enemies);
            shuffled.addAll(this.monsters);
            shuffled.add(player);
        } else {
            shuffled.add(player);
            shuffled.addAll(this.monsters);
            shuffled.addAll(this.enemies);
            Collections.shuffle(shuffled);
        }

        this.suprised = 0;

        for (Object m : shuffled) {
            if (m instanceof CharacterRecord) {
                SpellLabel sp = this.spells.getSelected();
                if (sp != null) {
                    Spells spell = sp.spell != null ? sp.spell : sp.item.spell;

                    Object target;
                    if (targetsPlayer(spell)) {
                        target = player;
                    } else {
                        target = pickEnemy();
                    }

                    if (target != null) {
                        spellCast(spell, player, target, sp.item != null);
                    }

                    if (sp.item != null && sp.item.changeChance > 0) {
                        boolean decayed = Utils.percentChance(sp.item.changeChance);
                        if (decayed) {
                            Item changeTo = WER_ITEMS.get(sp.item.changeTo);
                            player.removeItem(sp.item.id, sp.item.scenarioID);
                            if (changeTo.id != 0) {
                                player.inventory.add(changeTo);
                            }
                            this.spells.getItems().removeValue(sp, false);
                            this.spells.getSelection().clear();
                        }
                    }
                }
            } else if (m instanceof MutableMonster) {
                monsterFight((MutableMonster) m);
            } else {
                enemyFight((MutableCharacter) m);
            }
        }

        CTX.endTurn();

        boolean alive = false;
        for (MutableCharacter c : this.enemies) {
            if (!c.isDead()) {
                alive = true;
            }
        }

        if (!alive) {
            end(false);
        } else if (player.isDead()) {
            end(false);
        }

        log("------------ end of round " + round, Color.YELLOW);
        round++;
    }

    private void fight() {
        if (ended) return;

        List<Object> shuffled = new ArrayList();

        if (this.suprised == 1) {
            shuffled.add(player);
            shuffled.addAll(this.monsters);
            shuffled.addAll(this.enemies);
        } else if (this.suprised == 2) {
            shuffled.addAll(this.enemies);
            shuffled.addAll(this.monsters);
            shuffled.add(player);
        } else {
            shuffled.add(player);
            shuffled.addAll(this.monsters);
            shuffled.addAll(this.enemies);
            Collections.shuffle(shuffled);
        }

        this.suprised = 0;

        for (Object m : shuffled) {
            if (m instanceof CharacterRecord) {
                MutableCharacter defender = pickEnemy();
                if (defender != null) {
                    if (!player.isDisabled()) {
                        boolean hit = Utils.attackHit(player, defender);
                        if (hit && !defender.name().equals("Hawkwind")) {
                            Item weapon = player.weapon == null ? Item.HANDS : player.weapon;
                            int damage = Utils.dealDamage(player, weapon, defender);
                            log(String.format("%s %s %s [%d] %s",
                                    player.name.toUpperCase(),
                                    "hit",
                                    defender.name().toUpperCase(),
                                    damage, weapon.name), Color.SCARLET);
                        } else {
                            log(String.format("%s misses %s", player.name.toUpperCase(), defender.name()), Color.WHITE);
                        }
                    }
                }
            } else if (m instanceof MutableMonster) {
                monsterFight((MutableMonster) m);
            } else {
                enemyFight((MutableCharacter) m);
            }
        }

        CTX.endTurn();

        boolean alive = false;
        for (MutableCharacter c : this.enemies) {
            if (!c.isDead()) {
                alive = true;
            }
        }

        if (!alive) {
            end(false);
        } else if (player.isDead()) {
            end(false);
        }

        log("------------ end of round " + round, Color.YELLOW);
        round++;

    }

    public void end(boolean fled) {
        ended = true;
        player.acmodifier1 = 0;

        this.fight.remove();
        this.cast.remove();
        this.flee.remove();

        if (fled) {
            WizardryDungeonScreen sc = (WizardryDungeonScreen) Map.WIZARDRY4.getScreen();
            sc.teleport(this.fromCell.address, false);
        }

        this.stage.addActor(this.exit);
    }

    private void monsterFight(MutableMonster attacker) {

        if (attacker.isDead()) {
            return;
        }

        attacker.processStatusAffects();

        if (attacker.status().isDisabled()) {
            log(attacker.name().toUpperCase() + " is " + attacker.status().toLongString(), attacker.status().color());
            return;
        }

        CombatAction action = CombatAction.ATTACK;
        Spells spell = null;

        boolean silenced = attacker.status().has(Status.SILENCED);
        if (!silenced) {
            int mlvl = attacker.getCurrentMageSpellLevel();
            if (mlvl > 0 && Utils.RANDOM.nextInt(100) < 75) {
                action = CombatAction.CAST;
                spell = attacker.castMageSpell();
            }
        }

        if (action == CombatAction.ATTACK && !silenced) {
            int plvl = attacker.getCurrentPriestSpellLevel();
            if (plvl > 0 && Utils.RANDOM.nextInt(100) < 75) {
                action = CombatAction.CAST;
                spell = attacker.castPriestSpell();
            }
        }

        if (action == CombatAction.ATTACK && !silenced && attacker.breath() != Breath.NONE && Utils.RANDOM.nextInt(100) < 60) {
            action = CombatAction.BREATH;
        }

        if (action == CombatAction.ATTACK && !silenced
                && attacker.monster().ability.contains(Ability.CALLFORHELP)
                && Utils.shouldCallForHelp(attacker, this.monsters)) {
            action = CombatAction.CALL_FOR_HELP;
        } else if (action == CombatAction.ATTACK
                && attacker.monster().ability.contains(Ability.RUN)
                && Utils.shouldFlee(attacker)) {
            action = CombatAction.FLEE;
        }

        switch (action) {
            case BREATH:
                log(String.format("%s breathes %s", attacker.name(), attacker.breath()), Color.YELLOW);
                int baseBreathDamage = attacker.getCurrentHitPoints();
                for (MutableCharacter defender : this.enemies) {
                    if (defender.status().isDisabled()) {
                        continue;
                    }
                    int dmg = baseBreathDamage;
                    boolean saved = defender.savingThrowBreath();
                    boolean unaffected = defender.isUnaffectedByBreath(attacker.breath(), attacker.getMonsterType());
                    if (saved || unaffected) {
                        dmg /= 2;
                    }
                    if (dmg > 0) {
                        damage(attacker, defender, dmg, attacker.breath().toString());
                    }
                }
                break;

            case ATTACK:
                MutableCharacter defender = pickEnemy();
                if (defender != null) {
                    boolean hit = Utils.attackHit(attacker, defender);
                    if (hit) {
                        for (Dice dice : attacker.getDamage()) {
                            int dmg = dice.roll();
                            damage(attacker, defender, dmg, Utils.getAttackName(attacker));
                        }
                        if (!defender.isDead()) {
                            Utils.applyAttackSpecialEffects(attacker, defender, logs);
                        }
                    } else {
                        log(String.format("%s misses %s", attacker.name(), defender.name()));
                    }
                }
                break;

            case CAST: {
                Object target = null;

                if (spell == Spells.DIOS || spell == Spells.DIAL || spell == Spells.DIALMA
                        || spell == Spells.MADI || spell == Spells.MOGREF || spell == Spells.SOPIC || spell == Spells.PORFIC) {
                    if (spell == Spells.DIOS || spell == Spells.DIAL || spell == Spells.DIALMA || spell == Spells.MADI) {
                        MutableMonster weakestAlly = null;
                        for (MutableMonster mm : this.monsters) {
                            if (!mm.isDead()) {
                                if (weakestAlly == null || mm.getCurrentHitPoints() < weakestAlly.getCurrentHitPoints()) {
                                    weakestAlly = mm;
                                }
                            }
                        }
                        if (weakestAlly != null && weakestAlly.getCurrentHitPoints() < weakestAlly.getMaxHitPoints() / 2) {
                            target = weakestAlly;
                        } else if (!player.isDead()) {
                            target = player;
                        }
                        Sounds.play(Sound.HEALING);
                    } else {
                        target = attacker;
                    }

                } else {
                    target = pickEnemy();
                }

                if (target != null) {
                    spellCast(spell, attacker, target, false);
                }
                break;
            }

            case FLEE:
                log(String.format("%s fled!", attacker.name()), Color.SKY);
                this.monsters.remove(attacker);
                removeMonsterFromTable(attacker);
                Sounds.play(Sound.FLEE);
                break;

            case CALL_FOR_HELP:
                log(String.format("%s called for help!", attacker.name()), Color.RED);
                MutableMonster clone = new MutableMonster(attacker.monster());
                this.monsters.add(clone);
                this.monstersTable.add(new MonsterListing(clone)).pad(2);
                this.monstersTable.row();
                Sounds.play(Sound.GIGGLE);
                break;
        }
    }

    private void enemyFight(MutableCharacter attacker) {

        if (attacker.isDead()) {
            return;
        }

        attacker.processStatusAffects();

        if (attacker.status().isDisabled()) {
            log(attacker.name().toUpperCase() + " is " + attacker.status().toLongString(), attacker.status().color());
            return;
        }

        CombatAction action = CombatAction.ATTACK;
        Spells spell = null;

        if (!attacker.status().has(Status.SILENCED) && Utils.RANDOM.nextInt(100) < 80) {
            int plvl = attacker.getCurrentPriestSpellLevel();
            int mlvl = attacker.getCurrentMageSpellLevel();
            if (mlvl > 0 && mlvl > plvl) {
                action = CombatAction.CAST;
                spell = attacker.castMageSpell();
            } else if (plvl > 0 && plvl > mlvl) {
                action = CombatAction.CAST;
                spell = attacker.castPriestSpell();
            } else if (plvl > 0 && mlvl > 0 && plvl == mlvl) {
                action = CombatAction.CAST;
                spell = Utils.randomBoolean() ? attacker.castMageSpell() : attacker.castPriestSpell();
            }
        }

        DoGooder dg = (DoGooder) attacker.baseType();

        switch (action) {
            case ATTACK:
                boolean criticalHit = dg.crithitm && Utils.percentChance(Math.min(dg.charlev * 2, 50));
                for (int swing = 0; swing < dg.swingCount; swing++) {
                    Object def = pickMonster();
                    if (def != null) {
                        if (def instanceof MutableMonster defender) {
                            boolean hit = Utils.attackHit(attacker, defender);
                            if (hit) {
                                for (Dice dice : attacker.getDamage()) {
                                    int dmg;
                                    if (criticalHit) {
                                        dmg = defender.getMaxHitPoints();
                                        criticalHit = false;
                                        log(String.format("%s decapitated %s!", attacker.name(), defender.name()), Color.RED);
                                        Sounds.play(Sound.PC_STRUCK);
                                        damage(attacker, defender, dmg, Utils.getAttackName(attacker));
                                    } else {
                                        dmg = Utils.dealDamage(attacker, defender, dice);
                                        log(String.format("%s %s %s [%d] %s",
                                                attacker.name().toUpperCase(),
                                                "hit",
                                                defender.name().toUpperCase(),
                                                dmg,
                                                Utils.getAttackName(attacker)), Color.SCARLET);
                                    }
                                }
                            } else {
                                log(String.format("%s misses %s", attacker.name(), defender.name()));
                            }
                        } else {
                            boolean hit = Utils.attackHit(attacker, player);
                            if (hit) {
                                for (Dice dice : attacker.getDamage()) {
                                    int dmg = dice.roll();
                                    if (criticalHit) {
                                        dmg = player.maxhp;
                                        criticalHit = false;
                                        log(String.format("%s decapitated %s!", attacker.name(), player.name.toUpperCase()), Color.RED);
                                        Sounds.play(Sound.RAGE);
                                    }
                                    damage(attacker, player, dmg, Utils.getAttackName(attacker));
                                }
                            } else {
                                log(String.format("%s misses %s", attacker.name(), player.name.toUpperCase()));
                            }
                        }
                    }
                }
                break;

            case CAST: {
                Object target = null;

                if (spell == Spells.DIOS || spell == Spells.DIAL || spell == Spells.DIALMA || spell == Spells.MADI
                        || spell == Spells.MOGREF || spell == Spells.SOPIC || spell == Spells.PORFIC) {

                    if (spell == Spells.DIOS || spell == Spells.DIAL || spell == Spells.DIALMA || spell == Spells.MADI) {
                        MutableCharacter weakestAlly = null;
                        for (MutableCharacter mc : this.enemies) {
                            if (!mc.isDead()) {
                                if (weakestAlly == null || mc.getCurrentHitPoints() < weakestAlly.getCurrentHitPoints()) {
                                    weakestAlly = mc;
                                }
                            }
                        }
                        target = weakestAlly;
                    } else {
                        target = attacker;
                    }

                } else {
                    target = pickMonster();
                }

                if (target != null) {
                    spellCast(spell, attacker, target, false);
                }
                break;
            }
        }
    }

    public MutableCharacter pickEnemy() {
        Actor parent = selected.getParent();
        if (parent instanceof DoGooderListing dgl && !dgl.mm.isDead()) {
            return dgl.mm;
        }

        MutableCharacter weakest = null;
        for (MutableCharacter c : enemies) {
            if (c.getCurrentHitPoints() > 0) {
                if (weakest == null || c.getCurrentHitPoints() < weakest.getCurrentHitPoints()) {
                    weakest = c;
                }
            }
        }
        return weakest;
    }

    private Object pickMonster() {

        List<Object> shuffled = new ArrayList();
        for (MutableMonster m : monsters) {
            if (m.getCurrentHitPoints() > 0) {
                shuffled.add(m);
            }
        }
        shuffled.add(player);
        Collections.shuffle(shuffled);

        if (shuffled.isEmpty()) {
            return null;
        }

        return shuffled.get(0);
    }

    private void damage(Object attacker, Object defender, int damage, String kindOfAttack) {

        if (damage <= 0) {
            return;
        }

        String attName = (attacker instanceof Mutable a) ? a.name() : player.name.toUpperCase();

        if (defender instanceof Mutable m) {
            if (!m.isDead()) {
                if (m.name().equals("Hawkwind")) {
                    if (attacker instanceof Mutable a) {
                        if (a.name().equals("Dink")) {
                            damage = Utils.getRandomBetween(300, 500);
                            m.adjustHitPoints(-damage);
                            m.getHealthCursor().adjust(m.getCurrentHitPoints(), m.getMaxHitPoints());
                            log(String.format("%s %s %s [%d] %s",
                                    attName.toUpperCase(),
                                    "hit",
                                    m.name().toUpperCase(),
                                    damage, kindOfAttack), Color.SCARLET);
                            if (m.isDead()) {
                                log("As mighty Achilles fell to Paris arrow, so does Lord Hawkwind fall to the stab of a lowly Dink!  Long will Skara Brae be mourning his passing!", Color.WHITE);
                            }
                        } else {
                            log("Lord Hawkwind - " + nextHawkindQuote(), Color.SKY);
                        }
                    } else {
                        log("Lord Hawkwind - " + nextHawkindQuote(), Color.SKY);
                    }
                } else {
                    m.adjustHitPoints(-damage);
                    m.getHealthCursor().adjust(m.getCurrentHitPoints(), m.getMaxHitPoints());
                    log(String.format("%s %s %s [%d] %s",
                            attName.toUpperCase(),
                            "hit",
                            m.name().toUpperCase(),
                            damage, kindOfAttack), attacker instanceof MutableMonster ? Color.CORAL : Color.SCARLET);

                    if (m.isDead() && selected.getParent() instanceof DoGooderListing dgl && dgl.mm == m) {
                        selected.remove();
                    }
                }
            }
        } else {
            if (!player.isDead()) {
                player.adjustHP(-damage);
                log(String.format("%s hit %s [%d] %s", attName, player.name.toUpperCase(), damage, kindOfAttack), Color.RED);
            }
        }
    }

    private void spellCast(Spells spell, Object caster, Object target, boolean item) {

        if (caster instanceof CharacterRecord) {
            if (player.isDisabled()) {
                log(player.name.toUpperCase() + " cannot cast spell in current state!");
                return;
            }

            if (!item) {
                if (!player.canCast(spell)) {
                    log(player.name.toUpperCase() + " does not have enough magic points!");
                    return;
                }
                player.decrMagicPts(spell);
            }

            log(player.name.toUpperCase() + " casts " + spell, Color.SKY);

        } else {
            Mutable m = (Mutable) caster;
            if (m.status().isDisabled()) {
                log(m.name() + " cannot cast spell in current state!");
                return;
            }
            log(m.name() + " casts " + spell, Color.SKY);

            if (!(m instanceof MutableCharacter mc && mc.isPendingSpellFromItem(spell))) {
                m.decrementSpellPoints(spell);
            }
        }

        switch (spell) {
            case MAKANITO:
            case LAKANITO:
            case LITOKAN:
            case LORTO:
            case MALIKTO:
            case MAHALITO:
            case MOLITO:
            case DALTO:
            case LAHALITO:
            case HAMAN:
            case MAHAMAN:
            case TILTOWAIT:
            case MADALTO:
                spellGroupDamage(caster, spell);
                break;
            case HALITO:
            case BADIAL:
            case BADIALMA:
            case ZILWAN:
            case BADIOS:
            case BADI:
            case DIOS:
            case DIAL:
            case DIALMA:
            case MADI:
                spellDamage(caster, spell, target);
                break;
            case MABADI:
                spellGroupMabadi(caster, spell);
                break;
            case KATINO:
                spellGroupAffect(caster, spell, Status.ASLEEP);
                break;
            case MANIFO:
                spellGroupAffect(caster, spell, Status.PARALYZED);
                break;
            case MONTINO:
                spellGroupAffect(caster, spell, Status.SILENCED);
                break;
            case DILTO:
            case MORLIS:
            case MAMORLIS:
                spellGroupAffect(caster, spell, Status.AFRAID);
                break;
            case DIALKO:
            case LATUMAPIC:
                if (caster instanceof MutableMonster || caster instanceof CharacterRecord) {
                    player.status.set(Status.PARALYZED, 0);
                    player.status.set(Status.ASLEEP, 0);
                    player.status.set(Status.SILENCED, 0);
                    player.status.set(Status.AFRAID, 0);

                    for (MutableMonster m : this.monsters) {
                        m.status().set(Status.PARALYZED, 0);
                        m.status().set(Status.ASLEEP, 0);
                        m.status().set(Status.SILENCED, 0);
                        m.status().set(Status.AFRAID, 0);
                    }
                } else {
                    for (MutableCharacter mc : this.enemies) {
                        mc.status().set(Status.PARALYZED, 0);
                        mc.status().set(Status.ASLEEP, 0);
                        mc.status().set(Status.SILENCED, 0);
                        mc.status().set(Status.AFRAID, 0);
                    }
                }
                break;
            case MOGREF:
            case SOPIC:
            case PORFIC:
                if (caster instanceof CharacterRecord) {
                    player.acmodifier1 = Math.max(player.acmodifier1, spell.getHitBonus());
                } else {
                    Mutable m = (Mutable) caster;
                    m.setACModifier(Math.max(m.getACModifier(), spell.getHitBonus()));
                }
                break;
            case KALKI:
            case MATU:
            case BAMATU:
            case MASOPIC:
            case MAPORFIC:
                spellGroupACModify(caster, spell.getHitBonus());
                break;
        }

        if (caster instanceof MutableCharacter mc) {
            mc.resolvePendingSpellItemDecay();
        }
    }

    private void spellDamage(Object caster, Spells spell, Object target) {
        if (target == null) {
            return;
        }

        // Friendly side: player + allied monsters attacking/healing DoGooders or allies
        if (caster instanceof MutableMonster || caster instanceof CharacterRecord) {

            // Healing spells target allies, not enemies
            if (spell == Spells.DIOS || spell == Spells.DIAL || spell == Spells.DIALMA || spell == Spells.MADI) {
                if (target instanceof CharacterRecord pc) {
                    if (!pc.isDead()) {
                        int heal = spell.damage();
                        pc.adjustHP(heal);
                        log(pc.name.toUpperCase() + " is healed for " + heal + " by " + spell.getName(), Color.GREEN);
                    }
                } else if (target instanceof MutableMonster mm) {
                    if (!mm.isDead()) {
                        int heal = spell.damage();
                        mm.adjustHitPoints(heal);
                        mm.adjustHealthCursor();
                        log(mm.name() + " is healed for " + heal + " by " + spell.getName(), Color.GREEN);
                    }
                } else if (target instanceof MutableCharacter mc) {
                    // only if you intentionally allow friendly-side healing on a DoGooder target
                    if (!mc.isDead()) {
                        int heal = spell.damage();
                        mc.adjustHitPoints(heal);
                        mc.adjustHealthCursor();
                        log(mc.name() + " is healed for " + heal + " by " + spell.getName(), Color.GREEN);
                    }
                }
                return;
            }

            // Damage spells from friendly side should hit enemy DoGooders
            if (target instanceof MutableCharacter mc) {
                if (!mc.status().isDisabled() && mc.savingThrowSpell()) {
                    log(mc.name() + " saves versus " + spell + " unaffected!");
                } else {
                    int dmg = spell.damage();
                    damage(caster, mc, dmg, spell.getName());
                }
            }

            return;
        }

        // Enemy DoGooder side casting on player/allied-monster side
        if (caster instanceof MutableCharacter) {

            // Enemy healing spells can heal other DoGooders or the player if that is ever passed in
            if (spell == Spells.DIOS || spell == Spells.DIAL || spell == Spells.DIALMA || spell == Spells.MADI) {
                if (target instanceof MutableCharacter mc) {
                    if (!mc.isDead()) {
                        int heal = spell.damage();
                        mc.adjustHitPoints(heal);
                        mc.adjustHealthCursor();
                        log(mc.name() + " is healed for " + heal + " by " + spell.getName(), Color.GREEN);
                    }
                } else if (target instanceof CharacterRecord pc) {
                    if (!pc.isDead()) {
                        int heal = spell.damage();
                        pc.adjustHP(heal);
                        log(pc.name.toUpperCase() + " is healed for " + heal + " by " + spell.getName(), Color.GREEN);
                    }
                } else if (target instanceof MutableMonster mm) {
                    if (!mm.isDead()) {
                        int heal = spell.damage();
                        mm.adjustHitPoints(heal);
                        mm.adjustHealthCursor();
                        log(mm.name() + " is healed for " + heal + " by " + spell.getName(), Color.GREEN);
                    }
                }
                return;
            }

            // Damage spells against allied monsters
            if (target instanceof MutableMonster mm) {
                boolean unaffected = mm.isUnaffected(spell, ((MutableCharacter) caster).getMonsterType());
                int dmg = spell.damage();

                if (unaffected) {
                    dmg = dmg / 2;
                }
                if (spell.equals(Spells.ZILWAN) && mm.getMonsterType() != CharacterType.UNDEAD) {
                    dmg = 0;
                }

                damage(caster, mm, dmg, spell.getName());
                return;
            }

            // Damage spells against player
            if (target instanceof CharacterRecord pc) {
                if (pc.savingThrowSpell()) {
                    log(pc.name.toUpperCase() + " saves versus " + spell + " unaffected!");
                } else {
                    int dmg = spell.damage();
                    damage(caster, pc, dmg, spell.getName());
                }
            }
        }
    }

    private void spellGroupDamage(Object caster, Spells spell) {
        int dmg = spell.damage();
        log("for group damage of " + dmg, Color.SKY);

        if (caster instanceof MutableMonster mmCaster) {
            CharacterType casterType = mmCaster.getMonsterType();

            for (MutableCharacter mc : this.enemies) {
                if (mc.status().isDisabled()) {
                    continue;
                }

                boolean unaffected = mc.isUnaffected(spell, casterType);
                boolean saved = mc.savingThrowSpell();

                if (unaffected || saved) {
                    log(mc.name() + " saves versus " + spell + " for half damage!");
                    damage(caster, mc, dmg / 2, spell.getName());
                } else {
                    damage(caster, mc, dmg, spell.getName());
                }
            }

            return;
        }

        if (caster instanceof CharacterRecord crCaster) {
            CharacterType casterType = CharacterType.valueOf(crCaster.classType.toString());

            for (MutableCharacter mc : this.enemies) {
                if (mc.status().isDisabled()) {
                    continue;
                }

                boolean unaffected = mc.isUnaffected(spell, casterType);
                boolean saved = mc.savingThrowSpell();

                if (unaffected || saved) {
                    log(mc.name() + " saves versus " + spell + " for half damage!");
                    damage(caster, mc, dmg / 2, spell.getName());
                } else {
                    damage(caster, mc, dmg, spell.getName());
                }
            }

            return;
        }

        if (caster instanceof MutableCharacter mcCaster) {
            CharacterType casterType = mcCaster.getMonsterType();

            for (MutableMonster mm : this.monsters) {
                if (mm.isDead()) {
                    continue;
                }

                boolean unaffected = mm.isUnaffected(spell, casterType);

                if (unaffected) {
                    log(mm.name() + " saves versus " + spell + " for half damage!");
                    damage(caster, mm, dmg / 2, spell.getName());
                } else {
                    damage(caster, mm, dmg, spell.getName());
                }
            }

            if (player.armor != null
                    && player.armor.id == 89
                    && (spell.equals(Spells.KATINO)
                    || spell.equals(Spells.LAKANITO)
                    || spell.equals(Spells.MAKANITO))) {

                log("Saved by Oxygen Mask!", Color.GREEN);

            } else if (player.savingThrowSpell()) {
                log(player.name.toUpperCase() + " saves versus " + spell + " for half damage!");
                damage(caster, player, dmg / 2, spell.getName());

            } else {
                damage(caster, player, dmg, spell.getName());
            }
        }
    }

    private void spellGroupMabadi(Object caster, Spells spell) {

        if (caster instanceof MutableMonster mmCaster) {
            CharacterType casterType = mmCaster.getMonsterType();

            for (MutableCharacter mc : this.enemies) {
                boolean unaffected = mc.isUnaffected(spell, casterType);

                if (!mc.status().isDisabled() && (mc.savingThrowSpell() || unaffected)) {
                    log(mc.name() + " saves versus " + spell + " unaffected!");
                } else {
                    int pointsLeft = Utils.getRandomBetween(1, 8);
                    mc.adjustHitPoints(pointsLeft - mc.getCurrentHitPoints());
                    mc.getHealthCursor().adjust(mc.getCurrentHitPoints(), mc.getMaxHitPoints());
                }
            }

        } else if (caster instanceof CharacterRecord crCaster) {
            CharacterType casterType = CharacterType.valueOf(crCaster.classType.toString());

            for (MutableCharacter mc : this.enemies) {
                boolean unaffected = mc.isUnaffected(spell, casterType);

                if (!mc.status().isDisabled() && (mc.savingThrowSpell() || unaffected)) {
                    log(mc.name() + " saves versus " + spell + " unaffected!");
                } else {
                    int pointsLeft = Utils.getRandomBetween(1, 8);
                    mc.adjustHitPoints(pointsLeft - mc.getCurrentHitPoints());
                    mc.getHealthCursor().adjust(mc.getCurrentHitPoints(), mc.getMaxHitPoints());
                }
            }

        } else if (caster instanceof MutableCharacter mcCaster) {
            CharacterType casterType = mcCaster.getMonsterType();

            for (MutableMonster mm : this.monsters) {
                boolean unaffected = mm.isUnaffected(spell, casterType);

                if (unaffected) {
                    log(mm.name() + " saves versus " + spell + " unaffected!");
                } else {
                    int pointsLeft = Utils.getRandomBetween(1, 8);
                    mm.adjustHitPoints(pointsLeft - mm.getCurrentHitPoints());
                    mm.getHealthCursor().adjust(mm.getCurrentHitPoints(), mm.getMaxHitPoints());
                }
            }

            if (player.savingThrowSpell()) {
                log(player.name.toUpperCase() + " saves versus " + spell + " unaffected!");
            } else {
                int pointsLeft = Utils.getRandomBetween(1, 8);
                player.hp = pointsLeft;
                player.healthCursor.adjust(player.hp, player.maxhp);
            }
        }
    }

    private void spellGroupAffect(Object caster, Spells spell, Status effect) {
        if (caster instanceof MutableMonster || caster instanceof CharacterRecord) {
            for (MutableCharacter mc : this.enemies) {
                CharacterType ct = caster instanceof MutableMonster
                        ? ((MutableMonster) caster).getMonsterType()
                        : CharacterType.valueOf(player.classType.toString());
                boolean unaffected = mc.isUnaffected(spell, ct);
                if (!mc.status().isDisabled() && (mc.savingThrowSpell() || unaffected)) {
                    log(mc.name() + " saves versus " + spell + " unaffected!");
                } else {
                    mc.status().set(effect, Math.abs(spell.getHitBonus()));
                }
            }
        }
        if (caster instanceof MutableCharacter) {
            for (MutableMonster mm : this.monsters) {
                boolean unaffected = mm.isUnaffected(spell, ((MutableCharacter) caster).getMonsterType());
                if (unaffected) {
                    log(mm.name() + " saves versus " + spell + " unaffected!");
                } else {
                    mm.status().set(effect, Math.abs(spell.getHitBonus()));
                }
            }
            if (player.savingThrowSpell()) {
                log(player.name.toUpperCase() + " saves versus " + spell + " unaffected!");
            } else {
                player.status.set(effect, Math.abs(spell.getHitBonus()));
            }
        }
    }

    private void spellGroupACModify(Object caster, int modifier) {
        if (caster instanceof MutableMonster || caster instanceof CharacterRecord) {
            for (MutableMonster mm : this.monsters) {
                mm.setACModifier(Math.max(mm.getACModifier(), modifier));
            }
            player.acmodifier1 = Math.max(player.acmodifier1, modifier);
        }
        if (caster instanceof MutableCharacter) {
            for (MutableCharacter mm : this.enemies) {
                mm.setACModifier(Math.max(mm.getACModifier(), modifier));
            }
        }
    }

    private class SpellLabel extends Label {

        final Spells spell;
        final Item item;

        public SpellLabel(Spells spell) {
            super(spell.toString(), combatSkin, "default-12");
            this.item = null;
            this.spell = spell;
        }

        public SpellLabel(Item it) {
            super(it.name, combatSkin, "default-12");
            this.item = it;
            this.spell = null;
        }

        @Override
        public String toString() {
            if (this.spell != null) {
                return this.spell.label() + "    " + this.spell.getName();
            }
            return this.item.name + " - " + this.item.spell + "  " + this.item.spell.getName();
        }

    }

    private void addSpell(Spells s) {
        if (s.getArea() == SpellArea.COMBAT || s.getArea() == SpellArea.ANY_TIME) {
            SpellLabel label = new SpellLabel(s);
            spells.getItems().add(label);
        }
    }

    private void addSpell(Item i) {
        if (i.spell.getArea() == SpellArea.COMBAT || i.spell.getArea() == SpellArea.ANY_TIME) {
            SpellLabel label = new SpellLabel(i);
            spells.getItems().add(label);
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(this.stage));
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        combatSkin.dispose();
        uiPixel.dispose();
        iconAtlas.dispose();
    }

    private void log(String s) {
        this.logs.add(s.toUpperCase());
    }

    private void log(String s, Color c) {
        this.logs.add(s.toUpperCase(), c);
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(.025f, .035f, .05f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.getViewport().apply();
        updateStatus();
        stage.act(delta);
        stage.draw();

    }

    private class PlayerStatusLabel extends Label {

        private final CharacterRecord rec;

        public PlayerStatusLabel(CharacterRecord rec) {
            super("", combatSkin, "default-12");
            this.rec = rec;
            setColor(rec.status.color());
            setText(getText());
        }

        @Override
        public com.badlogic.gdx.utils.CharArray getText() {
            return new com.badlogic.gdx.utils.CharArray(
                    String.format("HP: %d /%d  AC: %d  ST: %s", rec.hp, rec.maxhp, rec.calculateAC(), rec.isDead() ? "DEAD" : rec.status));
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            setText(getText());
            super.draw(batch, parentAlpha);
        }

        @Override
        public Color getColor() {
            return rec.isDead() ? Color.SCARLET : rec.status.color();
        }

    }

    private class PlayerMagicPointsLabel extends Label {

        private final CharacterRecord rec;

        public PlayerMagicPointsLabel(CharacterRecord rec) {
            super("", combatSkin, "default-12");
            this.rec = rec;
            setText(getText());
        }

        @Override
        public com.badlogic.gdx.utils.CharArray getText() {
            int[] ms = rec.magePoints;
            int[] cs = rec.clericPoints;
            return new com.badlogic.gdx.utils.CharArray(
                    String.format("M: %d/%d/%d/%d/%d/%d/%d  P: %d/%d/%d/%d/%d/%d/%d",
                            ms[0], ms[1], ms[2], ms[3], ms[4], ms[5], ms[6],
                            cs[0], cs[1], cs[2], cs[3], cs[4], cs[5], cs[6]));
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            setText(getText());
            super.draw(batch, parentAlpha);
        }

    }

    private class PlayerListing extends Group {

        final Label l1;
        final PlayerStatusLabel l2;
        final PlayerMagicPointsLabel l3;
        final ListingBackground bckgrnd;
        final SaveGame.CharacterRecord c;

        PlayerListing(SaveGame.CharacterRecord rec) {
            this.c = rec;
            this.bckgrnd = new ListingBackground();
            rec.healthCursor = this.bckgrnd;
            rec.healthCursor.adjust(rec.hp, rec.maxhp);

            this.l1 = new Label("", combatSkin, "default-12");
            this.l2 = new PlayerStatusLabel(rec);
            this.l3 = new PlayerMagicPointsLabel(rec);

            String d1 = String.format("%s  %s  LVL %d  %s", rec.name.toUpperCase(), rec.race.toString(), rec.level, rec.classType.toString());
            this.l1.setText(d1);

            addActor(this.bckgrnd);
            addActor(this.l1);
            addActor(this.l2);
            addActor(this.l3);

            float x = 3;
            this.l1.setBounds(x, LINE_HEIGHT * 2, LISTING_WIDTH - 6, LINE_HEIGHT);
            for (Label label : new Label[] {l1, l2, l3}) label.setEllipsis(true);
            this.l2.setBounds(x, LINE_HEIGHT * 1, LISTING_WIDTH - 6, LINE_HEIGHT);
            this.l3.setBounds(x, LINE_HEIGHT * 0, LISTING_WIDTH - 6, LINE_HEIGHT);

            this.setSize(LISTING_WIDTH, LINE_HEIGHT * 3f);
        }

    }

    private class MonsterStatusLabel extends Label {

        private final MutableMonster mm;

        public MonsterStatusLabel(MutableMonster mm) {
            super("", combatSkin, "default-12");
            this.mm = mm;
            setColor(mm.status().color());
            setText(getText());
        }

        @Override
        public com.badlogic.gdx.utils.CharArray getText() {
            return new com.badlogic.gdx.utils.CharArray(
                    String.format("HP: %d / %d AC: %d  ST: %s", mm.getCurrentHitPoints(), mm.getMaxHitPoints(), mm.getArmourClass(), mm.getCurrentHitPoints() <= 0 ? "DEAD" : mm.status()));
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            setText(getText());
            super.draw(batch, parentAlpha);
        }

        @Override
        public Color getColor() {
            return mm.getCurrentHitPoints() <= 0 ? Color.SCARLET : mm.status().color();
        }

    }

    private class MonsterMagicPointsLabel extends Label {

        private final MutableMonster mm;

        public MonsterMagicPointsLabel(MutableMonster mm) {
            super("", combatSkin, "default-12");
            this.mm = mm;
            setText(getText());
        }

        @Override
        public com.badlogic.gdx.utils.CharArray getText() {
            return new com.badlogic.gdx.utils.CharArray(String.format("MG: %d  PR: %d ", mm.getCurrentMageSpellLevel(), mm.getCurrentPriestSpellLevel()));
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            setText(getText());
            super.draw(batch, parentAlpha);
        }

    }

    private class MonsterListing extends Group {

        final Label l1;
        final MonsterStatusLabel l2;
        final MonsterMagicPointsLabel l3;
        final ListingBackground bckgrnd;

        final Monster m;
        final MutableMonster mm;

        MonsterListing(MutableMonster mm) {
            this.mm = mm;
            this.m = (Monster) mm.baseType();
            this.bckgrnd = new ListingBackground();
            mm.setHealthCursor(this.bckgrnd);
            this.bckgrnd.adjust(mm.getCurrentHitPoints(), mm.getMaxHitPoints());

            this.l1 = new Label("", combatSkin, "default-12");
            this.l2 = new MonsterStatusLabel(mm);
            this.l3 = new MonsterMagicPointsLabel(mm);

            String d1 = String.format("%s  LVL %d", m.name.toUpperCase(), mm.getLevel());
            this.l1.setText(d1);

            addActor(this.bckgrnd);
            addActor(this.l1);
            addActor(this.l2);
            addActor(this.l3);

            float x = 3;
            this.l1.setBounds(x, LINE_HEIGHT * 2, LISTING_WIDTH - 6, LINE_HEIGHT);
            for (Label label : new Label[] {l1, l2, l3}) label.setEllipsis(true);
            this.l2.setBounds(x, LINE_HEIGHT * 1, LISTING_WIDTH - 6, LINE_HEIGHT);
            this.l3.setBounds(x, LINE_HEIGHT * 0, LISTING_WIDTH - 6, LINE_HEIGHT);

            this.setSize(LISTING_WIDTH, LINE_HEIGHT * 3f);

        }

    }

    private class DoGooderStatusLabel extends Label {

        private final MutableCharacter mm;

        public DoGooderStatusLabel(MutableCharacter mm) {
            super("", combatSkin, "default-12");
            this.mm = mm;
            setColor(mm.status().color());
            setText(getText());
        }

        @Override
        public com.badlogic.gdx.utils.CharArray getText() {
            return new com.badlogic.gdx.utils.CharArray(
                    String.format("HP: %d / %d AC: %d  ST: %s", mm.getCurrentHitPoints(), mm.getMaxHitPoints(), mm.getArmourClass(), mm.getCurrentHitPoints() <= 0 ? "DEAD" : mm.status()));
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            setText(getText());
            super.draw(batch, parentAlpha);
        }

        @Override
        public Color getColor() {
            return mm.getCurrentHitPoints() <= 0 ? Color.SCARLET : mm.status().color();
        }

    }

    private class DoGooderMagicPointsLabel extends Label {

        private final MutableCharacter mm;

        public DoGooderMagicPointsLabel(MutableCharacter mm) {
            super("", combatSkin, "default-12");
            this.mm = mm;
            setText(getText());
        }

        @Override
        public com.badlogic.gdx.utils.CharArray getText() {
            int[] ms = mm.getMageSpellLevels();
            int[] cs = mm.getPriestSpellLevels();
            return new com.badlogic.gdx.utils.CharArray(
                    String.format("M: %d/%d/%d/%d/%d/%d/%d  P: %d/%d/%d/%d/%d/%d/%d",
                            ms[0], ms[1], ms[2], ms[3], ms[4], ms[5], ms[6],
                            cs[0], cs[1], cs[2], cs[3], cs[4], cs[5], cs[6]));
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            setText(getText());
            super.draw(batch, parentAlpha);
        }

    }

    private class DoGooderListing extends Group {

        final Image icon;
        final Label l1, l2;
        final DoGooderStatusLabel l3;
        final DoGooderMagicPointsLabel l4;
        final ListingBackground bckgrnd;

        final DoGooder m;
        final MutableCharacter mm;

        DoGooderListing(MutableCharacter mm) {
            this.mm = mm;
            this.m = (DoGooder) mm.baseType();

            this.bckgrnd = new ListingBackground(ENEMY_WIDTH, ENEMY_HEIGHT);
            mm.setHealthCursor(this.bckgrnd);
            this.bckgrnd.adjust(mm.getCurrentHitPoints(), mm.getMaxHitPoints());

            this.icon = makeIcon(this.m.iconID, this.m.charlev);

            this.l1 = new Label("", combatSkin, "default-12");
            this.l2 = new Label("", combatSkin, "default-12");
            this.l3 = new DoGooderStatusLabel(mm);
            this.l4 = new DoGooderMagicPointsLabel(mm);

            String d1 = String.format("%s  %s", m.name.toUpperCase(), m.race != null ? m.race : "NO RACE");
            this.l1.setText(d1);

            String d2 = String.format("LVL %d  %s", mm.getLevel(), m.characterClass);
            this.l2.setText(d2);

            addActor(this.bckgrnd);
            addActor(this.icon);
            addActor(this.l1);
            addActor(this.l2);
            addActor(this.l3);
            addActor(this.l4);

            float x = 84;
            this.l1.setBounds(x, 64, ENEMY_WIDTH - 92, LINE_HEIGHT);
            this.l2.setBounds(x, 46, ENEMY_WIDTH - 92, LINE_HEIGHT);
            this.l3.setBounds(x, 28, ENEMY_WIDTH - 92, LINE_HEIGHT);
            this.l4.setBounds(x, 10, ENEMY_WIDTH - 92, LINE_HEIGHT);

            this.icon.setBounds(6, 4, 72, 80);
            for (Label label : new Label[] {l1, l2, l3, l4}) label.setEllipsis(true);

            this.setSize(ENEMY_WIDTH, ENEMY_HEIGHT);

            this.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (ended || mm.isDead()) return;
                    selected.remove();
                    DoGooderListing.this.addActor(selected);
                }
            });

        }

    }

    private class ListingBackground extends HealthCursor {
        private final int width, height;
        private float healthWidth;

        ListingBackground() { this(LISTING_WIDTH, LINE_HEIGHT * 3); }

        ListingBackground(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override public void adjust(int hp, int maxhp) {
            healthWidth = maxhp <= 0 ? 0 : width * Math.max(0, Math.min(1f, (float) hp / maxhp));
        }

        @Override public void draw(Batch batch, float parentAlpha) {
            Color previous = new Color(batch.getColor());
            batch.setColor(.065f, .095f, .12f, parentAlpha);
            batch.draw(uiPixel, getX(), getY(), width, height);
            batch.setColor(.20f, .75f, .63f, parentAlpha);
            batch.draw(uiPixel, getX(), getY(), healthWidth, 3);
            batch.setColor(previous);
        }
    }
    private void removeMonsterFromTable(MutableMonster mm) {

        Cell found = null;
        for (Cell cell : this.monstersTable.getCells()) {
            Actor actor = cell.getActor();
            if (actor instanceof MonsterListing ml) {
                if (ml.mm == mm) {
                    found = cell;
                    break;
                }
            }
        }

        if (found != null) {

            found.getActor().remove();
            monstersTable.getCells().removeValue(found, true);

            List<Actor> cells = new ArrayList<>();
            for (Cell c : monstersTable.getCells().toArray(Cell.class)) {
                cells.add(c.getActor());
            }

            monstersTable.reset();

            for (Actor a : cells) {
                monstersTable.add(a).pad(2);
                monstersTable.row();
            }

            monstersTable.layout();
        }
    }

    private Image makeIcon(String iconID, int charlev) {
        String regionName = iconID;

        if (iconID.startsWith("Mage")) {
            regionName = String.format("Mage_%02d", Utils.getRandomBetween(1, 9));
        } else if (iconID.startsWith("Fighter")) {
            int clampedLevel = Math.max(0, Math.min(charlev, 80));
            int fighterIconNumber = (int) Math.floor((clampedLevel / 81.0) * 22) + 1;
            regionName = String.format("Fighter_%02d", fighterIconNumber);
        } else if (iconID.startsWith("Priest")) {
            regionName = String.format("Priest_%02d", Utils.getRandomBetween(1, 7));
        }

        TextureRegion region = iconAtlas.findRegion(regionName);
        if (region == null) {
            region = iconAtlas.findRegion(iconID);
        }

        return new Image(region);
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

}
