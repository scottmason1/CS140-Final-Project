package project;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class ControlPanel implements Observer {

	private GUIMediator gui;
	private JButton stepButton = new JButton("Step");
	private JButton clearButton = new JButton("Clear");
	private JButton runButton = new JButton("Run/Pause");
	private JButton reloadButton = new JButton("Reload");

	public ControlPanel(GUIMediator aGUI){
		gui = aGUI;
		gui.addObserver(this);
	}

	public JComponent createControlDisplay(){
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1,0));
		stepButton.setBackground(Color.WHITE);
		stepButton.addActionListener(e -> gui.step());
		panel.add(stepButton);
		clearButton.setBackground(Color.WHITE);
		clearButton.addActionListener(e -> gui.clearJob());
		panel.add(clearButton);
		runButton.setBackground(Color.WHITE);
		runButton.addActionListener(e -> gui.toggleAutoStep());
		panel.add(runButton);
		reloadButton.setBackground(Color.WHITE);
		reloadButton.addActionListener(e -> gui.reload());
		panel.add(reloadButton);
		JSlider slider = new JSlider(5,1000);
		slider.addChangeListener(e -> gui.setPeriod(slider.getValue()));
		panel.add(slider);
		return panel;
	}
	@Override
	public void update(Observable o, Object arg) {
		stepButton.setEnabled(gui.getCurrentState().getStepActive());
		clearButton.setEnabled(gui.getCurrentState().getClearActive());
		runButton.setEnabled(gui.getCurrentState().getRunPauseActive());
		reloadButton.setEnabled(gui.getCurrentState().getReloadActive());
	}
}