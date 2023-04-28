import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Device {

    private static final Logger logger = Logger.getLogger(Device.class.getName());

    private int identifier;
    private final Map<Integer, Application> portMapping = new HashMap<>();
    private final Map<Application, Integer> appMapping = new HashMap<>();
    private boolean receiveBroadcast;
    private Motherboard motherboard = null;

    protected Device(int identifier, boolean receiveBroadcast) {
        this.identifier = identifier;
        this.receiveBroadcast = receiveBroadcast;
    }

    /**
     * @param message the message to be sent
     * @return whether this device is connected to a motherboard and can therefore send the message up
     */
    protected boolean sendMessage(Message message) {
        Objects.requireNonNull(message);
        if (!connectedToMotherboard()) {
            logger.log(Level.WARNING, "couldn't send message from device with ID %d because it is not connected to a motherboard".formatted(identifier));
            return false;
        }
        return motherboard.sendMessage(message);
    }

    /**
     * @param payload the contents of the broadcast message
     * @return whether this message was successfully broadcast
     */
    protected boolean sendBroadcastMessage(String payload) {
        Objects.requireNonNull(payload);
        if (!Message.binaryString(payload)) {
            logger.log(Level.WARNING, "payload is not in the correct format (binary string)");
            return false;
        }
        if (!connectedToMotherboard()) {
            logger.log(Level.WARNING, "couldn't send message from device with ID %d because it is not connected to a motherboard".formatted(identifier));
            return false;
        }
        return motherboard.sendBroadcastMessage(payload);
    }

    /**
     * @param message the message to be received
     * @return whether an application is on the specified port and the message can be delivered there
     */
    protected boolean receiveMessage(Message message) {
        Objects.requireNonNull(message);
        if (!portMapping.containsKey(message.port())) {
            logger.log(Level.WARNING, "no application is listening on port %d for device with ID %d to deliver the message to".formatted(message.port(), message.recipient()));
            return false;
        }
        portMapping.get(message.port()).receiveMessage(message);
        return true;
    }

    protected abstract void receiveBroadcastMessage(String payload);

    protected int identifier() {
        return identifier;
    }

    /**
     * @param identifier the new identifier for this device
     * @return whether this device can change its identifier to the given one;
     *   if this device isn't connected to a motherboard, it can freely change its identifier
     *   if the motherboard it's connected to does not have a device with the given identifier, change it
     *   otherwise, return false and exit early
     */
    protected boolean setIdentifier(int identifier) {
        if (connectedToMotherboard()) {
            if (motherboard.hasDeviceWithID(identifier))
                return false;
            motherboard.removeDevice(this.identifier);
            this.identifier = identifier;
            motherboard.addDevice(this);
        }
        this.identifier = identifier;
        return true;
    }

    protected Map<Integer, Application> portMapping() {
        return Map.copyOf(portMapping);
    }

    protected Map<Application, Integer> appMapping() { return Map.copyOf(appMapping); }

    protected boolean isApplicationConnected(Application application) {
        return appMapping.containsKey(application);
    }

    /**
     * @param port the port number to add this application on for this device
     * @param application the application itself
     * @return whether this application was successfully added
     */
    protected boolean addApplication(int port, Application application) {
        Objects.requireNonNull(application);
        if (portMapping.containsKey(port)) {
            logger.log(Level.WARNING, "application %s couldn't be connected to port %d on device with ID %d because the port is already taken by application %s".formatted(application, port, identifier, portMapping.get(port)));
            return false;
        }
        if (appMapping.containsKey(application)) {
            logger.log(Level.WARNING, "this application %s is already connected to port %d so it was not connected to given port %d".formatted(application, appMapping.get(application), port));
            return false;
        }
        portMapping.put(port, application);
        appMapping.put(application, port);
        return true;
    }

    /**
     * @param port the port number the application to be removed is at
     * @return whether an application exists at that port and was removed successfully
     */
    protected boolean removeApplication(int port) {
        if (!portMapping.containsKey(port))
            return false;
        appMapping.remove(portMapping.get(port));
        portMapping.remove(port);
        return true;
    }

    protected boolean receiveBroadcast() {
        return receiveBroadcast;
    }

    protected void setReceiveBroadcast(boolean receiveBroadcast) {
        this.receiveBroadcast = receiveBroadcast;
    }

    protected boolean connectedToMotherboard() {
        return motherboard != null;
    }

    /**
     * @param motherboard the motherboard to connect this device to
     * @return whether this device was successfully connected to the given motherboard
     */
    protected boolean setMotherboard(Motherboard motherboard) {
        Objects.requireNonNull(motherboard);
        if (motherboard.hasDeviceWithID(this.identifier))
            return false;
        this.motherboard = motherboard;
        motherboard.addDevice(this);
        return true;
    }
}
