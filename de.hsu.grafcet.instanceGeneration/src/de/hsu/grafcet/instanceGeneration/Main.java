package de.hsu.grafcet.instanceGeneration;

import de.hsu.grafcet.Grafcet;
import de.hsu.grafcet.instanceGeneration.GrafcetSerializer.GrafcetSerializerInstanceType;

public class Main {

	public static void main(String[] args) {
		
//		serializeInstance(2, 2, GrafcetSerializerInstanceType.BASIC_VARIABLES);
		serializeInstances(0, 15, 2);

	}
	
	private static void serializeInstance(int m, int n, GrafcetSerializerInstanceType type) {
		GrafcetSerializer serializer = new GrafcetSerializer(type, m , n);
		Grafcet g = serializer.getGrafcet();
		XMISerializer.serilizeGrafcetToXMI(g, type.toString() + "_m" + m + "_n" + n);
	}
	private static void serializeInstances(int start_m, int end_m, int n) {
		for(int i = start_m ; i <= end_m ; i ++) {
//			serializeInstance(i, n, GrafcetSerializerInstanceType.BASIC_SEQUENCE);
			serializeInstance(i, n, GrafcetSerializerInstanceType.BASIC_PARALLEL);
//			serializeInstance(i, n, GrafcetSerializerInstanceType.BASIC_VARIABLES);
//			serializeInstance(i, n, GrafcetSerializerInstanceType.HIERARCHICAL_SEQUENCE);
//			serializeInstance(i, n, GrafcetSerializerInstanceType.HIERARCHICAL_PARALLEL);
		}
	}

}
