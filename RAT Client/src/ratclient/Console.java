package ratclient;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;

import java.awt.*;
import java.net.*;
import java.awt.event.*;


//features to add maybe
/*
 * Log command to get errors
 * updated screenshots
 * delete command to remove server completely
 */
@SuppressWarnings("serial")
public class Console extends JFrame implements KeyListener { //GUI Console
	JTextArea screen; //Where all the text is
	Font dosFont; //Cool font
	JScrollPane scrollPane; //make sure we can scroll
	CommandLib commandlib; //Library of commands
	public String prefix; //thing before every command, default '>'
	public RatClient client; //client communication
	public ConsoleWriter out; //direct reference to console output
	
	

	public void initFont() {
		Font dosFont = null;
        try {
        	//setup font
				URL fontUrl = getURL("fonts/DOSFont.ttf");
		    	dosFont = Font.createFont(Font.TRUETYPE_FONT, fontUrl.openStream());
	        	dosFont = dosFont.deriveFont(Font.PLAIN, 15);
	        	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	        	ge.registerFont(dosFont);
	        } catch(Exception e) {
	            e.printStackTrace();
	            System.exit(-1);
	        }
        screen.setFont(dosFont);//set font on JTextArea
	}
	
	public void initGui() { //set up gui
		add(scrollPane);
		setSize(655, 360);
		setResizable(false);
        setLocationRelativeTo(null); //center on screen
        addKeyListener(this);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        out.print(prefix);
        screen.setCaretPosition(1);
	}
	
	public void initComponents() { //add stuff, make it look pretty :)
		screen = new JTextArea();
        screen.setBackground(Color.black);
        screen.setForeground(Color.green);
        screen.setCaretColor(Color.green);
        ConsoleCaret caret = new ConsoleCaret(); //set up custom caret, for looks
        caret.setUpdatePolicy(ConsoleCaret.ALWAYS_UPDATE);
        screen.setCaret(caret);
        screen.setEditable(false);
        screen.addKeyListener(this);
        screen.setLineWrap(true);
        screen.setFocusable(false);
        screen.getCaret().setVisible(true);
        initFont();
        
        commandlib = new CommandLib(this); //init command lib
        out = new ConsoleWriter(new ConsoleStream(screen), commandlib, prefix);
        commandlib.setOutput(out);
        scrollPane = new JScrollPane(screen);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setForeground(Color.green);
        scrollPane.setBackground(Color.black);
        scrollPane.requestFocus();
        scrollPane.addKeyListener(this);
        
    }
	
	public Console() {
		super("Console");
		prefix = ">";
		initComponents();
		client = new RatClient(out);
		commandlib.setClient(client);
		initGui();
		out.backspace(); //remove backspace needed to initalizing consolewriter line 
        
    }
	
	public static void main(String args[]) {
		new Console();
	}
	
	public void clear() {
		screen.setText("");
		out.clear();
		screen.setText("");
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
		out.setPrefix(prefix);
	}
	
	public String getPrefix() {
		return prefix;
	}
	
    private URL getURL(String filename) { //get resource on filesystem
		URL url = null;
		try { 
			url = this.getClass().getResource(filename);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return url;
	}

    boolean ctrl;
    
    class Timer extends Thread implements Runnable { //seperate thread to check if you've pressed ctrl
    	double time;
    	public Timer(double t) {
    		this.time = t;
    	}
    	
    	@Override
    	public void run() {
    		try {
    			Thread.sleep((int)(time * 1000));
    			ctrl = false;
    		} catch(Exception e) {
    			e.printStackTrace();
    		}
    	}
    }
    
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_CONTROL) { //Register control input
			ctrl = true;
			new Timer(.5).start();
		}
		if(e.getKeyCode() == KeyEvent.VK_C && ctrl) { //Canceling command
			out.print("^C\n");
			commandlib.killCurrentCommand();
			ctrl = false;
		}
		if(e.getKeyCode() != KeyEvent.VK_CONTROL) {
			ctrl = false;
		}
		if(e.getKeyChar() == '\b') {//backspace
			out.backspace();
		} else if((int)e.getKeyChar() > 31 && (int)e.getKeyChar() < 128 || e.getKeyChar() == '\n'){
			//only print keys used for typing (not arrow keys, esc, etc)
			out.inputWrite(e.getKeyChar());
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
	
	}
}
//ConsoleWriter console = new ConsoleWriter(ta);
//console.close();
