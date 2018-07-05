import static org.junit.Assert.assertEquals;

import com.mnemosyne.MainTest;
import com.mnemosyne.Mnemosyne;
import com.mnemosyne.slave.SlaveNodeService;
import javax.ws.rs.core.Application;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

/**
 * Created by 希罗 on 2018/7/3
 */
public class MnemosyneTest  extends JerseyTest{

    @Test
    public void mainTest(){

        Mnemosyne mnemosyne = new Mnemosyne();
        mnemosyne.initContext();
        mnemosyne.start();
    }


    @Override
    protected Application configure() {
        return new ResourceConfig(MainTest.class);
    }

    @Test
    public void jersey(){

        final String responseMsg = target().path("preload/pre").request().get(String.class);
        System.out.println(responseMsg);

        assertEquals("ok", responseMsg);
    }
}
