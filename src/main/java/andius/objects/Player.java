/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius.objects;

import andius.Constants.ArmorType;
import andius.Constants.HealType;
import andius.Constants.Map;
import andius.Constants.Status;
import andius.Constants.WeaponType;
import andius.objects.SaveGame.CharacterRecord;
import java.util.Random;
import utils.PartyDeathException;
import utils.Utils;
import utils.XORShiftRandom;

/**
 *
 * @author Paul
 */
public class Player {

    private final CharacterRecord player;
    public boolean usedFreeSpellInCombat;
    public final Random rand = new XORShiftRandom();

    public Player(CharacterRecord p) {
        this.player = p;
    }

    public int getDamage() {
        int maxDamage = player.weapon.getDmax();
        maxDamage += player.str * 1.5;
        if (maxDamage > 255) {
            maxDamage = 255;
        }
        return Utils.getRandomBetween(player.weapon.getDmin(), maxDamage);
    }

    public boolean heal(HealType type) {
        switch (type) {

            case NONE:
                return true;

            case CURE:
                if (player.status != Status.POISONED) {
                    return false;
                }
                player.status = Status.GOOD;
                break;

            case FULLHEAL:
                if (player.status == Status.DEAD || player.health == player.getMaxHealth()) {
                    return false;
                }
                player.health = player.getMaxHealth();
                break;

            case RESURRECT:
                if (player.status != Status.DEAD) {
                    return false;
                }
                player.health = 1;
                player.status = Status.GOOD;
                break;

            case HEAL:
                if (player.status == Status.DEAD || player.health == player.getMaxHealth()) {
                    return false;
                }
                player.health += 75 + (rand.nextInt(256) % 25);
                break;

            default:
                return false;
        }

        if (player.health > player.getMaxHealth()) {
            player.health = player.getMaxHealth();
        }

        return true;
    }

    public CharacterRecord getPlayer() {
        return player;
    }

    public Creature nearestOpponent(int dist, boolean ranged) {
        return null;
    }

    public boolean isDead() {
        return player.status == Status.DEAD;
    }

    public boolean isDisabled() {
        return player.status == Status.GOOD || player.status == Status.POISONED;
    }

    public boolean readyWeapon(int i) {

        if (i >= 16) {
            return false;
        }

        //check if they are going bare hands
        if (i == 0) {
            //take off the old and put it in inventory
            if (player.weapon.ordinal() != 0) {
                player.weapons[player.weapon.ordinal()]++;
            }
            player.weapon = WeaponType.NONE;
            return true;
        }

        //check if they are already wearing it
        if (player.weapon.ordinal() == i) {
            return true;
        }

        //check if it is in the inventory
        if (player.weapons[i] <= 0) {
            return false;
        }

        //check if they can wear it
        WeaponType wt = WeaponType.get(i);
        if (!wt.canUse(player.profession)) {
            return false;
        }

        //take off the old and put it in inventory
        if (player.weapon.ordinal() != 0) {
            player.weapons[player.weapon.ordinal()]++;
        }

        player.weapon = wt;
        player.weapons[i]--;
        return true;
    }

    public boolean wearArmor(int i) {

        if (i >= 8) {
            return false;
        }

        //check if they are going naked
        if (i == 0) {
            //take off the old and put it in inventory
            if (player.armor.ordinal() != 0) {
                player.armors[player.armor.ordinal()]++;
            }
            player.armor = ArmorType.NONE;
            return true;
        }

        //check if they are already wearing it
        if (player.armor.ordinal() == i) {
            return true;
        }

        //check if it is in the inventory
        if (player.armors[i] <= 0) {
            return false;
        }

        //check if they can wear it
        ArmorType at = ArmorType.get(i);
        if (!at.canUse(player.profession)) {
            return false;
        }

        //take off the old and put it in inventory
        if (player.armor.ordinal() != 0) {
            player.armors[player.armor.ordinal()]++;
        }

        player.armor = at;
        player.armors[i]--;
        return true;
    }

    public boolean applyDamage(int damage, boolean combatRelatedDamage) throws PartyDeathException {
        int newHp = player.health;

        if (isDead()) {
            return false;
        }

        newHp -= damage;

        if (newHp < 0) {
            player.status = Status.DEAD;
            newHp = 0;
        }

        player.health = newHp;

        if (!combatRelatedDamage && isDead()) {
            throw new PartyDeathException();
        }

        return true;
    }

    public int getAttackBonus() {
        if (player.dex >= 40) {
            return 255;
        }
        return player.dex;
    }

    public int getDefense() {
        return player.armor.getDefense();
    }

    public void endTurn(Map map) throws PartyDeathException {
        int decr_interval = (map == Map.WORLD ? 40 : 10);
        if (player.status != Status.DEAD) {
            player.submorsels -= decr_interval;
            if (player.submorsels < 0) {
                player.submorsels = 400;
                if (player.status == Status.POISONED) {
                    applyDamage(1, false);
                } else {
                    player.health = Utils.adjustValue(player.health, 1, player.getMaxHealth(), 0);
                }
                if (!isDisabled() && player.mana < player.getMaxMana()) {
                    player.mana++;
                }
            }
        }

    }

}
