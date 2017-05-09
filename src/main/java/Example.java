import java.io.IOException;

/**
 * Created by niksaz on 04/05/2017.
 */
public class Example {

    @XTest
    public void test1() throws IOException {
        throw new IOException();
    }

    @XTest
    public void test2() {

    }
}
