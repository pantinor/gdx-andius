package andius.objects;

import andius.Constants;
import static andius.Constants.LEVEL_PROGRESSION_TABLE;
import andius.Direction;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import java.util.Random;
import utils.Utils;
import com.google.gson.GsonBuilder;
import com.google.gson.Gson;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.IOUtils;

public class SaveGame implements Constants {

    public static final Random RANDOM = new Random();

    public CharacterRecord[] players;
    public Map map;
    public int wx;//world x
    public int wy;//world y
    public int x;//map x
    public int y;//map y
    public int level;//map level
    public Direction direction;

    public final java.util.Map<Map, List<String>> removedActors = new HashMap<>();
    public final java.util.Map<Map, List<AnsweredRiddle>> riddles = new HashMap<>();

    public static SaveGame read(String file) throws Exception {
        //GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(file));
        //String b64 = IOUtils.toString(gzis, StandardCharsets.UTF_8);
        //gzis.close();
        //String json = Base64Coder.decodeString(b64);

        InputStream is = new FileInputStream(file);
        String json = IOUtils.toString(is, StandardCharsets.UTF_8);
        is.close();

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();
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

        for (Map map : Map.values()) {
            if (map.getBaseMap() != null) {
                MapLayer peopleLayer = map.getTiledMap().getLayers().get("people");
                if (peopleLayer != null) {
                    Iterator<MapObject> iter = peopleLayer.getObjects().iterator();
                    while (iter.hasNext()) {
                        MapObject obj = iter.next();
                        float x = obj.getProperties().get("x", Float.class);
                        float y = obj.getProperties().get("y", Float.class);
                        String hash = "M:" + x + ":" + y;
                        boolean found = false;
                        for (Actor a : map.getBaseMap().actors) {
                            if (a.hash().equals(hash)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            List<String> l = this.removedActors.get(map);
                            if (l == null) {
                                l = new ArrayList<>();
                                this.removedActors.put(map, l);
                            }
                            if (!l.contains(hash)) {
                                l.add(hash);
                            }
                        }
                    }
                }
            }
        }

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();
        String json = gson.toJson(this);
        //String b64 = Base64Coder.encodeString(json);
        FileOutputStream fos = new FileOutputStream(file);
        //GZIPOutputStream gzos = new GZIPOutputStream(fos);
        //gzos.write(b64.getBytes("UTF-8"));
        //gzos.close();
        fos.write(json.getBytes("UTF-8"));
        fos.close();

    }

    public static class CharacterRecord {

        public String name = null;
        public int portaitIndex = 0;
        public final State status = new State();
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

        public List<MutableMonster> summonedMonsters = new ArrayList<>();

        public int submorsels = 400;
        public int acmodifier1; //lasts for single combat
        public int acmodifier2; //lasts until next rest at inn

        public transient HealthCursor healthCursor;

        public void awardXP(int amt) {
            exp = Utils.adjustValueMax(exp, amt, Integer.MAX_VALUE);
        }

        public void adjustGold(long amt) {
            gold = Utils.adjustValue(gold, amt, Integer.MAX_VALUE, 0);
        }

        public void adjustHP(int amt) {
            hp = Utils.adjustValue(hp, amt, maxhp, 0);
        }

        public boolean canCast(Spells spell) {

            if (weapon != null && weapon.spell != null) {
                if (weapon.spell == spell) {
                    return true;
                }
            }

            if (item1 != null && item1.spell != null) {
                if (item1.spell == spell) {
                    return true;
                }
            }

            if (item2 != null && item2.spell != null) {
                if (item2.spell == spell) {
                    return true;
                }
            }

            for (Item i : inventory) {
                if (i.spell != null) {
                    if (i.spell == spell) {
                        return true;
                    }
                }
            }

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
                if (magePoints[spell.getLevel() - 1] > 0) {
                    magePoints[spell.getLevel() - 1]--;
                }
            } else if (clericPoints[spell.getLevel() - 1] > 0) {
                clericPoints[spell.getLevel() - 1]--;
            }
        }

        public boolean isDisabled() {
            if (isDead()) {
                return true;
            }
            return this.status.isDisabled();
        }

        public boolean isAllWell() {
            if (isDead()) {
                return false;
            }
            for (Status s : Status.values()) {
                if (this.status.has(s)) {
                    return false;
                }
            }
            return true;
        }

        public boolean isDead() {
            return hp <= 0;
        }

        public void decrementStatusEffects() {
            this.status.decrement(Status.PARALYZED);
            this.status.decrement(Status.SILENCED);
            this.status.decrement(Status.ASLEEP);
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

            if (ac < -10) {
                ac = -10;
            }

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

        public int extraSwings() {
            int v = this.weapon.extraSwings == 0 ? 1 : this.weapon.extraSwings;
            if (this.classType == ClassType.FIGHTER || this.classType == ClassType.SAMURAI || this.classType == ClassType.LORD) {
                int vc = this.level / 5 + 1;
                if (vc > v) {
                    return vc;
                }
            }
            if (this.classType == ClassType.NINJA) {
                int vc = this.level / 5 + 2;
                if (vc > v) {
                    return vc;
                }
            }
            if (v > 10) {
                v = 10;
            }
            return v;
        }

        public boolean savingThrowBreath() {
            int roll = Utils.RANDOM.nextInt(100) + 1;
            int base = this.level / 5 + this.luck / 6;
            int raceBonus = 0;
            if (this.race == Race.DWARF) {
                raceBonus += 4;
            }
            int classBonus = 0;
            if (this.classType == ClassType.NINJA) {
                classBonus += 3;
            } else if (this.classType == ClassType.THIEF) {
                classBonus += 3;
            }
            return roll < (base + raceBonus + classBonus) * 5;
        }

        public boolean savingThrowSpell() {
            int roll = Utils.RANDOM.nextInt(100) + 1;
            int base = this.level / 5 + this.luck / 6;
            int raceBonus = 0;
            if (this.race == Race.HOBBIT) {
                raceBonus += 3;
            }
            int classBonus = 0;
            if (this.classType == ClassType.MAGE) {
                classBonus += 3;
            } else if (this.classType == ClassType.BISHOP || this.classType == ClassType.SAMURAI || this.classType == ClassType.NINJA) {
                classBonus += 2;
            }
            return roll < (base + raceBonus + classBonus) * 5;
        }

        public boolean savingThrowPetrify() {
            int roll = Utils.RANDOM.nextInt(100) + 1;
            int base = this.level / 5 + this.luck / 6;
            int raceBonus = 0;
            if (this.race == Race.GNOME) {
                raceBonus += 2;
            }
            int classBonus = 0;
            if (this.classType == ClassType.PRIEST) {
                classBonus += 3;
            } else if (this.classType == ClassType.BISHOP || this.classType == ClassType.LORD || this.classType == ClassType.NINJA) {
                classBonus += 2;
            }
            return roll < (base + raceBonus + classBonus) * 5;
        }

        public boolean savingThrowDeath() {
            int roll = Utils.RANDOM.nextInt(100) + 1;
            int base = this.level / 5 + this.luck / 6;
            int raceBonus = 0;
            if (this.race == Race.HUMAN) {
                raceBonus += 1;
            }
            int classBonus = 0;
            if (this.classType == ClassType.FIGHTER || this.classType == ClassType.NINJA) {
                classBonus += 3;
            } else if (this.classType == ClassType.SAMURAI || this.classType == ClassType.LORD) {
                classBonus += 2;
            }
            return roll < (base + raceBonus + classBonus) * 5;
        }

        @Override
        public String toString() {
            return this.name.toUpperCase();
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

    private static void setMinMageSpellCounts(int[] magePoints, List<Spells> knownSpells) {
        int[] pts = new int[7];
        setSpellCount(pts, 0, 1, 4, knownSpells);
        setSpellCount(pts, 1, 5, 6, knownSpells);
        setSpellCount(pts, 2, 7, 8, knownSpells);
        setSpellCount(pts, 3, 9, 11, knownSpells);
        setSpellCount(pts, 4, 12, 14, knownSpells);
        setSpellCount(pts, 5, 15, 18, knownSpells);
        setSpellCount(pts, 6, 19, 21, knownSpells);
        System.arraycopy(pts, 0, magePoints, 0, pts.length);
    }

    private static void setMinPriestSpellCounts(int[] clericPoints, List<Spells> knownSpells) {
        int[] pts = new int[7];
        setSpellCount(pts, 0, 22, 26, knownSpells);
        setSpellCount(pts, 1, 27, 30, knownSpells);
        setSpellCount(pts, 2, 31, 34, knownSpells);
        setSpellCount(pts, 3, 35, 38, knownSpells);
        setSpellCount(pts, 4, 39, 44, knownSpells);
        setSpellCount(pts, 5, 45, 48, knownSpells);
        setSpellCount(pts, 6, 49, 50, knownSpells);
        System.arraycopy(pts, 0, clericPoints, 0, pts.length);
    }

    public static void setSpellPoints(CharacterRecord rec) {

        setMinMageSpellCounts(rec.magePoints, rec.knownSpells);
        setMinPriestSpellCounts(rec.clericPoints, rec.knownSpells);

        if (rec.classType == ClassType.MAGE) {
            setSpellsPerLevel(rec.magePoints, rec.level, 0, 2);
        }
        if (rec.classType == ClassType.PRIEST) {
            setSpellsPerLevel(rec.clericPoints, rec.level, 0, 2);
        }
        if (rec.classType == ClassType.BISHOP) {
            setSpellsPerLevel(rec.magePoints, rec.level, 0, 4);
            setSpellsPerLevel(rec.clericPoints, rec.level, 3, 4);
        }
        if (rec.classType == ClassType.LORD) {
            setSpellsPerLevel(rec.clericPoints, rec.level, 3, 2);
        }
        if (rec.classType == ClassType.SAMURAI) {
            setSpellsPerLevel(rec.magePoints, rec.level, 3, 3);
        }
    }

    private static void setSpellsPerLevel(int[] spellCounts, int level, int lvlModifier1, int lvlModifier2) {
        int count = level - lvlModifier1;
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
            learned = tryLearnSpell(rec.knownSpells, rec.intell, 1, 4) || learned;
        }
        if (rec.magePoints[1] > 0) {
            learned = tryLearnSpell(rec.knownSpells, rec.intell, 5, 6) || learned;
        }
        if (rec.magePoints[2] > 0) {
            learned = tryLearnSpell(rec.knownSpells, rec.intell, 7, 8) || learned;
        }
        if (rec.magePoints[3] > 0) {
            learned = tryLearnSpell(rec.knownSpells, rec.intell, 9, 11) || learned;
        }
        if (rec.magePoints[4] > 0) {
            learned = tryLearnSpell(rec.knownSpells, rec.intell, 12, 14) || learned;
        }
        if (rec.magePoints[5] > 0) {
            learned = tryLearnSpell(rec.knownSpells, rec.intell, 15, 18) || learned;
        }
        if (rec.magePoints[6] > 0) {
            learned = tryLearnSpell(rec.knownSpells, rec.intell, 19, 21) || learned;
        }

        if (rec.clericPoints[0] > 0) {
            learned = tryLearnSpell(rec.knownSpells, rec.piety, 22, 26) || learned;
        }
        if (rec.clericPoints[1] > 0) {
            learned = tryLearnSpell(rec.knownSpells, rec.piety, 27, 30) || learned;
        }
        if (rec.clericPoints[2] > 0) {
            learned = tryLearnSpell(rec.knownSpells, rec.piety, 31, 34) || learned;
        }
        if (rec.clericPoints[3] > 0) {
            learned = tryLearnSpell(rec.knownSpells, rec.piety, 35, 38) || learned;
        }
        if (rec.clericPoints[4] > 0) {
            learned = tryLearnSpell(rec.knownSpells, rec.piety, 39, 44) || learned;
        }
        if (rec.clericPoints[5] > 0) {
            learned = tryLearnSpell(rec.knownSpells, rec.piety, 45, 48) || learned;
        }
        if (rec.clericPoints[6] > 0) {
            learned = tryLearnSpell(rec.knownSpells, rec.piety, 49, 50) || learned;
        }

        return learned;
    }

    private static boolean tryLearnSpell(List<Spells> knownSpells, int attrib, int low, int high) {
        boolean knowsSpellsAtThisLevel = false;
        boolean learned = false;
        for (int i = low; i <= high; i++) {
            Spells spell = Spells.values()[i - 1];
            knowsSpellsAtThisLevel = knowsSpellsAtThisLevel || knownSpells.contains(spell);
        }
        for (int i = low; i <= high; i++) {
            Spells spell = Spells.values()[i - 1];
            if (!knownSpells.contains(spell)) {
                if (RANDOM.nextInt(30) < attrib || !knowsSpellsAtThisLevel) {
                    learned = true;
                    knowsSpellsAtThisLevel = true;
                    knownSpells.add(spell);
                }
            }
        }
        return learned;
    }

    public static class AnsweredRiddle {

        public int level;
        public int x;
        public int y;

        public AnsweredRiddle() {

        }

        public AnsweredRiddle(int level, int x, int y) {
            this.level = level;
            this.x = x;
            this.y = y;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final AnsweredRiddle other = (AnsweredRiddle) obj;
            if (this.level != other.level) {
                return false;
            }
            if (this.x != other.x) {
                return false;
            }
            return this.y == other.y;
        }

    }

}
