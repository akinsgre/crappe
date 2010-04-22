import java.io.*;
import java.util.Iterator;
import java.util.*;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.security.MessageDigest;
import java.lang.reflect.Method;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result ; 
import java.net.URLClassLoader;
import java.net.URL;


public final class crappe extends TimerTask
{
    public static boolean VERBOSE = false;
    public static String VERSION = "0.1";
    public static String JUNIT  = "/home/cyn0n/";
    

    public static Map<String, String> filelist = new HashMap<String, String>();
    private static long delay = 1000;
    private static long period = 1000;

    public static void main(String args[])
    {
	Timer timer = new Timer();
	TimerTask atask = new crappe();
	
	for(String s: args) {
	    if(s.equals("--version")) {
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
	String scandir = System.getProperty("scandir");
	if (VERBOSE) System.out.println("Scanning directory " + scandir);
	
	File dir = new File(scandir);
	
	String[] children = dir.list();
	if (children == null) {
	    // Either dir does not exist or is not a directory
	} else {
	    for (int i=0; i<children.length; i++) {
		String filename = children[i];
		
		//ignore swap files
		if((filename.indexOf(".java") > 1) && (filename.indexOf(".swp") == -1)){
		    filename = scandir + "/"  + filename ;
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
	String classname = "";
	String rfile = "";
	//compile
	try {
	    boolean successful = compile(file);
	    if (!successful) System.out.println("There was a problem compiling " + file);
	}
	catch(Exception ex) {
	    System.out.println("Caught an exception while compiling  " + 
			       file + ".\n" + ex.getMessage());
	    ex.printStackTrace();
	}

	//run tests
	rfile = file.replace(".java", "");
	rfile = rfile.replace(System.getProperty("scandir") + "/", "");
	System.out.println("RFile = " + rfile);
	if(rfile.indexOf("Test") == 0) {

	    try {
		if (VERBOSE) System.out.println("Going to test " + rfile);
		// Create a File object on the root of the directory containing the class file 
		File afile = new File(System.getProperty("scandir"));
		try { 
		    // Convert File to a URL 
		    URL url = afile.toURI().toURL(); 
		    URL[] urls = new URL[]{url}; 
		    // Create a new class loader with the directory 
		    cl = new URLClassLoader(urls); 
		    Class cls = cl.loadClass(rfile); 
		    runTest(cls);
		} 
		catch (Exception e) {
		    e.printStackTrace(); 
		} 

	    }
	    catch (Exception ex) {
		ex.printStackTrace();
	    }
	}
    }
    static ClassLoader cl ; 
    public static boolean compile(String filename) throws Exception
    {
	// Use reflection to be able to build on all JDKs >= 1.1:
	System.out.println("Compiling " + filename);
        try {
            Class<?> c = Class.forName ("com.sun.tools.javac.Main");
            Object compiler = c.newInstance ();
            Method compile = c.getMethod ("compile",
                 new Class [] {(new String [] {}).getClass ()});
            int result = ((Integer) compile.invoke
			  (compiler, new Object[] {new String[] {filename} }))
                 .intValue ();
	    if (VERBOSE) System.out.println("Compiling " + filename + " returned " + result);
            return result == 0;
        } catch (Exception ex) {
	    throw ex;
	}

    }
    public static void runTest(Class testClass) {
    	Runtime run = Runtime.getRuntime();
    	String colore = "";
    	String colorb = "";
    	String line = "Test Value";
    	try {

    	    if(VERBOSE) {
    		System.out.println("Running Tests in : " + testClass);
    	    }

	    colorb = "";
	    colore = "";

	    JUnitCore core = new JUnitCore();
	    Result result = core.run(testClass);

	    if(result.wasSuccessful()) {
		colorb = "\033[1m\033[32m";
		colore = "\033[0m";
	    }
	    else {
		colorb = "\033[1m\033[31m";
		colore = "\033[0m";
	    }

	    System.out.println(colorb + line + colore);


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


}
