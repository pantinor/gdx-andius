/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius.objects;

import andius.Constants.Map;
import andius.Constants.Status;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.utils.Array;
import java.util.Random;
import utils.PartyDeathException;
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

        public CharacterRecord rec() {
            return player;
        }

        public boolean isDead() {
            return player.status == Status.DEAD;
        }

        public boolean isDisabled() {
            return player.status == Status.GOOD || player.status == Status.POISONED;
        }

        public void endTurn(Map map) throws PartyDeathException {
            int decr_interval = (map == Map.WORLD ? 40 : 10);
            if (player.status != Status.DEAD) {
                player.submorsels -= decr_interval;
                if (player.submorsels < 0) {
                    player.submorsels = 400;
                    if (player.status == Status.POISONED) {
                        //applyDamage(1, false);
                    } else {
                        //player.hp = Utils.adjustValue(player.hp, 1, player.getMaxHP(), 0);
                    }

                }
            }

        }
    }

}
