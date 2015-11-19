package ratserver;

public abstract class RatCommand { //basic structure for a Remote administration command to run on server
	public boolean raw;
	private String name;
	int argc;
	
	public RatCommand(String name) {
		this.name = name;
		argc = 0;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public abstract void run(int argc, String args[]);
}
