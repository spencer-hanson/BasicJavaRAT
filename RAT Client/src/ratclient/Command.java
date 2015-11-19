package ratclient;

//Command Class, all commands must be created using this as the superclass
public abstract class Command {

	private String name;
	private String desc;
	private String syntax;
	private int argc;
	
	public Command(String name, String desc, String syntax) {
		this.name = name;
		this.desc = desc;
		this.syntax = syntax;
		argc = 0;
	}
	
	public Command(String name, int argc, String desc, String syntax) {
		this.name = name;
		this.desc = desc;
		this.argc = argc;
		this.syntax = syntax;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setDesc(String desc) { //Description of command
		this.desc = desc;
	}
	
	public String getDesc() {
		return desc;
	}
	
	public void setArgc(int argc) {
		this.argc = argc;
	}
	
	public int getArgc() { //Number of arguments the command takes
		return argc;
	}
	
	public void setSyntax(String syntax) { //How to use the command
		this.syntax = syntax;
	}
	
	public String getSyntax() {
		return syntax;
	}
	
	public abstract void run(int argc, String argv[]); //Implementation of command
}
