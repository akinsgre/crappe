import java.io.*;
import java.util.Iterator;
import java.util.*;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.security.MessageDigest;

public final class jalopy extends TimerTask
{
  public static boolean VERBOSE = false;
  public static String VERSION = "0.1";

  public static void main(String args[])
  {
    Timer timer = new Timer();
    TimerTask atask = new jalopy();

    for(String s: args) {
      if(s.equals("-version")) {
        System.out.println("Jalopy Version " + VERSION);
        System.exit(0);
      } else if(s.equals("--verbose")) {
        VERBOSE = true;
        System.out.println("Running in Verbose Mode");
      } else if(s.equals("--help")) {
        System.out.println("java ./jalopy [OPTIONS]\n" +
                           "--help\t this help screen\n" +
                           "--verbose\t verbose mode\n" +
                           "--version\t Display version\n\n");
        System.exit(0);
      }
    }

    grabfiles();
    timer.schedule(atask, delay, period);
  }

  //prototype for a list of files to test in CWD
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

          if(filename.indexOf(".java") > 1) {
            if(VERBOSE) {
              System.out.println("Tracking:" + filename);
            }
            filelist.put(filename, new String("filehash"));
          }
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
      String CP = "/home/cyn0n/junit-4.5.jar";
      String cmd = "javac -cp " + CP + " *.java && java -cp " + CP + ":. org.junit.runner.JUnitCore";
      Runtime run = Runtime.getRuntime();
      String rfile = "";

      try {
        rfile = file.replace(".java", "");
        
        if(rfile.indexOf("Test") == 0) {
          cmd += " " + rfile;
        } else {
          cmd += " Test" + rfile;
        }

        if(VERBOSE) {
          System.out.println("Running: " + cmd);
        }
        Process pr = run.exec(cmd);
        
        BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line = "";

        System.out.println("get here"); 
        while ((line=buf.readLine())!=null) {
          System.out.println("should get something");
          System.out.println(line);
        }
        pr.waitFor();
      } catch (Exception e) {
        e.printStackTrace();
      }

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
