import java.lang.IllegalArgumentException;
import org.junit.Test;
import org.wikimedia.morelangs.InputMethod;

public class ParserTests {

    @Test(expected = IllegalArgumentException.class)
    public void testNonExistantMethod() {
        InputMethod im = InputMethod.fromName("does-not-exist");
    }

}
