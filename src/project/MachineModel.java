package project;

import java.util.Map;
import java.util.TreeMap;

public class MachineModel {

	public final Map<Integer, Instruction> IMAP = new TreeMap();
	private	CPU cpu = new CPU();
	private Memory memory = new Memory();
	private HaltCallback callback;
	private Code code = new Code();
	private Job[] jobs = new Job[4];
	private Job currentJob;

	public MachineModel() {
		this(() -> System.exit(0));
	}

	public MachineModel(HaltCallback callback) {

		for(int i = 0; i < jobs.length; i++){
			jobs[i] = new Job();
			jobs[i].setId(i);
			jobs[i].setStartcodeIndex(i*Code.CODE_MAX/4);
			jobs[i].setStartmemoryIndex(i*Memory.DATA_SIZE/4);
			jobs[i].getCurrentState().enter();
		}

		this.callback = callback;

		currentJob = jobs[0];

		//Instruction 0 is NOP and simply increments the pCounter in the cpu. The arg and indirLvl (level) are ignored in this instruction.
		//NOP
		IMAP.put(0x0, (arg, level) -> {
			cpu.incrPC();
		});

		//Instruction 1 is LOD, which loads arg in one of the 3 modes above (offset from memBase in the direct and indirect modes) into the accumulator of the cpu. Increment pCounter. 
		//LOD
		IMAP.put(0x1, (arg, level) -> {
			if(level < 0 || level > 2) {
				throw new IllegalArgumentException(
						"Illegal indirection level in LOD instruction");
			}
			if(level > 0) {
				IMAP.get(0x1).execute(memory.getData(arg + cpu.getMemBase()), level - 1);
			}
			else{
				cpu.setAccum(arg);
				cpu.incrPC();
			}

		});

		//Instruction 2 is STO, which stores the value in the accumulator into memory at the index arg in the direct or indirect modes above (offset from memBase). 
		//Increment pCounter. Since immediate mode is not allowed, the test for IllegalArgumentException uses level < 1 instead of level < 0 
		//and the recursion stops when level == 1 rather than level == 0 as in the previous instructions (0x1 through 0x6).
		//STO
		IMAP.put(0x2, (arg, level) -> {
			if(level < 1 || level > 2) {
				throw new IllegalArgumentException(
						"Illegal indirection level in STO instruction");
			}
			if(level > 1){
				IMAP.get(0x2).execute(memory.getData(arg + cpu.getMemBase()), level -1);
			}
			else{
				memory.setData(arg + cpu.getMemBase(), cpu.getAccum());
				cpu.incrPC();
			}
		});

		/*ADD has an immediate mode (indirLvl 0), The argument arg is used directly in the operation. ADD has a direct memory access mode (indirLvl 1), 
		 where arg is the index in memory (offset from memBase) of the actual argument used in the operation. ADD has an indirect memory access mode (indirLvl 2), 
		 where arg is the index in memory (offset from memBase) of the address of the argument used in the operation. 
		 In all three cases the operation is to add the argument to the accumulator in the cpu. 
		 Write corresponding instructions for SUB, MUL, DIV, which are numbered in increasing order 0x4 through 0x6--count in hexadecimal. 
		 For the 3 division instructions, throw DivideByZeroException instead of dividing by 0.
		 */
		//ADD
		IMAP.put(0x3, (arg, level) -> {
			if(level < 0 || level > 2) {
				throw new IllegalArgumentException(
						"Illegal indirection level in ADD instruction");
			}
			if(level > 0) {
				IMAP.get(0x3).execute(memory.getData(cpu.getMemBase()+arg), level-1);
			} else {
				cpu.setAccum(cpu.getAccum() + arg);
				cpu.incrPC();
			}
		});
		//SUB
		IMAP.put(0x4, (arg, level) -> {
			if(level < 0 || level > 2) {
				throw new IllegalArgumentException(
						"Illegal indirection level in SUB instruction");
			}
			if(level > 0) {
				IMAP.get(0x4).execute(memory.getData(cpu.getMemBase() + arg), level-1);
			} else {
				cpu.setAccum(cpu.getAccum() - arg);
				cpu.incrPC();
			}

		});
		//MUL
		IMAP.put(0x5, (arg, level) -> {
			if(level < 0 || level > 2) {
				throw new IllegalArgumentException(
						"Illegal indirection level in MUL instruction");
			}
			if(level > 0) {
				IMAP.get(0x5).execute(memory.getData(cpu.getMemBase() + arg), level-1);
			} else {
				cpu.setAccum(cpu.getAccum() * arg);
				cpu.incrPC();
			}
		});
		//DIV
		IMAP.put(0x6, (arg, level) -> {
			if(arg == 0){
				throw new DivideByZeroException("Divide by zero");
			}
			else{
				if(level < 0 || level > 2) {
					throw new IllegalArgumentException(
							"Illegal indirection level in DIV instruction");
				}
				if(level > 0) {
					IMAP.get(0x6).execute(memory.getData(cpu.getMemBase() + arg), level-1);
				} else {
					cpu.setAccum(cpu.getAccum() / arg);
					cpu.incrPC();
				}
			}
		});
		/*
		 * Instruction 7 is AND: if accum is not zero and arg is not zero, change the accum to 1. In all other situations set the accum to 0. 
		 * Either way, increment pCounter. This relates to the && operation in C: non-zero values can be treated as true and 0 as false. 
		 * a && b is only true if both a and b are true. There are immediate, direct and indirect modes (the arg in the direct and indirect modes are offset with memBase) as in the ADD instruction.
		 */
		IMAP.put(0x7, (arg, level) -> {
			if(level == 0){ 
				if(cpu.getAccum() != 0 && arg != 0){
					cpu.setAccum(1);
				}
				else{
					cpu.setAccum(0);
				}
				cpu.incrPC();
			}
			else if(level == 1 || level == 2){
				if(cpu.getAccum() != 0 && memory.getData(arg + cpu.getMemBase()) != 0){
					cpu.setAccum(1);
				}
				else{
					cpu.setAccum(0);
				}
				cpu.incrPC();
			}

		});
		/*
		 * Instruction 8 is NOT: if accum is not zero in the cpu, set it to 0. 
		 * If accum is 0 set it to 1. Again this corresponds to exchanging true and false. 
		 * Increment pCounter. The arg and indirLvl (level) are ignored in this instruction.
		 */
		IMAP.put(0x8, (arg, level) -> {
			if(level != 0){
				throw new IllegalArgumentException("Illegal Arguments");
			}
			if(cpu.getAccum() != 0){
				cpu.setAccum(0);
			}
			else{
				cpu.setAccum(1);
			}
			cpu.incrPC();
		});
		/*
		 * Instruction 9 is CMPL. If the value in memory at index arg (offset from memBase) is strictly negative, set accum to 1, otherwise set it to 0.
		 * Increment pCounter. If the indicated memory value is negative this signals true in the accumulator, otherwise false. 
		 * Immediate mode is illegal, only direct and indirect modes are allowed (similar to STO).
		 */
		IMAP.put(0x9, (arg, level) -> {
			if(level < 1 || level > 2){
				throw new IllegalArgumentException("Illegal Arguments!");
			}
			if(level > 1){
				IMAP.get(0x9).execute(memory.getData(arg + cpu.getMemBase()), level - 1);
			}else{
				if(memory.getData(cpu.getMemBase() + arg) < 0){
					cpu.setAccum(1);
				}
				else{
					cpu.setAccum(0);
				}
				cpu.incrPC();
			}
		});
		/*
		 * Instruction 10 (0xA) is CMPZ. If the value in memory at index arg (offset from memBase) is zero, set accum to 1, otherwise set it to 0. 
		 * Increment pCounter. If the indicated memory value is zero this signals true in the accumulator, otherwise false. 
		 * Immediate mode is illegal, only direct and indirect modes are allowed (similar to STO).
		 */
		IMAP.put(0xA, (arg, level) -> {

			if(level < 1 || level > 2){
				throw new IllegalArgumentException("Illegal Arguments!");
			}
			if(level > 0){
				if(memory.getData(arg + cpu.getMemBase()) == 0){
					cpu.setAccum(1);
				}
				else{
					cpu.setAccum(0);
				}
				cpu.incrPC();
			}
		});
		/* 
		 * Instruction 11 (0xB) is JUMP, which does a relative jump. This means that in immediate mode arg is added to pCounter in the cpu. 
		 * Note that a negative arg allows for jumping to an earlier point in the program. There are also the direct and indirect modes where arg is read from memory as above.
		 * We will add an absolute mode (indirLvl 3) where arg is add to the starting value of pCounter--we will come back to this mode later on, after introducing more classes. 
		 * DO NOT increment the pCounter at the end of this instruction.
		 */
		IMAP.put(0xB, (arg, level) -> {
			if(level < 0 || level > 3){
				throw new IllegalArgumentException("Illegal Arguments");
			}
			if(level == 0){
				cpu.setpCounter(arg + cpu.getpCounter());
			}
			if(level == 1 || level == 2){
				IMAP.get(0xB).execute(memory.getData(arg + cpu.getMemBase()), level - 1);
				//				cpu.setpCounter(memory.getData(arg + cpu.getMemBase()) + cpu.getpCounter());
			}
			if(level == 3){
				int arg1 = memory.getData(cpu.getMemBase()+arg); 
				cpu.setpCounter(arg1 + currentJob.getStartcodeIndex());   
			}
		});
		/*
		 * Instruction 12 (0xC) is JMPZ does the same as JUMP IF accum is 0 in the cpu. Otherwise, the pCounter is incremented.
		 */
		IMAP.put(0xC, (arg, level) -> {
			if(cpu.getAccum() == 0){
				if(level == 0){
					cpu.setpCounter(arg + cpu.getpCounter());
				}
				if(level == 1 || level == 2){
					IMAP.get(0xB).execute(memory.getData(arg + cpu.getMemBase()), level - 1);
					//					cpu.setpCounter(memory.getData(arg + cpu.getMemBase()) + cpu.getpCounter());
				}
				if(level == 3){
					int arg1 = memory.getData(cpu.getMemBase()+arg); 
					cpu.setpCounter(arg1 + currentJob.getStartcodeIndex());   
				}
			}
			else{
				cpu.incrPC();
			}
		});
		//Instruction 15 (0xF) is HALT. The instruction calls halt() and does not increment the pCounter.
		IMAP.put(0xF, (arg, level) -> {
			callback.halt();
		});
		currentJob = jobs[0];
	}

	public Instruction get(Integer key){
		return IMAP.get(key);
	}

	public void incrPC() {
		cpu.incrPC();
	}

	public int getAccum() {
		return cpu.getAccum();
	}

	public int getpCounter() {
		return cpu.getpCounter();
	}

	public int getMemBase() {
		return cpu.getMemBase();
	}

	public void setAccum(int accum) {
		cpu.setAccum(accum);
	}

	public void setpCounter(int pCounter) {
		cpu.setpCounter(pCounter);
	}

	public void setMemBase(int memBase) {
		cpu.setMemBase(memBase);
	}

	public int[] getData() {
		return memory.getData();
	}

	public int getData(int index) {
		return memory.getData(index);
	}

	public void setData(int index, int value) {
		memory.setData(index, value);
	}

	public int getChangedIndex() {
		return memory.getChangedIndex();
	}

	public Job getCurrentJob() {
		return currentJob;
	}

	public Code getCode(){
		return code;
	}

	public States getCurrentState() {
		return currentJob.getCurrentState();
	}

	public void setCurrentState(States currentState) {
		currentJob.setCurrentState(currentState);
	}

	public void changeToJob(int i){
		if(i < 0 || i > 3){
			throw new IllegalArgumentException("Illegal Inputs!");
		}

		if(i == currentJob.getId()){
		}
		else{
			currentJob.setCurrentAC(cpu.getAccum());
			currentJob.setCurrentPC(cpu.getpCounter());
		}
		currentJob = jobs[i];
		cpu.setAccum(currentJob.getCurrentAC());
		cpu.setpCounter(currentJob.getCurrentPC());
		cpu.setMemBase(currentJob.getStartmemoryIndex());
	}

	public void setCode(int index, int op, int indirLvl, int arg){
		code.setCode(index, op, indirLvl, arg);
	}

	public void clearJob(){
		memory.clear(currentJob.getStartmemoryIndex(), currentJob.getStartmemoryIndex()+Memory.DATA_SIZE/4);
		code.clear(currentJob.getStartcodeIndex(), currentJob.getCodeSize());
		//code.clear(currentJob.getStartcodeIndex(), currentJob.getStartcodeIndex()+currentJob.getCodeSize());
		cpu.setAccum(0);
		cpu.setpCounter(currentJob.getStartcodeIndex());
		currentJob.reset();
	}
	public void step(){
		try{
			int pc = cpu.getpCounter();
			if(cpu.getpCounter() < currentJob.getStartcodeIndex() || cpu.getpCounter() >= currentJob.getStartcodeIndex()+currentJob.getCodeSize()){
				throw new CodeAccessException("Illegal access outside of executing code");
			}
			else{
				int opCode = code.getOp(pc);
				int indirLvl = code.getIndirLvl(pc);
				int arg = code.getArg(pc);
				get(opCode).execute(arg, indirLvl);
			}
		}catch (Exception e){
			callback.halt();
			throw e;
		}
	}
}
