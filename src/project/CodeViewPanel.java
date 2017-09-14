package project;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class CodeViewPanel implements Observer {

	private MachineModel model;
	private Code code;
	private JScrollPane scroller;
	private JTextField[] codeText = new JTextField[Code.CODE_MAX];
	private JTextField[] codeHex = new JTextField[Code.CODE_MAX];
	private int previousColor = -1;
	
	public CodeViewPanel(GUIMediator gui, MachineModel mdl){
		model = mdl;
		gui.addObserver(this);
	}
	public JComponent createCodeDisplay(){
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		Border border = BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.BLACK), 
				"Code Memory View",
				TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION);
		panel.setBorder(border);
		JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new BorderLayout());
		JPanel numPanel = new JPanel();
		numPanel.setLayout(new GridLayout(0,1));
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new GridLayout(0,1));
		JPanel hexPanel = new JPanel();
		hexPanel.setLayout(new GridLayout(0,1));
		innerPanel.add(numPanel, BorderLayout.LINE_START);
		innerPanel.add(textPanel, BorderLayout.CENTER);
		innerPanel.add(hexPanel, BorderLayout.LINE_END);
		
		for(int i = 0; i < Code.CODE_MAX; i++){
			codeText[i] = new JTextField(10);
			codeHex[i] = new JTextField(10);
			numPanel.add(new JLabel(i+": ", JLabel.RIGHT));
			textPanel.add(codeText[i]);
			hexPanel.add(codeHex[i]);
		}
		
		scroller = new JScrollPane(innerPanel);
		panel.add(scroller);
		return panel;
	}
	@Override
	public void update(Observable arg0, Object arg1) {
		if(arg1 != null && arg1.equals("Load Code")) {
			code = model.getCode();
			int offset = model.getCurrentJob().getStartcodeIndex();
			for(int i = offset; 
					i < offset + model.getCurrentJob().getCodeSize(); i++) {
				codeText[i].setText(code.getText(i));
				codeHex[i].setText(code.getHex(i));
			}	
			previousColor = model.getpCounter();			
			codeHex[previousColor].setBackground(Color.YELLOW);
			codeText[previousColor].setBackground(Color.YELLOW);
	//CHANGE AS OF 5/4/17
		} else if(arg1 != null && arg1 instanceof String && ((String)arg1).startsWith("Clear")) {
			int offset = model.getCurrentJob().getStartcodeIndex();
			int codeSize = Integer.parseInt(((String)arg1).substring(6).trim());
			for(int i = offset; 
					i < offset + codeSize; i++) {
				codeText[i].setText("");
				codeHex[i].setText("");
			}	
	// END OF CHANGE
			if(previousColor >= 0 && previousColor < Code.CODE_MAX) {
				codeText[previousColor].setBackground(Color.WHITE);
				codeHex[previousColor].setBackground(Color.WHITE);
			}
			previousColor = -1;
		}		
		if(this.previousColor >= 0 && previousColor < Code.CODE_MAX) {
			codeText[previousColor].setBackground(Color.WHITE);
			codeHex[previousColor].setBackground(Color.WHITE);
		}
		previousColor = model.getpCounter();
		if(this.previousColor >= 0 && previousColor < Code.CODE_MAX) {
			codeText[previousColor].setBackground(Color.YELLOW);
			codeHex[previousColor].setBackground(Color.YELLOW);
		} 
		if(scroller != null && code != null && model!= null) {
			JScrollBar bar= scroller.getVerticalScrollBar();
			int pc = model.getpCounter();
			if(pc > 0 && pc < Code.CODE_MAX && codeHex[pc] != null) {
				Rectangle bounds = codeHex[pc].getBounds();
				bar.setValue(Math.max(0, bounds.y - 15*bounds.height));
			}
		}
	}

	public static void main(String[] args) {
		GUIMediator view = new GUIMediator(); 
		MachineModel model = new MachineModel();
		CodeViewPanel panel = new CodeViewPanel(view, model);
		JFrame frame = new JFrame("TEST");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 700);
		frame.setLocationRelativeTo(null);
		frame.add(panel.createCodeDisplay());
		frame.setVisible(true);
		int size = Integer.parseInt(Loader.load(model, new File("out.pexe"), 0, 0));
		model.getCurrentJob().setCodeSize(size);
		panel.update(view, "Load Code");
	}
}