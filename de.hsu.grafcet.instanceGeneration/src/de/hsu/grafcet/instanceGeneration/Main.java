package de.hsu.grafcet.instanceGeneration;

import de.hsu.grafcet.Grafcet;
import de.hsu.grafcet.instanceGeneration.GrafcetSerializer.GrafcetSerializerInstanceType;

public class Main {

	public static void main(String[] args) {
		
//		serializeInstance(2, 2, GrafcetSerializerInstanceType.BASIC_PARALLEL);
		serializeInstances(225, 300, 2);
		
		
		
		

	}
	
	private static void serializeInstance(int m, int n, GrafcetSerializerInstanceType type) {
		GrafcetSerializer serializer = new GrafcetSerializer(type, m , n);
		Grafcet g = serializer.getGrafcet();
		XMISerializer.serilizeGrafcetToXMI(g, type.toString() + "_m" + String.format("%04d", m) + "_n" + n);
	}
	private static void serializeInstances(int start_m, int end_m, int n) {
		for(int i = start_m ; i <= end_m ; i = i + 25) {
			serializeInstance(i, n, GrafcetSerializerInstanceType.BASIC_SEQUENCE);
//			serializeInstance(n, i, GrafcetSerializerInstanceType.BASIC_PARALLEL);
//			serializeInstance(i, n, GrafcetSerializerInstanceType.BASIC_VARIABLES);
//			serializeInstance(i, n, GrafcetSerializerInstanceType.HIERARCHICAL_SEQUENCE);
//			serializeInstance(i, n, GrafcetSerializerInstanceType.HIERARCHICAL_PARALLEL);
		}
	}
	
	
	
	private static void testVariableValues() {
		for (int i = 100; i <= 120; i++) {
			int number = i;
			String binaryString = Integer.toBinaryString(number);
			System.out.println(binaryString);
			
			char[] c = binaryString.toCharArray();
			for(int j = 0; j < c.length; j++) {
//				System.out.println(c[j]);
				if (c[j] == '1') {
					System.out.println(c[j] + " - " + true);
				} else if (c[j] == '0') {
					System.out.println(c[j] + " - " + false);
				} else {
					throw new IllegalArgumentException("Error in Algorithm");
				}
			}
		
		}
	}

}
