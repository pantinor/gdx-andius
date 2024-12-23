/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius;

import andius.Constants.Status;
import andius.objects.Aura;
import andius.objects.Item;
import andius.objects.SaveGame;
import andius.objects.SaveGame.CharacterRecord;
import java.util.ArrayList;
import java.util.List;
import utils.PartyDeathException;
import utils.Utils;

public class Context {

    public final Aura activeSpellEffect = new Aura();
    public SaveGame saveGame;

    public void setSaveGame(SaveGame sg) {
        this.saveGame = sg;
    }

    public CharacterRecord[] players() {
        return this.saveGame.players;
    }

    public CharacterRecord pickRandomEnabledPlayer() {
        List<CharacterRecord> enabled = new ArrayList<>();
        for (CharacterRecord cr : this.saveGame.players) {
            if (!cr.isDisabled()) {
                enabled.add(cr);
            }
        }
        if (enabled.isEmpty()) {
            return null;
        }
        int pick = Utils.RANDOM.nextInt(enabled.size());
        return enabled.get(pick);
    }

    public CharacterRecord getOwner(Item item) {
        for (int i = 0; i < this.saveGame.players.length && item != null; i++) {
            if (this.saveGame.players[i].inventory.contains(item)) {
                return players()[i];
            }
        }
        return null;
    }

    public void endTurn(Constants.Map map) throws PartyDeathException {
        int decr_interval = (map == Constants.Map.WORLD ? 40 : 10);
        for (CharacterRecord player : this.saveGame.players) {
            if (!player.isDead()) {
                player.submorsels -= decr_interval;
                if (player.submorsels < 0) {
                    player.submorsels = 400;

                    player.decrementStatusEffects();

                    if (player.status.has(Status.POISONED)) {
                        player.adjustHP(-1);
                    }
                }
            }
        }
    }

}
