package project;

public class Job {
	private int id;
	private int startcodeIndex;
	private int codeSize;
	private int startmemoryIndex;
	private int currentPC;
	private int currentAC;
	private States currentState = States.NOTHING_LOADED;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getStartcodeIndex() {
		return startcodeIndex;
	}
	public void setStartcodeIndex(int startcodeIndex) {
		this.startcodeIndex = startcodeIndex;
	}
	public int getCodeSize() {
		return codeSize;
	}
	public void setCodeSize(int codeSize) {
		this.codeSize = codeSize;
	}
	public int getStartmemoryIndex() {
		return startmemoryIndex;
	}
	public void setStartmemoryIndex(int startmemoryIndex) {
		this.startmemoryIndex = startmemoryIndex;
	}
	public int getCurrentPC() {
		return currentPC;
	}
	public void setCurrentPC(int currentPC) {
		this.currentPC = currentPC;
	}
	public int getCurrentAC() {
		return currentAC;
	}
	public void setCurrentAC(int currentAC) {
		this.currentAC = currentAC;
	}
	public States getCurrentState() {
		return currentState;
	}
	public void setCurrentState(States currentState) {
		this.currentState = currentState;
	}
	
	public void reset(){
		codeSize = 0;
		currentAC = 0;
		currentPC = startcodeIndex;
		currentState = States.NOTHING_LOADED;
	}
}
