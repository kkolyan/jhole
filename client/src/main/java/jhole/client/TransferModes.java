package jhole.client;

import jhole.streamcoding.TransferMode;

public class TransferModes {
    private TransferModes() {
    }

    public static TransferMode getCurrent() {
        String transferMode = System.getProperty("jhole.client.transferMode", "text");
        for (TransferMode mode: TransferMode.values()) {
            if (mode.name().equalsIgnoreCase(transferMode)) {
                return mode;
            }
        }
        throw new IllegalStateException("unsupported transferMode: "+transferMode);
    }
}
