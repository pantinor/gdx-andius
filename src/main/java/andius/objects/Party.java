/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius.objects;

import andius.ArmorType;
import andius.Constants.HealType;
import andius.Constants.Map;
import andius.Constants.Status;
import andius.WeaponType;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.utils.Array;
import java.util.Random;
import utils.PartyDeathException;
import utils.Utils;
import utils.XORShiftRandom;

/**
 *
 * @author Paul
 */
public class Party {
    
    public final Array<Player> players = new Array<>();

    public class Player {

        private final CharacterRecord player;
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

        public CharacterRecord rec() {
            return player;
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
                    int v = player.weapons.get(player.weapon);
                    player.weapons.put(player.weapon, v++);
                }
                player.weapon = WeaponType.NONE;
                return true;
            }

            //check if they are already wearing it
            if (player.weapon.ordinal() == i) {
                return true;
            }

            //check if it is in the inventory
            if (player.weapons.get(WeaponType.values()[i]) <= 0) {
                return false;
            }

            //check if they can wear it
            WeaponType wt = WeaponType.get(i);
            if (!wt.canUse(player.classType)) {
                return false;
            }

            //take off the old and put it in inventory
            if (player.weapon.ordinal() != 0) {
                int v = player.weapons.get(player.weapon);
                player.weapons.put(player.weapon, v++);
            }

            player.weapon = wt;
            int v = player.weapons.get(wt);
            player.weapons.put(wt, v--);

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
                    int v = player.armors.get(player.armor);
                    player.armors.put(player.armor, v++);
                }
                player.armor = ArmorType.NONE;
                return true;
            }

            //check if they are already wearing it
            if (player.armor.ordinal() == i) {
                return true;
            }

            //check if it is in the inventory
            if (player.armors.get(ArmorType.values()[i]) <= 0) {
                return false;
            }

            //check if they can wear it
            ArmorType at = ArmorType.get(i);
            if (!at.canUse(player.classType)) {
                return false;
            }

            //take off the old and put it in inventory
            if (player.armor.ordinal() != 0) {
                int v = player.armors.get(player.armor);
                player.armors.put(player.armor, v++);
            }

            player.armor = at;
            int v = player.armors.get(at);
            player.armors.put(at, v--);

            return true;
        }

        public boolean applyDamage(int damage, boolean combatRelatedDamage) throws PartyDeathException {
            int newHp = player.hp;

            if (isDead()) {
                return false;
            }

            newHp -= damage;

            if (newHp < 0) {
                player.status = Status.DEAD;
                newHp = 0;
            }

            player.hp = newHp;

            if (!combatRelatedDamage && isDead()) {
                throw new PartyDeathException();
            }

            return true;
        }

        public int getAC() {
            return player.armor.getAC();
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
                        //player.hp = Utils.adjustValue(player.hp, 1, player.getMaxHP(), 0);
                    }

                }
            }

        }
    }

}
