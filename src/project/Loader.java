package project;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Loader {

	public static String load(MachineModel model, File file, int codeOffset, int memoryOffset){
		int codeSize = 0;

		if(model == null || file == null){
			return null;
		}

		try {
			Scanner input = new Scanner(file);
			boolean inCode = true;
			while(input.hasNextLine()){
				String line = input.nextLine();
				//System.out.println(line);
				Scanner parser = new Scanner(line);
				int opcode = parser.nextInt(16);
				//System.out.println(opcode);
				if(inCode && opcode == -1){
					inCode = false;
				}
				else if(inCode && opcode != -1){
					int indirLvl = parser.nextInt(16);
					int arg = parser.nextInt(16);
					model.setCode(codeOffset+codeSize, opcode, indirLvl, arg);
					codeSize++;
				}
				else if(!inCode){
					model.setData(opcode + memoryOffset, parser.nextInt(16));
				}
				parser.close();
			}
			input.close();
		} catch (FileNotFoundException e) {
			System.out.println("File " + file.getName() + " Not Found");
		} catch (ArrayIndexOutOfBoundsException e1){
			System.out.println("Array Index " + e1.getMessage());
		} catch (NoSuchElementException e2){
			e2.printStackTrace();
			System.out.println("From Scanner: NoSuchElementException");
		}
		return "" + codeSize;
	}
	
	public static void main(String[] args) {
		MachineModel model = new MachineModel();
		String s = Loader.load(model, new File("out.pexe"),16,32);
		for(int i = 16; i < 16+Integer.parseInt(s); i++) {
			System.out.println(model.getCode().getText(i));			
		}
		System.out.println("--");
		System.out.println("4FF " + 
			Integer.toHexString(model.getData(0x20+0x4FF)).toUpperCase());
		System.out.println("0 " + 
			Integer.toHexString(model.getData(0x20)).toUpperCase());
		System.out.println("10 -" + 
			Integer.toHexString(-model.getData(0x20+0x10)).toUpperCase());
	}
}
