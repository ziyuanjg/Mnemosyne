import static org.junit.Assert.assertEquals;

import com.mnemosyne.common.MnemosyneProperties;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

/**
 * Created by Mr.Luo on 2018/7/3
 */
public class MnemosyneTest {

    @Test
    public void mainTest() {

        MnemosyneProperties mnemosyne = new MnemosyneProperties();
        mnemosyne.initContext();
        mnemosyne.start();
    }

//    @Override
//    protected Application configure() {
//        return new ResourceConfig(MainTest.class);
//    }

//    @Test
//    public void jersey() {
//
//        final String responseMsg = target().path("preload/pre").request().get(String.class);
//        System.out.println(responseMsg);
//
//        assertEquals("ok", responseMsg);
//    }
}
