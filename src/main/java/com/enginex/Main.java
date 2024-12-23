package com.enginex;

public class Main {
    public static void main(String[] args) throws Exception {
        Controller controller = new Controller();
        if (args[0].equalsIgnoreCase("-scan")) {
            controller.run(args[1]);
        } else if (args[0].equalsIgnoreCase("-delete")) {
            controller.cleanUp();
        } else {
            System.err.println("[ERROR]. Unknown operation : " + args[0]);
        }
    }
}