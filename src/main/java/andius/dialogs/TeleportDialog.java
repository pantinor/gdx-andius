package andius.dialogs;

import andius.BaseScreen;
import andius.Context;
import andius.GameScreen;
import andius.OverworldScreen;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TeleportDialog extends Dialog {

    public TeleportDialog(Context ctx, BaseScreen screen) {
        super(ctx, screen);

        Vector3 v = new Vector3();
        screen.getCurrentMapCoords(v);

        if (screen instanceof OverworldScreen || screen instanceof GameScreen) {
            scrollPane.add("Enter the destination coordinates one wishes to travel as (North, East).");
            scrollPane.add("Current coordinates are");
            scrollPane.add(String.format("North [%d]", (int) v.y));
            scrollPane.add(String.format("East [%d]", (int) v.x));
        } else {
            scrollPane.add("Enter the destination coordinates one wishes to travel as (Level, North, East).");
            scrollPane.add("Current coordinates are");
            scrollPane.add(String.format("Level [%d]", (int) v.z + 1));
            scrollPane.add(String.format("North [%d]", (int) v.x));
            scrollPane.add(String.format("East [%d]", (int) v.y));
        }

        input.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField tf, char key) {
                if (key == '\r') {

                    if (tf.getText().length() == 0) {
                        hide();
                        return;
                    }

                    String coordinates = tf.getText().trim();

                    if (screen instanceof OverworldScreen || screen instanceof GameScreen) {
                        String regex = "^\\d+,\\d+$";
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(coordinates);

                        if (matcher.matches()) {
                            String[] coords = coordinates.split(",");
                            int northsouth = Integer.parseInt(coords[0]);
                            int eastwest = Integer.parseInt(coords[1]);
                            hide();
                            screen.teleport(0, northsouth, eastwest);
                        } else {
                            scrollPane.add("Nothing is happening");
                        }
                    } else {
                        String regex = "^\\d+,\\d+,\\d+$";
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(coordinates);

                        if (matcher.matches()) {
                            String[] coords = coordinates.split(",");
                            int vertical = Integer.parseInt(coords[0]);
                            int northsouth = Integer.parseInt(coords[1]);
                            int eastwest = Integer.parseInt(coords[2]);
                            hide();
                            screen.teleport(vertical, northsouth, eastwest);
                        } else {
                            scrollPane.add("Nothing is happening");
                        }
                    }
                }
            }
        });

    }

}
