package andius.dialogs;

import andius.BaseScreen;
import andius.Context;
import andius.WizardryData.Scenario;
import static andius.WizardryData.getMessage;
import andius.objects.Item;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class KadortoDialog extends Dialog {

    public enum ConversationState {
        INITIAL(getMessage(Scenario.WER.messages(), 220).getText()) {
            @Override
            public ConversationState next(Context ctx, String input) {
                Item holyLimpWrist = ctx.partyHasItem(113, 4);
                if (holyLimpWrist != null) {
                    return DIALKO_CASTED;
                } else {
                    return PRIEST_LAUGH;
                }
            }
        },
        PRIEST_LAUGH(getMessage(Scenario.WER.messages(), 221).getText()) {
            @Override
            public ConversationState next(Context ctx, String input) {
                return null;
            }
        },
        DIALKO_CASTED(getMessage(Scenario.WER.messages(), 222).getText()) {
            @Override
            public ConversationState next(Context ctx, String input) {
                if (ctx.players()[0].glove != null && ctx.players()[0].glove.id == 112) {
                    return CATCH_AMULET_GLOVE;
                } else {
                    return CATCH_AMULET_BARE_HAND;
                }
            }
        },
        CATCH_AMULET_BARE_HAND(getMessage(Scenario.WER.messages(), 223).getText()) {
            @Override
            public ConversationState next(Context ctx, String input) {
                ctx.players()[0].adjustHP(-ctx.players()[0].maxhp);
                return null;
            }
        },
        CATCH_AMULET_GLOVE(getMessage(Scenario.WER.messages(), 224).getText()) {
            @Override
            public ConversationState next(Context ctx, String input) {
                if (ctx.players()[0].weapon != null && ctx.players()[0].weapon.id == 7) {
                    return GREEN_SWORD_BATTLE;
                } else if (ctx.players()[0].weapon != null && ctx.players()[0].weapon.id == 8) {
                    return BLUE_SWORD_BATTLE;
                } else if (ctx.players()[0].weapon != null && ctx.players()[0].weapon.id == 9) {
                    return CLAW_SWORD_BATTLE;
                } else if (ctx.players()[0].weapon != null && ctx.players()[0].weapon.id == 15) {
                    return DAGGER_LIGHT_BATTLE;
                } else {
                    return NO_WEAPONS;
                }
            }
        },
        NO_WEAPONS(getMessage(Scenario.WER.messages(), 225).getText()) {
            @Override
            public ConversationState next(Context ctx, String input) {
                return null;
            }
        },
        GREEN_SWORD_BATTLE(getMessage(Scenario.WER.messages(), 226).getText()) {
            @Override
            public ConversationState next(Context ctx, String input) {
                return HAVE_YOU_FORGOTTEN_SOMETHING;
            }
        },
        BLUE_SWORD_BATTLE(getMessage(Scenario.WER.messages(), 227).getText()) {
            @Override
            public ConversationState next(Context ctx, String input) {
                return HAVE_YOU_FORGOTTEN_SOMETHING;
            }
        },
        CLAW_SWORD_BATTLE(getMessage(Scenario.WER.messages(), 228).getText()) {
            @Override
            public ConversationState next(Context ctx, String input) {
                return HAVE_YOU_FORGOTTEN_SOMETHING;
            }
        },
        DAGGER_LIGHT_BATTLE(getMessage(Scenario.WER.messages(), 229).getText()) {
            @Override
            public ConversationState next(Context ctx, String input) {
                return HAVE_YOU_FORGOTTEN_SOMETHING;
            }
        },
        HAVE_YOU_FORGOTTEN_SOMETHING(getMessage(Scenario.WER.messages(), 230).getText()) {
            @Override
            public ConversationState next(Context ctx, String input) {
                return CONGRATULATIONS;
            }
        },
        CONGRATULATIONS(getMessage(Scenario.WER.messages(), 231).getText()) {
            @Override
            public ConversationState next(Context ctx, String input) {
                return null;
            }
        };

        private String text;

        private ConversationState(String text) {
            this.text = text;
        }

        public abstract ConversationState next(Context ctx, String input);
    }

    private ConversationState state;

    public KadortoDialog(Context ctx, BaseScreen screen) {
        super(ctx, screen);

        this.state = ConversationState.INITIAL;

        scrollPane.add(state.text, Color.WHITE);

        input.setTextFieldListener((TextField tf, char key) -> {
            state = state.next(ctx, "");
            
            scrollPane.add(" ");
            
            if (state == null) {
                hide();
            } else {
                scrollPane.add(state.text, Color.WHITE);
            }
        });

    }

}
