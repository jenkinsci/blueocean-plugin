package io.jenkins.blueocean.service.embedded.redirect;

public class InterfaceOption {
    public static final InterfaceOption classic = new InterfaceOption("classic", "Jenkins Classic");
    public static final InterfaceOption blueocean = new InterfaceOption("blueocean", "BlueOcean");

    private String interfaceId;
    private String name;

    public InterfaceOption(String interfaceId, String name) {
        this.interfaceId = interfaceId;
        this.name = name;
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public String getName() {
        return name;
    }
}
