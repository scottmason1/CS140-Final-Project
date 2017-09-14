package project;

public class Code {

	public static int CODE_MAX = 1024;
	private long[] code = new long[CODE_MAX];

	public void setCode(int index, int op, int indirLvl, int arg) {
		// the opcode will use 29 bits, multiplying by
		// 8 moves 3 bits to the left
		long longOp = op*8;
		// put the indirection level in those last 3 bits
		longOp += indirLvl;
		long longArg = arg;
		// move the opcode and indirLvl to the upper 32 bits
		long OpAndArg = longOp << 32;
		// if arg was negative, longArg will have 32 leading 1s,
		// remove them:
		longArg = longArg & 0x00000000FFFFFFFFL;
		//join the upper 32 bits and the lower 32 bits
		code[index] = OpAndArg | longArg;
	}
	
	String getHex(int i){
		int arg = getArg(i);
		int a = getOp(i);
		int b = getIndirLvl(i);
		int c = Math.abs(arg);
		String retVal = Integer.toHexString(a).toUpperCase() + " " + Integer.toHexString(b).toUpperCase() + 
				" " + Integer.toHexString(c).toUpperCase();
		if(arg < 0){
			retVal = Integer.toHexString(a).toUpperCase() + " " + Integer.toHexString(b).toUpperCase() + 
					" -" + Integer.toHexString(c).toUpperCase();
		}
		return retVal;
	}

	int getOp(int i) {
		// move upper half to the lower half discarding lower half 
		// and the 3 bit of the indirLvl
		return (int)(code[i] >> 35);
	}

	int getIndirLvl(int i) {
		// move upper half to the lower half discarding lower half
		// then get last 3 bits
		return (int)(code[i] >> 32)%8;
	}

	int getArg(int i) {
		// cut out upper half keeping lower half
		return (int)(code[i] & 0x00000000FFFFFFFFL);
	}

	public void clear(int start, int length){
		//5/11 added -1 to control statement
		for(int i = start; i < start+length-1; i++){
			code[i] = 0;
		}
	}
	public String getText(int i){
		StringBuilder bldr = new StringBuilder();
		String mnem = InstructionMap.mnemonics.get(getOp(i));
		bldr.append(mnem);
		int x = getIndirLvl(i);
		if(x == 0 && !(InstructionMap.noArgument.contains(mnem))){
			bldr.append("I ");
		}
		if(x == 0 && InstructionMap.noArgument.contains(mnem)){
			bldr.append(" ");
		}
		if(x == 1){
			bldr.append(" ");
		}
		if(x == 2){
			bldr.append(" [");
		}
		if(x == 3){
			bldr.append("A ");
		}
		int arg = getArg(i);

		if(arg >= 0){
			bldr.append(Integer.toHexString(arg).toUpperCase());
		}
		if(arg < 0){
			//bldr.append("-");
			bldr.append("-" + Integer.toHexString(-arg).toUpperCase());
		}
		if(x == 2){
			bldr.append("]");
		}
		return bldr.toString();
	}
//	public static void main(String[] args) {
//		Code c = new Code();
//		for(int i = 0; i <= 3; i++) {
//			c.setCode(2*i, 12, i, 2015);
//			System.out.print(c.getText(2*i) + ", ");
//			System.out.print(c.code[2*i] + ", ");
//			System.out.print(c.getOp(2*i) + " ");
//			System.out.print(c.getIndirLvl(2*i) + " ");
//			System.out.println(c.getArg(2*i));
//			c.setCode(2*i+1, 12, i, -2015);
//			System.out.print(c.getText(2*i+1) + ", ");
//			System.out.print(c.code[2*i+1] + ", ");
//			System.out.print(c.getOp(2*i+1) + " ");
//			System.out.print(c.getIndirLvl(2*i+1) + " ");
//			System.out.println(c.getArg(2*i+1));
//		}
//		c.setCode(8, 0, 0, 0);	//NEW
//		c.setCode(9, 8, 0, 0);	//NEW
//		c.setCode(10, 15, 0, 0);	//NEW
//		System.out.println(c.getText(8));	//NEW
//		System.out.println(c.getText(9));	//NEW
//		System.out.println(c.getText(10));	//NEW
//	}

	public long[] getCode() {
		return code;
	}
}