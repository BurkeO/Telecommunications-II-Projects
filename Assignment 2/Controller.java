
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashMap;

import tcdIO.*;

//Name : Owen Burke
//Student Number : 15316452

public class Controller extends Node implements Runnable
{
	Terminal terminal;
	static final int DEFAULT_PORT = 5000;
	static final String DEFAULT_FINAL_DST_NODE = "localhost";
	DatagramSocket mainSocket;
	
	//hashmap with key being string ("dstClientPortNumber, srcClientPortNumber") and value being object with three arrays(three routers), each having a
	//sendOut[index 0] and nextPort [index 1]
	
	HashMap<String, RouterInfoInst> info = new HashMap<String, RouterInfoInst>();
	int[] initRouter1 = new int[2];
	int[] initRouter2 = new int[2];
	int[] initRouter3 = new int[2];
	int infoPacketCount = 0;
	int client1Port;
	int client2Port;
	
	private class RouterInfoInst
	{
		int[] routerOne;
		int[] routerTwo;
		int[] routerThree;
		
		RouterInfoInst(int[] routerOnePorts, int[] routerTwoPorts, int[] routerThreePorts)
		{
			this.routerOne = routerOnePorts.clone();
			this.routerTwo = routerTwoPorts.clone();
			this.routerThree = routerThreePorts.clone();
		}
	}

	Controller()
	{
		try
		{
			RouterInfoInst client1To2 = new RouterInfoInst(new int[]{10002, 20001}, new int[]{20002, 30001}, new int[]{30002, 50000});
			info.put("40000, 50000", client1To2);
			RouterInfoInst client2To1 = new RouterInfoInst(new int[]{10001, 40000}, new int[]{20001, 10002}, new int[]{30001, 20002});
			info.put("50000, 40000", client2To1);	//FOR IMPLEMENTATION PART1
			this.terminal = new Terminal("Controller");
			mainSocket = new DatagramSocket(Controller.DEFAULT_PORT);
			new Listener(mainSocket).start();
			new Thread(this).start();
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

	@Override
	public void onReceipt(DatagramPacket packet)
	{
		try
		{
			StringContent content = new StringContent(packet);
			String[] messageContent = (content.toString()).split("\n");
			/*if (messageContent[0].equals("Initialise"))							//////
			{
				this.initialiseArrays(messageContent);		///FOR IMPLEMENTATION PART2
				this.infoPacketCount++;
				if (infoPacketCount == 3)
				{
					this.makeTable();
				}
				return;
			}			*/													//////
			String srcClientPort = messageContent[0];
			String dstClientPort = messageContent[1];
			String fromRouterPort = messageContent[2];
			terminal.println("Received need for rules going to: " + dstClientPort + "\n from: " + srcClientPort + "\n from router port: " + fromRouterPort);
			RouterInfoInst routingInfo = this.info.get(srcClientPort + ", " + dstClientPort);
			if ((Integer.parseInt(srcClientPort) == 40000 && Integer.parseInt(dstClientPort) == 50000)
					|| (Integer.parseInt(srcClientPort) == 50000 && Integer.parseInt(dstClientPort) == 40000))//client 1 to 2 and vice versa
			{
				DatagramPacket infoResponse = (new StringContent(routingInfo.routerThree[0]+ "\n" + routingInfo.routerThree[1] + "\n" + dstClientPort)).toDatagramPacket();
				infoResponse.setSocketAddress(new InetSocketAddress(DEFAULT_FINAL_DST_NODE, 30000));
				mainSocket.send(infoResponse);
				infoResponse = (new StringContent(routingInfo.routerTwo[0]+ "\n" + routingInfo.routerTwo[1] + "\n" + dstClientPort)).toDatagramPacket();
				infoResponse.setSocketAddress(new InetSocketAddress(DEFAULT_FINAL_DST_NODE, 20000));
				mainSocket.send(infoResponse);
				infoResponse = (new StringContent(routingInfo.routerOne[0]+ "\n" + routingInfo.routerOne[1] + "\n" + dstClientPort)).toDatagramPacket();
				infoResponse.setSocketAddress(new InetSocketAddress(DEFAULT_FINAL_DST_NODE, 10000));
				mainSocket.send(infoResponse);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public synchronized void run() //throws Exception
	{
		try
		{
			terminal.println("Controller waiting for contact...");
			this.wait();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void initialiseArrays(String[] info)					//PART2 IMPLEMENTATION
	{
		terminal.println("Received Initial Info Packet \n from: " + info[1]);
		switch(Integer.parseInt(info[1]))
		{
		case 10000:
			this.initRouter1[1] = Integer.parseInt(info[3]);
			this.client1Port = Integer.parseInt(info[2]);
		case 20000:
			this.initRouter1[0] = Integer.parseInt(info[2]);
			this.initRouter2[1] = Integer.parseInt(info[3]);
		case 30000:
			this.initRouter2[0] = Integer.parseInt(info[2]);
			this.initRouter3[1] = Integer.parseInt(info[3]);
			this.initRouter3[0] = Integer.parseInt(info[1])+ 2;
			this.client2Port = this.initRouter3[1];
		}
	}
		
	public void makeTable()										//PART2 IMPLEMENTATION
	{
		RouterInfoInst client1To2 = new RouterInfoInst(this.initRouter1, this.initRouter2, this.initRouter3);
		info.put(this.client1Port + ", " + this.client2Port, client1To2);
		this.reverseArraysDirection();
		RouterInfoInst client2To1 = new RouterInfoInst(this.initRouter1, this.initRouter2, this.initRouter3);
		info.put(this.client2Port + ", " + this.client1Port, client2To1);
	}
	
	public void reverseArraysDirection()					//PART2 IMPLEMENTATION
	{
		int[] router1Temp = new int[2];
		int[] router2Temp = new int[2];
		int[] router3Temp = new int[2];
		router3Temp[0] = this.initRouter2[1];
		router3Temp[1] = this.initRouter2[0];
		router2Temp[0] = this.initRouter1[1];
		router2Temp[1] = this.initRouter1[0];
		router1Temp[0] = this.initRouter1[0] - 1;
		router1Temp[1] = this.client1Port;
		this.initRouter1 = router1Temp;
		this.initRouter2 = router2Temp;
		this.initRouter3 = router3Temp;
	}
}






	
	