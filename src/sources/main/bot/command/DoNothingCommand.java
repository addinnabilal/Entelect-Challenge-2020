package main.bot.command;

public class DoNothingCommand implements Command {
    @Override
    public String render() {
        return "NOTHING";
    }
}
