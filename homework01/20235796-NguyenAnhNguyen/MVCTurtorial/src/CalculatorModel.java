public class CalculatorModel {

	// Holds the value of the sum of the numbers
	// entered in the view
	
	private int calculationValue;
	
	public void addTwoNumbers(int firstNumber, int secondNumber){
		
		calculationValue = firstNumber + secondNumber;
		
	}
	
	public int getCalculationValue(){
		
		return calculationValue;
		
	}
	
}