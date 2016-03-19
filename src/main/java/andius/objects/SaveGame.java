package andius.objects;

import andius.Constants;
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
import java.util.ArrayList;
import java.util.List;
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
        
        public int[] magePoints = new int[]{0, 0, 0, 0, 0, 0, 0};
        public int[] clericPoints = new int[]{0, 0, 0, 0, 0, 0, 0};

        public List<Item> inventory = new ArrayList<>();

        public int submorsels = 400;

        public void awardXP(int v) {
            exp = Utils.adjustValueMax(exp, v, Integer.MAX_VALUE);
        }

        public void adjustGold(int v) {
            gold = Utils.adjustValue(gold, v, Integer.MAX_VALUE, 0);
        }
        
        public void adjustHP(int v) {
            hp = Utils.adjustValue(hp, v, Integer.MAX_VALUE, 0);
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
            if (classType == ClassType.NINJA) {
                ac = ( level / 3 ) - 2;
            }
            return ac;
        }

        public int getMoreHP() {
            int hp = this.classType.getHitDie();
            hp += rand.nextInt(this.classType.getHitDie()) + 1;
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

        public int[] getMaxMageSpellPoints() {
            try {
                if (this.classType == ClassType.MAGE) {
                    return MAGE_SPELL_PTS[this.level];
                } else if (this.classType == ClassType.WIZARD) {
                    return WIZARD_MAGE_SPELL_PTS[this.level];
                } else if (this.classType == ClassType.SAMURAI) {
                    return SAMURAI_MAGE_SPELL_PTS[this.level];
                }
            } catch (Exception e) {
                return new int[]{9, 9, 9, 9, 9, 9, 9};
            }
            return new int[]{0, 0, 0, 0, 0, 0, 0};
        }

        public int[] getMaxClericSpellPoints() {
            try {
                if (this.classType == ClassType.CLERIC) {
                    return CLERIC_SPELL_PTS[this.level];
                } else if (this.classType == ClassType.WIZARD) {
                    return WIZARD_CLERIC_SPELL_PTS[this.level];
                } else if (this.classType == ClassType.LORD) {
                    return LORD_CLERIC_SPELL_PTS[this.level];
                }
            } catch (Exception e) {
                return new int[]{9, 9, 9, 9, 9, 9, 9};
            }
            return new int[]{0, 0, 0, 0, 0, 0, 0};
        }

    }
}
