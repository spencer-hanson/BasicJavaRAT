package ratclient;
import javax.swing.*;
import java.awt.event.*;
public class TextInputWindow extends JFrame implements KeyListener { //Send raw data to the server, open
	JTextArea text;  //GUI to type in a very basic text editor
	CommandLib command;
	public TextInputWindow(CommandLib command) {
		super("Input Text");
		setSize(500, 500);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		this.command = command;
		text =  new JTextArea();
		add(text);
		addKeyListener(this);
		text.addKeyListener(this);
		setVisible(true);
	}
	
	boolean ctrl = false;
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_CONTROL) {
			ctrl = true;
		}
		if(ctrl && e.getKeyCode() == KeyEvent.VK_X) {
			command.wake();
		}
	}
	
	public String getTxt() {
		this.setVisible(false);
		return text.getText();
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		ctrl = false;
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		
		
	}

}
