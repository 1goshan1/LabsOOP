package myfirstpackage;

public class MyFirstPackage { // Minimum
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
	public MyFirstPackage(int fNum, int sNum){// firstNum and secondNum
		this.firstNum = fNum;
		this.secondNum = sNum;
	}
	public int min(){ // 6 variant
		return (firstNum > secondNum ? secondNum : firstNum); // if firstNum = secondNum it is going to work anyway
	}
}