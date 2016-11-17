package com.microsoft.azure.iothub;

/**
 * Created by prmathur on 11/16/2016.
 */
public class translation_wrapper_sample
{
    public static void main(String [] args)
    {
        try
        {
            RunTime nestedVMRuntime = new RunTime();
            SocketIo sio = new SocketIo(nestedVMRuntime);
            sio.SocketIoSetCallbacks();
            nestedVMRuntime.startRunTime();
            /* Call execute if you want to run sample from C */
            nestedVMRuntime.executeRunTime();
        }
        catch (Exception e)
        {
            System.out.print(e);
        }

    }
}
