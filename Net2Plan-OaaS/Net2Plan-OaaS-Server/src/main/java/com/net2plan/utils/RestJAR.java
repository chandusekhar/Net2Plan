package com.net2plan.utils;

import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.IReport;
import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.internal.ErrorHandling;
import com.net2plan.internal.IExternal;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

@XmlRootElement
public class RestJAR
{
    @XmlElement(required=true)
    private File file;

    private String name;

   /* private List<IExternal> algorithmsIncluded;
    private List<IExternal> reportsIncluded;*/

    public RestJAR(){}

    public RestJAR(File file)
    {
        this.file = file;
        this.name = file.getName();
        //algorithmsIncluded = getInternalFiles("algorithm");
        //reportsIncluded = getInternalFiles("report");
    }

    public void setFile(File file)
    {
        this.file = file;
    }

    public File getFile()
    {
        return file;
    }

    /* private List<IExternal> getInternalFiles(String type)
    {
        if (type.equalsIgnoreCase("algorithm"))
        {
            List<Class<IAlgorithm>> algorithmClasses = ClassLoaderUtils.getClassesFromFile(file, IAlgorithm.class, null);
            algorithmClasses.stream().forEach( algorithmClass ->
            {
                try {
                    algorithmsIncluded.add(algorithmClass.newInstance());
                } catch (InstantiationException e) {
                    ErrorHandling.printStackTrace(e);
                } catch (IllegalAccessException e) {
                    ErrorHandling.printStackTrace(e);
                }
            });

            return algorithmsIncluded;
        }
        else if (type.equalsIgnoreCase("report"))
        {
            List<Class<IReport>> reportClasses = ClassLoaderUtils.getClassesFromFile(file, IReport.class, null);
            reportClasses.stream().forEach( reportClass ->
            {
                try {
                    reportsIncluded.add(reportClass.newInstance());
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });

            return reportsIncluded;
        }
        else
        {
            throw new Net2PlanException("Unknown IExternal File");
        }
    }

    public List<IAlgorithm> getInternalAlgorithms()
    {
        List<IAlgorithm> algorithms = new LinkedList<>();
        algorithmsIncluded.stream().forEach(alg -> algorithms.add((IAlgorithm) alg));
        return algorithms;
    }

    public List<IReport> getInternalReports()
    {
        List<IReport> reports = new LinkedList<>();
        reportsIncluded.stream().forEach(rep -> reports.add((IReport) rep));
        return reports;
    }*/
}


