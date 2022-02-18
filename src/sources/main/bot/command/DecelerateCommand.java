package main.bot.command;

public class DecelerateCommand implements Command {

    @Override
    public String render() {
        return String.format("DECELERATE");
    }
}
