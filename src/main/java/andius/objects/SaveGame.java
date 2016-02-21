package andius.objects;

import andius.Constants;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
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

        for (int i = 0; i < 4; i++) {
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

        map = dis.readByte();
        wx = dis.readByte();
        wy = dis.readByte();
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

        for (int i = 0; i < 4; i++) {
            players[i] = new CharacterRecord();
            players[i].read(dis);
        }

        dis.close();
    }

    public static class CharacterRecord {

        public String name = null;
        public int portaitIndex = 11 + 2 * 16;
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
            dos.writeInt(gold);
            
            dos.writeShort(exp);
            dos.writeByte(armor.ordinal());
            dos.writeByte(weapon.ordinal());

            for (ArmorType t : ArmorType.values()) {
                if (t == ArmorType.NONE) {
                    continue;
                }
                dos.writeByte(armors[t.ordinal()]);
            }

            for (WeaponType t : WeaponType.values()) {
                if (t == WeaponType.NONE) {
                    continue;
                }
                dos.writeByte(weapons[t.ordinal()]);
            }

            dos.writeShort(portaitIndex);

            dos.writeByte(0);
            dos.writeByte(0);

            dos.writeInt(0);

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

            lastLeveledUpLevel = dis.readByte();
            status = Status.values()[dis.readByte()];
            str = dis.readByte();
            dex = dis.readByte();
            intell = dis.readByte();
            wis = dis.readByte();
            race = ClassType.values()[dis.readByte()];
            profession = Profession.values()[dis.readByte()];
            mana = dis.readShort();
            health = dis.readShort();
            gold = dis.readInt();
            
            exp = dis.readShort();
            armor = ArmorType.get(dis.readByte());
            weapon = WeaponType.get(dis.readByte());

            for (ArmorType t : ArmorType.values()) {
                armors[t.ordinal()] = dis.readByte();
            }

            for (WeaponType t : WeaponType.values()) {
                weapons[t.ordinal()] = dis.readByte();
            }

            portaitIndex = dis.readShort();

            dis.readByte();
            dis.readByte();
            dis.readInt();

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

        public int getMaxHealth() {
            return getLevel() * 100 + 50;
        }

        public void adjustMagic(int pts) {
            //mana = Utils.adjustValueMax(mana, pts, getMaxMana());
        }

        public void adjustGold(int v) {
            //gold = Utils.adjustValue(gold, v, 99999, 0);
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
