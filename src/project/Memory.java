package project;

public class Memory {

	public static int DATA_SIZE = 2048;
	private int[] data = new int[DATA_SIZE];
	private int changedIndex = -1;
	
	public int[] getData(){ //package private?, just private?
		return data;			
	}
	
	public int getData(int index){
		return data[index];
	}
	
	public void setData(int index, int value){
		data[index] = value;
		changedIndex = index;
	}

	public int getChangedIndex() {
		return changedIndex;
	}
	
	public void clear(int start, int end){
		for(int i = start; i < end; i++){
			data[i] = 0;
		}
		changedIndex = -1;
	}
}
