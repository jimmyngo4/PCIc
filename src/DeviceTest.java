import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DeviceTest extends Mock {

    private final Logger logger = Logger.getLogger(Device.class.getName());
    private final LoggerTestingHandler handler = new LoggerTestingHandler();
    @Before
    public void setup() {
        logger.addHandler(handler);
    }

    @Test
    public void sendMessage() {
        MockDevice sender = new MockDevice(1, false);
        handler.clearLogRecords();
        Message message = new Message(2, 2, "100");

        // device has not been connected to a motherboard so sending a message fails
        assertFalse(sender.sendMessage(message));
        assertTrue(handler.getLastLog().orElse("").contains("couldn't send message because device is not connected to a motherboard"));

        Motherboard motherboard = new Motherboard();
        sender.setMotherboard(motherboard);

        // motherboard does not have a device that matches the message's recipient so sending a message fails
        assertFalse(sender.sendMessage(message));

        MockDevice receiver = new MockDevice(2, false);
        receiver.setMotherboard(motherboard);

        // the receiving device does not have an application on the specified port so sending a message fails
        assertFalse(sender.sendMessage(message));

        MockApplication application = new MockApplication(receiver);
        application.connectToPort(2);

        assertTrue(sender.sendMessage(message));
    }

    @Test
    public void receiveMessage() {
        Message message = new Message(2, 2, "100");
        MockDevice device = new MockDevice(2, false);
        MockApplication application = new MockApplication(device);
        handler.clearLogRecords();

        assertFalse(device.receiveMessage(message));
        assertTrue(handler.getLastLog().orElse("").contains("no application exists for the message's port to deliver the message to"));

        application.connectToPort(2);
        assertTrue(device.receiveMessage(message));
    }

    @Test
    public void identifier() {
        MockDevice device = new MockDevice(1, false);
        assertEquals(device.identifier(), 1);
    }

    @Test
    public void setIdentifier() {
        MockDevice device = new MockDevice(1, false);
        assertTrue(device.setIdentifier(3));
        assertEquals(device.identifier(), 3);

        Motherboard motherboard = new Motherboard();
        device.setMotherboard(motherboard);
        assertFalse(device.setIdentifier(3));

        assertTrue(device.setIdentifier(5));
        assertTrue(motherboard.devices().containsKey(5));
        assertFalse(motherboard.devices().containsKey(3));
    }

    @Test
    public void portMapping() {
        MockDevice device = new MockDevice(1, false);
        MockApplication application = new MockApplication(device);

        assertEquals(device.portMapping(), new HashMap<>());
        assertThrows(UnsupportedOperationException.class, () -> {
            device.portMapping().put(2, application);
        });

        device.addApplication(2, application);
        assertEquals(device.portMapping(), Map.of(2, application));
    }

    @Test
    public void addApplication() {
        MockDevice device = new MockDevice(1, false);
        MockApplication app1 = new MockApplication(device);
        MockApplication app2 = new MockApplication(device);

        assertTrue(device.addApplication(1, app1));
        assertFalse(device.addApplication(1, app2));
        assertTrue(device.addApplication(2, app2));
    }

    @Test
    public void removeApplication() {
        MockDevice device = new MockDevice(1, false);
        MockApplication app1 = new MockApplication(device);
        MockApplication app2 = new MockApplication(device);
        device.addApplication(1, app1);
        device.addApplication(2, app2);

        assertTrue(device.removeApplication(1));
        assertFalse(device.removeApplication(1));
        assertTrue(device.removeApplication(2));
    }

    @Test
    public void receiveBroadcast() {
        MockDevice device = new MockDevice(1, true);
        assertTrue(device.receiveBroadcast());
    }

    @Test
    public void setReceiveBroadcast() {
        MockDevice device = new MockDevice(1, true);
        assertTrue(device.receiveBroadcast());
        device.setReceiveBroadcast(false);
        assertFalse(device.receiveBroadcast());
        device.setReceiveBroadcast(true);
        assertTrue(device.receiveBroadcast());
    }

    @Test
    public void connectedToMotherboard() {
        MockDevice device = new MockDevice(1, true);
        assertFalse(device.connectedToMotherboard());

        Motherboard motherboard = new Motherboard();
        device.setMotherboard(motherboard);
        assertTrue(device.connectedToMotherboard());
    }

    @Test
    public void setMotherboard() {
        Motherboard motherboard = new Motherboard();
        MockDevice device1 = new MockDevice(1, false);
        MockDevice duplicate = new MockDevice(1, true);

        assertTrue(device1.setMotherboard(motherboard));
        assertFalse(duplicate.setMotherboard(motherboard));
    }
}