package project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FilesMgr {
	private GUIMediator gui;
	private MachineModel model;
	private String defaultDir; //this is Eclipse's default directory
	private String sourceDir; // stored directory for pasm source files
	private String executableDir; // stored directory for pexe assembled files
	private Properties properties = null; // java.util.Properties<br />
	// Java method for persistent program properties
	//private File currentlyExecutingFile = null; // java.io.File<br /> - Changed 5/11 due to instructions
	private File[] currentlyExecutingFile = new File[4];
	public FilesMgr(GUIMediator gui) {
		super();
		this.gui = gui;
		model = gui.getModel();
	}
	void initialize(){
		locateDefaultDirectory();
		loadPropertiesFile();
	}
	private void locateDefaultDirectory() {
		//CODE TO DISCOVER THE ECLIPSE DEFAULT DIRECTORY:
		File temp = new File("propertyfile.txt");
		if(!temp.exists()) {
			PrintWriter out;
			try {
				out = new PrintWriter(temp);
				out.close();
				defaultDir = temp.getAbsolutePath();
				temp.delete();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			defaultDir = temp.getAbsolutePath();
		}
		// change to forward slashes
		defaultDir = defaultDir.replace('\\','/');
		int lastSlash = defaultDir.lastIndexOf('/');
		defaultDir  = defaultDir.substring(0, lastSlash + 1);
	}
	void loadPropertiesFile() {
		try { // load properties file "propertyfile.txt", if it exists
			properties = new Properties();
			properties.load(new FileInputStream("propertyfile.txt"));
			sourceDir = properties.getProperty("SourceDirectory");
			executableDir = properties.getProperty("ExecutableDirectory");
			// CLEAN UP ANY ERRORS IN WHAT IS STORED:
			if (sourceDir == null || sourceDir.length() == 0 
					|| !new File(sourceDir).exists()) {
				sourceDir = defaultDir;
			}
			if (executableDir == null || executableDir.length() == 0 
					|| !new File(executableDir).exists()) {
				executableDir = defaultDir;
			}
		} catch (Exception e) {
			// PROPERTIES FILE DID NOT EXIST
			sourceDir = defaultDir;
			executableDir = defaultDir;
		}		
	}
	/**
	 * Translate method reads a source "pasm" file and saves the
	 * file with the extension "pexe" by collecting the input and output
	 * files and calling Assembler.assemble. If the source has errors 
	 * the error messages will be reported in a JOptionPane.  
	 */
	public void assembleFile() {
		File source = null;
		File outputExe = null;
		JFileChooser chooser = new JFileChooser(sourceDir);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Pippin Source Files", "pasm");
		chooser.setFileFilter(filter);
		// CODE TO LOAD DESIRED FILE
		int openOK = chooser.showOpenDialog(null);
		if(openOK == JFileChooser.APPROVE_OPTION) {
			source = chooser.getSelectedFile();
		}
		if(source != null && source.exists()) {
			// CODE TO REMEMBER WHICH DIRECTORY HAS THE pexe FILES
			// WHICH WE WILL ALLOW TO BE DIFFERENT
			sourceDir = source.getAbsolutePath();
			sourceDir = sourceDir.replace('\\','/');
			int lastDot = sourceDir.lastIndexOf('.');
			String outName = sourceDir.substring(0, lastDot + 1) + "pexe";			
			int lastSlash = sourceDir.lastIndexOf('/');
			sourceDir = sourceDir.substring(0, lastSlash + 1);
			outName = outName.substring(lastSlash+1); 
			filter = new FileNameExtensionFilter(
				"Pippin Executable Files", "pexe");
				if(executableDir.equals(defaultDir)) {
				chooser = new JFileChooser(sourceDir);
			} else {
				chooser = new JFileChooser(executableDir);
			}
			chooser.setFileFilter(filter);
			chooser.setSelectedFile(new File(outName));
			int saveOK = chooser.showSaveDialog(null);
			if(saveOK == JFileChooser.APPROVE_OPTION) {
				outputExe = chooser.getSelectedFile();
			}
			if(outputExe != null) {
				executableDir = outputExe.getAbsolutePath();
				executableDir = executableDir.replace('\\','/');
				lastSlash = executableDir.lastIndexOf('/');
				executableDir = executableDir.substring(0, lastSlash + 1);
				try { 
					properties.setProperty("SourceDirectory", sourceDir);
					properties.setProperty("ExecutableDirectory", executableDir);
					properties.store(new FileOutputStream("propertyfile.txt"), 
							"File locations");
				} catch (Exception e) {
					System.out.println("Error writing properties file");
				}
				ArrayList<String> errors = new ArrayList<>();
				Assembler2.assemble(source, outputExe, errors); 
				if (errors.size() == 0){
					JOptionPane.showMessageDialog(
							gui.getFrame(), 
							"The source was assembled to an executable",
							"Success",
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					Map<Integer, ArrayList<String>> errorMap = new TreeMap<>();
					for (String err : errors) {
						String part = err.replaceAll("[^\\d]",""); // remove anything that is not a digit
						Integer k = Integer.parseInt(part);
						if(!errorMap.containsKey(k)) {      // make sure there is a list in the map
							errorMap.put(k, new ArrayList<>());
						}
						errorMap.get(k).add(err);           // store the error
					}
					StringBuilder sb = new StringBuilder();
					for(Integer key : errorMap.keySet()) {  // the keys will be in increasing order
						ArrayList<String> list = errorMap.get(key);
						for(String s : list) {
							sb.append(s); 
							sb.append("\n");  
						}
					}
					JOptionPane.showMessageDialog(
							gui.getFrame(), 
							sb.toString(),
							"Source code error",
							JOptionPane.INFORMATION_MESSAGE);
				}
			} else {// outputExe Still null
				JOptionPane.showMessageDialog(
						gui.getFrame(), 
						"The output file has problems.\n" +
								"Cannot assemble the program",
								"Warning",
								JOptionPane.OK_OPTION);
			}
		} else {// source file does not exist
			JOptionPane.showMessageDialog(
					gui.getFrame(), 
					"The source file has problems.\n" +
							"Cannot assemble the program",
							"Warning",
							JOptionPane.OK_OPTION);				
		}
	}
	public void loadFile(Job job) {
		//added 5/11, added [index] to all currentlyExecutingFile Variables
		int index = job.getId();
		JFileChooser chooser = new JFileChooser(executableDir);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Pippin Executable Files", "pexe");
		chooser.setFileFilter(filter);
		// CODE TO LOAD DESIRED FILE
		int openOK = chooser.showOpenDialog(null);
		if(openOK == JFileChooser.APPROVE_OPTION) {
			currentlyExecutingFile[index] = chooser.getSelectedFile();
		}
		//added 5/11 per part 16
		if(openOK == JFileChooser.CANCEL_OPTION){
			currentlyExecutingFile[index] = null;
		}
		if(currentlyExecutingFile[index] != null && currentlyExecutingFile[index].exists()) {
			// CODE TO REMEMBER WHICH DIRECTORY HAS THE pexe FILES
			executableDir = currentlyExecutingFile[index].getAbsolutePath();
			executableDir = executableDir.replace('\\','/');
			int lastSlash = executableDir.lastIndexOf('/');
			executableDir = executableDir.substring(0, lastSlash + 1);
			try { 
				properties.setProperty("SourceDirectory", sourceDir);
				properties.setProperty("ExecutableDirectory", executableDir);
				properties.store(new FileOutputStream("propertyfile.txt"), 
						"File locations");
			} catch (Exception e) {
				System.out.println("Error writing properties file");
			}			
		}
		//finalLoad_ReloadStep(job); - 5/11 change
		if(currentlyExecutingFile[index] != null) {
			finalLoad_ReloadStep(job);
		}
		else {
			JOptionPane.showMessageDialog(gui.getFrame(), "No file Selected. \n" + "Cannot load the program", "Warning", JOptionPane.OK_OPTION);
		}
	}
	
	void finalLoad_ReloadStep(Job job) {
		gui.clearJob();
		String str = Loader.load(model, currentlyExecutingFile[job.getId()], 
				job.getStartcodeIndex(), job.getStartmemoryIndex());
		try {
			int len = Integer.parseInt(str);
			job.setCodeSize(len);
			gui.makeReady("Load Code");

		} catch (NumberFormatException e ) {
			JOptionPane.showMessageDialog(
					gui.getFrame(),  
					"The file being selected has problems.\n" +
							str + "\n" +
							"Cannot load the program",
							"Warning",
							JOptionPane.OK_OPTION);
		}
	}
	
}






