/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius;

import andius.Constants.Map;
import static andius.Constants.SAVE_FILENAME;
import andius.objects.Aura;
import andius.objects.Player;
import andius.objects.SaveGame;
import java.util.Random;
import utils.XORShiftRandom;

public class Context {

    private final Player player;
    private final Aura aura = new Aura();
    private final SaveGame saveGame = new SaveGame();
    private final Random rand = new XORShiftRandom();

    public Context() {
        try {
            this.saveGame.read(SAVE_FILENAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.player = new Player(saveGame.players[0]);

    }

    public void save(Map map, float x, float y) {
        try {
            this.saveGame.write(SAVE_FILENAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SaveGame getSaveGame() {
        return saveGame;
    }
    
    public Player getPlayer() {
        return player;
    }

    public Aura getAura() {
        return aura;
    }

}
