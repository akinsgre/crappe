import java.io.*;
import java.util.Iterator;
import java.util.*;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.security.MessageDigest;

public final class crappe extends TimerTask
{
  public static boolean VERBOSE = false;
  public static String VERSION = "0.1";
  public static String JUNIT  = "/home/cyn0n/";

  public static void main(String args[])
  {
    Timer timer = new Timer();
    TimerTask atask = new crappe();

    for(String s: args) {
      if(s.equals("-version")) {
        System.out.println("crappe version " + VERSION);
        System.exit(0);
      } else if(s.equals("--verbose")) {
        VERBOSE = true;
        System.out.println("Running in Verbose Mode");
      } else if(s.equals("--help")) {
        System.out.println("java ./crappe [OPTIONS]\n" +
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

          //ignore swap files
          if((filename.indexOf(".java") > 1) && (filename.indexOf(".swp") == -1)){
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
      String CP = JUNIT + "junit-4.5.jar";
      String cmd1 = "javac -cp " + CP + " " + file;
      String cmd2 = "java -cp " + CP + ":. org.junit.runner.JUnitCore";
      String rfile = "";

      //compile
      runcmd(cmd1);

      //run tests
      rfile = file.replace(".java", "");
      if(rfile.indexOf("Test") == 0) {
        cmd2 += " " + rfile;
      } else {
        cmd2 += " Test" + rfile;
      }
 
      runcmd(cmd2);
    }

    public static void runcmd(String cmd) {
      Runtime run = Runtime.getRuntime();
      String colore = "";
      String colorb = "";

      try {

        if(VERBOSE) {
          System.out.println("Running: " + cmd);
        }
        Process pr = run.exec(cmd);
        pr.waitFor();

        //stderr
        //BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

        //stdout 
        BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line = "";

        while ((line=buf.readLine())!=null) {

          colorb = "";
          colore = "";
          if(line.indexOf("OK") >= 0) {
            colorb = "\033[1m\033[32m";
            colore = "\033[0m";
          }

          if((line.indexOf("failure") >= 0) || 
              (line.indexOf("FAILURE") >=0)) {
            colorb = "\033[1m\033[31m";
            colore = "\033[0m";
          }

        System.out.println(colorb + line + colore);
        }

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
