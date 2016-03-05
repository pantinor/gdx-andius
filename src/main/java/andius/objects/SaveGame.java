package andius.objects;

import andius.Constants;
import static andius.Constants.LEVEL_PROGRESSION_TABLE;
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

    public CharacterRecord[] players = new CharacterRecord[1];
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
        return gson.fromJson(json, SaveGame.class);
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
        public int health;
        public int exp;
        public int gold;
        public int level;
        public ArmorType armor = ArmorType.NONE;
        public WeaponType weapon = WeaponType.NONE;
        public int submorsels = 400;
        public HashMap<WeaponType, Integer> weapons = new HashMap<>();
        public HashMap<ArmorType, Integer> armors = new HashMap<>();

        public void awardXP(int value) {
            exp = Utils.adjustValueMax(exp, value, 9999);
        }

        public int getMaxHP() {
            int hp = this.classType.getStartHP();
            if (this.level > 0) {
                hp += this.level * this.classType.getIncrHP();
            }
            return hp;
        }

        public void adjustGold(int v) {
            gold = Utils.adjustValue(gold, v, 1000000, 0);
        }

        public int checkLevel() {

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

    }
}
