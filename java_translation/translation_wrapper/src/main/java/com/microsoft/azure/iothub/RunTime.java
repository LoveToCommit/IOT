// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.iothub;
import org.ibex.nestedvm.UnixRuntime;
/**
 * Created by prmathur on 11/11/2016.
 */

/* New runtime only once per program */

public class RunTime
{

    private org.ibex.nestedvm.UnixRuntime runTime;
    private int usageCount = 0;
    private Logger lg;

    public RunTime() throws Exception
    {
        this.runTime = (UnixRuntime) Class.forName("mips_shared_util").newInstance();
        lg = new Logger();
    }

    // getRuntime : Instantiated only once per program
    org.ibex.nestedvm.UnixRuntime getRuntime()
    {
        return this.runTime;
    }

    // useRuntime : to guard sequential execution
    void useRuntime()
    {
        usageCount++;
    }

    // freeRuntime : to guard sequential execution
    void freeRuntime()
    {

        usageCount--;
    }

    // startRuntime : to start runtime (optional sometimes)
    void startRunTime()
    {
        this.useRuntime();
        this.getRuntime().start();
        this.freeRuntime();
    }

    // executeRuntime : to run main program in C
    void executeRunTime()
    {
        this.useRuntime();
        this.getRuntime().execute();
        this.freeRuntime();
    }

    void closeRunTime()
    {
        if (usageCount > 0)
        {
            lg.print("Runtime used but not freed");
        }

    }

}
