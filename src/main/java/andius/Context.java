/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius;

import static andius.Constants.SAVE_FILENAME;
import andius.objects.Aura;
import andius.objects.SaveGame;
import andius.objects.SaveGame.CharacterRecord;
import utils.PartyDeathException;

public class Context {

    public final Aura aura = new Aura();
    public SaveGame saveGame;

    public Context() {
        try {
            this.saveGame = SaveGame.read(SAVE_FILENAME);
        } catch (Exception e) {
        }
    }
    
    public CharacterRecord[] players() {
        return this.saveGame.players;
    }

    public void endTurn(Constants.Map map, CharacterRecord player) throws PartyDeathException {
        int decr_interval = (map == Constants.Map.WORLD ? 40 : 10);
        if (player.status != Constants.Status.DEAD) {
            player.submorsels -= decr_interval;
            if (player.submorsels < 0) {
                player.submorsels = 400;
                if (player.status == Constants.Status.POISONED) {
                    //applyDamage(1, false);
                } else {
                    //player.hp = Utils.adjustValue(player.hp, 1, player.getMaxHP(), 0);
                }

            }
        }

    }

}
