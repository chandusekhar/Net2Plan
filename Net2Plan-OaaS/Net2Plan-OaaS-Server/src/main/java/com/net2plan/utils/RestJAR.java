package com.net2plan.utils;

import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.IReport;
import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.internal.ErrorHandling;
import com.net2plan.internal.IExternal;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@XmlRootElement
public class RestJAR
{
    @XmlElement(required=true)
    private File file;

    private List<IExternal> algorithmsIncluded;
    private List<IExternal> reportsIncluded;

    public RestJAR(){}

    public RestJAR(File file)
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

    public List<IAlgorithm> getInternalAlgorithms()
    {
        List<IAlgorithm> algorithms = new LinkedList<>();
        List<Class<IAlgorithm>> algorithmClasses = ClassLoaderUtils.getClassesFromFile(file, IAlgorithm.class, null);
        System.out.println("SIZE ALGORITHMS -> "+algorithmClasses.size());
        algorithmClasses.stream().forEach( algorithmClass ->
        {
            try {
                algorithms.add(algorithmClass.newInstance());
            } catch (InstantiationException e) {
                ErrorHandling.printStackTrace(e);
            } catch (IllegalAccessException e) {
                ErrorHandling.printStackTrace(e);
            }
        });
        return algorithms;
    }

    public List<IReport> getInternalReports()
    {
        System.out.println(file.getPath());
        List<IReport> reports = new LinkedList<>();
        List<Class<IReport>> reportClasses = ClassLoaderUtils.getClassesFromFile(file, IReport.class, null);
        System.out.println("SIZE REPORTS -> "+reportClasses.size());
        reportClasses.stream().forEach( reportClass ->
        {
            try {
                reports.add(reportClass.newInstance());
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        return reports;
    }
}


