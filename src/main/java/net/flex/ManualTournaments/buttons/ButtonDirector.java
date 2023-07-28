package net.flex.ManualTournaments.buttons;

public class ButtonDirector {
    public Button constructButton(ButtonBuilder buttonBuilder){
        return buttonBuilder.buildButton();
    }
}

