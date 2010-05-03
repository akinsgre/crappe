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
import org.junit.runner.notification.Failure ; 
import java.net.URLClassLoader;
import java.net.URL;

import org.apache.commons.io.FileUtils ; 

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import javax.tools.SimpleJavaFileObject;

import java.net.URI;

public final class Crappe extends TimerTask
{
    public static boolean VERBOSE = false;
    public static String VERSION = "0.1";

    public static Map<String, String> filelist = new HashMap<String, String>();
    private static long delay = 1000;
    private static long period = 1000;
    private static ClassLoader cl ; 

    
    public static void main(String args[])
    {
	System.out.println("Classpath ");
	Timer timer = new Timer();
	TimerTask atask = new Crappe();
	
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

	try {
	    filelist = Crappe.listFilesAsMap(new File(System.getProperty("scandir")), 
						new FilenameFilter() {
						    public boolean accept(File f, String s) { 
							return s.endsWith(".java"); 
						    }
						}, 
					true);
	}
	catch (Exception ex) {
	    
	}	
	timer.schedule(atask, delay, period);
    }
    public ClassLoader createClassLoader()
    {
	ClassLoader cl = null;
	// Create a File object on the root of the directory containing the class file 
	File afile = new File(System.getProperty("scandir"));
	File jfile = new File(System.getProperty("junitdir"));
	File ifile = new File(System.getProperty("fileio"));
	File cfile = new File("/Users/gakins/Projects/jautotest/target/test-classes/");
	try { 
	    // Convert File to a URL 
	    URL scanUrl = afile.toURI().toURL(); 
	    System.out.println("Loading " + scanUrl.toString() + " into Classpath");
	    URL junitUrl = jfile.toURI().toURL(); 
	    System.out.println("Loading " + junitUrl.toString() + " into Classpath");
	    URL fileioUrl = ifile.toURI().toURL(); 
	    System.out.println("Loading " + fileioUrl.toString() + " into Classpath");
	    URL curDirUrl = cfile.toURI().toURL(); 
	    System.out.println("Loading " + curDirUrl.toString() + " into Classpath");
	    URL[] urls = new URL[]{scanUrl, junitUrl, fileioUrl, curDirUrl}; 
	    // Create a new class loader with the directory 
	    cl = new URLClassLoader(urls);		    

	   
	} 
	catch (Exception e) {
	    e.printStackTrace(); 
	}
	    return cl ; 
    }
    //need to exclude everything but *.java
    public static void grabfiles() {
	String scandir = System.getProperty("scandir");
	System.out.println("Scanning " + scandir);	
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
		    this.reruntest(entry.getKey());
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

    public  void reruntest(String file) {
	// need to export our classpath before running this..
	String classname = "";
	String rfile = "";
	//compile

	try {
	    boolean successful = this.compile(file);
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
	System.out.println("Running File = " + rfile);
	
	//TODO  There should be a better way of figuring out what the tests are..
	//but Eclipse does the same thing.. it just executes everything that matches a pattern in the project
	//Could probably do a better matcher?  Or specify the Dir instead of the pattern'

	if(rfile.contains("Test")) {
	    try {
		ClassLoader cl = this.createClassLoader();
		
		Class cls = cl.loadClass(rfile.replace("/", ".")); 
		runTest(cls);
	    }
	    catch (Exception ex) {
		ex.printStackTrace();
	    }
	}
    }
    public boolean compile(String filename) throws Exception
    {
	JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	System.out.println( "Compiling.."  + filename);
	File oldClassFile = new File(filename.replace(".java", ".class"));
	oldClassFile.delete();

	String compResult = null;

	if ( compiler!=null ) {

	    File file = new File(filename);
	    ClassLoader cl = this.createClassLoader();
	    Class cls = cl.loadClass("org.apache.commons.io.FileUtils");
	    Method mainMethod = cls.getMethod("readFileToString", new Class[]{File.class});
	    String source = (String)mainMethod.invoke(null, new Object[]{file});
	    System.out.println(source);

	    JavaSourceFromString javaString = new JavaSourceFromString(
								       filename.replace(".java", ""),
								       source);

	    ArrayList<JavaSourceFromString> al =
		new ArrayList<JavaSourceFromString>();
	    al.add( javaString );

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    OutputStreamWriter osw = new OutputStreamWriter( baos );
	    List<String> optionList = new ArrayList<String>();
	    optionList.addAll(Arrays.asList("-classpath",System.getProperty("junitdir")));
	    optionList.addAll(Arrays.asList("-d",System.getProperty("testOutDir")));

	    JavaCompiler.CompilationTask task = compiler.getTask(
								 osw,
								 null,
								 null,
								 optionList,
								 null,
								 al);

	    boolean success = task.call();
	    System.out.println("Compile output: " + baos.toString());
	    compResult = "Compiled without errors: " + success;
	    System.out.println("Result = " + compResult);
	} 
	return true ;
    }

    public static void runTest(Class testClass) {

    	String colore = "";
    	String colorb = "";
    	String line = "";
    	try {

    	    if(VERBOSE) {
    		System.out.println("Running Tests in : " + testClass);
    	    }

	    colorb = "";
	    colore = "";

	    JUnitCore core = new JUnitCore();
	    Result result = core.run(testClass);
	    
	    if(result.wasSuccessful()) {
		line = testClass.getName() + " OK";
		colorb = "\033[1m\033[32m";
		colore = "\033[0m";
	    }
	    else {
		List<Failure> failures = result.getFailures();
		line = "Tests " + result.getFailureCount() + " failed" ; 
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
    
    public static Map<String, String> listFilesAsMap(
					  File directory,
					  FilenameFilter filter,
					  boolean recurse) throws IOException
    {
	Collection<File> files = listFiles(directory,
					   filter, recurse);
	//Java4: Collection files = listFiles(directory, filter, recurse);
	Map<String, String> fileMap = new HashMap<String, String>();
	for (File file : files) {
	    System.out.println("Adding " + file.getCanonicalPath() );
	    fileMap.put( file.getCanonicalPath(), new String("filehash"));
	}
	return fileMap ; 
    }
    
    public static Collection<File> listFiles(
					     // Java4: public static Collection listFiles(
					     File directory,
					     FilenameFilter filter,
					     boolean recurse)
    {
	// List of files / directories
	Vector<File> files = new Vector<File>();
	// Java4: Vector files = new Vector();
	
	// Get files / directories in the directory
	File[] entries = directory.listFiles();
	
	// Go over entries
	for (File entry : entries)
	    {
		// Java4: for (int f = 0; f < files.length; f++) {
		// Java4: 	File entry = (File) files[f];
		
		// If there is no filter or the filter accepts the 
		// file / directory, add it to the list
		if (filter == null || filter.accept(directory, entry.getName()))
		    {
			files.add(entry);
		    }
		
		// If the file is a directory and the recurse flag
		// is set, recurse into the directory
		if (recurse && entry.isDirectory())
		    {
			files.addAll(listFiles(entry, filter, recurse));
		    }
	    }
	
	// Return collection of files
	return files;		
    }
}
/**
 * A file object used to represent source coming from a string.
 */
class JavaSourceFromString extends SimpleJavaFileObject {
    /**
     * The source code of this "file".`
     */
    final String code;
	
    /**
     * Constructs a new JavaSourceFromString.
     * @param name the name of the compilation unit represented by this file object
     * @param code the source code for the compilation unit represented by this file object
     */
    JavaSourceFromString(String name, String code) {
	super(URI.create("string:///" + name.replace('.','/') + Kind.SOURCE.extension),
	      Kind.SOURCE);
	this.code = code;
    }

    @Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) {
	return code;
    }



}