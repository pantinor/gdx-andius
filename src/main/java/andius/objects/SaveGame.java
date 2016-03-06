package andius.objects;

import andius.Constants;
import static andius.Constants.LEVEL_PROGRESSION_TABLE;
import static andius.Constants.SPELL_PTS;
import andius.Race;
import java.util.Random;
import utils.Utils;
import utils.XORShiftRandom;
import com.google.gson.GsonBuilder;
import com.google.gson.Gson;
import java.util.HashMap;
import com.badlogic.gdx.utils.Base64Coder;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.io.IOUtils;

public class SaveGame implements Constants {

    public static final Random rand = new XORShiftRandom();

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
        public Status status = Status.GOOD;
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
        
        public ArmorType armor = ArmorType.NONE;
        public WeaponType weapon = WeaponType.NONE;
        public HelmType helm = HelmType.NONE;
        public GloveType glove = GloveType.NONE;
        public ItemType item1 = ItemType.NONE;
        public ItemType item2 = ItemType.NONE;

        public HashMap<WeaponType, Integer> weapons = new HashMap<>();
        public HashMap<ArmorType, Integer> armors = new HashMap<>();
        public HashMap<HelmType, Integer> helms = new HashMap<>();
        public HashMap<GloveType, Integer> gloves = new HashMap<>();
        public HashMap<ItemType, Integer> items = new HashMap<>();
        
        public int submorsels = 400;

        public void awardXP(int value) {
            exp = Utils.adjustValueMax(exp, value, 999999999);
        }

        public void adjustGold(int v) {
            gold = Utils.adjustValue(gold, v, 999999999, 0);
        }

        public int calculateMaxHP() {
            int hp = this.classType.getHitDie();
            for (int i = 0; i < this.level; i++) {
                hp += Utils.getRandomBetween(1, this.classType.getHitDie());
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
            }
            return hp;
        }

        public int calculateLevel() {

            int lvl = 0;

            for (int i = 0; i < LEVEL_PROGRESSION_TABLE.length; i++) {
                int[] levels = LEVEL_PROGRESSION_TABLE[i];
                int thr = levels[this.classType.ordinal()];
                if (this.exp >= thr) {
                    lvl = i + 1;
                }
            }

            if (lvl == LEVEL_PROGRESSION_TABLE.length) {
                int thr = LEVEL_PROGRESSION_TABLE[LEVEL_PROGRESSION_TABLE.length - 1][this.classType.ordinal()];
                int sum = thr * 2;
                while (this.exp > sum) {
                    lvl++;
                    sum += thr;
                }
            }

            return lvl;

        }

        public int[] getMageSpellPoints() {
            try {
                if (this.classType == ClassType.MAGE || this.classType == ClassType.WIZARD) {
                    return SPELL_PTS[this.level];
                } else if (this.classType == ClassType.SAMURAI) {
                    if (this.level >= 4) {
                        return SPELL_PTS[this.level - 4];
                    }
                }
            } catch (Exception e) {
                return SPELL_PTS[SPELL_PTS.length - 1];
            }
            return new int[]{0, 0, 0, 0, 0, 0, 0};
        }

        public int[] getClericSpellPoints() {
            try {
                if (this.classType == ClassType.CLERIC) {
                    return SPELL_PTS[this.level];
                } else if (this.classType == ClassType.WIZARD || this.classType == ClassType.LORD) {
                    if (this.level >= 4) {
                        return SPELL_PTS[this.level - 4];
                    }
                }
            } catch (Exception e) {
                return SPELL_PTS[SPELL_PTS.length - 1];
            }
            return new int[]{0, 0, 0, 0, 0, 0, 0};
        }

    }
}
