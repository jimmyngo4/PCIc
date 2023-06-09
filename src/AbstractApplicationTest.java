import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AbstractApplicationTest {

    private final Logger logger = Logger.getLogger(AbstractApplication.class.getName());
    private final LoggerTestingHandler handler = new LoggerTestingHandler();
    @Before
    public void setup() {
        logger.addHandler(handler);
    }

    @Test
    public void connectToPort() {
        Mock.MockDevice device = new Mock.MockDevice(1, false);
        Mock.MockApplication application1 = new Mock.MockApplication(device);
        Mock.MockApplication application2 = new Mock.MockApplication(device);

        assertTrue(application1.connectToPort(1));
        assertFalse(application2.connectToPort(1));
        assertTrue(application2.connectToPort(2));
        assertFalse(application2.connectToPort(3));
    }

    @Test
    public void connectedToAPort() {
        Mock.MockDevice device = new Mock.MockDevice(1, false);
        Mock.MockApplication application1 = new Mock.MockApplication(device);

        assertFalse(application1.connectedToAPort());

        application1.connectToPort(1);

        assertTrue(application1.connectedToAPort());
    }

    @Test
    public void device() {
        Mock.MockDevice device = new Mock.MockDevice(1, false);
        Mock.MockApplication application = new Mock.MockApplication(device);

        assertEquals(application.device(), device);
    }

    @Test
    public void sendMessage() {
        Mock.MockDevice device = new Mock.MockDevice(1, false);
        Mock.MockApplication application = new Mock.MockApplication(device);
        Message message = new Message(1, 1, "100");
        handler.clearLogRecords();

        assertThrows(NullPointerException.class, () -> application.sendMessage(null));

        application.sendMessage(message);
        assertTrue(handler.getLastLog().orElse("").contains("messages cannot be received"));

        application.connectToPort(1);
        // will fail because the device isn't connected to a motherboard
        assertFalse(application.sendMessage(message));
    }

    @Test
    public void sendBroadcastMessage() {
        handler.clearLogRecords();
        String binary = "100";
        String notBinary = "not binary";
        Mock.MockDevice device = new Mock.MockDevice(1, false);
        Mock.MockApplication app = new Mock.MockApplication(device);

        assertThrows(NullPointerException.class, () -> app.sendBroadcastMessage(null));

        assertFalse(app.sendBroadcastMessage(notBinary));
        assertTrue(handler.getLastLog().orElse("").contains("payload is not in the correct format (binary string)"));

        // will fail because the device isn't connected to a motherboard
        assertFalse(app.sendBroadcastMessage(binary));
        assertTrue(handler.getLastLog().orElse("").contains("messages cannot be received"));

        app.connectToPort(3);
        device.setMotherboard(new Motherboard());
        assertTrue(app.sendBroadcastMessage(binary));
    }
}