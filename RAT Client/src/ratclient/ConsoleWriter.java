package ratclient;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import javax.swing.JTextArea;

public class ConsoleWriter extends PrintStream { //printstream to write to console
	private ConsoleStream output;
	private CommandLib commands;
	private String prefix;
	public ConsoleWriter(ConsoleStream output, CommandLib commands, String prefix) {
		super(output);
		this.output = output;
		this.commands = commands;
		this.prefix = prefix;
	}
	
	private boolean input = true;
	public void inputWrite(char c) {
		if(input) {
			if(c == '\n' && input) {
				commands.command(output.line);
			} else {
				output.inputWrite(c);	
			}
		}
	}
	
	public void clearLine() {
		output.clearLine();
	}
	
	public String getLine() {
		return output.getLine();
	}
	
	public void disableInput() {
		input = false;
	}
	
	public void enableInput() {
		input = true;
	}
	
	public void fixCaret() {
		output.output.getCaret().setVisible(true);
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public void clear() {
		output.clear();
	}
	
	public void backspace() {
		output.backspace();
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
}

class ConsoleStream extends OutputStream {
	ArrayList <Byte> data = new ArrayList<Byte>(); //data in console
	JTextArea output;
	String line; //Line
	
    public ConsoleStream(JTextArea output) {
        this.output = output;
        line = " "; //not null
    }

    private void updateData() {
    	int lines = 0;
        for (int i = 0; i < data.size(); i++) {
            byte b = data.get(i);
            if (b == 10) {
                lines++;
            }
            
            if (lines >= 500) {
                clear();//only keep 500 lines of data
            	//data = (ArrayList<Byte>) data.subList(i, data.size());
            }
        }
        StringBuilder bldr = new StringBuilder();
        for (byte b : data) {
            bldr.append((char) b);
        }
        output.setText(bldr.toString() + line); //append line to saved output
    }

    @Override
    public void write(int i) throws IOException {
    	data.add((byte) i);
    	updateData();
    }
    
    public void inputWrite(char c) {
    	line = line + c;
		updateData();
	}
	
    public void backspace() {
		if(line.length()-1 < 0) {//don't go past the caret
			//nothing
		} else {
			line = line.substring(0, line.length()-1);	
		}
		updateData();
	}
    
    public void clearLine() {
    	line = "";
    }
    
    public String getLine() {
    	return line;
    }
    
    public void clear() { //reset on screen data
    	line = "";
    	data = new ArrayList<Byte>();
    }
    
}