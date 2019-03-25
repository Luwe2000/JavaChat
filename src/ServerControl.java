import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;

public class ServerControl implements Runnable
{
	private JTextField textFieldPort;
	private JTextField textFieldLocalHost;
	private JLabel labelStatus;
	private JList list;
	private DefaultListModel<String> nachrichten;
	private ArrayList<ClientProxy> proxyList = new ArrayList<>();
	private ServerSocket server;
	private Thread t;
	private Socket client = null;
	
	public ServerControl(JTextField textFieldPort, JTextField textFieldLocalHost, JLabel labelStatus, JList list, DefaultListModel<String> nachrichten)
	{
		this.textFieldPort = textFieldPort;
		this.textFieldLocalHost = textFieldLocalHost;
		this.labelStatus = labelStatus;
		this.list = list;
		this.nachrichten = nachrichten;
		t = new Thread(this);
	}
	
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					ServerGui frame = new ServerGui();
					frame.setVisible(true);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}
	
	public void run()
	{
		try
		{		
			server = new ServerSocket(Integer.parseInt(textFieldPort.getText()));
			while(!Thread.currentThread().isInterrupted())
			{
				client = server.accept();
				if(client.isBound())
				{
					labelStatus.setText("Client verbunden");
					ClientProxy c = new ClientProxy(client, this);
					System.out.println("bla");
					proxyList.add(c);
				}
				else
					System.out.println("blubb");
				t.sleep(10);
			}
			
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			try
			{
				server.close();
			} catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
	}
	
	public void verarbeiteNachricht(Protokoll p)
	{
		
		if(p.isMsg())
		{
			nachrichten.addElement(p.getMessage());
			broadcastMessage(p);
			System.out.println("broadcasted " + p);
		}
		if(p.isLogout())
		{
			
		}
		
	}
	
	public void broadcastMessage(Protokoll p)
	{
		for (ClientProxy cp : proxyList)
		{
			try
			{
				cp.writeMessage(p);
			} catch (SocketException e)
			{
				proxyList.remove(cp);
			}
		}
	}
	
	public void starteServer()
	{
		t.start();		
	}
	
	public void beendeServer()
	{
		t.interrupt();
		Socket dummySocket;
		try
		{
			dummySocket = new Socket("localhost", 8008);
			server.close();
			dummySocket.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}

}