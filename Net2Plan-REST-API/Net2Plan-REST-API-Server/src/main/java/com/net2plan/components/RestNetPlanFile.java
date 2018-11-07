package com.net2plan.components;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;

@XmlRootElement
public class RestNetPlanFile
{
    private File file;

    public RestNetPlanFile(){}

    public RestNetPlanFile(File file)
    {
        this.file = file;
    }

    public void setFile(File file)
    {
        this.file = file;
    }

    public File getFile()
    {
        return file;
    }
}


