package org.acme;


public class RobotCommand {
    public String command;
    public String parameter;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    @Override
    public String toString() {
        return "RobotCommand [command=" + command + ", parameter=" + parameter + "]";
    }
    
}
