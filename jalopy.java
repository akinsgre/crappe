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

    grabfiles();
    System.out.println("Starting..");
    timer.schedule(atask, delay, period);
  }

  //prototype for a list of files to test in CWD
  //should eventually just build list of what we need to check
  //
  //need to exclude everything but *.java
  public static void grabfiles() {
    File dir = new File(".");
    
    String[] children = dir.list();
    if (children == null) {
        // Either dir does not exist or is not a directory
    } else {
        for (int i=0; i<children.length; i++) {
          String filename = children[i];
          System.out.println("need to chk " + filename);
          filelist.put(filename, new String("filehash"));
        }
    }
  }

  public void run()
  {
    String md5sum;

    try {
      for (Map.Entry<String, String> entry : filelist.entrySet()) {
        md5sum = getMD5Checksum(entry.getKey());
        if(md5sum.equals(entry.getValue())) {
        } else {
          System.out.println("Checking...");
          reruntest(entry.getKey());
          filelist.put(entry.getKey(), md5sum);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

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

    public static void reruntest(String file) {
      // need to export our classpath before running this..
      String cmd = "javac *.java && java org.junit.runner.JUnitCore";
      Runtime run = Runtime.getRuntime();
      
      try {
        System.out.println("running test on " + file);
        cmd += " Test" + file;
        System.out.println("running: " + cmd);
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

  // why do we have all this down here??? prob. cause I like to smoke crack
  // every now and then -- that and my cat likes to sit right in front of my
  // screen -- cunt..
  public static Map<String, String> filelist = new HashMap<String, String>();
  private static long delay = 1000;
  private static long period = 1000;
}
