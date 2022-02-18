package main.bot.command;

public class BoostCommand implements Command {

    @Override
    public String render() {
        return String.format("USE_BOOST");
    }
}
