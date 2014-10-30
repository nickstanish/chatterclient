package net.vizbits.chatterclient.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.text.SimpleAttributeSet;

import net.vizbits.chatterclient.Message;

import com.google.gson.Gson;


public abstract class Client implements Connection, Runnable {
  private BufferedReader in; // to read from the socket
  private PrintWriter out; // to write on the socket
  private Socket socket;
  private Gson gson = new Gson();
  private boolean keepGoing = true;

  protected final boolean prepareAndSendMessage(Message.Type type, String[] recipients,
      String message, SimpleAttributeSet style, boolean typing) {
    Message m = null;
    switch (type) {
      case Command:
        m = new Message(Message.Type.Command, message);
        break;
      case Contacts:
        m = new Message(Message.Type.Contacts);
        break;
      case Logout:
        m = new Message(Message.Type.Logout);
        break;
      case Message:
        m = new Message(message, style, recipients);
        break;
      case Typing:
        m = new Message(typing, message, recipients);
        break;
      default:
        return false;
    }
    try {
      if (m != null)
        send(gson.toJson(m));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return false;
    }
    return true;
  }

  @Override
  public final boolean connect(String username, String host, int port) throws IOException,
      UsernameTakenException {
    socket = new Socket(host, port);
    socket.setKeepAlive(true);
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    out = new PrintWriter(socket.getOutputStream(), true);
    out.println(username);
    String validation = in.readLine();
    if (validation.charAt(0) == '0') {
      throw new UsernameTakenException(username);
    }
    new Thread(this).start();
    // success we inform the caller that it worked
    return true;
  }

  @Override
  public final void disconnect() {
    prepareAndSendMessage(Message.Type.Logout, null, null, null, false);
    keepGoing = false;
    try {
      socket.setKeepAlive(false);
    } catch (SocketException e) {
      e.printStackTrace();
    }
    try {
      if (in != null)
        in.close();
    } catch (Exception e) {
    } // not much else I can do

    try {
      if (out != null)
        out.close();
    } catch (Exception e) {
    } // not much else I can do

    try {
      if (socket != null)
        socket.close();
    } catch (Exception e) {
    } // not much else I can do
  }

  /**
   * Transfer Strings to the server via PrintWriter for transferring data, use a(n
   * Buffered)OutputStream Xfer is threaded to reduce delays in UI, but could potentially screw up
   * order of sending..
   */
  @Override
  public final boolean send(String s) throws IOException {
    if (connected()) {
      // send in thread
      final String message = s; // final variable so runnable doesnt complain
      Thread t = new Thread(new Runnable() {
        public void run() {
          synchronized (out) {
            out.println(message);
          }
        }
      });
      t.start();
      return true;
    }
    // disconnected
    return false;

  }

  /**
   * unimplemented
   */
  @Override
  public boolean reconnect(int timeout) {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * returns boolean if connection is active
   */
  @Override
  public final boolean connected() {
    if (socket != null) {
      return socket.isConnected();
    }
    return false;

  }

  /**
   * Run method for the listening thread of the client called in the connect method should run after
   * started until told to stop through disconnect
   */
  @Override
  public final void run() {
    keepGoing = true;
    while (keepGoing) {
      String line = null;
      try {
        line = in.readLine();
      } catch (IOException e) {
        messageHandler(new Message(Message.Type.Error, "IO Error in thread "
            + Thread.currentThread().getName()));
      }
      if (line == null)
        continue;
      Message m = gson.fromJson(line, Message.class);
      messageHandler(m);
    }
  }

  public abstract void messageHandler(Message m);


}
