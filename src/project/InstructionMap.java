package project;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class InstructionMap {
	public static Set<String> sourceCodes = new TreeSet<>();
	public static Map<String, Integer> opcode = new TreeMap<>();
	public static Map<Integer, String> mnemonics = new TreeMap<>();
	public static Set<String> noArgument = new TreeSet<>();
	public static Set<String> indirectOK= new TreeSet<>();

	static {

		sourceCodes.add("NOP");
		sourceCodes.add("LOD");
		sourceCodes.add("LODI");
		sourceCodes.add("STO");
		sourceCodes.add("ADD");
		sourceCodes.add("ADDI");
		sourceCodes.add("SUB");
		sourceCodes.add("SUBI");
		sourceCodes.add("MUL");
		sourceCodes.add("MULI");
		sourceCodes.add("DIV");
		sourceCodes.add("DIVI");
		sourceCodes.add("AND");
		sourceCodes.add("ANDI");
		sourceCodes.add("NOT");
		sourceCodes.add("CMPL");
		sourceCodes.add("CMPZ");
		sourceCodes.add("JUMP");
		sourceCodes.add("JUMPI");
		sourceCodes.add("JUMPA");
		sourceCodes.add("JMPZ");
		sourceCodes.add("JMPZI");
		sourceCodes.add("JMPZA");
		sourceCodes.add("HALT");
		
		//add the other source code names that allow indirect forms (11 including LOD)
		indirectOK.add("LOD");
		indirectOK.add("STO");
		indirectOK.add("ADD");
		indirectOK.add("SUB");
		indirectOK.add("MUL");
		indirectOK.add("DIV");
		indirectOK.add("AND");
		indirectOK.add("CMPL"); //8
		indirectOK.add("CMPZ");
		indirectOK.add("JUMP");
		indirectOK.add("JMPZ");
		
		//add the other source code names that do not take arguments (3 including HALT)
		noArgument.add("HALT");
		noArgument.add("NOP");
		noArgument.add("NOT");
		
		//add all the other instructions given in Project Part 1 and the mapping to their
		//opcode, which is the number of the instruction. Note ADDI maps to 0x3, the same
		//as ADD--similarly for the other instructions ending in I
		//JUMPA maps to 0xB and JMPZ maps to 0xC (the same as JUMP and JMPZ)
		opcode.put("NOP", 0x0);
		opcode.put("LOD", 0x1);
		opcode.put("LODI", 0x1);
		opcode.put("STO", 0x2);
		opcode.put("ADD", 0x3);
		opcode.put("ADDI", 0x3);
		opcode.put("SUB", 0x4);
		opcode.put("SUBI", 0x4);
		opcode.put("MUL", 0x5);
		opcode.put("MULI", 0x5);
		opcode.put("DIV", 0x6);
		opcode.put("DIVI", 0x6);
		opcode.put("AND", 0x7);
		opcode.put("ANDI", 0x7);
		opcode.put("NOT", 0x8);
		opcode.put("CMPL", 0x9);
		opcode.put("CMPZ", 0xA);
		opcode.put("JUMP", 0xB);
		opcode.put("JUMPI", 0xB);
		opcode.put("JUMPA", 0xB);
		opcode.put("JMPZ", 0xC);
		opcode.put("JMPZI", 0XC);
		opcode.put("JMPZA", 0xC);
		opcode.put("HALT", 0xF);

		//add one entry for 0x1 through 0xC and 0xF. Where there are multiple possibilities
		//use the basic mnemonic, e.g. mnemonics.put(0x3, "ADD"), 
		//mnemonics.put(0xB, "JUMP")
		//Here and in opcode, it is OK if you prefer to use 0,1,...,12 and 15 in place of 
		//0x0, 0x1, ..., 0xC, and 0x
		mnemonics.put(0x0, "NOP");
		mnemonics.put(0x1, "LOD");
		mnemonics.put(0x2,  "STO");
		mnemonics.put(0x3, "ADD");
		//mnemonics.put(0x3, "ADDI");
		mnemonics.put(0x4, "SUB");
		//mnemonics.put(0x4, "SUBI");
		mnemonics.put(0x5, "MUL");
		//mnemonics.put(0x5, "MULI");
		mnemonics.put(0x6, "DIV");
		//mnemonics.put(0x6, "DIVI");
		mnemonics.put(0x7, "AND");
		//mnemonics.put(0x7, "ANDI");
		mnemonics.put(0x8, "NOT");
		mnemonics.put(0x9, "CMPL");
		mnemonics.put(0xA, "CMPZ");
		mnemonics.put(0xB, "JUMP");
		mnemonics.put(0xC, "JMPZ");
		mnemonics.put(0xF, "HALT");
		
		  
	}
}
