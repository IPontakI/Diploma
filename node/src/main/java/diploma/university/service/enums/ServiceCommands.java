package diploma.university.service.enums;

public enum ServiceCommands {
    HELP("/help"),
    REGISTRATION("/registration"),
    LOG_IN("/log_in"),
    CANCEL("/cancel"),
    START("/start");
    private final String value;

    ServiceCommands(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
    public static ServiceCommands fromValue(String v){
        for (ServiceCommands c: ServiceCommands.values()){
            if (c.value.equals(v)){
                return c;
            }
        }
        return null;
    }

}
