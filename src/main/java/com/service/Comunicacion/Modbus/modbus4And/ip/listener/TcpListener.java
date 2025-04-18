/*
 * ============================================================================
 * GNU General Public License
 * ============================================================================
 *
 * Copyright (C) 2014 - MCA Desenvolvimento de Sistemas Ltda - http://www.mcasistemas.com.br
 * @author Diego R. Ferreira
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.service.Comunicacion.Modbus.modbus4And.ip.listener;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.service.Comunicacion.Modbus.modbus4And.ModbusMaster;
import com.service.Comunicacion.Modbus.modbus4And.base.BaseMessageParser;
import com.service.Comunicacion.Modbus.modbus4And.exception.ModbusInitException;
import com.service.Comunicacion.Modbus.modbus4And.exception.ModbusTransportException;
import com.service.Comunicacion.Modbus.modbus4And.ip.IpMessageResponse;
import com.service.Comunicacion.Modbus.modbus4And.ip.IpParameters;
import com.service.Comunicacion.Modbus.modbus4And.ip.encap.EncapMessageParser;
import com.service.Comunicacion.Modbus.modbus4And.ip.encap.EncapMessageRequest;
import com.service.Comunicacion.Modbus.modbus4And.ip.encap.EncapWaitingRoomKeyFactory;
import com.service.Comunicacion.Modbus.modbus4And.ip.xa.XaMessageParser;
import com.service.Comunicacion.Modbus.modbus4And.ip.xa.XaMessageRequest;
import com.service.Comunicacion.Modbus.modbus4And.ip.xa.XaWaitingRoomKeyFactory;
import com.service.Comunicacion.Modbus.modbus4And.msg.ModbusRequest;
import com.service.Comunicacion.Modbus.modbus4And.msg.ModbusResponse;
import com.service.Comunicacion.Modbus.modbus4And.sero.messaging.EpollStreamTransport;
import com.service.Comunicacion.Modbus.modbus4And.sero.messaging.MessageControl;
import com.service.Comunicacion.Modbus.modbus4And.sero.messaging.OutgoingRequestMessage;
import com.service.Comunicacion.Modbus.modbus4And.sero.messaging.StreamTransport;
import com.service.Comunicacion.Modbus.modbus4And.sero.messaging.Transport;
import com.service.Comunicacion.Modbus.modbus4And.sero.messaging.WaitingRoomKeyFactory;

public class TcpListener extends ModbusMaster {
    // Configuration fields.
    private final Log LOG = LogFactory.getLog(TcpListener.class);
    private short nextTransactionId = 0;
    private short retries = 0;
    private final IpParameters ipParameters;

    // Runtime fields.
    private ServerSocket serverSocket;
    private Socket socket;
    private ExecutorService executorService;
    private ListenerConnectionHandler handler;

    public TcpListener(IpParameters params) {
        LOG.debug("Creating TcpListener in port " + params.getPort());
        ipParameters = params;
        connected = false;
        LOG.debug("TcpListener created! Port: " + ipParameters.getPort());
    }

    protected short getNextTransactionId() {
        return nextTransactionId++;
    }

    @Override
    synchronized public void init() throws ModbusInitException {
        LOG.debug("Init TcpListener Port: " + ipParameters.getPort());
        executorService = Executors.newCachedThreadPool();
        startListener();
        initialized = true;
        LOG.warn("Initialized Port: " + ipParameters.getPort());
    }

    private void startListener() throws ModbusInitException {
        try {
            if (handler != null) {
                LOG.debug("handler not null!!!");
            }
            handler = new ListenerConnectionHandler(socket);
            LOG.debug("Init handler thread");
            executorService.execute(handler);
        }
        catch (Exception e) {
            LOG.warn("Error initializing TcpListener ", e);
            throw new ModbusInitException(e);
        }
    }

    @Override
    synchronized public void destroy() {
        LOG.debug("Destroy TCPListener Port: " + ipParameters.getPort());
        // Close the serverSocket first to prevent new messages.
        try {
            if (serverSocket != null)
                serverSocket.close();
        }
        catch (IOException e) {
            LOG.warn("Error closing socket" + e.getLocalizedMessage());
            getExceptionHandler().receivedException(e);
        }

        // Close all open connections.
        if (handler != null) {
            handler.closeConnection();
        }

        // Terminate Listener
        terminateListener();
        initialized = false;
        LOG.debug("TCPListener destroyed,  Port: " + ipParameters.getPort());
    }

    private void terminateListener() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(300, TimeUnit.MILLISECONDS);
            LOG.debug("Handler Thread terminated,  Port: " + ipParameters.getPort());
        }
        catch (InterruptedException e) {
            LOG.debug("Error terminating executorService - " + e.getLocalizedMessage());
            getExceptionHandler().receivedException(e);
        }
        handler = null;
    }

    @Override
    synchronized public ModbusResponse sendImpl(ModbusRequest request) throws ModbusTransportException {

        if (!connected) {
            LOG.debug("No connection in Port: " + ipParameters.getPort());
            throw new ModbusTransportException(new Exception("TCP Listener has no active connection!"),
                    request.getSlaveId());
        }

        if (!initialized) {
            LOG.debug("Listener already terminated " + ipParameters.getPort());
            return null;
        }

        // Wrap the modbus request in a ip request.
        OutgoingRequestMessage ipRequest;
        if (ipParameters.isEncapsulated()) {
            ipRequest = new EncapMessageRequest(request);
            StringBuilder sb = new StringBuilder();
            for (byte b : Arrays.copyOfRange(ipRequest.getMessageData(), 0, ipRequest.getMessageData().length)) {
                sb.append(String.format("%02X ", b));
            }
            LOG.debug("Encap Request: " + sb.toString());
        }
        else {
            ipRequest = new XaMessageRequest(request, getNextTransactionId());
            StringBuilder sb = new StringBuilder();
            for (byte b : Arrays.copyOfRange(ipRequest.getMessageData(), 0, ipRequest.getMessageData().length)) {
                sb.append(String.format("%02X ", b));
            }
            LOG.debug("Xa Request: " + sb.toString());
        }

        // Send the request to get the response.
        IpMessageResponse ipResponse;
        try {
            // Send data via handler!
            handler.conn.DEBUG = true;
            ipResponse = (IpMessageResponse) handler.conn.send(ipRequest);
            if (ipResponse == null) {
                throw new ModbusTransportException(new Exception("No valid response from slave!"), request.getSlaveId());
            }
            StringBuilder sb = new StringBuilder();
            for (byte b : Arrays.copyOfRange(ipResponse.getMessageData(), 0, ipResponse.getMessageData().length)) {
                sb.append(String.format("%02X ", b));
            }
            LOG.debug("Response: " + sb.toString());
            return ipResponse.getModbusResponse();
        }
        catch (Exception e) {
            LOG.debug(e.getLocalizedMessage() + ",  Port: " + ipParameters.getPort() + ", retries: " + retries);
            if (retries < 10 && !e.getLocalizedMessage().contains("Broken")) {
                retries++;
            }
            else {
                /*
                 * To recover from a Broken Pipe, the only way is to restart serverSocket
                 */
                LOG.debug("Restarting Socket,  Port: " + ipParameters.getPort() + ", retries: " + retries);

                // Close the serverSocket first to prevent new messages.
                try {
                    if (serverSocket != null)
                        serverSocket.close();
                }
                catch (IOException e2) {
                    LOG.debug("Error closing socket" + e2.getLocalizedMessage(), e);
                    getExceptionHandler().receivedException(e2);
                }

                // Close all open connections.
                if (handler != null) {
                    handler.closeConnection();
                    terminateListener();
                }

                if (!initialized) {
                    LOG.debug("Listener already terminated " + ipParameters.getPort());
                    return null;
                }

                executorService = Executors.newCachedThreadPool();
                try {
                    startListener();
                }
                catch (Exception e2) {
                    LOG.warn("Error trying to restart socket" + e2.getLocalizedMessage(), e);
                    throw new ModbusTransportException(e2, request.getSlaveId());
                }
                retries = 0;
            }
            LOG.warn("Error sending request,  Port: " + ipParameters.getPort() + ", msg: " + e.getMessage());
            // Simple send error!
            throw new ModbusTransportException(e, request.getSlaveId());
        }
    }

    class ListenerConnectionHandler implements Runnable {
        private Socket socket;
        private Transport transport;
        private MessageControl conn;
        private BaseMessageParser ipMessageParser;
        private WaitingRoomKeyFactory waitingRoomKeyFactory;

        public ListenerConnectionHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            LOG.debug(" ListenerConnectionHandler::run() ");

            if (ipParameters.isEncapsulated()) {
                ipMessageParser = new EncapMessageParser(true);
                waitingRoomKeyFactory = new EncapWaitingRoomKeyFactory();
            }
            else {
                ipMessageParser = new XaMessageParser(true);
                waitingRoomKeyFactory = new XaWaitingRoomKeyFactory();
            }

            try {
                acceptConnection();
            }
            catch (IOException e) {
                LOG.debug("Error in TCP Listener! - " + e.getLocalizedMessage(), e);
                conn.close();
                closeConnection();
                getExceptionHandler().receivedException(new ModbusInitException(e));
            }
        }

        private void acceptConnection() throws IOException, BindException {
            while (true) {
                try {
                    Thread.sleep(500);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!connected) {
                    try {
                        serverSocket = new ServerSocket(ipParameters.getPort());
                        LOG.debug("Start Accept on port: " + ipParameters.getPort());
                        socket = serverSocket.accept();
                        LOG.info("Connected: " + socket.getInetAddress() + ":" + ipParameters.getPort());

                        if (getePoll() != null)
                            transport = new EpollStreamTransport(socket.getInputStream(), socket.getOutputStream(),
                                    getePoll());
                        else
                            transport = new StreamTransport(socket.getInputStream(), socket.getOutputStream());
                        break;
                    }
                    catch (Exception e) {
                        LOG.warn(
                                "Open connection failed on port " + ipParameters.getPort() + ", caused by "
                                        + e.getLocalizedMessage(), e);
                        if (e instanceof SocketTimeoutException) {
                            continue;
                        }
                        else if (e.getLocalizedMessage().contains("closed")) {
                            return;
                        }
                        else if (e instanceof BindException) {
                            closeConnection();
                            throw (BindException) e;
                        }
                    }
                }
            }

            conn = getMessageControl();
            conn.setExceptionHandler(getExceptionHandler());
            conn.DEBUG = true;
            conn.start(transport, ipMessageParser, null, waitingRoomKeyFactory);
            if (getePoll() == null)
                ((StreamTransport) transport).start("Modbus4J TcpMaster");
            connected = true;
        }

        void closeConnection() {
            if (conn != null) {
                LOG.debug("Closing Message Control on port: " + ipParameters.getPort());
                closeMessageControl(conn);
            }

            try {
                if (socket != null) {
                    socket.close();
                }
            }
            catch (IOException e) {
                LOG.debug("Error closing socket on port " + ipParameters.getPort() + ". " + e.getLocalizedMessage());
                getExceptionHandler().receivedException(new ModbusInitException(e));
            }
            connected = false;
            conn = null;
            socket = null;
        }
    }
}
