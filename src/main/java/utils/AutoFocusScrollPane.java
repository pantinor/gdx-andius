package utils;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class AutoFocusScrollPane extends ScrollPane {

    public AutoFocusScrollPane(Actor widget, Skin skin) {
        super(widget, skin);

        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Stage s = getStage();
                if (s != null) {
                    s.setScrollFocus(AutoFocusScrollPane.this);
                }
                return false; // don't steal the click from the List
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                Stage s = getStage();
                if (s != null) {
                    s.setScrollFocus(AutoFocusScrollPane.this);
                }
            }
        });
    }
}
