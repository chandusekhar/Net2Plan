package com.net2plan.interfaces.networkDesign;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static junit.framework.TestCase.fail;

/**
 * @author Jorge San Emeterio
 * @date 5/05/17
 */
public class NetPlanReaderTest
{
    @Test
    public void parseDefaultTopologies()
    {
        try
        {
            File resources = new File("src/main/resources/data/networkTopologies");

            final File[] topologyFiles = resources.listFiles();
            if (topologyFiles == null) fail();

            for (File f : topologyFiles)
            {
                InputStream is = null;
                try
                {
                    is = new FileInputStream(f);
                    new NetPlan(is);
                } catch (Exception e)
                {
                    e.printStackTrace();
                    fail();
                } finally
                {
                    if (is != null)
                        is.close();
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
