package andius.objects;

import andius.Constants;
import static andius.Constants.LEVEL_PROGRESSION_TABLE;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import utils.Utils;
import utils.XORShiftRandom;

public class SaveGame implements Constants {

    private static final Random rand = new XORShiftRandom();
    public final CharacterRecord[] players = new CharacterRecord[1];

    public int map;
    public int wx;
    public int wy;

    public byte empty1;

    public void write(String strFilePath) throws Exception {

        FileOutputStream fos = new FileOutputStream(strFilePath);
        LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(fos);

        dos.writeByte(map);
        dos.writeByte(wx);
        dos.writeByte(wy);
        dos.writeByte(empty1);

        dos.writeByte(empty1);
        dos.writeByte(empty1);
        dos.writeByte(empty1);
        dos.writeByte(empty1);

        dos.writeByte(empty1);
        dos.writeByte(empty1);
        dos.writeByte(empty1);
        dos.writeByte(empty1);

        dos.writeByte(empty1);
        dos.writeByte(empty1);
        dos.writeByte(empty1);
        dos.writeByte(empty1);

        for (int i = 0; i < 1; i++) {
            if (players[i] == null) {
                players[i] = new CharacterRecord();
            }
            players[i].write(dos);
        }

        dos.close();

    }

    public void read(String strFilePath) throws Exception {
        InputStream is;
        LittleEndianDataInputStream dis = null;
        try {
            is = new FileInputStream(strFilePath);
            dis = new LittleEndianDataInputStream(is);
        } catch (Exception e) {
            throw new Exception("Cannot read save file");
        }
        read(dis);
    }

    public void read(LittleEndianDataInputStream dis) throws Exception {

        map = dis.readByte() & 0xff;
        wx = dis.readByte() & 0xff;
        wy = dis.readByte() & 0xff;
        empty1 = dis.readByte();

        empty1 = dis.readByte();
        empty1 = dis.readByte();
        empty1 = dis.readByte();
        empty1 = dis.readByte();

        empty1 = dis.readByte();
        empty1 = dis.readByte();
        empty1 = dis.readByte();
        empty1 = dis.readByte();

        empty1 = dis.readByte();
        empty1 = dis.readByte();
        empty1 = dis.readByte();
        empty1 = dis.readByte();

        for (int i = 0; i < 1; i++) {
            players[i] = new CharacterRecord();
            players[i].read(dis);
        }

        //set initial start
        if (wx == 0 && wy == 0) {
            wx = Map.WORLD.getStartX();
            wy = Map.WORLD.getStartY();
        }

        dis.close();
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

        public int[] weapons = new int[WeaponType.values().length];
        public int[] armors = new int[ArmorType.values().length];

        public void write(LittleEndianDataOutputStream dos) throws Exception {

            if (name == null || name.length() < 1) {
                for (int i = 0; i < 16; i++) {
                    dos.writeByte(0);
                }
            } else {
                String paddedName = StringUtils.rightPad(name, 16);
                byte[] nameArray = paddedName.getBytes();
                for (int i = 0; i < 16; i++) {
                    if (nameArray[i] == 32) {
                        nameArray[i] = 0;
                    }
                    dos.writeByte(nameArray[i]);
                }
            }

            dos.writeShort(portaitIndex);
            dos.writeByte(level);
            dos.writeByte(status.ordinal());

            dos.writeByte(str);
            dos.writeByte(intell);
            dos.writeByte(piety);
            dos.writeByte(vitality);
            dos.writeByte(agility);
            dos.writeByte(luck);

            dos.writeByte(race.ordinal());
            dos.writeByte(classType.ordinal());

            dos.writeShort(health);

            dos.writeInt(gold);
            dos.writeInt(exp);
            dos.writeByte(armor.ordinal());
            dos.writeByte(weapon.ordinal());

            for (ArmorType t : ArmorType.values()) {
                dos.writeByte(armors[t.ordinal()]);
            }

            for (WeaponType t : WeaponType.values()) {
                dos.writeByte(weapons[t.ordinal()]);
            }

        }

        public void read(LittleEndianDataInputStream dis) throws Exception {

            byte[] nameArray = new byte[16];
            boolean end = false;
            for (int i = 0; i < 16; i++) {
                byte b = dis.readByte();
                if (b == 0) {
                    end = true;
                };
                if (!end) {
                    nameArray[i] = b;
                }
            }
            name = new String(nameArray).trim();

            portaitIndex = dis.readShort();
            level = dis.readByte() & 0xff;
            status = Status.values()[dis.readByte() & 0xff];

            str = dis.readByte() & 0xff;
            intell = dis.readByte() & 0xff;
            piety = dis.readByte() & 0xff;
            vitality = dis.readByte() & 0xff;
            agility = dis.readByte() & 0xff;
            luck = dis.readByte() & 0xff;

            race = Race.values()[dis.readByte() & 0xff];
            classType = ClassType.values()[dis.readByte() & 0xff];
            health = dis.readShort();

            gold = dis.readInt();
            exp = dis.readInt();
            armor = ArmorType.get(dis.readByte() & 0xff);
            weapon = WeaponType.get(dis.readByte() & 0xff);

            for (ArmorType t : ArmorType.values()) {
                armors[t.ordinal()] = dis.readByte() & 0xff;
            }

            for (WeaponType t : WeaponType.values()) {
                weapons[t.ordinal()] = dis.readByte() & 0xff;
            }

        }

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

            for (int i=0;i<LEVEL_PROGRESSION_TABLE.length;i++) {
                int[] levels = LEVEL_PROGRESSION_TABLE[i];
                int thr = levels[this.classType.ordinal()];
                if (this.exp >= thr) {
                    lvl = i+1;
                }
            }
            
            if (lvl == LEVEL_PROGRESSION_TABLE.length) {
                int thr = LEVEL_PROGRESSION_TABLE[LEVEL_PROGRESSION_TABLE.length - 1][this.classType.ordinal()];
                int sum = thr * 2;
                while (this.exp > sum) {
                    lvl ++;
                    sum += thr;
                }
            }
            
            return lvl;

        }

    }
}
