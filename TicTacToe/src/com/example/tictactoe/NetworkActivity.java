package com.example.tictactoe;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import org.apache.http.conn.util.InetAddressUtils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/*
 * This activity is intended to set up a local socket connection between two Android devices.
 * It currently connects two devices on the same network (client and server) and allows messages to be sent back and forth
 * It also immediately notifies the user if the game is disconnected
 * Eventually, game objects (i.e. a move in the form of a Point) will be passed back and forth as part of a network game. 
 */
public class NetworkActivity extends Activity implements Constants {
	
	//Default server IP address
	private static String SERVER_IP_ADDRESS = "192.168.0.189";
	private static String LOCAL_IP_ADDRESS = "";
	
	//Default port to listen over
	private static final int SERVER_PORT = 8080;
	
	//Informs user of networking status changes
	private TextView serverOutput;
	
	//Message input and output
	private TextView messageOutput;
	private EditText messageInput;
	
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private ObjectOutputStream objectOut;
	private ObjectInputStream objectIn;
	
	private DatagramSocket dgSocket;
	
	//Handler for UI events that occur in a separate thread (i.e. a network related thread)
	private Handler outputHandler;
	
	private Button clientButton;
	private Button serverButton;
	private Button stopButton;
	private Button sendButton;
	
	private boolean connected;
	private boolean stopConnection;
	private boolean newSendMessage;
	private boolean clientReadyToConnect;
	
	private String message;
	private String allMessages;
	private String currentNetworkClass;
	private String oppositeNetworkClass;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		setContentView(R.layout.network_activity_screen);
		
		//For later use in passing game parameters for a network tictactoe game
		Intent networkButtonIntent = getIntent();
		
		serverOutput = (TextView) findViewById(R.id.textViewServerStatus);
		messageOutput = (TextView) findViewById(R.id.textViewMessageDisplay);
		messageOutput.setMovementMethod(new ScrollingMovementMethod());
		messageInput = (EditText) findViewById(R.id.editTextMessage);
		messageInput.setEnabled(false);
				
		outputHandler = new Handler();
		
		clientButton = (Button) findViewById(R.id.buttonClient);
		serverButton = (Button) findViewById(R.id.buttonServer);
		stopButton = (Button) findViewById(R.id.buttonStop);
		sendButton = (Button) findViewById(R.id.buttonSend);
		
		stopButton.setEnabled(false);
		sendButton.setEnabled(false);
		
		clientSocket = null;
		serverSocket = null;
		objectOut = null;
		objectIn = null;
		
		clientButton.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				resetBooleans();
				LOCAL_IP_ADDRESS = getLocalIPAddress();
				if(LOCAL_IP_ADDRESS!=""){
					messageInput.setEnabled(true);
					messageInput.setText(getIPSubstring(LOCAL_IP_ADDRESS));
					sendButton.setText("Connect");
					sendButton.setEnabled(true);
					serverButton.setEnabled(false);
					stopButton.setEnabled(true);
					clientButton.setEnabled(false);
					serverOutput.setText("Please enter Server IP Address and tap Connect");
					clientReadyToConnect = true;
				} else {
					serverOutput.setText("No network connection detected");
				}
			}
		});
		
		serverButton.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				resetBooleans();
				serverOutput.setText("Starting server, waiting for client...");
				new Thread(new Server()).start();
				serverButton.setEnabled(false);
				clientButton.setEnabled(false);
				stopButton.setEnabled(true);
			}
		});
		
		stopButton.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				stopConnection = true;
				closeConnections();
				resetButtons();
				serverOutput.setText("Connection stopped");
			}
		});
		
		sendButton.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				if(clientReadyToConnect){
					String potentialIP = messageInput.getText().toString();
					potentialIP.replace(" ", "");
					if(isValidIPFormat(potentialIP)){
						clientReadyToConnect = false;
						messageInput.setEnabled(false);
						messageInput.setText("");
						sendButton.setEnabled(false);
						sendButton.setText("Send");
						SERVER_IP_ADDRESS = potentialIP;
						new Thread(new Client()).start();
						serverOutput.setText("Starting client, searching for server...");
					} else {
						serverOutput.setText("Please enter a valid IP Address");
					}
				} else {
					message = messageInput.getText().toString();
					if(!message.equals("")){
						newSendMessage = true;
					} else {
						serverOutput.setText("No text in message, cannot send!");
					}
				}
			}
		});
	}
	
	@Override
	public void onStop(){
		closeConnections();
		super.onStop();
	}
	private void resetButtons(){
		messageInput.setText("");
		messageInput.setEnabled(false);
		messageOutput.setText("");
		sendButton.setEnabled(false);
		stopButton.setEnabled(false);
		serverButton.setEnabled(true);
		clientButton.setEnabled(true);	
	}
	
	private void resetBooleans(){
		stopConnection = false;
		connected = false;
		newSendMessage = false;
		clientReadyToConnect = false;
	}
	
	private void closeConnections(){
		connected=false;
		try {
			if(clientSocket!=null) clientSocket.close();
			if(serverSocket!=null) serverSocket.close();
			if(objectOut!=null)	objectOut.close();
			if(objectIn!=null) objectIn.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		clientSocket = null;
		serverSocket = null;
		objectOut = null;
		objectIn = null;
	}	

	class UpdateOutput implements Runnable{
		TextView outputDisplay;
		String outputText;
		public UpdateOutput(String outputTxt, TextView textView){
			outputDisplay = textView;
			outputText = outputTxt;
		}
		@Override
		public void run() {
			outputDisplay.setText(outputText);
		}
	}
	
	class Server implements Runnable {
			
		@Override
		public void run() {
			currentNetworkClass = "server";
			oppositeNetworkClass = "client";
			LOCAL_IP_ADDRESS = getLocalIPAddress();
			if(LOCAL_IP_ADDRESS!=""){
				try{
					//checks to see if the serverSocket is already set up
					if(serverSocket==null) {
						serverSocket = new ServerSocket(SERVER_PORT);
						outputHandler.post(new UpdateOutput("Listening on IP " + LOCAL_IP_ADDRESS + " over port " + SERVER_PORT, serverOutput));
					}
				}catch(IOException e){
					e.printStackTrace();
					outputHandler.post(new UpdateOutput("Error, could not open a socket over port " + SERVER_PORT, serverOutput));
				}
				//checks to see if the clientSocket is already connected for some reason
				if(clientSocket==null){
					 try{
						 //Server starts listening for client
					 	clientSocket = serverSocket.accept();
					 	//Opens input and output streams
					 	objectOut = new ObjectOutputStream(clientSocket.getOutputStream());
					 	objectOut.flush();
					 	objectIn = new ObjectInputStream(clientSocket.getInputStream());
						outputHandler.post(new UpdateOutput("Client connected!", serverOutput));
						connected = true;
						
						//Continuously checks for a dropped connection on a new thread, notifies user if connection is dropped
						//new Thread(new checkForDisconnect("Client disconnected")).start();
						//Enable the send button and text field
						outputHandler.post(new Runnable(){
							@Override
							public void run(){
								messageInput.setEnabled(true);
								sendButton.setEnabled(true);
							}
						});
					//catches any exceptions from serverSocket.accept()	
					}catch(IOException e){
						e.printStackTrace();
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				//listen for incoming messages
				new Thread(new listenForNewMessages()).start();
				//check if there are any new messages to send;
				while(connected && !stopConnection){
					if(newSendMessage) sendMessage(message);
				}
			} else {
				outputHandler.post(new UpdateOutput("No network connection detected", serverOutput));		
			}
		}
	}
	
	class Client implements Runnable {
		@Override
		public void run() {
			currentNetworkClass = "client";
			oppositeNetworkClass = "server";
			int numAttempts = 0;
			while(!connected && !stopConnection && clientSocket==null){
				//continuously attempts to connect to the server
				try{
					clientSocket = new Socket(SERVER_IP_ADDRESS, SERVER_PORT);
					objectOut = new ObjectOutputStream(clientSocket.getOutputStream());
					objectOut.flush();
				 	objectIn = new ObjectInputStream(clientSocket.getInputStream());
					outputHandler.post(new UpdateOutput("Successfully connected to the server!", serverOutput));
					connected = true;
				}catch (IOException ioe){
					ioe.printStackTrace();
				} catch (Exception e){
					e.printStackTrace();
				}
				if(!connected){
					numAttempts++;
					outputHandler.post(new UpdateOutput("Tried to connect to IP " + SERVER_IP_ADDRESS + " " + numAttempts + " times, reattempting...", serverOutput));
				}
			}
			//Enable the send message button and message text field
			outputHandler.post(new Runnable(){
				@Override
				public void run(){
					messageInput.setEnabled(true);
					sendButton.setEnabled(true);
				}
			});
			new Thread(new listenForNewMessages()).start();
			while(connected && !stopConnection){
				if(newSendMessage) sendMessage(message);
			}
		}
	}
			
	private void handleDisconnect(String disconnectText){
		closeConnections();
		outputHandler.post(new Runnable(){
			@Override
			public void run(){
				resetButtons();
			}
		});
		outputHandler.post(new UpdateOutput(disconnectText, serverOutput));
	}
	
	/**Sends a message over the network**/
	private void sendMessage(String msg){
		try{
			objectOut.writeObject(msg);
			objectOut.flush();
			outputHandler.post(new UpdateOutput("Message sent!", serverOutput));
			outputHandler.post(new Runnable(){
				@Override
				public void run(){
					messageInput.setText("");
				}
			});
			if(allMessages==null){
				allMessages = currentNetworkClass +": " +msg;
			} else {
				allMessages = allMessages+"\n"+currentNetworkClass + ": " + msg;
			}
			outputHandler.post(new UpdateOutput(allMessages, messageOutput));
			newSendMessage = false;
		} catch (IOException ioe){
			ioe.printStackTrace();
			handleDisconnect("Could not send message, " + oppositeNetworkClass + " disconnected");
		}
	}
	
	/**Continuously listens for new messages**/
	class listenForNewMessages implements Runnable{
		@Override
		public void run(){
			while(connected){
				try{
					String newMessageReceived = (String) objectIn.readObject();
					outputHandler.post(new UpdateOutput("Message received!", serverOutput));
					if(allMessages==null){
						allMessages = oppositeNetworkClass +": " + newMessageReceived;
					} else {
						allMessages = allMessages+"\n"+oppositeNetworkClass + ": " + newMessageReceived;
					}
					outputHandler.post(new UpdateOutput(allMessages, messageOutput));
				} catch (ClassNotFoundException classnfe){
					classnfe.printStackTrace();
					if(!stopConnection)	handleDisconnect(oppositeNetworkClass + " connection lost");
				} catch (IOException ioe){
					ioe.printStackTrace();
					if(!stopConnection)	handleDisconnect(oppositeNetworkClass + " connection lost");
				}
			}
		}
	}
	
	/**Returns the local IP address (i.e. 192.168... or 10.0...**/
	private String getLocalIPAddress(){
		try{
			for (Enumeration<NetworkInterface> enumNI = NetworkInterface.getNetworkInterfaces(); enumNI.hasMoreElements();){
				NetworkInterface NetInt = enumNI.nextElement();
				for(Enumeration<InetAddress> enumIPAddress = NetInt.getInetAddresses(); enumIPAddress.hasMoreElements();){
					InetAddress IPAddress = enumIPAddress.nextElement();
					if(!IPAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(IPAddress.getHostAddress())){
						return IPAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException se){
			Log.e("getLocalIPAddress - NetworkActivity", se.toString());
		}
		return "";
	}
	
	private InetAddress getServerIPAddress() throws IOException {
		WifiManager wifiMan = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcpInfo = wifiMan.getDhcpInfo();
		if(dhcpInfo!=null){
			int broadcast = (dhcpInfo.ipAddress & dhcpInfo.netmask) | ~dhcpInfo.netmask;
			byte [] fours = new byte[4];
			for(int i=0; i<fours.length; i++){
				fours[i] = (byte) ((broadcast >> i*8) & 0xFF);
			}
			return InetAddress.getByAddress(fours);
		}
		return null;
	}
	
	private String getIPSubstring(String IPAddress){
		return IPAddress.substring(0, IPAddress.lastIndexOf(".")+1);	
	}
	
	private boolean isValidIPFormat(String IPAddress){
		String IPSubstring = getIPSubstring(LOCAL_IP_ADDRESS);
		if(IPAddress.startsWith(IPSubstring) && IPAddress.length() > IPSubstring.length()){
			for(int i = IPAddress.lastIndexOf(".")+1; i<IPAddress.length(); i++){
				char c = IPAddress.charAt(i);
				if(!Character.isDigit(c))	return false;
			}
			return true;
		}
		return false;
	}
	
	class DatagramServer implements Runnable{

		@Override
		public void run() {
			currentNetworkClass = "server";
			oppositeNetworkClass = "client";
			LOCAL_IP_ADDRESS = getLocalIPAddress();
			if(LOCAL_IP_ADDRESS!=""){
				try{
					//checks to see if the serverSocket is already set up
					if(dgSocket==null) {
						dgSocket = new DatagramSocket(SERVER_PORT);
						outputHandler.post(new UpdateOutput("Listening on IP " + LOCAL_IP_ADDRESS + " over port " + SERVER_PORT, serverOutput));
					}
				}catch(IOException e){
					e.printStackTrace();
					outputHandler.post(new UpdateOutput("Error, could not open a socket over port " + SERVER_PORT, serverOutput));
				}
				//checks to see if the clientSocket is already connected for some reason
				if(clientSocket==null){
					 try{
						 //Server starts listening for client
					 	clientSocket = serverSocket.accept();
					 	//Opens input and output streams
					 	objectOut = new ObjectOutputStream(clientSocket.getOutputStream());
					 	objectOut.flush();
					 	objectIn = new ObjectInputStream(clientSocket.getInputStream());
						outputHandler.post(new UpdateOutput("Client connected!", serverOutput));
						connected = true;
						
						//Continuously checks for a dropped connection on a new thread, notifies user if connection is dropped
						//new Thread(new checkForDisconnect("Client disconnected")).start();
						//Enable the send button and text field
						outputHandler.post(new Runnable(){
							@Override
							public void run(){
								messageInput.setEnabled(true);
								sendButton.setEnabled(true);
							}
						});
					//catches any exceptions from serverSocket.accept()	
					}catch(IOException e){
						e.printStackTrace();
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				//listen for incoming messages
				new Thread(new listenForNewMessages()).start();
				//check if there are any new messages to send;
				while(connected && !stopConnection){
					if(newSendMessage) sendMessage(message);
				}
			} else {
				outputHandler.post(new UpdateOutput("Couldn't detect a local network connection", serverOutput));		
			}
		}
	}
}
