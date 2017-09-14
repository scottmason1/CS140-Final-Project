package project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Assembler2 {

	public static void assemble(File input, File output, ArrayList<String> errors){
		Map<String, Integer> lineCount = new TreeMap();
		ArrayList<String> code = new ArrayList<>();
		ArrayList<String> data = new ArrayList<>();
		ArrayList<String> inText = new ArrayList<>();
		boolean lineFlag = false;
		boolean emptyLineFlag = false;

		int k = 0;
		int emptyLineInt = 0;
		int dashCount = 0;
		try {
			Scanner scanInput = new Scanner(input);
			while(scanInput.hasNextLine()){
				String line = scanInput.nextLine();
				k++;

				//System.out.println(k + line);

				if(emptyLineFlag & line.trim().length() == 0){
					continue;
				}

				if(line.trim().length() == 0){
					emptyLineFlag = true;
					emptyLineInt = k;
				}

				if(emptyLineFlag){
					if(line.trim().length() > 0 && emptyLineFlag){
						errors.add("Error: line " + emptyLineInt + " is an illegal blank line");
						emptyLineFlag = false;
					}
				}

				if(!emptyLineFlag){
					if(line.startsWith(" ") || line.charAt(0) == '\t'){
						errors.add("Error: line " + k + " starts with illegal white space");
					}

					if(line.trim().toUpperCase().startsWith("--")){
						if(line.trim().replace("-","").length() != 0){
							errors.add("Error: line " + k + " dashed line has non-dashes");
							lineFlag = true;
						}
					}
					if(line.trim().startsWith("-")){
						dashCount++;
					}
					//					if(line.trim().toUpperCase().startsWith("-") && line.length() == 1){
					//						errors.add("Error: line " + k + " has an erroneous data seperator");
					//					}

					if(line.trim().toUpperCase().startsWith("--") && lineFlag){
						errors.add("Error: line " + k + " has a duplicate data separator");
						
					}

					inText.add(line);
					lineCount.put(line, k);
				}
			}

			scanInput.close();

		} catch (FileNotFoundException e1) {
			errors.add("Input file does not exist");
		}
		//System.out.println("InText" + inText.toString());
		boolean listFlag = false;
		for(int i = 0; i < inText.size(); i++){
			if(inText.get(i).startsWith("--")){
				listFlag = true;
				String[] parts = inText.get(i).trim().split("\\s+");
				if(parts.length == 2 && parts[0].startsWith("-")){
					int arg;
					
					//int lineNum = ++i;
					try {
						arg = Integer.parseInt(parts[0],16);
					}

					catch (NumberFormatException e) {
						errors.add("Error: line " + lineCount.get(inText.get(i))
								+ " data address not a hex number");
					}

					try {
						arg = Integer.parseInt(parts[1],16);
					}

					catch (NumberFormatException e) {
						errors.add("Error: line " + lineCount.get(inText.get(i))
								+ " data value not a hex number");
					}

				}
				continue;
			}
			if(!listFlag){
				code.add(inText.get(i));
			}
			else{
				data.add(inText.get(i));
			}
		}
		//System.out.println(code.toString());
		//System.out.println(data.toString());

		ArrayList<String> outText = new ArrayList<>();
		int count = 0;

		for(int i = 0; i < code.size(); i++){
			String line = code.get(i);
			String[] parts = line.trim().split("\\s+");
			int opcode = 0;
			count = i;

			//System.out.println(Arrays.toString(parts));
			if(InstructionMap.sourceCodes.contains(parts[0].toUpperCase()) && !InstructionMap.sourceCodes.contains(parts[0])){
				errors.add("Error: line " + lineCount.get(inText.get(i)) + " does not have the instruction mnemonic in upper case");
			}
			else if(InstructionMap.noArgument.contains(parts[0]) && parts.length != 1){
				errors.add("Error: line " + lineCount.get(inText.get(i)) + " this mnemonic does not take arguments");
			}
			if(!InstructionMap.noArgument.contains(parts[0]) && parts.length != 2){
				if(parts.length == 1 && !parts[0].startsWith("-")){
					errors.add("Error: line " + lineCount.get(inText.get(i)) + " is missing an argument");
				}
				if(parts.length >= 3){
					errors.add("Error: line " + lineCount.get(inText.get(i)) + " has more than one argument");
				}
			}
			if(!InstructionMap.sourceCodes.contains(parts[0])){ // && !InstructionMap.opcode.containsKey(parts[0].toUpperCase())
				errors.add("Error: line " + lineCount.get(inText.get(i)) + " contains an illegal mnemonic");
				
			}
			else if(line.trim().length() > 0){
				opcode = InstructionMap.opcode.get(parts[0]);
			}
			
			if(parts.length == 2){
				int level = 1;

				if(parts[1].startsWith("[")){
					if(!InstructionMap.indirectOK.contains(parts[0])){
						errors.add("Error: line " + lineCount.get(inText.get(i)) + " contains an invalid argument");
					}
					else if(InstructionMap.indirectOK.contains(parts[0]) && !parts[1].endsWith("]")){
						errors.add("Error: line " + lineCount.get(inText.get(i)) + " : this argument is missing closing \"]\"");
					}
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

				int arg;

				try {
					arg = Integer.parseInt(parts[1],16);
				} catch (NumberFormatException e) {
					errors.add("Error: line " + lineCount.get(inText.get(i))
							+ " is not a hex number");
				}

				String addInt = Integer.toHexString(opcode).toUpperCase() + " " + level + " " + parts[1];
				outText.add(addInt);
			}

			if(parts.length == 1){
				String addInt = Integer.toHexString(opcode).toUpperCase() + " 0 0";

				int arg;

				outText.add(addInt);
			}
		}

		for(int i = 0; i < data.size(); i++){
			String[] parts = data.get(i).trim().split("\\s+");
			
			//System.out.println(inText.get(i + count + dashCount));
			//System.out.println(Arrays.toString(parts));
			//System.out.println(lineCount.toString();

			int arg1 = lineCount.get(inText.get(i + count + dashCount));
			arg1 +=1;
			
			for(int f = 1; f < dashCount; f++){
				arg1 -= 1;
			}

			int arg = 0; 

			if(parts.length == 1){
				errors.add("Error : line " + arg1 + " data format does not consist of two numbers");
				continue;
			}

			try {
				arg = Integer.parseInt(parts[0],16);
			}

			catch (NumberFormatException e) {
				errors.add("Error: line " + arg1
						+ " data address not a hex number");
			}

			if(parts.length == 2){
				try {
					arg = Integer.parseInt(parts[1],16);
				}

				catch (NumberFormatException e) {
					errors.add("Error: line " + arg1
							+ " data value not a hex number");
				}
			}
		}
		
		//System.out.println(lineCount.toString());

		outText.add("-1");
		outText.addAll(data);

		if(errors.size() == 0){
			try (PrintWriter out = new PrintWriter(output)){
				for(String s : outText) out.println(s);
			} catch (FileNotFoundException e) {
				errors.add("Cannot create output file");
			}
		}
//		else{
//			//System.out.println("Errors present" + errors.toString());
//		}

	}

	//	public static void main(String[] args) {
	//		ArrayList<String> errors = new ArrayList<>();
	//		assemble(new File("in.pasm"), new File("out.pexe"), errors);
	//	}

}
