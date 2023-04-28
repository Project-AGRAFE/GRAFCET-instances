package test;

public class TestSize {

	public static void main(String[] args) {
		
		
		double result = 3;
		int width = 3;
		
		for (int i = 1; i<= 10; i++) {
			int end = recursion(width, i);	
			end += 2;
			System.out.println("i: " + i + " result: " + end);
			
			//Über Rekursive Reihe: a0 = 3; a_i = a_i-1 - width^(i-1) + 3*width^(i)
			result = result - Math.pow(width, i - 1) + 3*Math.pow(width, i);
			System.out.println("i: " + i + " result: " + (int) result + " (recusion)");
		}

	}
	
	private static int recursion(int width, int depth) {
		int end = 0;
		for(int j = 1; j <= width; j++) {
			end+=1;
			if(depth > 1) {
				end += recursion(width, depth - 1);
			} else {
				end += 1;
			}
			end+=1;
		}
//		System.out.println("depth : " + depth + " - " + end);
		return end;
	}

}
