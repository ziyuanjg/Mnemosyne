import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import org.junit.Test;

/**
 * Created by 希罗 on 2018/4/26
 */
public class ioTest {



    @Test
    public void t(){

        thread t1 = new thread("11111111111111");
        thread t2 = new thread("22222222222222");
        thread t3 = new thread("33333333333333");

        t1.start();
        t2.start();
        t3.start();


    }


    class thread extends Thread{

        private String s;
        thread(String s){
            this.s = s;
        }
        @Override
        public void run() {
            for(int i = 0; i<100; i++){
                ioTest(s);
                if(i == 99)
                    System.out.println(i);
            }
        }
    }

    public void ioTest(String s){

        File file = new File("/Users/xiluo/test.txt");
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try(RandomAccessFile rf = new RandomAccessFile(file, "rw"); FileChannel fileChannel = rf.getChannel();) {

            rf.seek(rf.length());
            rf.write(s.getBytes());
            rf.write("\n".getBytes());
        } catch (Exception e){
            System.out.println(e);
        }
    }
}
