package project;

import java.awt.GridLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.sun.scenario.effect.Blend.Mode;

public class ProcessorViewPanel implements Observer {
	private MachineModel model;
	private JTextField acc = new JTextField();
	private JTextField acc2 = new JTextField();
	private JTextField acc3 = new JTextField();

	public ProcessorViewPanel(GUIMediator gui, MachineModel mdl) {
		model = mdl;
		gui.addObserver(this);
	}

	public JComponent createProcessorDisplay() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1,0));
		panel.add(new JLabel("Accumulator: ", JLabel.RIGHT));
		panel.add(acc);
		panel.add(new JLabel("Program Counter: ", JLabel.RIGHT));
		panel.add(acc2);
		panel.add(new JLabel("Memory Base: ", JLabel.RIGHT));
		panel.add(acc3);
		return panel;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		if(model != null) {
			acc.setText("" + model.getAccum());
			acc2.setText("" + model.getpCounter());
			acc3.setText("" + model.getMemBase());
		}
	}
	
	public static void main(String[] args) {
		GUIMediator view = new GUIMediator(); 
		MachineModel model = new MachineModel();
		ProcessorViewPanel panel = 
			new ProcessorViewPanel(view, model);
		JFrame frame = new JFrame("TEST");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 60);
		frame.setLocationRelativeTo(null);
		frame.add(panel.createProcessorDisplay());
		frame.setVisible(true);
	}
}