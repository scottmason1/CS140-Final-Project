package project;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class InstructionTester {

	MachineModel machine = new MachineModel();
	int[] dataCopy = new int[Memory.DATA_SIZE];
	int accInit;
	int ipInit;
	int offsetinit;

	@Before
	public void setup() {
		for (int i = 0; i < Memory.DATA_SIZE; i++) {
			dataCopy[i] = -5*Memory.DATA_SIZE + 10*i;
			machine.setData(i, -5*Memory.DATA_SIZE + 10*i);
			// Initially the machine will contain a known spread
			// of different numbers: 
			// -10240, -10230, -10220, ..., 0, 10, 20, ..., 10230 
			// This allows us to check that the instructions do 
			// not corrupt machine unexpectedly.
			// 0 is at index 1024
		}
		accInit = 30;
		ipInit = 30;
		offsetinit = 200;
		machine.setAccum(accInit);
		machine.setpCounter(ipInit);
		machine.setMemBase(offsetinit);
	}

	@Test
	public void testNOP(){
		Instruction instr = machine.get(0x0);
		instr.execute(0,0);
		//Test machine is not changed
		assertArrayEquals(dataCopy, machine.getData());
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit+1,
				machine.getpCounter());
		//Test accumulator untouched
		assertEquals("Accumulator unchanged", accInit,
				machine.getAccum());
	}

	@Test
	// Test whether load is correct with immediate addressing
	public void testLODimmediate(){
		Instruction instr = machine.get(0x1);
		machine.setAccum(27);
		int arg = 12;
			// should load 12 into the accumulator
			instr.execute(arg, 0);
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData());
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit+1,
                machine.getpCounter());
        //Test accumulator modified
        assertEquals("Accumulator changed", 12,
                machine.getAccum());
	}

	@Test
	// Test whether load is correct with direct addressing
	public void testLODdirect(){
		Instruction instr = machine.get(0x1);
		machine.setAccum(27);
		int arg = 12;
		// should load dataCopy[offsetinit+12] into the accumulator
		instr.execute(arg, 1);
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData());
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit+1,
        		machine.getpCounter());
        //Test accumulator modified
        assertEquals("Accumulator changed", dataCopy[offsetinit+12],
        		machine.getAccum());
	}

	@Test
	// Test whether load is correct with direct addressing
	public void testLODindirect() {
		Instruction instr = machine.get(0x1);
		machine.setAccum(-1);
		int arg = 1028-160;
		// if offset1 = dataCopy[offsetinit+1028-160] 
		// should load dataCopy[offsetinit+offset1] into the accumulator
		instr.execute(arg, 2);
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData());
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit+1,
                machine.getpCounter());
        //Test accumulator modified
        int offset1 = dataCopy[offsetinit+1028-160]; 
        assertEquals("Accumulator changed", dataCopy[offsetinit+offset1],
                machine.getAccum());
	}	
	
	@Test
	// Test whether store is correct with direct addressing
	public void testSTOdirect() {
		Instruction instr = machine.get(0x2);
		int arg = 12;
		machine.setAccum(567);
		dataCopy[offsetinit + 12] = 567;
		instr.execute(arg, 1);
		//Test machine is changed correctly
		assertArrayEquals(dataCopy, machine.getData());
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit+1,
				machine.getpCounter());
		//Test accumulator unchanged
		assertEquals("Accumulator unchanged", 567,
				machine.getAccum());
	}

	@Test
	// Test whether store is correct with indirect addressing
	public void testSTOindirect() {
		Instruction instr = machine.get(0x2);
		int arg = 940; 
		machine.setAccum(567);
		// if offset1 = dataCopy[offsetinit + 940]
		// changed memory should be at offset1+offsetinit
		dataCopy[1360] = 567;
		instr.execute(arg, 2);
		//Test machine is changed correctly
		assertArrayEquals(dataCopy, machine.getData());
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit+1,
				machine.getpCounter());
		//Test accumulator unchanged
		assertEquals("Accumulator unchanged", 567,
				machine.getAccum());
	}

	@Test (expected=IllegalArgumentException.class)
	// Test whether STO throws exception with immediate addressing
	public void testSTOimmediate() {
		Instruction instr = machine.get(0x2);
		instr.execute(0, 0);
	}

	@Test 
	// this test checks whether the add is done correctly, when
	// addressing is immediate
	public void testADDimmediate() {
		Instruction instr = machine.get(0x3);
		int arg = 12; 
		machine.setAccum(200);
		instr.execute(arg, 0); 
		// should have added 12 to accumulator
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit + 1,
				machine.getpCounter());
		assertEquals("Accumulator was changed", 200+12,
				machine.getAccum());
	}

	@Test 
	// this test checks whether the add is done correctly, when
	// addressing is direct
	public void testADDdirect() {
		Instruction instr = machine.get(0x3);
		int arg = 12; 
		machine.setAccum(250);
		// should add dataCopy[offsetinit+12] into the accumulator
		instr.execute(arg, 1); 
		// should have added -10240+2120 to accumulator
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit + 1,
				machine.getpCounter());
		assertEquals("Accumulator was changed", 250-10240+2120,
				machine.getAccum());
	}

	@Test 
	// this test checks whether the add is done correctly, when
	// addressing is indirect
	public void testADDindirect() {
		Instruction instr = machine.get(0x3);
		int arg = 1028-160;
		// if offset1 = dataCopy[offsetinit+1028-160] = 440
		// should add dataCopy[offsetinit+offset1] to the accumulator	
		// -5840
		machine.setAccum(250);
		instr.execute(arg, 2); 
		// should have added dataCopy[offsetinit+440] = -3840 to accumulator
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit + 1,
				machine.getpCounter());
		assertEquals("Accumulator was changed", 250-3840,
				machine.getAccum());
	}

	@Test 
	// this test checks whether the subtract is done correctly, when
	// addressing is immediate
	public void testSUBimmediate() {
		Instruction instr = machine.get(0x4);
		int arg = 12; 
		machine.setAccum(200);
		instr.execute(arg, 0); 
		// should have subtracted 12 from accumulator
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit + 1,
				machine.getpCounter());
		assertEquals("Accumulator was changed", 200-12,
				machine.getAccum());
	}

	@Test 
	// this test checks whether the subtract is done correctly, when
	// addressing is direct
	public void testSUBdirect() {
		Instruction instr = machine.get(0x4);
		int arg = 12; // we know that machine value is -2560+120
		machine.setAccum(200);
		instr.execute(arg, 1); 
		// should have subtracted -10240+2120 from accumulator
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit + 1,
				machine.getpCounter());
		assertEquals("Accumulator was changed", 200-(-10240+2120),
				machine.getAccum());
	}

	@Test 
	// this test checks whether the subtract is done correctly, when
	// addressing is indirect
	public void testSUBindirect() {
		Instruction instr = machine.get(0x4);
		int arg = 1028-160; 
		// see ADDindirect
		machine.setAccum(250);
		instr.execute(arg, 2); 
		// should have subtracted -3840 from accumulator (see ADDindirect)
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit + 1,
				machine.getpCounter());
		assertEquals("Accumulator was changed", 250+3840,
				machine.getAccum());
	}

	@Test 
	// this test checks whether the multiplication is done correctly, when
	// addressing is immediate
	public void testMULimmediate() {
		Instruction instr = machine.get(0x5);
		int arg = 12; 
		machine.setAccum(200);
		instr.execute(arg, 0); 
		// should have multiplied accumulator by 12
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit + 1,
				machine.getpCounter());
		assertEquals("Accumulator was changed", 200*12,
				machine.getAccum());
	}

	@Test 
	// this test checks whether the multiplication is done correctly, when
	// addressing is direct
	public void testMULdirect() {
		Instruction instr = machine.get(0x5);
		int arg = 12; 
		machine.setAccum(200);
		instr.execute(arg, 1); 
		// see ADDdirect
		// should have multiplied accumulator by -10240+2120 
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit + 1,
				machine.getpCounter());
		assertEquals("Accumulator was changed", 200*(-10240+2120),
				machine.getAccum());
	}

	@Test 
	// this test checks whether the multiplication is done correctly, when
	// addressing is indirect
	public void testMULindirect() {
		Instruction instr = machine.get(0x5);
		int arg = 1028-160; 
		// see ADDindirect
		machine.setAccum(200);
		instr.execute(arg, 2); 
		// should have multiplied accumulator by -3840
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit + 1,
				machine.getpCounter());
		assertEquals("Accumulator was changed", 200*(-3840),
				machine.getAccum());
	}


 	@Test 
	// this test checks whether the division is done correctly, when
	// addressing is immediate
	public void testDIVimmediate() {
		Instruction instr = machine.get(0x6);
		int arg = 12; 
		machine.setAccum(200);
		instr.execute(arg, 0); 
		// should have divided accumulator by 12
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit + 1,
				machine.getpCounter());
		assertEquals("Accumulator was changed", 200/12,
				machine.getAccum());
	}

	@Test 
	// this test checks whether the division is done correctly, when
	// addressing is direct
	public void testDIVdirect() {
		Instruction instr = machine.get(0x6);
		int arg = 12; 
		machine.setAccum(1024011);
		instr.execute(arg, 1); 
		// should have divided accumulator by -10240+2120 
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit + 1,
				machine.getpCounter());
		assertEquals("Accumulator was changed", 1024011/(-10240+2120),
				machine.getAccum());
	}

	@Test 
	// this test checks whether the division is done correctly, when
	// addressing is indirect
	public void testDIVindirect() {
		Instruction instr = machine.get(0x6);
		int arg = 1028-160; 
		machine.setAccum(400000);
		instr.execute(arg, 2); 
		// should have divided to accumulator -3840 (see ADDindirect
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit + 1,
				machine.getpCounter());
		assertEquals("Accumulator was changed", 400000/(-3840),
				machine.getAccum());
	}
	
	@Test (expected=DivideByZeroException.class) 
	// this test checks whether the DivideByZeroException is thrown 
	// for immediate division by 0
	public void testDIVZEROImmediate() {
		Instruction instr = machine.get(0x6);
		int arg = 0; 
		instr.execute(arg, 0);
	}

	@Test (expected=DivideByZeroException.class) 
	// this test checks whether the DivideByZeroException is thrown 
	// for division by 0 from machine
	public void testDIVZERODirect() {
		Instruction instr = machine.get(0x6);
		int arg = 1024-offsetinit; 
		instr.execute(arg, 1);
	}

	@Test (expected=DivideByZeroException.class) 
	// this test checks whether the DivideByZeroException is thrown 
	// for division by 0 from machine
	public void testDIVZEROIndirect() {
		Instruction instr = machine.get(0x6);
		machine.setData(100+offsetinit, 1024-offsetinit);
		int arg = 100; 
		instr.execute(arg, 2);
	}
	
	@Test 
	// this test checks whether the jump is done correctly, when
	// addressing is direct
	public void testJUMPimmediate() {
		Instruction instr = machine.get(0xB);
		int arg = 260;  
		machine.setAccum(200);
		machine.setpCounter(400);
		instr.execute(arg, 0); 
		// should add 260 to the program counter
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was changed", 400+260,
				machine.getpCounter());
		assertEquals("Accumulator was not changed", 200,
				machine.getAccum());
	}

	@Test 
	// this test checks whether the jump is done correctly, when
	// addressing is direct
	public void testJUMPdirect() {
		Instruction instr = machine.get(0xB);
		int arg = 1024-160; // the memory value is data[offsetinit-160 + 1024] = 400 
		machine.setAccum(200);
		machine.setpCounter(400);
		instr.execute(arg, 1); 
		// should add 400 to the program counter
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was changed", 800,
				machine.getpCounter());
		assertEquals("Accumulator was not changed", 200,
				machine.getAccum());
	}

	@Test 
	// this test checks whether the jump is done correctly, when
	// addressing is indirect
	public void testJUMPindirect() {
		Instruction instr = machine.get(0xB);
		int arg = 910; 
		// if index = data[offsetinit + 910] = 860
		// then the memory value is data[offsetinit + 860] = data[1060] = 360
		machine.setAccum(200);
		machine.setpCounter(400);
		instr.execute(arg, 2); 
		// should add 360 to the program counter
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was changed", 760,
				machine.getpCounter());
		assertEquals("Accumulator was not changed", 200,
				machine.getAccum());
	}
	
	@Test 
	// this test checks whether the jump is done correctly, when
	// addressing is indirect
	public void testJUMPabsolute() {
		Instruction instr = machine.get(0xB);
		int arg = 910; 
		machine.setAccum(200);
		machine.setpCounter(400);
		Job job = machine.getCurrentJob();
		job.setStartcodeIndex(777);
		instr.execute(arg, 3); 
		// should add 360 to the program counter
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was changed", 777+860,
				machine.getpCounter());
		assertEquals("Accumulator was not changed", 200,
				machine.getAccum());
	}
	
	@Test 
	// this test checks whether the jump is done correctly, when
	// addressing is direct
	public void testJMPZimmediateAccumZero() {
		Instruction instr = machine.get(0xC);
		int arg = 260;  
		machine.setAccum(0);
		machine.setpCounter(400);
		instr.execute(arg, 0); 
		// should add 260 to the program counter
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was changed", 400+260,
				machine.getpCounter());
		assertEquals("Accumulator was not changed", 0,
				machine.getAccum());
	}

	@Test 
	// this test checks whether the jump is done correctly, when
	// addressing is indirect
	public void testJMPZdirectAccumZero() {
		Instruction instr = machine.get(0xC);
		int arg = 1024-160; // the memory value is data[offsetinit-160 + 1024] = 400 
		machine.setAccum(0);
		machine.setpCounter(400);
		instr.execute(arg, 1); 
		// should add 400 to the program counter
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was changed", 800,
				machine.getpCounter());
		assertEquals("Accumulator was not changed", 0,
				machine.getAccum());
	}

	@Test 
	// this test checks whether the jump is done correctly, when
	// addressing is indirect
	public void testJMPZindirectAccumZero() {
		Instruction instr = machine.get(0xC);
		int arg = 910; 
		// if index = data[offsetinit + 910] = 860
		// then the memory value is data[offsetinit + 860] = data[1060] = 360
		machine.setAccum(0);
		machine.setpCounter(400);
		instr.execute(arg, 2); 
		// should add 360 to the program counter
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was changed", 760,
				machine.getpCounter());
		assertEquals("Accumulator was not changed", 0,
				machine.getAccum());
	}

	@Test 
	// this test checks whether the jump is done correctly, when
	// addressing is indirect
	public void testJMPZabsoluteAccumZero() {
		Instruction instr = machine.get(0xC);
		int arg = 910; 
		machine.setAccum(0);
		machine.setpCounter(400);
		Job job = machine.getCurrentJob();
		job.setStartcodeIndex(777);
		instr.execute(arg, 3); 
		// should add 360 to the program counter
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was changed", 777+860,
				machine.getpCounter());
		assertEquals("Accumulator was not changed", 0,
				machine.getAccum());
	}
	
	@Test 
	// this test checks whether no jump is done if accumulator is zero, 
	// when addressing is direct
	public void testJMPZimmedAccumNonZero() {
		Instruction instr = machine.get(0xC);
		int arg = 260;  
		machine.setAccum(200);
		instr.execute(arg, 0); 
		// should have set the program counter incremented
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit+1,
				machine.getpCounter());
		assertEquals("Accumulator was not changed", 200,
				machine.getAccum());
	}

	@Test 
	// this test checks whether no jump is done if accumulator is zero, 
	// when addressing is indirect
	public void testJMPZdirectAccumNonZero() {
		Instruction instr = machine.get(0xC);
		int arg = 1024-160; // the memory value is data[offsetinit-160 + 1024] = 400 
		machine.setAccum(200);
		instr.execute(arg, 1); 
		// should have set the program counter incremented
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit+1,
				machine.getpCounter());
		assertEquals("Accumulator was not changed", 200,
				machine.getAccum());
	}

	@Test 
	// this test checks whether the jump is done correctly, when
	// addressing is indirect
	public void testJMPZindirectAccumNonZero() {
		Instruction instr = machine.get(0xC);
		int arg = 910; 
		// if index = data[offsetinit + 910] = 860
		// then the memory value is data[offsetinit + 860] = data[1060] = 360
		machine.setAccum(200);
		instr.execute(arg, 2); 
		// should have set the program counter incremented
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was changed", ipInit+1,
				machine.getpCounter());
		assertEquals("Accumulator was not changed", 200,
				machine.getAccum());
	}

	@Test 
	// this test checks whether the jump is done correctly, when
	// addressing is indirect
	public void testJMPZabsoluteAccumNonZero() {
		Instruction instr = machine.get(0xC);
		int arg = 910; 
		machine.setAccum(200);
		machine.setpCounter(400);
		Job job = machine.getCurrentJob();
		job.setStartcodeIndex(777);
		instr.execute(arg, 3); 
		// should add 360 to the program counter
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was changed", 401,
				machine.getpCounter());
		assertEquals("Accumulator was not changed", 200,
				machine.getAccum());
	}
	
	@Test
	// Check CMPL when comparing less than 0 gives true
	public void testCMPLdirectMemLT0() {
		Instruction instr = machine.get(0x9);
		int arg = 100;
		instr.execute(arg, 1);
        assertTrue(machine.getData(arg+offsetinit) < 0); 
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 1", 1,
                machine.getAccum());
	}

	@Test
	// Check CMPL when comparing equal to 0 gives false
	public void testCMPLdirectMemGT0() {
		Instruction instr = machine.get(0x9);
		int arg = 1024;
		instr.execute(arg, 1);
        assertTrue(machine.getData(arg+offsetinit) > 0); 
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 0", 0,
                machine.getAccum());
	}

	@Test
	// Check CMPL when comparing greater than 0 gives false
	public void testCMPdirectMemEQ0() {
		Instruction instr = machine.get(0x9);
		int arg = 1024 - offsetinit;
		instr.execute(arg, 1);
        assertTrue(machine.getData(arg+offsetinit) == 0); 
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 0", 0,
                machine.getAccum());
	}

	@Test
	// Check CMPL when comparing less than 0 gives true
	public void testCMPLindirectMemLT0() {
		Instruction instr = machine.get(0x9);
		int arg = 850;
		instr.execute(arg, 2);
		int index = machine.getData(arg+offsetinit);
        assertTrue(machine.getData(index+offsetinit) < 0); 
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 1", 1,
                machine.getAccum());
	}

	@Test
	// Check CMPL when comparing equal to 0 gives false
	public void testCMPLindirectMemGT0() {
		Instruction instr = machine.get(0x9);
		int arg = 950;
		instr.execute(arg, 2);
		int index = machine.getData(arg+offsetinit);
        assertTrue(machine.getData(index+offsetinit) > 0); 
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 0", 0,
                machine.getAccum());
	}

	@Test
	// Check CMPL when comparing greater than 0 gives false
	public void testCMPLindirectMemEQ0() {
		Instruction instr = machine.get(0x9);
		int arg = 1024 - offsetinit;
		machine.setData(offsetinit, 0);
		dataCopy[offsetinit] = 0;
		instr.execute(arg, 2);
		int index = machine.getData(arg+offsetinit);
        assertTrue(machine.getData(index+offsetinit) == 0); 
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 0", 0,
                machine.getAccum());
	}

	@Test (expected=IllegalArgumentException.class)
	// Test whether CMPL throws exception with indirect addressing
	public void testCMPLimmediate() {
		Instruction instr = machine.get(0x9);
		instr.execute(0, 0);
	}

	@Test
	// Check CMPZ when comparing less than 0 gives false
	public void testCMPZdirectMemLT0() {
		Instruction instr = machine.get(0xA);
		int arg = 100;
		instr.execute(arg, 1);
        assertTrue(machine.getData(arg+offsetinit) < 0); 
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 0", 0,
                machine.getAccum());
	}

	@Test
	// Check CMPZ when comparing equal to 0 gives true
	public void testCMPZdirectMemGT0() {
		Instruction instr = machine.get(0xA);
		int arg = 950;
		instr.execute(arg, 2);
		int index = machine.getData(arg+offsetinit);
        assertTrue(machine.getData(index+offsetinit) > 0); 
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 0", 0,
                machine.getAccum());
	}

	@Test
	// Check CMPZ when comparing greater than 0 gives false
	public void testCMPZdirectMemEQ0() {
		Instruction instr = machine.get(0xA);
		int arg = 1024 - offsetinit;
		instr.execute(arg, 1);
        assertTrue(machine.getData(arg+offsetinit) == 0); 
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 1", 1,
                machine.getAccum());
	}

	@Test
	// Check CMPZ when comparing less than 0 gives false
	public void testCMPZindirectMemLT0() {
		Instruction instr = machine.get(0xA);
		int arg = 850;
		instr.execute(arg, 2);
		int index = machine.getData(arg+offsetinit);
        assertTrue(machine.getData(index+offsetinit) < 0); 
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 0", 0,
                machine.getAccum());
	}

	@Test
	// Check CMPZ when comparing equal to 0 gives true
	public void testCMPZindirectMemGT0() {
		Instruction instr = machine.get(0xA);
		int arg = 950;
		instr.execute(arg, 2);
		int index = machine.getData(arg+offsetinit);
        assertTrue(machine.getData(index+offsetinit) > 0); 
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 0", 0,
                machine.getAccum());
	}

	@Test
	// Check CMPZ when comparing greater than 0 gives false
	public void testCMPZindirectMemEQ0() {
		Instruction instr = machine.get(0xA);
		int arg = 1024 - offsetinit;
		machine.setData(offsetinit, 0);
		dataCopy[offsetinit] = 0;
		instr.execute(arg, 2);
		int index = machine.getData(arg+offsetinit);
        assertTrue(machine.getData(index+offsetinit) == 0); 
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 1", 1,
                machine.getAccum());
	}

	@Test (expected=IllegalArgumentException.class)
	// Test whether CMPZ throws exception with immediate addressing
	public void testCMPZimmediate() {
		Instruction instr = machine.get(0xA);
		instr.execute(0, 0);
	}

	@Test
	// Check AND when accum and arg equal to 0 gives false
	public void testANDimmediateAccEQ0argEQ0() {
		Instruction instr = machine.get(0x7);
		int arg = 0;
		machine.setAccum(0);
		instr.execute(arg, 0);
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 0", 0,
                machine.getAccum());
	}
	
	@Test
	// Check AND when accum < 0 and arg equal to 0 gives false
	public void testANDimmediateAccLT0argEQ0() {
		Instruction instr = machine.get(0x7);
		int arg = 0;
		machine.setAccum(-1);
		instr.execute(arg, 0);
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 0", 0,
                machine.getAccum());
	}
	
	@Test
	// Check AND when accum > 0 and arg equal to 0 gives false
	public void testANDimmediateAccGT0argEQ0() {
		Instruction instr = machine.get(0x7);
		int arg = 0;
		machine.setAccum(1);
		instr.execute(arg, 0);
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 0", 0,
                machine.getAccum());
	}
	
	@Test
	// Check AND when accum = 0 and arg < 0 gives false
	public void testANDimmediateAccEQ0argLT0() {
		Instruction instr = machine.get(0x7);
		int arg = -1;
		machine.setAccum(0);
		instr.execute(arg, 0);
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 0", 0,
                machine.getAccum());
	}
	
	@Test
	// Check AND when accum < 0 and arg < 0 gives true
	public void testANDimmediateAccLT0argLT0() {
		Instruction instr = machine.get(0x7);
		int arg = -1 ;
		machine.setAccum(-1);
		instr.execute(arg, 0);
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 1", 1,
                machine.getAccum());
	}
	
	@Test
	// Check AND when accum > 0 and arg > 0 gives true
	public void testANDimmediateAccGT0argLT0() {
		Instruction instr = machine.get(0x7);
		int arg = -1;
		machine.setAccum(1);
		instr.execute(arg, 0);
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 1", 1,
                machine.getAccum());
	}
	
	@Test
	// Check AND when accum = 0 and arg > 0 gives false
	public void testANDimmediateAccEQ0argGT0() {
		Instruction instr = machine.get(0x7);
		int arg = 1;
		machine.setAccum(0);
		instr.execute(arg, 0);
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 0", 0,
                machine.getAccum());
	}
	
	@Test
	// Check AND when accum < 0 and arg > 0 gives true
	public void testANDimmediateAccLT0argGT0() {
		Instruction instr = machine.get(0x7);
		int arg = 1 ;
		machine.setAccum(-1);
		instr.execute(arg, 0);
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 1", 1,
                machine.getAccum());
	}
	
	@Test
	// Check AND when accum > 0 and arg > 0 gives true
	public void testANDimmediateAccGT0argGT0() {
		Instruction instr = machine.get(0x7);
		int arg = 1;
		machine.setAccum(1);
		instr.execute(arg, 0);
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 1", 1,
                machine.getAccum());
	}

	@Test
	// Check AND when accum direct mem equal to 0 gives false
	public void testANDdirectAccEQ0memEQ0() {
		Instruction instr = machine.get(0x7);
		int arg = 1024-offsetinit;
		machine.setAccum(0);
		instr.execute(arg, 1);
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 0", 0,
                machine.getAccum());
	}

	@Test
	// Check AND when accum < 0 direct mem equal to 0 gives false
	public void testANDdirectAccLT0memEQ0() {
		Instruction instr = machine.get(0x7);
		int arg = 1024-offsetinit;
		machine.setAccum(-1);
		instr.execute(arg, 1);
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData());
        System.out.println(dataCopy[1024]);
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 0", 0,
                machine.getAccum());
	}
	
	@Test
	// Check AND when accum > 0 direct mem equal to 0 gives false
	public void testANDdirectAccGT0memEQ0() {
		Instruction instr = machine.get(0x7);
		int arg = 1024-offsetinit;
		machine.setAccum(1);
		instr.execute(arg, 1);
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 0", 0,
                machine.getAccum());
	}
	
	@Test
	// Check AND when accum = 0 direct mem < 0 gives false
	public void testANDdirectAccEQ0memLT0() {
		Instruction instr = machine.get(0x7);
		int arg = 100;
		machine.setAccum(0);
		instr.execute(arg, 1);
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 0", 0,
                machine.getAccum());
	}
	
	@Test
	// Check AND when accum < 0 direct mem < 0 gives true
	public void testANDdirectAccLT0memLT0() {
		Instruction instr = machine.get(0x7);
		int arg = 100 ;
		machine.setAccum(-1);
		instr.execute(arg, 1);
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 1", 1,
                machine.getAccum());
	}
	
	@Test
	// Check AND when accum > 0 direct mem < 0 gives true
	public void testANDdirectAccGT0memLT0() {
		Instruction instr = machine.get(0x7);
		int arg = 100;
		machine.setAccum(1);
		instr.execute(arg, 1);
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 1", 1,
                machine.getAccum());
	}
	
	@Test
	// Check AND when accum = 0 direct mem > 0 gives false
	public void testANDdirectAccEQ0memGT0() {
		Instruction instr = machine.get(0x7);
		int arg = 1030-offsetinit;
		machine.setAccum(0);
		instr.execute(arg, 1);
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 0", 0,
                machine.getAccum());
	}
	
	@Test
	// Check AND when accum < 0 direct mem > 0 gives true
	public void testANDdirectAccLT0argGT0() {
		Instruction instr = machine.get(0x7);
		int arg = 1 ;
		machine.setAccum(-1);
		instr.execute(arg, 1);
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 1", 1,
                machine.getAccum());
	}
	
	@Test
	// Check AND when accum > 0 direct mem > 0 gives true
	public void testANDdirectAccGT0argGT0() {
		Instruction instr = machine.get(0x7);
		int arg = 1;
		machine.setAccum(1);
		instr.execute(arg, 1);
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 1", 1,
                machine.getAccum());
	}

	@Test
	// Check AND when accum indirect mem equal to 0 gives false
	public void testANDindirectAccEQ0memEQ0() {
		Instruction instr = machine.get(0x7);
		int arg = 1024 - offsetinit;
		machine.setAccum(0);
		machine.setData(offsetinit, 0);
		dataCopy[offsetinit] = 0;
		instr.execute(arg, 2);
		int index = machine.getData(arg+offsetinit);
        assertTrue(machine.getData(index+offsetinit) == 0); 
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 0", 0,
                machine.getAccum());
	}

	@Test
	// Check AND when accum < 0 indirect mem equal to 0 gives false
	public void testANDindirectAccLT0memEQ0() {
		Instruction instr = machine.get(0x7);
		int arg = 1024 - offsetinit;
		machine.setAccum(-1);
		machine.setData(offsetinit, 0);
		dataCopy[offsetinit] = 0;
		instr.execute(arg, 2);
		int index = machine.getData(arg+offsetinit);
        assertTrue(machine.getData(index+offsetinit) == 0); 
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData());
        System.out.println(dataCopy[1024]);
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 0", 0,
                machine.getAccum());
	}
	
	@Test
	// Check AND when accum > 0 indirect mem equal to 0 gives false
	public void testANDindirectAccGT0memEQ0() {
		Instruction instr = machine.get(0x7);
		int arg = 1024 - offsetinit;
		machine.setAccum(1);
		machine.setData(offsetinit, 0);
		dataCopy[offsetinit] = 0;
		instr.execute(arg, 2);
		int index = machine.getData(arg+offsetinit);
        assertTrue(machine.getData(index+offsetinit) == 0); 
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 0", 0,
                machine.getAccum());
	}
	
	@Test
	// Check AND when accum = 0 indirect mem < 0 gives false
	public void testANDindirectAccEQ0memLT0() {
		Instruction instr = machine.get(0x7);
		int arg = 1020 - offsetinit;
		machine.setAccum(0);
		instr.execute(arg, 2);
		int index = machine.getData(arg+offsetinit);
        assertTrue(machine.getData(index+offsetinit) < 0); 
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 0", 0,
                machine.getAccum());
	}
	
	@Test
	// Check AND when accum < 0 indirect mem < 0 gives true
	public void testANDindirectAccLT0memLT0() {
		Instruction instr = machine.get(0x7);
		int arg = 1020 - offsetinit;
		machine.setAccum(-1);
		instr.execute(arg, 2);
		int index = machine.getData(arg+offsetinit);
        assertTrue(machine.getData(index+offsetinit) < 0); 
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 1", 1,
                machine.getAccum());
	}
	
	@Test
	// Check AND when accum > 0 indirect mem < 0 gives true
	public void testANDindirectAccGT0memLT0() {
		Instruction instr = machine.get(0x7);
		int arg = 1020 - offsetinit;
		machine.setAccum(1);
		instr.execute(arg, 2);
		int index = machine.getData(arg+offsetinit);
        assertTrue(machine.getData(index+offsetinit) < 0); 
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 1", 1,
                machine.getAccum());
	}
	
	@Test
	// Check AND when accum = 0 indirect mem > 0 gives false
	public void testANDindirectAccEQ0memGT0() {
		Instruction instr = machine.get(0x7);
		int arg = 1200-offsetinit;
		machine.setAccum(0);
		instr.execute(arg, 2);
		int index = machine.getData(arg+offsetinit);
        assertTrue(machine.getData(index+offsetinit) > 0); 
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 0", 0,
                machine.getAccum());
	}
	
	@Test
	// Check AND when accum < 0 indirect mem > 0 gives true
	public void testANDindirectAccLT0argGT0() {
		Instruction instr = machine.get(0x7);
		int arg = 1200-offsetinit;
		machine.setAccum(-1);
		instr.execute(arg, 2);
		int index = machine.getData(arg+offsetinit);
        assertTrue(machine.getData(index+offsetinit) > 0); 
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 1", 1,
                machine.getAccum());
	}
	
	@Test
	// Check AND when accum > 0 indirect mem > 0 gives true
	public void testANDindirectAccGT0argGT0() {
		Instruction instr = machine.get(0x7);
		int arg = 1200-offsetinit;
		machine.setAccum(1);
		instr.execute(arg, 2);
		int index = machine.getData(arg+offsetinit);
        assertTrue(machine.getData(index+offsetinit) > 0); 
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 1", 1,
                machine.getAccum());
	}

	@Test
	// Check NOT greater than 0 gives false
	public void testNOTaccGT0() {
		Instruction instr = machine.get(0X8);
		machine.setAccum(10);
		instr.execute(0, 0);
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 0", 0,
                machine.getAccum());
	}

	@Test
	// Check NOT equal to 0 gives true
	public void testNOTaccEQ0() {
		Instruction instr = machine.get(0X8);
		machine.setAccum(0);
		instr.execute(0, 0);
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 1", 1,
                machine.getAccum());
	}
	
	@Test
	// Check NOT less than 0 gives false
	public void testNOTaccLT0() {
		Instruction instr = machine.get(0X8);
		machine.setAccum(-10);
		instr.execute(0, 0);
		//Test machine is not changed
        assertArrayEquals(dataCopy, machine.getData()); 
        //Test program counter incremented
        assertEquals("Program counter incremented", ipInit + 1,
                machine.getpCounter());
        //Accumulator is 1
        assertEquals("Accumulator is 0", 0,
                machine.getAccum());
	}
	
	@Test (expected=IllegalArgumentException.class)
	// Test whether NOT throws exception with immediate addressing
	public void testNOTdirect() {
		Instruction instr = machine.get(0X8);
		instr.execute(0, 1);
	}

	@Test (expected=IllegalArgumentException.class)
	// Test whether NOT throws exception with indirect addressing
	public void testNOTindirect() {
		Instruction instr = machine.get(0X8);
		instr.execute(0, 2);
	}
}

