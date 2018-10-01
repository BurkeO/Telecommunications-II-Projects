
/**
 * 
 */

import java.net.DatagramSocket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;

import tcdIO.*;

//Name : Owen Burke
//Student Number : 15316452

public class Client extends Node
{
	int srcPort;
	int tiedRouterPort;
	int dstClientPort;
	static final String DEFAULT_FINAL_DST_NODE = "localhost";
	Terminal terminal;
	InetSocketAddress routerAddress;
	DatagramSocket mainSocket;

	Client(String name, int srcPort, int tiedRouterPort, int dstClientPort) throws SocketException
	{
		try
		{
			this.srcPort = srcPort;
			this.tiedRouterPort = tiedRouterPort;
			this.dstClientPort = dstClientPort;
			
			this.routerAddress = new InetSocketAddress(DEFAULT_FINAL_DST_NODE, this.tiedRouterPort);
			mainSocket = new DatagramSocket(srcPort);
			this.terminal= new Terminal(name);
			mainSocket.connect(new InetSocketAddress(DEFAULT_FINAL_DST_NODE, tiedRouterPort));
			new Listener(mainSocket).start();
			this.run();
		}
		catch(java.lang.Exception e) 
		{
			e.printStackTrace();
		}
	}

	public synchronized void onReceipt(DatagramPacket packet)
	{
		try
		{
			StringContent content = new StringContent(packet);
			String[] messageContent = content.toString().split("\n");
			terminal.println("\nRecieved: " + messageContent[0]);
			terminal.print("String to send: ");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public synchronized void run() throws Exception
	{
		try
		{
			new Thread()
			{
				public void run()
				{
					try
					{
						while(true)
						{
							DatagramPacket packet = null;			
							byte[] payload = null;
							byte[] header = null;
							byte[] buffer = null;
							
							payload = (terminal.readString("String to send: ") + "\n" + srcPort + "\n" + dstClientPort).getBytes();
					
							header = new byte[PacketContent.HEADERLENGTH];
					
							buffer = new byte[header.length + payload.length];
							System.arraycopy(header, 0, buffer, 0, header.length);
							System.arraycopy(payload, 0, buffer, header.length, payload.length);
					
							terminal.println("Sending packet...");
							routerAddress = new InetSocketAddress(DEFAULT_FINAL_DST_NODE, tiedRouterPort);
							packet = new DatagramPacket(buffer, buffer.length, routerAddress);
							mainSocket.send(packet);
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}.start();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}




