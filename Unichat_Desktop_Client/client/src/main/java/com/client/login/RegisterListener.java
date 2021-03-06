package com.client.login;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.security.auth.callback.ConfirmationCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.chatwindow.ChatController;
import com.messages.Constants;

import aboullaite.Data;
import aboullaite.LoginData;
import javafx.stage.Stage;

public class RegisterListener implements Runnable {
	
	private Socket socket;
	private String server;
    public RegisterController controller;
    public static POPController popController;
	private static ObjectOutputStream oos;
	private InputStream is;
	private ObjectInputStream input;
	private OutputStream outputStream;
	Logger logger = LoggerFactory.getLogger(RegisterController.class);
	
	
	
	public RegisterListener(String server, RegisterController controller) {
		super();
		this.server = server;
		this.controller = controller;
	}

	@Override
	public void run() {
		try {
			socket = new Socket(server, 2222);
			LoginController.getInstance().showScene();
			outputStream = socket.getOutputStream();
			oos = new ObjectOutputStream(outputStream);
			is = socket.getInputStream();
			input = new ObjectInputStream(is);
		} catch (IOException e) {
			LoginController.getInstance().showErrorDialog("Could not connect to server");
			logger.error("Could not Connect");
			return;
		}
		logger.info("Connection accepted " + socket.getInetAddress() + ":" + socket.getPort());

		try {
			logger.info("Sockets in and out ready!");
			while (socket.isConnected()) {
				Data message = null;
				message = (Data) input.readObject();
				if (message != null) {
					logger.debug("Message recieved:" + message.getMsg() + " MessageType:" + message.getType() + "Name:" + message.getSendorEmail());
                    switch (message.getMsg()) {
                    case Constants.REGISTER_FAIL_ID:
                    	controller.ShowErrorDuplicatedId();
                    	break;
                    case Constants.REGISTER_WAITING_AUTHCODE:
                    	controller.ShowConfirmationPOP();
                    	break;
                    case Constants.REGISTER_SUCCESS:
                    	popController.Popconf();
                    	break;
                    case Constants.REGISTER_FAIL_AUTHCODE:
                    	popController.ShowErrorInvaildCode();
                    	break;
                    case Constants.REGISTER_CANCEL:
                    	popController.closePopup();
                    	break;
                    }
				}
			}
		}
		catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void sendRegister(LoginData loginData) {
        try {
        	oos.writeObject(loginData);
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void sendAuthcode(String authcode) {
		try {
			oos.writeObject(new Data(Constants.TYPE_MSG, authcode, null));
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public POPController getPopController() {
		return popController;
	}

	public static void setPopController(POPController controller) {
		popController = controller;
	}
}
