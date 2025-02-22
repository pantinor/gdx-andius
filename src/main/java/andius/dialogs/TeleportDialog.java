package andius.dialogs;

import andius.BaseScreen;
import andius.Context;
import andius.objects.Sound;
import andius.objects.Sounds;
import andius.WizardryData.Scenario;
import static andius.WizardryData.getMessage;
import andius.objects.Dialog;
import andius.objects.Item;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TeleportDialog extends Dialog {

    public TeleportDialog(Context ctx, BaseScreen screen) {
        super(ctx, screen);

        scrollPane.add("Enter the destination coordinates one wishes to travel as (Level, North, East).");
        Vector3 v = new Vector3();
        screen.getCurrentMapCoords(v);
        scrollPane.add("Current coordinates are");
        scrollPane.add(String.format("Level [%d]", (int) v.z));
        scrollPane.add(String.format("North [%d]", (int) v.x));
        scrollPane.add(String.format("East [%d]", (int) v.y));

        input.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField tf, char key) {
                if (key == '\r') {

                    if (tf.getText().length() == 0) {
                        hide();
                        return;
                    }

                    String coordinates = tf.getText().trim();

                    String regex = "^-?\\d+,-?\\d+,-?\\d+$";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(coordinates);

                    if (matcher.matches()) {
                        String[] coords = coordinates.split(",");
                        int vertical = Integer.parseInt(coords[0]);
                        int northsouth = Integer.parseInt(coords[1]);
                        int eastwest = Integer.parseInt(coords[2]);
                        screen.teleport(vertical, northsouth, eastwest);
                        hide();
                        input.setTextFieldListener(null);
                    } else {
                        scrollPane.add("Nothing is happening");
                    }
                }
            }
        });

    }

}
