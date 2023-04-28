package de.hsu.grafcet.instanceGeneration;

import de.hsu.grafcet.Grafcet;
import de.hsu.grafcet.instanceGeneration.GrafcetSerializer.GrafcetSerializerInstanceType;

public class Main {

	public static void main(String[] args) {
		serializePrallelInstances(4);
		

	}
	
	private static void serializePrallelInstances(int noOfInstances) {
		for(int i = 1 ; i <= noOfInstances ; i ++) {
			GrafcetSerializer serializer = new GrafcetSerializer(GrafcetSerializerInstanceType.BASIC_PARALLEL, i , 3);
			Grafcet g = serializer.getGrafcet();
			XMISerializer.serilizeGrafcetToXMI(g, "g_" + i);
		}
	}

}
