package andius.objects;

import andius.Constants;
import static andius.Constants.LEVEL_PROGRESSION_TABLE;
import andius.Sound;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.io.IOUtils;

public class SaveGame implements Constants {

    public static final Random RANDOM = new XORShiftRandom();

    public CharacterRecord[] players;
    public int map;
    public int wx;
    public int wy;
    public final java.util.Map<Map, List<Integer>> removedActors = new HashMap<>();

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
//            for (int i = 0; i < 7; i++) {
//                if (player.magePoints[i] < 0) {
//                    player.magePoints[i] = 0;
//                }
//                if (player.clericPoints[i] < 0) {
//                    player.clericPoints[i] = 0;
//                }
//            }
//            if (player.level == 0) {
//                player.level = 1;
//            }
        }

        for (Map map : Map.values()) {
            if (map.getMap() != null) {
                MapLayer peopleLayer = map.getTiledMap().getLayers().get("people");
                if (peopleLayer != null) {
                    Iterator<MapObject> iter = peopleLayer.getObjects().iterator();
                    while (iter.hasNext()) {
                        MapObject obj = iter.next();
                        int id = obj.getProperties().get("id", Integer.class);
                        boolean found = false;
                        for (Actor a : map.getMap().actors) {
                            if (a.getId() == id) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            List<Integer> l = this.removedActors.get(map);
                            if (l == null) {
                                l = new ArrayList<>();
                                this.removedActors.put(map, l);
                            }
                            l.add(id);
                        }
                    }
                }
            }
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

        public final AtomicInteger paralyzedCountdown = new AtomicInteger();
        public final AtomicInteger silencedCountdown = new AtomicInteger();
        public final AtomicInteger asleepCountdown = new AtomicInteger();

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
                if (magePoints[spell.getLevel() - 1] > 0) {
                    magePoints[spell.getLevel() - 1]--;
                }
            } else if (clericPoints[spell.getLevel() - 1] > 0) {
                clericPoints[spell.getLevel() - 1]--;
            }
        }

        public boolean isDisabled() {
            boolean disabled = false;
            switch (this.status) {
                case OK:
                case SILENCED:
                case POISONED:
                    disabled = false;
                    break;
                case PARALYZED:
                case ASLEEP:
                case STONED:
                case AFRAID:
                case DEAD:
                case ASHES:
                    disabled = true;
                    break;
                default:
                    disabled = false;
                    break;
            }

            if (this.paralyzedCountdown.get() > 0) {
                disabled = true;
            }

            if (this.asleepCountdown.get() > 0) {
                disabled = true;
            }

            return disabled;
        }

        public boolean isDead() {
            return this.status == Status.DEAD;
        }

        public void decrementStatusEffects() {
            if (this.paralyzedCountdown.get() > 0) {
                this.paralyzedCountdown.decrementAndGet();
            }
            if (this.silencedCountdown.get() > 0) {
                this.silencedCountdown.decrementAndGet();
            }
            if (this.asleepCountdown.get() > 0) {
                this.asleepCountdown.decrementAndGet();
            }
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

    private static void setMinMageSpellCounts(int[] pts, List<Spells> knownSpells) {
        pts = new int[7];
        setSpellCount(pts, 0, 1, 4, knownSpells);
        setSpellCount(pts, 1, 5, 6, knownSpells);
        setSpellCount(pts, 2, 7, 8, knownSpells);
        setSpellCount(pts, 3, 9, 11, knownSpells);
        setSpellCount(pts, 4, 12, 14, knownSpells);
        setSpellCount(pts, 5, 15, 18, knownSpells);
        setSpellCount(pts, 6, 19, 21, knownSpells);
    }

    private static void setMinPriestSpellCounts(int[] pts, List<Spells> knownSpells) {
        pts = new int[7];
        setSpellCount(pts, 0, 22, 26, knownSpells);
        setSpellCount(pts, 1, 27, 30, knownSpells);
        setSpellCount(pts, 2, 31, 34, knownSpells);
        setSpellCount(pts, 3, 35, 38, knownSpells);
        setSpellCount(pts, 4, 39, 44, knownSpells);
        setSpellCount(pts, 5, 45, 48, knownSpells);
        setSpellCount(pts, 6, 49, 50, knownSpells);
    }

    public static void setSpellPoints(CharacterRecord rec) {

        setMinMageSpellCounts(rec.magePoints, rec.knownSpells);
        setMinPriestSpellCounts(rec.clericPoints, rec.knownSpells);

        if (rec.classType == ClassType.MAGE) {
            setSpellsPerLevel(rec.magePoints, rec.level, 0, 2);
        }
        if (rec.classType == ClassType.CLERIC) {
            setSpellsPerLevel(rec.clericPoints, rec.level, 0, 2);
        }
        if (rec.classType == ClassType.WIZARD) {
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

    public static void setMonsterSpellPoints(MutableMonster m) {

        setMinMageSpellCounts(m.magePoints, m.knownSpells);
        setMinPriestSpellCounts(m.clericPoints, m.knownSpells);

        if (m.mageSpellLevel > 0) {
            setSpellsPerLevel(m.magePoints, m.mageSpellLevel, 0, 2);
        }
        if (m.priestSpellLevel > 0) {
            setSpellsPerLevel(m.clericPoints, m.priestSpellLevel, 0, 2);
        }
    }

    public static boolean tryLearn(MutableMonster m) {

        boolean learned = false;

        if (m.mageSpellLevel > 0) {
            int intell = 8;
            if (m.mageSpellLevel == 7) {
                intell = 18;
            } else if (m.mageSpellLevel >= 5) {
                intell = 16;
            } else if (m.mageSpellLevel >= 3) {
                intell = 12;
            }
            for (int i = 0; i < m.mageSpellLevel; i++) {
                if (m.magePoints[0] > 0) {
                    learned = tryLearnSpell(m.knownSpells, intell, 1, 4) || learned;
                }
                if (m.magePoints[1] > 0) {
                    learned = tryLearnSpell(m.knownSpells, intell, 5, 6) || learned;
                }
                if (m.magePoints[2] > 0) {
                    learned = tryLearnSpell(m.knownSpells, intell, 7, 8) || learned;
                }
                if (m.magePoints[3] > 0) {
                    learned = tryLearnSpell(m.knownSpells, intell, 9, 11) || learned;
                }
                if (m.magePoints[4] > 0) {
                    learned = tryLearnSpell(m.knownSpells, intell, 12, 14) || learned;
                }
                if (m.magePoints[5] > 0) {
                    learned = tryLearnSpell(m.knownSpells, intell, 15, 18) || learned;
                }
                if (m.magePoints[6] > 0) {
                    learned = tryLearnSpell(m.knownSpells, intell, 19, 21) || learned;
                }
            }
        }

        if (m.priestSpellLevel > 0) {
            int piety = 8;
            if (m.priestSpellLevel == 7) {
                piety = 18;
            } else if (m.priestSpellLevel >= 5) {
                piety = 16;
            } else if (m.priestSpellLevel >= 3) {
                piety = 12;
            }
            for (int i = 0; i < m.priestSpellLevel; i++) {
                if (m.clericPoints[0] > 0) {
                    learned = tryLearnSpell(m.knownSpells, piety, 22, 26) || learned;
                }
                if (m.clericPoints[1] > 0) {
                    learned = tryLearnSpell(m.knownSpells, piety, 27, 30) || learned;
                }
                if (m.clericPoints[2] > 0) {
                    learned = tryLearnSpell(m.knownSpells, piety, 31, 34) || learned;
                }
                if (m.clericPoints[3] > 0) {
                    learned = tryLearnSpell(m.knownSpells, piety, 35, 38) || learned;
                }
                if (m.clericPoints[4] > 0) {
                    learned = tryLearnSpell(m.knownSpells, piety, 39, 44) || learned;
                }
                if (m.clericPoints[5] > 0) {
                    learned = tryLearnSpell(m.knownSpells, piety, 45, 48) || learned;
                }
                if (m.clericPoints[6] > 0) {
                    learned = tryLearnSpell(m.knownSpells, piety, 49, 50) || learned;
                }
            }

            Iterator<Spells> iter = m.knownSpells.iterator();
            while (iter.hasNext()) {
                Spells s = iter.next();
                if (s != Spells.MANIFO) {
                    //iter.remove();
                }
                if (s.getArea() != SpellArea.COMBAT) {
                    if (s.getSound() != Sound.HEALING) {
                        iter.remove();
                    }
                } else if (s == Spells.LOKTOFEIT || s == Spells.DIALKO || s == Spells.LATUMOFIS) {
                    iter.remove();
                }

            }
        }

        return learned;

    }
}
