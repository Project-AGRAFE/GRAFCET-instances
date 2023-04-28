package de.hsu.grafcet.instanceGeneration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import de.hsu.grafcet.Grafcet;

public class XMISerializer {
	
	public static void serilizeGrafcetToXMI(Grafcet grafcet, String name) {

		//https://stackoverflow.com/questions/40202206/how-to-generate-emf-models-with-java-code
		   /*
	     * Save your EPackage to file ecore file:
	     */
	    /*Initialize your EPackage*/
		grafcet.eClass();
	    Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
	    Map<String, Object> m = reg.getExtensionToFactoryMap();
	    /*add default .ecore extension for ecore file*/
	    m.put(EcorePackage.eNAME, new XMIResourceFactoryImpl());
	    // Obtain a new resource set
	    ResourceSet resSet = new ResourceSetImpl();
	    // create a resource
	    Resource resource = null;
	    try {
	        resource = resSet.createResource(URI.createFileURI(new File(name + ".ecore").getAbsolutePath())); //.ecore ending is required
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    /*add your EPackage as root, everything is hierarchical included in this first node*/
	    resource.getContents().add(grafcet);

	    // now save the content.
	    try {
	        resource.save(Collections.EMPTY_MAP);
	        //file path:
	        System.out.println(new File(name + ".ecore").getAbsolutePath());
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}
