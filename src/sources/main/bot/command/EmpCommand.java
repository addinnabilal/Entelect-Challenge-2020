package main.bot.command;

public class EmpCommand implements Command {

    @Override
    public String render() {
        return String.format("USE_EMP");
    }
}
