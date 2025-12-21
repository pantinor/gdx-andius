/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius;

import andius.Constants.Status;
import static andius.Constants.Status.AFRAID;
import static andius.Constants.Status.ASHES;
import static andius.Constants.Status.ASLEEP;
import static andius.Constants.Status.PARALYZED;
import static andius.Constants.Status.POISONED;
import static andius.Constants.Status.SILENCED;
import static andius.Constants.Status.STONED;
import andius.objects.Aura;
import andius.objects.Dice;
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

    public boolean allDead() {
        for (CharacterRecord cr : this.saveGame.players) {
            if (!cr.isDead()) {
                return false;
            }
        }
        return true;
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

    public void poolGold(CharacterRecord player) {
        for (CharacterRecord cr : this.saveGame.players) {
            if (cr != player) {
                int gold = cr.gold;
                cr.adjustGold(-gold);
                player.adjustGold(gold);
            }
        }
    }

    public void damageGroup(Dice dice) {
        for (CharacterRecord cr : this.saveGame.players) {
            int dmg = dice.roll();
            cr.adjustHP(-dmg);
        }
    }

    public boolean partyHasItem(Item item) {
        for (CharacterRecord rec : this.saveGame.players) {
            if (rec.itemOwned(item)) {
                return true;
            }
        }
        return false;
    }

    public Item partyHasItem(int id, int scenarioID) {
        for (CharacterRecord rec : this.saveGame.players) {
            Item it = rec.itemOwned(id, scenarioID);
            if (it != null) {
                return it;
            }
        }
        return null;
    }

    public Item partyHasAnyOfTheseItems(List<Integer> items, int scenarioID) {
        for (int id : items) {
            for (CharacterRecord rec : this.saveGame.players) {
                Item it = rec.itemOwned(id, scenarioID);
                if (it != null) {
                    return it;
                }
            }
        }
        return null;
    }

    public void removeItemFromParty(int id, int scenarioID) {
        for (CharacterRecord rec : this.saveGame.players) {
            rec.removeItem(id, scenarioID);
        }
    }

    public void endTurn(Constants.Map map) throws PartyDeathException {
        int decr_interval = (map == Constants.Map.WORLD? 50 : 20);
        for (CharacterRecord player : this.saveGame.players) {
            if (!player.isDead()) {
                player.submorsels -= decr_interval;
                if (player.submorsels < 0) {
                    player.submorsels = 100;

                    player.decrementStatusEffects();

                    if (player.status.has(Status.POISONED)) {
                        player.adjustHP(-1);
                    }

                    player.adjustHP(player.regenerationPoints());
                }
            }
        }
        if (pickRandomEnabledPlayer() == null) {
            throw new PartyDeathException();
        }
    }

    public void endTurn() {
        for (CharacterRecord player : this.saveGame.players) {
            if (!player.isDead()) {

                player.adjustHP(player.regenerationPoints());

                for (Status s : Status.values()) {
                    int roll = Utils.RANDOM.nextInt(100);
                    boolean decr = false;
                    switch (s) {
                        case AFRAID:
                            decr = roll < Math.max(player.level * 10, 50);
                            break;
                        case SILENCED:
                        case ASLEEP:
                            decr = roll < Math.max(player.level * 20, 50);
                            break;
                        case POISONED:
                        case PARALYZED:
                            decr = roll < Math.max(player.level * 7, 50);
                            break;
                        case STONED:
                            break;
                        case ASHES:
                            break;

                    }
                    if (decr) {
                        player.status.decrement(s);
                    }

                    if (player.status.has(Status.POISONED)) {
                        player.adjustHP(-1);
                    }
                }
            }
        }
    }

}
