import org.junit.*;
import static org.junit.Assert.*;

public class Testcrappe {

  @Test
  public void testoutput() {
    System.out.println("new test");
  }

  @Test
  public void testBad() {
    assertEquals(1, 1);
  }

}