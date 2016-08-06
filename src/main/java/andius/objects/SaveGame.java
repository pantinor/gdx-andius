package andius.objects;

import andius.Andius;
import andius.Constants;
import static andius.Constants.LEVEL_PROGRESSION_TABLE;
import java.util.Random;
import utils.Utils;
import utils.XORShiftRandom;
import com.google.gson.GsonBuilder;
import com.google.gson.Gson;
import com.badlogic.gdx.utils.Base64Coder;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.io.IOUtils;

public class SaveGame implements Constants {

    public static final Random RANDOM = new XORShiftRandom();

    public CharacterRecord[] players;
    public int map;
    public int wx;
    public int wy;

    public static SaveGame read(String file) throws Exception {
        GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(file));
        String b64 = IOUtils.toString(gzis, StandardCharsets.UTF_8);
        gzis.close();
        String json = Base64Coder.decodeString(b64);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        SaveGame sg = gson.fromJson(json, SaveGame.class);
        //set initial start
        if (sg.wx == 0 && sg.wy == 0) {
            sg.wx = Map.WORLD.getStartX();
            sg.wy = Map.WORLD.getStartY();
        }
        return sg;
    }

    public void write(String file) throws Exception {
        for (CharacterRecord player : players) {
            player.acmodifier1 = 0;
            player.acmodifier2 = 0;
        }
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String json = gson.toJson(this);
        String b64 = Base64Coder.encodeString(json);
        FileOutputStream fos = new FileOutputStream(file);
        GZIPOutputStream gzos = new GZIPOutputStream(fos);
        gzos.write(b64.getBytes("UTF-8"));
        gzos.close();
    }

    public static class CharacterRecord {

        public String name = null;
        public int portaitIndex = 0;
        public Status status = Status.OK;
        public int str;
        public int intell;
        public int piety;
        public int vitality;
        public int agility;
        public int luck;
        public Race race = Race.HUMAN;
        public ClassType classType = ClassType.FIGHTER;
        public int hp;
        public int maxhp;
        public int exp;
        public int gold;
        public int level;

        public Item armor;
        public Item weapon;
        public Item helm;
        public Item shield;
        public Item glove;
        public Item item1;
        public Item item2;

        public List<Spells> knownSpells = new ArrayList<>();
        public Spells[] spellPresets = new Spells[10];
        public int[] magePoints = new int[7];
        public int[] clericPoints = new int[7];

        public List<Item> inventory = new ArrayList<>();

        public int submorsels = 400;
        public int acmodifier1; //lasts for single combat
        public int acmodifier2; //lasts until next rest at inn

        public void awardXP(int amt) {
            exp = Utils.adjustValueMax(exp, amt, Integer.MAX_VALUE);
        }

        public void adjustGold(int amt) {
            gold = Utils.adjustValue(gold, amt, Integer.MAX_VALUE, 0);
        }

        public void adjustHP(int amt) {
            hp = Utils.adjustValue(hp, amt, maxhp, 0);
            if (hp <= 0) {
                status = Status.DEAD;
            }
        }

        public boolean canCast(Spells spell) {
            if (!knownSpells.contains(spell)) {
                return false;
            }
            if (spell.getType() == ClassType.MAGE) {
                return magePoints[spell.getLevel() - 1] > 0;
            } else {
                return clericPoints[spell.getLevel() - 1] > 0;
            }
        }

        public void decrMagicPts(Spells spell) {
            if (spell.getType() == ClassType.MAGE) {
                magePoints[spell.getLevel() - 1]--;
            } else {
                clericPoints[spell.getLevel() - 1]--;
            }
        }

        public boolean isDisabled() {
            return this.status != Status.OK && this.status != Status.POISONED;
        }

        public int calculateAC() {
            int ac = 10;
            if (weapon != null) {
                ac -= weapon.armourClass;
            }
            if (armor != null) {
                ac -= armor.armourClass;
            }
            if (helm != null) {
                ac -= helm.armourClass;
            }
            if (glove != null) {
                ac -= glove.armourClass;
            }
            if (shield != null) {
                ac -= shield.armourClass;
            }
            if (item1 != null) {
                ac -= item1.armourClass;
            }
            if (item2 != null) {
                ac -= item2.armourClass;
            }
            if (classType == ClassType.NINJA && weapon == null) {
                ac = (level / 3) - 2;
            }
            ac -= acmodifier1;
            ac -= acmodifier2;
            return ac;
        }

        public int getMoreHP() {
            int hp = this.classType.getHitDie();
            hp += RANDOM.nextInt(this.classType.getHitDie()) + 1;
            if (this.vitality <= 3) {
                hp -= 2;
            } else if (this.vitality == 4 || this.vitality == 5) {
                hp -= 1;
            } else if (this.vitality == 16) {
                hp += 1;
            } else if (this.vitality == 17) {
                hp += 2;
            } else if (this.vitality >= 18) {
                hp += 3;
            }
            return hp;
        }

        public int checkAndSetLevel() {

            int expnxtlvl = 0;
            if (this.level <= 12) {
                expnxtlvl = LEVEL_PROGRESSION_TABLE[this.level][this.classType.ordinal()];
            } else {
                for (int i = 13; i <= this.level; i++) {
                    expnxtlvl += LEVEL_PROGRESSION_TABLE[0][this.classType.ordinal()];
                }
            }

            if (this.exp - expnxtlvl >= 0) {
                this.level++;
            }

            return (this.exp - expnxtlvl);
        }

    }

    public static int gainOrLose(int attrib) {
        if (RANDOM.nextInt(4) != 0) {
            if (RANDOM.nextInt(130) < 25) {
                if (attrib == 18 && RANDOM.nextInt(6) != 4) {
                    //nothing
                } else {
                    attrib--;
                }
            } else if (attrib != 18) {
                attrib++;
            }
        }
        return attrib;
    }

    private static void setSpellCount(int[] spellCounts, int idx, int low, int high, List<Spells> knownSpells) {
        for (int i = low; i <= high; i++) {
            if (knownSpells.contains(Spells.values()[i - 1])) {
                spellCounts[idx]++;
            }
        }
    }

    private static void setMinMageSpellCounts(CharacterRecord rec) {
        rec.magePoints = new int[7];
        setSpellCount(rec.magePoints, 0, 1, 4, rec.knownSpells);
        setSpellCount(rec.magePoints, 1, 5, 6, rec.knownSpells);
        setSpellCount(rec.magePoints, 2, 7, 8, rec.knownSpells);
        setSpellCount(rec.magePoints, 3, 9, 11, rec.knownSpells);
        setSpellCount(rec.magePoints, 4, 12, 14, rec.knownSpells);
        setSpellCount(rec.magePoints, 5, 15, 18, rec.knownSpells);
        setSpellCount(rec.magePoints, 6, 19, 21, rec.knownSpells);
    }

    private static void setMinPriestSpellCounts(CharacterRecord rec) {
        rec.clericPoints = new int[7];
        setSpellCount(rec.clericPoints, 0, 22, 26, rec.knownSpells);
        setSpellCount(rec.clericPoints, 1, 27, 30, rec.knownSpells);
        setSpellCount(rec.clericPoints, 2, 31, 34, rec.knownSpells);
        setSpellCount(rec.clericPoints, 3, 35, 38, rec.knownSpells);
        setSpellCount(rec.clericPoints, 4, 39, 44, rec.knownSpells);
        setSpellCount(rec.clericPoints, 5, 45, 48, rec.knownSpells);
        setSpellCount(rec.clericPoints, 6, 49, 50, rec.knownSpells);
    }

    public static void setSpellPoints(CharacterRecord rec) {

        setMinMageSpellCounts(rec);
        setMinPriestSpellCounts(rec);

        if (rec.classType == ClassType.MAGE) {
            setSpellsPerLevel(rec.magePoints, rec, 0, 2);
        }
        if (rec.classType == ClassType.CLERIC) {
            setSpellsPerLevel(rec.clericPoints, rec, 0, 2);
        }
        if (rec.classType == ClassType.WIZARD) {
            setSpellsPerLevel(rec.magePoints, rec, 0, 4);
            setSpellsPerLevel(rec.clericPoints, rec, 3, 4);
        }
        if (rec.classType == ClassType.LORD) {
            setSpellsPerLevel(rec.clericPoints, rec, 3, 2);
        }
        if (rec.classType == ClassType.SAMURAI) {
            setSpellsPerLevel(rec.magePoints, rec, 3, 3);
        }
    }

    private static void setSpellsPerLevel(int[] spellCounts, CharacterRecord rec, int lvlModifier1, int lvlModifier2) {
        int count = rec.level - lvlModifier1;
        if (count <= 0) {
            return;
        }
        for (int i = 0; i < 7 && count > 0; i++) {
            if (count > spellCounts[i]) {
                spellCounts[i] = count;
            }
            count = count - lvlModifier2;
        }
        for (int i = 0; i < 7; i++) {
            if (spellCounts[i] > 9) {
                spellCounts[i] = 9;
            }
        }
    }

    public static boolean tryLearn(CharacterRecord rec) {

        boolean learned = false;

        if (rec.magePoints[0] > 0) {
            learned = tryLearnSpell(rec, rec.intell, 1, 4) || learned;
        }
        if (rec.magePoints[1] > 0) {
            learned = tryLearnSpell(rec, rec.intell, 5, 6) || learned;
        }
        if (rec.magePoints[2] > 0) {
            learned = tryLearnSpell(rec, rec.intell, 7, 8) || learned;
        }
        if (rec.magePoints[3] > 0) {
            learned = tryLearnSpell(rec, rec.intell, 9, 11) || learned;
        }
        if (rec.magePoints[4] > 0) {
            learned = tryLearnSpell(rec, rec.intell, 12, 14) || learned;
        }
        if (rec.magePoints[5] > 0) {
            learned = tryLearnSpell(rec, rec.intell, 15, 18) || learned;
        }
        if (rec.magePoints[6] > 0) {
            learned = tryLearnSpell(rec, rec.intell, 19, 21) || learned;
        }

        if (rec.clericPoints[0] > 0) {
            learned = tryLearnSpell(rec, rec.piety, 22, 26) || learned;
        }
        if (rec.clericPoints[1] > 0) {
            learned = tryLearnSpell(rec, rec.piety, 27, 30) || learned;
        }
        if (rec.clericPoints[2] > 0) {
            learned = tryLearnSpell(rec, rec.piety, 31, 34) || learned;
        }
        if (rec.clericPoints[3] > 0) {
            learned = tryLearnSpell(rec, rec.piety, 35, 38) || learned;
        }
        if (rec.clericPoints[4] > 0) {
            learned = tryLearnSpell(rec, rec.piety, 39, 44) || learned;
        }
        if (rec.clericPoints[5] > 0) {
            learned = tryLearnSpell(rec, rec.piety, 45, 48) || learned;
        }
        if (rec.clericPoints[6] > 0) {
            learned = tryLearnSpell(rec, rec.piety, 49, 50) || learned;
        }

        return learned;

    }

    private static boolean tryLearnSpell(CharacterRecord rec, int attrib, int low, int high) {
        boolean knowsSpellsAtThisLevel = false;
        boolean learned = false;
        for (int i = low; i <= high; i++) {
            Spells spell = Spells.values()[i - 1];
            knowsSpellsAtThisLevel = knowsSpellsAtThisLevel || rec.knownSpells.contains(spell);
        }
        for (int i = low; i <= high; i++) {
            Spells spell = Spells.values()[i - 1];
            if (!rec.knownSpells.contains(spell)) {
                if (RANDOM.nextInt(30) < attrib || !knowsSpellsAtThisLevel) {
                    learned = true;
                    knowsSpellsAtThisLevel = true;
                    rec.knownSpells.add(spell);
                }
            }
        }
        return learned;
    }

}
