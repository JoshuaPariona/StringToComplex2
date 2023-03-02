package Calculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import Calculator.PriorityStruct;
import Calculator.OperationUtils;
import Entities.Complex;
import Entities.Pair;

public class Operation extends Thread {
    private ArrayList<Thread> childrenThreads = new ArrayList<>();
    private String sequence;
    private ArrayList<PriorityStruct<String,String,Integer>> priorityList = new ArrayList<>();
    private List<String> results;
    private String opResult;
    private int maxWeight = 0;

    public Operation(String sequence, List<String> results, boolean itsCorrectOperation) {
        this.sequence = sequence;
        this.results = results;
        if (!itsCorrectOperation){
            splitSequence();
            invalidTokens();
        }
    }

    @Override
    public void run() {
        try {
            this.calculate();
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void splitSequence() {
        checkParentheses();
        String regex = "x|\\+|\\-|\\/|\\^|\\(|\\)";
        for (String part : sequence.split(regex)) {
            if (!isCorrectNumber(part.trim()))
                throw new IllegalArgumentException("There is a part of the operation that is not valid. <<"+part.trim()+">>");
        }
    }

    private void checkParentheses(){
        int counterParentheses = 0;
        boolean firts = true;
        for (int i = 0; i < this.sequence.length(); i++) {
            if (this.sequence.charAt(i) == '('){
                counterParentheses++;
                firts = false;
            }
            else if (this.sequence.charAt(i) == ')'){
                counterParentheses--;
                if (firts){
                    counterParentheses = -1;
                    break;
                }
            }
        }
        firts = true;
        if (counterParentheses != 0) 
            throw new IllegalArgumentException("The operation does not have the parentheses correctly placed.");
        for (int i = this.sequence.length()-1; i >= 0 ; i--) {
            if (this.sequence.charAt(i) == '('){
                counterParentheses++;
                if (firts){
                    counterParentheses = -1;
                    break;
                }
            }
            else if (this.sequence.charAt(i) == ')'){
                counterParentheses--;
                firts = false;
            }
        }
        if (counterParentheses != 0) 
            throw new IllegalArgumentException("The operation does not have the parentheses correctly placed.");
    }
    
    private boolean isCorrectNumber(String part) {
        if (OperationUtils.isInteger(part))
            return true;
        else if (OperationUtils.isReal(part))
            return true;
        else if (Complex.isComplex(part))
            return true;
        else if (part.equals("")) //space between operators
            return true;
        return false;
    }
    
    private void invalidTokens() { 
        String[] tokens = {"()","x)","/)","^)","-)","-)","+)","+)","+)","(x","(/","(^","-/","-x","-^","+/","+x","+^","/x","/^","//","x/","x^","xx","^x","^/","^^"};
        for (String t : tokens) {
            if (this.sequence.contains(t))
                throw new IllegalArgumentException("The operator <<"+t+">> is not valid");
        }
        char init = this.sequence.charAt(0);
        char fin = this.sequence.charAt(this.sequence.length()-1);
        if (init == '/' || init == 'x' || init =='^')
            throw new IllegalArgumentException("The operator <<"+init+">> , can't be at start");
        if (fin == '/' || fin == 'x' || fin =='^' || fin =='+' || fin =='-')
            throw new IllegalArgumentException("The operator <<"+fin+">> , can't be at the end");
    }

    private void separateByPriority(String sequence, int weight) {
        boolean record = false;
        int count1 = 0;
        int count2 = 0;
        String subSequence = "";
        for (char a :sequence.toCharArray()) {
            if (a == '(') {
                count1++; 
                record = true;
            }
            if (record){
                subSequence+=String.valueOf(a);
                if (a == ')') {
                    count2++;
                    if (count1 == count2) {
                        record = false;
                        count1 = 0;
                        count2 = 0;
                        this.priorityList.add(new PriorityStruct<String,String,Integer>(subSequence,subSequence,weight));
                        if (this.maxWeight <= weight)
                            this.maxWeight = weight;
                        subSequence = subSequence.substring(1, subSequence.length() - 1); //critic
                        separateByPriority(subSequence, weight+1);
                        subSequence = "";
                    }
                }
            }
        }
    }
    
    private String refactorSequence(String sequence) {
        sequence = signRefactor(sequence);
        for (int i = 0; i < sequence.length(); i++) {
            if (sequence.charAt(i) == '(') {
                if (i > 0) {
                    int currentLen = sequence.length();
                    if (sequence.charAt(i-1) == ')')
                        sequence = new String(sequence.substring(0,i)+"x"+sequence.substring(i,currentLen));
                    else if (sequence.charAt(i-1) == 'i' || sequence.charAt(i-1) == 'j')
                        sequence = new String(sequence.substring(0,i)+"x"+sequence.substring(i,currentLen));
                    else if (OperationUtils.isInteger(String.valueOf(sequence.charAt(i-1))))
                        sequence = new String(sequence.substring(0,i)+"x"+sequence.substring(i,currentLen));
                }
            }
            else if (sequence.charAt(i) == ')') {
                int currentLen = sequence.length();
                if (i < currentLen-1) {
                    if (sequence.charAt(i+1) == '(')
                        sequence = new String(sequence.substring(0,i+1)+"x"+sequence.substring(i+1,currentLen));
                    else if (sequence.charAt(i+1) == 'i' || sequence.charAt(i+1) == 'j')
                        sequence = new String(sequence.substring(0,i+1)+"x"+sequence.substring(i+1,currentLen));
                    else if (OperationUtils.isInteger(String.valueOf(sequence.charAt(i+1))))
                        sequence = new String(sequence.substring(0,i+1)+"x"+sequence.substring(i+1,currentLen));
                }
            }    
        }
        return sequence;
    }

    public String getSequence() {
        return this.sequence;
    }
    
    public String getResult() {
        return this.opResult;
    } 
    
    private void calculate() throws InterruptedException {
        this.sequence = refactorSequence(this.sequence);
        this.opResult = this.sequence;
        System.out.println(this.sequence);
        separateByPriority(this.sequence,0);
        for (PriorityStruct<String,String,Integer> s : this.priorityList) {
            System.out.println("Prioridad: "+s.getFirst()+"  Peso: "+s.getThird());
        }
        for(int i = this.maxWeight; i >= 0; i--){
            solveAll(i);
            replaceAll(i-1);
        }
        this.opResult = signRefactor(this.opResult);
        this.opResult = result(this.opResult);
        String complex = obtenerSubstringEntreTokens(this.opResult,'$','$');

        if (this.opResult.contains("-$")) {
            Complex c1 = new Complex(complex);
            complex = c1.multiScalar(-1).toString();
        }

        this.opResult = complex;

        for (PriorityStruct<String,String,Integer> s : this.priorityList) {
            System.out.println("Prioridad: "+s.getFirst()+" Resuelto: "+s.getSecond()+" Peso: "+s.getThird());
        }

        synchronized(results) {
            this.results.add(this.opResult);
        }

        for (Thread thread : childrenThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("Fin de todos los hilos");
    }
    
    // PriorityStruct<originalSequence,solveSequence,weight>
    private void solveAll(int weight) {
        for (PriorityStruct<String,String,Integer> sequence : this.priorityList) {
            if (sequence.getThird() == weight) {
                sequence.setSecond(solve(sequence.getSecond()));
            }
        }
    }

    private void replaceAll(int weight) {
        ArrayList<Pair<String,String>> tempList = new ArrayList<>();
        for (PriorityStruct<String,String,Integer> sequence : this.priorityList) {
            if (sequence.getThird() == (weight+1)) { //source
                tempList.add(new Pair<String,String>(sequence.getFirst(),sequence.getSecond()));//regex, alreadysolved
            }
        }
        if (weight == -1) {
            //final replace
            for (Pair<String,String>  temp: tempList) {
                this.opResult = this.opResult.replace(temp.getX(),temp.getY());
            }
            return;
        }
        for (PriorityStruct<String,String,Integer> sequence : this.priorityList) {
            if (sequence.getThird() == weight) { //target
                System.out.println("Remplace in this sequence: "+ sequence.getFirst());
                System.out.println("<<"+sequence.getSecond()+">>");
                for (Pair<String,String>  temp: tempList) {
                    if (sequence.getSecond().contains(temp.getX()))
                        sequence.setSecond(sequence.getSecond().replace(temp.getX(),temp.getY())); //ready to solve and new loop
                }
                System.out.println("<<"+sequence.getSecond()+">>");
            }
        }

    }

    //aqui va la magia
    private String solve(String sequence) {
        //from this point the sequence is a line of basic operations -a+bxc/d
        //this sequence comes with parentheses ("a+b") //served as regex to replace, now not useful
        System.out.println("========================");
        sequence = obtenerSubstringEntreTokens(sequence, '(',')');
        
        sequence = signRefactor(sequence);
        System.out.println("Sequence in solve: " + sequence );
        System.out.println("========================");
        if (Complex.isComplex(sequence) && !OperationUtils.isReal(sequence))
            return "$"+sequence+"$"; //treat as monary, with token $ defined
        else 
            return result(sequence);
    }

    private String signRefactor(String sequence) {
        while (sequence.contains("++") || sequence.contains("--") || sequence.contains("-+")|| sequence.contains("+-")){
            sequence = sequence.replaceAll("\\+\\+", "+");
            sequence = sequence.replaceAll("\\-\\-", "+");
            sequence = sequence.replaceAll("\\-\\+", "-");
            sequence = sequence.replaceAll("\\+\\-", "-");
        }
        return sequence;
    }

    private String result(String sequence) {
        ArrayList<String> listParts = splitLiteSequence(sequence);
        operate(listParts);
        String result = listParts.get(0);
        if (Complex.isComplex(result) && !OperationUtils.isReal(result))
            result = new String("$"+result+"$");
        return result;
    }

    private ArrayList<String> splitLiteSequence(String sequence) {
        ArrayList<String> listParts = new ArrayList<>();
        char operator = '\0';
        String part = "";
        boolean imaToken = false;
        boolean record = true;
        for (int i = 0; i < sequence.length(); i++) {
            char currentChar = sequence.charAt(i);
            if (currentChar == '$')
                imaToken = !imaToken;
            if (!imaToken) {
                if (i == 0 && (currentChar == '-' || currentChar == '+'))
                    record = true;
                else if (part != "" && (currentChar == '-' || currentChar == '+' || currentChar == '^' || currentChar == '/' || currentChar == 'x')) {
                    operator = currentChar;
                    record = false;
                }
                else 
                    record = true;
            }
            else
                record = true;
    
            //record
            if (record) {
                part+=String.valueOf(currentChar);
            }
            else{
                listParts.add(part);
                part = "";
            }
            if (operator != '\0'){
                listParts.add(String.valueOf(operator));
                operator = '\0';
            }
        }
        listParts.add(part);
        return listParts;
    }
    
    private void operate(ArrayList<String> lisPart) {
        //$a+bi$ y ai
        while (lisPart.contains("^")) {
            //System.out.println("paso ^");
            //TODO: complex elevado a double e integer o duble menor a 1 y mayor a cero retorna una lista de complex
            //TODO: inicar hilos q calculen el resultado de todos los posibles resultados;
            int k = getIndexFirstOccurrence(lisPart,"^","nan");
            String p = power(lisPart.get(k-1),lisPart.get(k+1));
            lisPart.remove(k-1);
            lisPart.remove(k-1);
            lisPart.remove(k-1);
            if(lisPart.size() == 0)
                lisPart.add(p);
            else
                lisPart.add(k-1, p);
        }
        while (lisPart.contains("x") || lisPart.contains("/")) {
            //System.out.println("paso x o /");
            int k = getIndexFirstOccurrence(lisPart,"x","/");
            String p = "NaN";
            if (lisPart.get(k).equals("x"))
                p = multi(lisPart.get(k-1),lisPart.get(k+1));
            else if (lisPart.get(k).equals("/"))
                p = divide(lisPart.get(k-1),lisPart.get(k+1));
            lisPart.remove(k-1);
            lisPart.remove(k-1);
            lisPart.remove(k-1);
            if(lisPart.size() == 0)
                lisPart.add(p);
            else
                lisPart.add(k-1, p);
        }
        while (lisPart.contains("-") || lisPart.contains("+")) {
            //System.out.println("paso + o -");
            int k = getIndexFirstOccurrence(lisPart,"+","-");
            String p = "NaN";
            if (lisPart.get(k).equals("+"))
                p = add(lisPart.get(k-1),lisPart.get(k+1));
            else if (lisPart.get(k).equals("-"))
                p = sub(lisPart.get(k-1),lisPart.get(k+1));
            lisPart.remove(k-1);
            lisPart.remove(k-1);
            lisPart.remove(k-1);
            if(lisPart.size() == 0)
                lisPart.add(p);
            else
                lisPart.add(k-1, p);
        }
        
        //Luego de la resolusion
        /* 
        System.out.println("==========");
        for (String s: lisPart) {
            System.out.println(s);
        }
        System.out.println("==========");*/
    }

    private int getIndexFirstOccurrence(ArrayList<String> lisPart, String op1, String op2){
        int index = -1;
        String part;
        for (int i = 0; i < lisPart.size(); i++) {
            part = lisPart.get(i);
            if (part.equals(op1) || part.equals(op2)) { 
                index = i;
                break;
            }
        }
        return index;
    }
    
    public String obtenerSubstringEntreTokens(String cadena, char token1, char token2) {
        int inicio = cadena.indexOf(token1);
        if (inicio == -1)
            return cadena;
        inicio++;
        int fin = cadena.indexOf(token2, inicio);
        if (fin == -1) 
            return cadena;
        return cadena.substring(inicio, fin);
    }

    //funciones aritmeticas

    public String power(String a ,String b) {
        String result = "NaN";
        String trace = "undifined";
        if (a.contains("i") || a.contains("j") || b.contains("j") || b.contains("i")) {
            String subA = obtenerSubstringEntreTokens(a,'$','$');
            String subB = obtenerSubstringEntreTokens(b,'$','$');
            trace = subA+"^"+subB;
            Complex aC = new Complex(subA);
            if (a.contains("-$"))
                aC=aC.multiScalar(-1);
            if (b.contains("i")) {
                Complex bC = new Complex(subB);
                if (b.contains("-$"))
                    bC=bC.multiScalar(-1);
                result = aC.power(bC).toString();
            }
            else {
                Integer denominator = OperationUtils.isRationalInverse(subB);
                if (denominator != null){
                    System.out.println(denominator);
                    Vector<Complex> vectorC = aC.root(denominator);
                    for (Complex c : vectorC) {
                        System.out.println("Complex root: "+ c.toString());
                    }
                    result = vectorC.get(0).toString();
                }
                else{
                    Complex bC = new Complex(subB);
                    if (b.contains("-$"))
                        bC=bC.multiScalar(-1);
                    result = aC.power(bC).toString();
                }
            }
        }
        else {
            trace = a+"^"+b;
            double aD = parseDouble(a);
            double bD = parseDouble(b);
            result = String.valueOf(Math.pow(aD, bD));
            if (result.equals("NaN")) {
                Complex aC = new Complex(a);
                Complex bC = new Complex(b);
                result = aC.power(bC).toString();
            }
        }
        if (result.equals("NaN")) 
            throw new IllegalArgumentException("The result of a part of the operation is not a valid number. <<"+trace+">>");
        return result;
    }

    private String multi(String a ,String b) {
        String result = "NaN";
        String trace = "undifined";
        if (a.contains("i") || a.contains("j") || b.contains("j") || b.contains("i")) {
            String subA = obtenerSubstringEntreTokens(a,'$','$');
            String subB = obtenerSubstringEntreTokens(b,'$','$');
            trace = subA + "x"+ subB;
            Complex aC = new Complex(subA);
            Complex bC = new Complex(subB);
            if (a.contains("-$"))
                aC=aC.multiScalar(-1);
            if (b.contains("-$"))
                bC=bC.multiScalar(-1);
            result = aC.multi(bC).toString();
        }
        else if (OperationUtils.isInteger(a) && OperationUtils.isInteger(b)) {
            trace = a+"x"+b;
            int aI = parseInt(a);
            int bI = parseInt(b);
            result = String.valueOf(aI*bI);
        }
        else {
            trace = a+"x"+b;
            double aD = parseDouble(a);
            double bD = parseDouble(b);
            result = String.valueOf(aD*bD);
        }

        if (result.equals("NaN")) 
            throw new IllegalArgumentException("The result of a part of the operation is not a valid number. <<"+trace+">>");
        return result;
    }

    private String divide(String a ,String b) {
        String result = "NaN";
        String trace = "undifined";

        if (a.contains("i") || a.contains("j") || b.contains("j") || b.contains("i")) {
            String subA = obtenerSubstringEntreTokens(a,'$','$');
            String subB = obtenerSubstringEntreTokens(b,'$','$');
            trace = subA + "/"+ subB;
            Complex aC = new Complex(subA);
            Complex bC = new Complex(subB);
            if (a.contains("-$"))
                aC=aC.multiScalar(-1);
            if (b.contains("-$"))
                bC=bC.multiScalar(-1);
            try{
                result = aC.divide(bC).toString();
            }
            catch (IllegalArgumentException e){
                result = "NaN";
            }
        }
        else if (OperationUtils.isInteger(a) && OperationUtils.isInteger(b)) {
            trace = a+"/"+b;
            int aI = parseInt(a);
            int bI = parseInt(b);
            if (bI==0)
                result = "NaN"; //o infinite, dependeria como no quiero hacerme bolas lo dejo en nan
            else
                result = String.valueOf((1.0*aI)/bI);
        }
        else {
            trace = a+"/"+b;
            double aD = parseDouble(a);
            double bD = parseDouble(b);
            result = String.valueOf(aD/bD);
        }

        if (result.equals("NaN")) 
            throw new IllegalArgumentException("The result of a part of the operation is not a valid number. <<"+trace+">>");
        return result;
    }

    private String add(String a ,String b) {
        String result = "NaN";
        String trace = "undifined";

        if (a.contains("i") || a.contains("j") || b.contains("j") || b.contains("i")) {
            String subA = obtenerSubstringEntreTokens(a,'$','$');
            String subB = obtenerSubstringEntreTokens(b,'$','$');
            trace = subA+"+"+subB;
            Complex aC = new Complex(subA);
            Complex bC = new Complex(subB);
            if (a.contains("-$"))
                aC=aC.multiScalar(-1);
            if (b.contains("-$"))
                bC=bC.multiScalar(-1);
            result = aC.add(bC).toString();
        }
        else if (OperationUtils.isInteger(a) && OperationUtils.isInteger(b)) {
            trace = a+"+"+b;
            int aI = parseInt(a);
            int bI = parseInt(b);
            result = String.valueOf(aI+bI);
        }
        else {
            trace = a+"+"+b;
            double aD = parseDouble(a);
            double bD = parseDouble(b);
            result = String.valueOf(aD+bD);
        }
    
        if (result.equals("NaN")) 
            throw new IllegalArgumentException("The result of a part of the operation is not a valid number. <<"+trace+">>");
        return result;
    }

    private String sub(String a ,String b) {
        String result = "NaN";
        String trace = "undifined";
        if (a.contains("i") || a.contains("j") || b.contains("j") || b.contains("i")) {
            String subA = obtenerSubstringEntreTokens(a,'$','$');
            String subB = obtenerSubstringEntreTokens(b,'$','$');
            trace = subA+"-"+subB;
            Complex aC = new Complex(subA);
            Complex bC = new Complex(subB);
            if (a.contains("-$"))
                aC=aC.multiScalar(-1);
            if (b.contains("-$"))
                bC=bC.multiScalar(-1);
            result = aC.sub(bC).toString();
        }
        else if (OperationUtils.isInteger(a) && OperationUtils.isInteger(b)) {
            trace = a+"-"+b;
            int aI = parseInt(a);
            int bI = parseInt(b);
            result = String.valueOf(aI-bI);
        }
        else {
            trace = a+"-"+b;
            double aD = parseDouble(a);
            double bD = parseDouble(b);
            result = String.valueOf(aD-bD);
        }
    
        if (result.equals("NaN")) 
            throw new IllegalArgumentException("The result of a part of the operation is not a valid number. <<"+trace+">>");
        return result;
    }

    private int parseInt(String a) {
        try {
            int aI = Integer.parseInt(a);
            return aI;
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("A part of the operation does not have the correct integer format. <<"+a+">>");
        }
        catch (NullPointerException e) {
            throw new IllegalArgumentException("A part of the operation does not exist. <<"+a+">>");
        }
    }

    private double parseDouble(String a) {
        try {
            double aD = Double.parseDouble(a);
            return aD;
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("A part of the operation does not have the correct real format. <<"+a+">>");
        }
        catch (NullPointerException e) {
            throw new IllegalArgumentException("A part of the operation does not exist. <<"+a+">>");
        }
    }
}
