
/**
 * 
 */

import java.net.DatagramSocket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.HashMap;

import tcdIO.*;

//Name : Owen Burke
//Student Number : 15316452

public class Router extends Node implements Runnable
{
	InetSocketAddress controllerAddress = new InetSocketAddress(DEFAULT_FINAL_DST_NODE, Controller.DEFAULT_PORT);
	static final String DEFAULT_FINAL_DST_NODE = "localhost";
	Terminal terminal;
	DatagramPacket tempPacket;
	boolean isHoldingPacket = false;
	
	HashMap<String, portPairing> rules = new HashMap<String, portPairing>();
	
	int localPort;
	int remotePorts[];
	int socketCount;
	DatagramSocket[] sockets;
	
	private class portPairing
	{
		int sendOutPort;
		int toNextPort;
		
		portPairing(int sendOutPort, int toNextPort)
		{
			this.sendOutPort = sendOutPort;
			this.toNextPort = toNextPort;
		}
	}
	
	Router(String name, int localPort, int[] remotePorts)
	{
		try
		{
			this.localPort = localPort;
			this.remotePorts = remotePorts.clone();
			this.socketCount = remotePorts.length;
			this.sockets = new DatagramSocket[socketCount];
			for(int i = 0;i<socketCount;i++)
			{
				sockets[i] = new DatagramSocket(localPort+i);
				sockets[i].connect(new InetSocketAddress(DEFAULT_FINAL_DST_NODE, remotePorts[i]));
				new Listener(sockets[i]).start();
			}
			this.terminal= new Terminal(name);
			Thread thread = new Thread(this);
			//this.initialise();						//FOR IMPLEMENTATION PART2
			thread.start();
			
		} 
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public synchronized void onReceipt(DatagramPacket packet)
	{
		try
		{
			StringContent content = new StringContent(packet);
			String[] messageContent = (content.toString()).split("\n");
			if(packet.getSocketAddress().equals(controllerAddress))
			{
				this.rules.put(messageContent[2], new portPairing(Integer.parseInt(messageContent[0]), Integer.parseInt(messageContent[1])));
				if (this.isHoldingPacket == true)
				{
					int sendOutPort = this.rules.get(messageContent[2]).sendOutPort;
					int nextPort = this.rules.get(messageContent[2]).toNextPort;
					for (int i = 0; i < this.sockets.length; i++)
					{
						if (sockets[i].getLocalPort() == sendOutPort)
						{
							this.tempPacket.setSocketAddress(new InetSocketAddress(DEFAULT_FINAL_DST_NODE, nextPort));
							sockets[i].send(this.tempPacket);
							i = this.sockets.length;
						}
					}
				}
				this.isHoldingPacket = false;
			}
			else
			{
				String srcClientPort = messageContent[1];
				String dstClientPort = messageContent[2];
				if(this.rules.containsKey(dstClientPort))
				{
					this.terminal.println("Has Rule. Forwaring Packet: " + messageContent[0]);
					this.terminal.println(this.rules.get(dstClientPort).toNextPort+ "");
					this.terminal.println(this.rules.get(dstClientPort).sendOutPort+ "");
					packet.setSocketAddress(new InetSocketAddress(DEFAULT_FINAL_DST_NODE, this.rules.get(dstClientPort).toNextPort));
					int sendOutPort = this.rules.get(dstClientPort).sendOutPort;
					for (int i = 0; i < this.sockets.length; i++)
					{
						if (sockets[i].getLocalPort() == sendOutPort)
						{
							sockets[i].send(packet);
							i = this.sockets.length;
						}
					}
				}
				else
				{
					this.terminal.println("No Rule.\nContacting Controller....");
					this.isHoldingPacket = true;
					this.tempPacket = packet;
					DatagramPacket packetToController = (new StringContent(srcClientPort + "\n" + dstClientPort + "\n" + 
														sockets[0].getLocalPort())).toDatagramPacket();
					packetToController.setSocketAddress(controllerAddress);
					sockets[0].send(packetToController);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public synchronized void run()
	{
		try
		{
			terminal.println("Router waiting for contact...");
			this.wait();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public synchronized void initialise()
	{
		try
		{
			this.terminal.println("Sending initialisation info");
			this.wait(100);								
			DatagramPacket routerInfo = (new StringContent("Initialise" + "\n" + this.localPort + "\n" + this.remotePorts[1] + "\n" + this.remotePorts[2])).toDatagramPacket();
			this.sockets[0].send(routerInfo);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}








