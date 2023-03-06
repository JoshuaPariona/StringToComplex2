import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import Calculator.Operation;

public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        List<String> results = Collections.synchronizedList(new ArrayList<>());
        System.out.println("Enter a mathematical operation, the characters allowed are the following:");
        System.out.println("0-9, ., i, j, E, (, ), +, -, x, /, ^, for the root use ^ (1/index) ");
        System.out.print("Operation: ");
        String myOp = input.nextLine();
        Operation op = null;

        try {
            op = new Operation(myOp,results,false);
            op.start();
            op.join();

            if (results.size() != 0) {
                System.out.println("========================");
                for (String result: results) {
                    System.out.println("Result = "+result);
                }
                System.out.println("========================");
            }
            else
                System.out.println("Result = NaN");
        }
        catch (IllegalThreadStateException e) {
            System.out.println("The thread has already been started before");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            System.out.println("Result = NaN");
        }
        catch (Exception e) {
            System.out.println("An unexpected exception has occurred");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        
        input.close();
    }
}
