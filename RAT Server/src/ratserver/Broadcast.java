package ratserver;

import java.net.*;

public class Broadcast extends Thread { //Used to let the client know where we should meet up
    String data;
    byte[] buffer;
    int port;
    boolean disconnect;
    
	public static void log(Exception e, String data) {
		e.printStackTrace();
		System.out.println(data);
	}
    
    public Broadcast(String data, int port) {
        this.data = data;
        this.buffer = data.getBytes();
        this.port = port;
        disconnect = false;
    }
    
    
	@Override
    public void run() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            while(true)
            {
                if(getDisconnect()) {
                	return;
                }		//send data to address    					//ip 224.134.68.1
                	socket.send(new DatagramPacket(buffer, buffer.length, Inet4Address.getByName("224.134.68.1"), port));
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    log(e, "Error sleeping in broadcast thread");
                    System.exit(-1);
                }
            }
        } catch (Exception e) {
            log(e, "Error broadcasting");
            System.exit(-1);
        }
        
        if(socket != null) {
        	socket.close();
        }
    }
    
    public boolean getDisconnect() { return disconnect; }
    public void disconnect() { disconnect = true; }
}
