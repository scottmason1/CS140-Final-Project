package project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Assembler {

	public static void assemble(File input, File output, ArrayList<String> errors){
		ArrayList<String> code = new ArrayList<>();
		ArrayList<String> data = new ArrayList<>();

		try {
			Scanner scanInput = new Scanner(input);
			while(scanInput.hasNextLine()){
				String line = scanInput.nextLine();
				if(line.trim().startsWith("--")){
					while(scanInput.hasNextLine()){
						data.add(scanInput.nextLine());
					}
				}
				else{
					code.add(line);
				}
			}
			scanInput.close();
		} catch (FileNotFoundException e1) {
			errors.add("Input file does not exist");
		}

		//System.out.print(code.toString());
		ArrayList<String> outText = new ArrayList<>();
		for(int i = 0; i < code.size(); i++){
			String[] parts = code.get(i).trim().split("\\s+");
			//System.out.println(Arrays.toString(parts));
			int opcode = InstructionMap.opcode.get(parts[0]);
			if(parts.length == 2){
				int level = 1;

				if(parts[1].startsWith("[")){
					parts[1] = parts[1].substring(1, parts[1].length()-1);
					level = 2;
				}
				if(parts[1].startsWith("[") && parts[1].endsWith("]")){
					level = 2;
				}
				if(parts[0].endsWith("I")){
					level = 0;
				}
				if(parts[0].endsWith("A")){
					level = 3;
				}

				String addInt = Integer.toHexString(opcode).toUpperCase() + " " + level + " " + parts[1];
				outText.add(addInt);
			}
			if(parts.length == 1){
				String addInt = Integer.toHexString(opcode).toUpperCase() + " 0 0";
				outText.add(addInt);
			}
		}
		outText.add("-1");
		outText.addAll(data);
		
		try (PrintWriter out = new PrintWriter(output)){
			for(String s : outText) out.println(s);
		} catch (FileNotFoundException e) {
			errors.add("Cannot create output file");
		}
	}

	public static void main(String[] args) {
		ArrayList<String> errors = new ArrayList<>();
		assemble(new File("in.pasm"), new File("out.pexe"), errors);		
	}
}


