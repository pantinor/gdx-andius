package andius.objects;

import andius.Constants.CharacterType;
import static andius.WizardryData.WER_ITEMS;
import andius.objects.Item.ItemType;
import java.util.ArrayList;
import java.util.List;
import utils.Utils;

public class DoGooder {

    public int id;
    public String iconID;
    public String name;
    public Race race;
    public ClassType characterClass;
    public String alignment;
    public int[] items;
    public int[] partyMembers;
    public int hpCalCmd;
    public int healPts;
    public int swingCount;
    public boolean crithitm;
    public Dice hpdamrc;
    public int hpLeft;
    public int hpMax;
    public int armourClass;
    public int maxlevac;
    public int[] attributes;
    public int[] saveVs;
    public int charlev;
    public Spells[] knownSpells;
    public int[][] spellAllowance;
    public List<CharacterType> protection = new ArrayList<>();
    public List<CharacterType> purposed = new ArrayList<>();
    public String slogan;

    public boolean savingThrowBreath() {
        int roll = Utils.RANDOM.nextInt(20) + 1;
        int base = Math.max(20 - this.charlev / 5 - this.attributes[5] / 6, 1);
        int raceBonus = 0;
        if (this.race == Race.DWARF) {
            raceBonus += 4;
        }
        int classBonus = 0;
        if (this.characterClass == ClassType.NINJA) {
            classBonus += 3;
        } else if (this.characterClass == ClassType.THIEF) {
            classBonus += 3;
        }
        return roll > Math.max(base - raceBonus - classBonus, 1);
    }

    public boolean savingThrowSpell() {
        int roll = Utils.RANDOM.nextInt(20) + 1;
        int base = Math.max(20 - this.charlev / 5 - this.attributes[5] / 6, 1);
        int raceBonus = 0;
        if (this.race == Race.HOBBIT) {
            raceBonus += 3;
        }
        int classBonus = 0;
        if (this.characterClass == ClassType.MAGE) {
            classBonus += 3;
        } else if (this.characterClass == ClassType.BISHOP || this.characterClass == ClassType.SAMURAI || this.characterClass == ClassType.NINJA) {
            classBonus += 2;
        }
        return roll > Math.max(base - raceBonus - classBonus, 1);
    }

    public boolean savingThrowPetrify() {
        int roll = Utils.RANDOM.nextInt(20) + 1;
        int base = Math.max(20 - this.charlev / 5 - this.attributes[5] / 6, 1);
        int raceBonus = 0;
        if (this.race == Race.GNOME) {
            raceBonus += 2;
        }
        int classBonus = 0;
        if (this.characterClass == ClassType.PRIEST) {
            classBonus += 3;
        } else if (this.characterClass == ClassType.BISHOP || this.characterClass == ClassType.LORD || this.characterClass == ClassType.NINJA) {
            classBonus += 2;
        }
        return roll > Math.max(base - raceBonus - classBonus, 1);
    }

    public boolean savingThrowDeath() {
        int roll = Utils.RANDOM.nextInt(20) + 1;
        int base = Math.max(20 - this.charlev / 5 - this.attributes[5] / 6, 1);
        int raceBonus = 0;
        if (this.race == Race.HUMAN) {
            raceBonus += 1;
        }
        int classBonus = 0;
        if (this.characterClass == ClassType.FIGHTER || this.characterClass == ClassType.NINJA) {
            classBonus += 3;
        } else if (this.characterClass == ClassType.SAMURAI || this.characterClass == ClassType.LORD) {
            classBonus += 2;
        }
        return roll > Math.max(base - raceBonus - classBonus, 1);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i : this.items) {
            Item it = WER_ITEMS.get(i);
            if (it.type == ItemType.MISC || it.type == ItemType.SPECIAL) {
                sb.append(it.name).append(",");
            }
        }
        return "DoGooder{" + "id=" + id + ", name=" + name + ", race=" + race + ", characterClass=" + characterClass + ", items=" + sb + '}';
    }

}
