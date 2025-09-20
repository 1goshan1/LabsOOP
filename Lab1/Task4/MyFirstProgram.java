import myfirstpackage.*;
class MyFirstClass {
 	public static void main(String[] s) 
	{
		MyFirstPackage o = new MyFirstPackage(42, 52);
		System.out.println(o.min());
		for (int i = 1; i <=8 ; i++){
			for (int j = 1; j <= 8; j++){
				o.setFirstNum(i);
				o.setSecondNum(j);
				System.out.print(o.min());
				System.out.print(" ");
			}
			System.out.println();
		}
	}
}
