package andius.objects;

import andius.Constants;
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
        public int lastLeveledUpLevel = 0;
        public Status status = Status.GOOD;
        public int str;
        public int dex;
        public int intell;
        public int wis;
        public ClassType race = ClassType.HUMAN;
        public Profession profession = Profession.FIGHTER;
        public int mana;
        public int health;
        public int exp;
        public int gold;
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
            dos.writeByte(lastLeveledUpLevel);
            dos.writeByte(status.ordinal());

            dos.writeByte(str);
            dos.writeByte(dex);
            dos.writeByte(intell);
            dos.writeByte(wis);

            dos.writeByte(race.ordinal());
            dos.writeByte(profession.ordinal());
            dos.writeShort(mana);

            dos.writeShort(health);
            dos.writeShort(0);

            dos.writeInt(gold);
            dos.writeInt(exp);
            dos.writeByte(armor.ordinal());
            dos.writeByte(weapon.ordinal());
            dos.writeByte(0);
            dos.writeByte(0);
            dos.writeInt(0);

            for (ArmorType t : ArmorType.values()) {
                dos.writeByte(armors[t.ordinal()]);
            }

            dos.writeInt(0);
            dos.writeInt(0);

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
            lastLeveledUpLevel = dis.readByte() & 0xff;
            status = Status.values()[dis.readByte() & 0xff];

            str = dis.readByte() & 0xff;
            dex = dis.readByte() & 0xff;
            intell = dis.readByte() & 0xff;
            wis = dis.readByte() & 0xff;

            race = ClassType.values()[dis.readByte() & 0xff];
            profession = Profession.values()[dis.readByte() & 0xff];
            mana = dis.readShort();
            health = dis.readShort();
            dis.readShort();

            gold = dis.readInt();
            exp = dis.readInt();
            armor = ArmorType.get(dis.readByte() & 0xff);
            weapon = WeaponType.get(dis.readByte() & 0xff);

            dis.readByte();
            dis.readByte();
            dis.readInt();

            for (ArmorType t : ArmorType.values()) {
                armors[t.ordinal()] = dis.readByte() & 0xff;
            }

            dis.readInt();
            dis.readInt();

            for (WeaponType t : WeaponType.values()) {
                weapons[t.ordinal()] = dis.readByte() & 0xff;
            }

        }

        public int getMaxMana() {
            int maxMana = 0;
            switch (profession) {
                case FIGHTER:
                case THIEF:
                    break;
                case RANGER:
                    maxMana = wis / 2 > intell / 2 ? intell / 2 : wis / 2;
                    break;
                case WIZARD:
                    maxMana = intell;
                    break;
                case CLERIC:
                    maxMana = wis;
                    break;
                case WITCHER:
                    maxMana = wis / 2;
                    break;
                default:

            }
            return maxMana;
        }

        public int getLevel() {
            int lvl = lastLeveledUpLevel + 1;
            return lvl;
        }
        
        public void awardXP(int value) {
            exp = Utils.adjustValueMax(exp, value, 9999);
        }

        public int getMaxHealth() {
            return getLevel() * 100 + 50;
        }

        public void adjustMagic(int pts) {
            mana = Utils.adjustValueMax(mana, pts, getMaxMana());
        }

        public void adjustGold(int v) {
            gold = Utils.adjustValue(gold, v, 99999, 0);
        }

        public boolean levelUp() {

            if (getLevel() >= 25) {
                return false;
            }

            int expLvl = exp / 100;

            if (expLvl <= lastLeveledUpLevel) {
                return false;
            }

            int times = expLvl - lastLeveledUpLevel;
            lastLeveledUpLevel = expLvl;

            status = Status.GOOD;
            health = getMaxHealth();

            for (int i = 0; i < times; i++) {
                /* improve stats by 1-8 each */
                str += rand.nextInt(8) + 1;
                dex += rand.nextInt(8) + 1;
                intell += rand.nextInt(8) + 1;
                wis += rand.nextInt(8) + 1;

                if (str > this.race.getMaxStr()) {
                    str = this.race.getMaxStr();
                }
                if (dex > this.race.getMaxDex()) {
                    dex = this.race.getMaxDex();
                }
                if (intell > this.race.getMaxInt()) {
                    intell = this.race.getMaxInt();
                }
                if (wis > this.race.getMaxWis()) {
                    wis = this.race.getMaxWis();
                }
            }
            return true;

        }

    }
}
