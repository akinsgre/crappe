import java.io.*;
import java.util.Iterator;
import java.util.*;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.security.MessageDigest;

public final class jalopy extends TimerTask
{
  public static void main(String args[])
  {
    Timer timer = new Timer();
    TimerTask atask = new jalopy();

    // make 3 entries to check for
    // file => hash
    filelist.put("cat", new String("cathash"));
    filelist.put("dog", new String("doghash"));
    filelist.put("mouse", new String("mousehash"));

    System.out.println("Starting..");
    timer.schedule(atask, delay, period);
  }

  public void run()
  {
    String md5sum;

    try {
      for (Map.Entry<String, String> entry : filelist.entrySet()) {
        md5sum = getMD5Checksum(entry.getKey());
        if(md5sum.equals(entry.getValue())) {
          System.out.println("No change");
        } else {
          System.out.println("Changed!");
          reruntest();
          filelist.put(entry.getKey(), md5sum);
        }
        //System.out.println(entry.getKey() + " => " + entry.getValue());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("checking...");
  }

   public static byte[] createChecksum(String filename) throws
       Exception
   {
     InputStream fis =  new FileInputStream(filename);

     byte[] buffer = new byte[1024];
     MessageDigest complete = MessageDigest.getInstance("MD5");
     int numRead;
     do {
      numRead = fis.read(buffer);
      if (numRead > 0) {
        complete.update(buffer, 0, numRead);
        }
      } while (numRead != -1);
     fis.close();
     return complete.digest();
   }

    public static void reruntest() {
      // need to export our classpath before running this..
      String cmd = "javac *.java && java org.junit.runner.JUnitCore Testhello";
      Runtime run = Runtime.getRuntime();
      
      try {
        System.out.println("got here");
        Process pr = run.exec(cmd);
        pr.waitFor();
        BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line = "";
       
        System.out.println("get here"); 
        while ((line=buf.readLine())!=null) {
          System.out.println(line);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

      System.out.println("running tests again....");
    }

   public static String getMD5Checksum(String filename) throws Exception {
     byte[] b = createChecksum(filename);
     String result = "";
     for (int i=0; i < b.length; i++) {
       result +=
          Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
      }
     return result;
   }

  public static Map<String, String> filelist = new HashMap<String, String>();
  private static long delay = 1000;
  private static long period = 1000;
}
