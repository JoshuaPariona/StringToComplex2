import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import Calculator.Operation;

public class Main {
    public static void main(String[] args){
        Scanner input = new Scanner(System.in);
        List<String> resultados = Collections.synchronizedList(new ArrayList<>());
        System.out.print("Operation: ");
        String myOp = input.nextLine();
        Operation op = null;

        try {
            op = new Operation(myOp,resultados,false);
            op.start();
        }
        catch (IllegalThreadStateException e) {
            System.out.println("El hilo ya ha sido iniciado anteriormente");
            e.printStackTrace();
        }
        catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
        catch (Exception e){
            System.out.println("Una excepción inesperada a ocurrido");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        if (op != null){
            try {
                op.join();
            } 
            catch (InterruptedException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }

        for (String resultado : resultados) {
            System.out.println("Resultado : "+ resultado);
        }
        
        input.close();
    }
}
