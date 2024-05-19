package de.swiftbyte.test;

import de.swiftbyte.jdaboot.annotation.interactions.ButtonDefinition;
import de.swiftbyte.jdaboot.interactions.buttons.ButtonExecutor;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

@ButtonDefinition(
        label = "Test",
        emoji = "\uD83D\uDC4D",
        url = "https://hufeisen-games.de",
        type = ButtonDefinition.Type.LINK
)
public class TestButton implements ButtonExecutor {

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        System.out.println("Button clicked!");
    }

}