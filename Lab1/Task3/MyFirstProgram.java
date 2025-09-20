class MyFirstClass {
 	public static void main(String[] s) 
	{
		MySecondClass o = new MySecondClass(42, 52);
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

class MySecondClass { // Minimum
	private int firstNum, secondNum;
	public int getFirstNum(){ // getters
		return firstNum;
	}
	public int getSecondNum(){
		return secondNum;
	}
	public void setFirstNum(int newNum1){ // setters
		this.firstNum = newNum1;	
	}
	public void setSecondNum(int newNum2){
		this.secondNum = newNum2;
	}
	public MySecondClass(int fNum, int sNum){// firstNum and secondNum
		this.firstNum = fNum;
		this.secondNum = sNum;
	}
	public int min(){ // 6 variant
		return (firstNum > secondNum ? secondNum : firstNum); // if firstNum = secondNum it is going to work anyway
	}
}
