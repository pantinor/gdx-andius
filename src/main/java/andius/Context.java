/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius;

import static andius.Constants.SAVE_FILENAME;
import andius.objects.Aura;
import andius.objects.Party;
import andius.objects.SaveGame;

public class Context {

    public final Aura aura = new Aura();
    public SaveGame saveGame;
    public final Party party = new Party();

    public Context() {
        try {
            this.saveGame = SaveGame.read(SAVE_FILENAME);
        } catch (Exception e) {
        }
    }
}
