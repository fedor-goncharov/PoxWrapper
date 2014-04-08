package ru.mail.fedka2005.tests;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import ru.mail.fedka2005.objects.ControllerWrapper;

public class ControllerWrapperTest {
	@Test public void testControllerWrapper() {
		try {
			Thread client1 = new Thread(new ControllerWrapper("test", "",
					"client1",1, "", 1));
			Thread client2 = new Thread(new ControllerWrapper("test", "",
					"client2",2, "", 1));
			client1.start();
			client2.start();
			
			assertEquals(true,true);
		} catch (Exception e) {
			assertEquals(true, false);
		}
		//TODO
		//add some tests here
	}

}
