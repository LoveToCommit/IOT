// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.iothub;

import org.ibex.nestedvm.UnixRuntime;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by prmathur on 11/16/2016.
 */
public class SocketIo
{
    private RunTime rt;
    private Logger lg;
    private Socket socket;
    private static final int SOCKET = 0;
    private static final int CLOSE = 1;
    private static final int SEND = 2;
    private static final int RECEIVE = 3;

    public SocketIo(RunTime rt) throws IOException
    {
        this.lg = new Logger();
        if (rt != null)
        {
            this.rt =  rt;
        }
        else
        {
            this.lg.print("Runtime cannot be NULL");
            throw new IOException("Cannot start SocketIo");
        }

    }

    //socket_open
    public int SocketIoOpen(String serverAddress, int port) throws IOException
    {
        int result = -1;

        this.socket = new Socket(serverAddress, port);
        if (this.socket.isConnected())
        {
            result = 0;
        }
        else
        {
            result = -1;
            this.lg.print("Socket could not be connected");
            throw new IOException("Cannot open SocketIo");
        }

        return result;
    }

    //socket_close
    public void SocketIoClose() throws IOException
    {
        this.socket.close();
        this.lg.print("Socket is disconnected");
    }

    //socket_send
    public int SocketIoSend(byte[] inBuffer) throws IOException
    {
        int result = -1;

        DataOutputStream outToServer = new DataOutputStream(this.socket.getOutputStream());
        if (outToServer != null)
        {
            outToServer.write(inBuffer);
            // Java does not tell actual number of bytes written on the network. Use flush if inaccurate results.
            result = inBuffer.length;
            outToServer.close();
            this.lg.print("inBuffer written on socket");

        }
        return result;
    }

    //socket_receive
    public int SocketIoReceive(byte[] outBuffer, int length) throws IOException
    {
        int result = -1;

        if (outBuffer != null && outBuffer.length == length)
        {
            DataInputStream fromServer = new DataInputStream(this.socket.getInputStream());
            if(fromServer != null)
            {
                fromServer.readFully(outBuffer, 0, outBuffer.length); // if, won't work try incremental reading
                result = outBuffer.length;
                fromServer.close();
            }
            else
            {
                this.lg.print("Input Stream cannot be null");
                result = -1;
            }
        }
        else
        {
            this.lg.print("outBuffer should be of length : " + length);
            result = -1;
        }

        return result;
    }

    //socket_setCallbacks
    public int SocketIoSetCallbacks() throws IOException
    {
        int result = -1;
        final SocketIo SocketIOInstance = this;

        rt.getRuntime().setCallJavaCB(new UnixRuntime.CallJavaCB()
        {
            public int call(int op, int args, int unused1, int unused2)
            {
                int result = -1;
                switch(op)
                {
                    case SOCKET:
                    {
                        try
                        {
                            rt.useRuntime();

                            /*
                                Expected struct declaration in C as follows :
                                struct socket
                                {
                                    int port;
                                    char* hostName;
                                }
                             */
                            int port = rt.getRuntime().memRead(args + 0);
                            int hostNameAddr = rt.getRuntime().memRead(args + 4);
                            String hostName = rt.getRuntime().cstring(hostNameAddr);
                            rt.freeRuntime();

                            result = SocketIOInstance.SocketIoOpen(hostName, port);
                            if (result < 0)
                            {
                                SocketIOInstance.lg.print("Unable to open socket from call back");
                            }

                        }
                        catch (Exception e)
                        {
                            result = -1;
                        }

                        break;
                    }

                    case CLOSE :
                    {
                        try
                        {
                            SocketIOInstance.SocketIoClose();
                        }
                        catch (Exception e)
                        {
                            SocketIOInstance.lg.print("Unable to close socket from call back");
                            result = -1;
                        }
                        break;
                    }

                    case SEND:
                    {
                        try
                        {
                            rt.useRuntime();
                            /*
                                Expected struct declaration in C as follows :
                                struct send
                                {
                                    int messageLength;
                                    char* message;
                                }
                             */
                            int messageLength = rt.getRuntime().memRead(args + 0);
                            int messageAddr = rt.getRuntime().memRead(args + 4);
                            String message = rt.getRuntime().cstring(messageAddr);
                            rt.freeRuntime();
                            result = SocketIOInstance.SocketIoSend(message.getBytes());

                            if (result < 0)
                            {
                                SocketIOInstance.lg.print("Unable to send data to socket from call back");
                            }
                        }
                        catch (Exception e)
                        {
                            SocketIOInstance.lg.print("Unable to send data to socket from call back");
                            result = -1;
                        }
                        break;

                    }

                    case RECEIVE:
                    {
                        try
                        {
                            rt.useRuntime();
                            /*
                                Expected struct declaration in C as follows :
                                struct receive
                                {
                                    int messageLength;
                                    //char* message;
                                }
                             */

                            int messageLength = rt.getRuntime().memRead(args + 0);

                            byte[] buffer = new byte[messageLength];
                            result = SocketIOInstance.SocketIoReceive(buffer, messageLength);

                            if (result > 0)
                            {
                                // Try returning as an argument later
                                //rt.getRuntime().memWrite(args + 4, rt.getRuntime().strdup(new String(buffer)));
                                result = rt.getRuntime().strdup(new String(buffer));
                            }
                            else
                            {
                                SocketIOInstance.lg.print("Unable to receive data from socket from call back");
                            }
                            rt.freeRuntime();
                        }
                        catch (Exception e)
                        {
                            SocketIOInstance.lg.print("Unable to receive data from socket from call back");
                            result = -1;
                        }
                        break;
                    }
                    default:
                    {
                        SocketIOInstance.lg.print("Not an option for socket call backs");
                        break;
                    }
                }
                return result;
            }
        });

        result = 0;
        this.lg.print("Completed setting callbacks for socket");
        return result;

    }

}
