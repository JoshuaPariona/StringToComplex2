package Calculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import Entities.Complex;
import Entities.Pair;

public class Operation extends Thread {
    private final ArrayList<Thread> childrenThreads = new ArrayList<>();
    private String sequence;
    private List<String> results;
    private String currentResult;

    public Operation(String sequence, List<String> results, boolean itsCorrectOperation){
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
        } catch (IllegalArgumentException e) {
            System.out.println(this.getName()+": "+e.getMessage());
        }
        catch (Exception e) {
            System.out.println(this.getName()+": An unexpected exception has occurred");
            e.printStackTrace();
        }
    }

    private void splitSequence() {
        sequence = sequence.replaceAll("\\s", "");
        checkParentheses();
        String regex = "[x+\\-/^()]";
        for (String part : sequence.split(regex)) {
            if (!isCorrectNumber(part.trim()))
                throw new IllegalArgumentException("There is a part of the operation that is not valid. <<"+part.trim()+">>");
        }
    }

    private void checkParentheses(){
        int counterParentheses = 0;
        boolean firts = true;
        for (int i = 0; i < this.sequence.length(); i++) {
            if (this.sequence.charAt(i) == '(') {
                counterParentheses++;
                firts = false;
            }
            else if (this.sequence.charAt(i) == ')') {
                counterParentheses--;
                if (firts) {
                    counterParentheses = -1;
                    break;
                }
            }
        }
        firts = true;
        if (counterParentheses != 0) 
            throw new IllegalArgumentException("The operation does not have the parentheses correctly placed.");
        for (int i = this.sequence.length()-1; i >= 0 ; i--) {
            if (this.sequence.charAt(i) == '(') {
                counterParentheses++;
                if (firts){
                    counterParentheses = -1;
                    break;
                }
            }
            else if (this.sequence.charAt(i) == ')') {
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
        else return part.equals("");
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
            throw new IllegalArgumentException("The operator <<"+init+">> , takes two arguments");
        if (fin == '/' || fin == 'x' || fin =='^' || fin =='+' || fin =='-')
            throw new IllegalArgumentException("The operator <<"+fin+">> , takes two arguments");
    }

    private String separateByPriority(String sequence) {
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
                        subSequence = subSequence.substring(1, subSequence.length() - 1);
                        while (subSequence.contains("(")){
                            subSequence = separateByPriority(subSequence);
                        } 
                        return subSequence;
                    }
                }
            }
        }
        return sequence;
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
        return this.currentResult;
    } 
    
    private void calculate() {
        this.sequence = refactorSequence(this.sequence);
        this.currentResult = this.sequence;
        System.out.println(this.getName()+": Starting thread calculation with this sequence: "+ this.sequence);
        String  prioritySequence;
        String  prioritySequenceID;
        String partialResult;
        while (this.currentResult.contains("(")) {
            prioritySequence = separateByPriority(this.currentResult);
            prioritySequenceID = new String("("+prioritySequence+")");
            partialResult = solve(prioritySequence);
            this.currentResult = this.currentResult.replace(prioritySequenceID, partialResult);
        }
        this.currentResult = signRefactor(this.currentResult);
        System.out.println("========================");
        this.currentResult = result(this.currentResult);
        String possibleComplex = OperationUtils.obtenerSubstringEntreTokens(this.currentResult,'$','$');
        if (this.currentResult.contains("-$")) {
            Complex c1 = new Complex(possibleComplex);
            possibleComplex = c1.multiScalar(-1).toString();
        }
        this.currentResult = possibleComplex;

        synchronized(results) {
            if (!results.contains(currentResult)) {
                this.results.add(this.currentResult);
            }
        }

        for (Thread thread : childrenThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println(this.getName()+": End of thread");
    }
    
    //aqui va la magia
    private String solve(String sequence) {
        //from this point the sequence is a line of basic operations -a+bxc/d
        //this sequence comes with parentheses ("a+b")
        System.out.println("========================");
        sequence = OperationUtils.obtenerSubstringEntreTokens(sequence, '(',')');
        sequence = signRefactor(sequence);
        System.out.println(this.getName()+": Sequence in solve: " + sequence );
        if (Complex.isComplex(sequence) && !OperationUtils.isReal(sequence)) {
            System.out.println(this.getName()+": Operating this math problem: ["+sequence+" = "+sequence+"]");
            System.out.println("========================");
            return "$"+sequence+"$"; //treat as monary, with token $ defined
        }
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
        System.out.println("========================");
        return result;
    }

    private ArrayList<String> splitLiteSequence(String sequence) {
        ArrayList<String> listParts = new ArrayList<>();
        char operator = '\0';
        StringBuilder part = new StringBuilder();
        boolean imaToken = false;
        boolean record = true;
        for (int i = 0; i < sequence.length(); i++) {
            char currentChar = sequence.charAt(i);
            if (currentChar == '$')
                imaToken = !imaToken;
            if (!imaToken) {
                if (i == 0 && (currentChar == '-' || currentChar == '+'))
                    record = true;
                else if (!part.toString().equals("") && (currentChar == '-' || currentChar == '+' || currentChar == '^' || currentChar == '/' || currentChar == 'x')) {
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
                part.append(String.valueOf(currentChar));
            }
            else{
                listParts.add(part.toString());
                part = new StringBuilder();
            }
            if (operator != '\0'){
                listParts.add(String.valueOf(operator));
                operator = '\0';
            }
        }
        listParts.add(part.toString());
        return listParts;
    }
    
    private void operate(ArrayList<String> lisPart) {
        //$a+bi$ y ai
        while (lisPart.contains("^")) {
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
    

    public String power(String a ,String b) {
        String result = "NaN";
        String trace = a+"^"+b;
        String subA = OperationUtils.obtenerSubstringEntreTokens(a,'$','$');
        String subB = OperationUtils.obtenerSubstringEntreTokens(b,'$','$');
        if (OperationUtils.isReal(a) && OperationUtils.isReal(b)) {
            trace = a+"^"+b;
            double aD = Double.parseDouble(a);
            double bD = Double.parseDouble(b);
            result = String.valueOf(Math.pow(aD, bD));
            if (result.equals("NaN")) {
                Complex aC = new Complex(a);
                Complex bC = new Complex(b);
                result = aC.power(bC).toString();
            }
        }
        else if (Complex.isComplex(subA)) {
            trace = subA+"^"+b;
            Complex aC = new Complex(subA);
            if (a.contains("-$"))
                aC=aC.multiScalar(-1);

            if (OperationUtils.isInteger(b)) {
                result = aC.power(Integer.parseInt(b)).toString();
            }
            else if (OperationUtils.isReal(b)) {
                Pair<Integer, Integer> fraction = OperationUtils.Rational(b);
                if (fraction != null) {
                    String id = a + "^" + b;
                    Vector<Complex> vectorC = aC.power(fraction.getX(), fraction.getY());
                    String newSequence;
                    for (Complex complex : vectorC) {
                        System.out.println(this.getName() + ": Complex root: " + complex.toString());
                    }
                    for (int i = 1; i < vectorC.size(); i++) {
                        newSequence = this.currentResult;
                        Operation subOperation = new Operation(newSequence.replace(id, "$" + vectorC.get(i).toString() + "$"), this.results, true);
                        this.childrenThreads.add(subOperation);
                        subOperation.start();
                    }
                    if (vectorC.size() != 0)
                        result = vectorC.get(0).toString();
                    else
                        result = "NaN";
                } else {
                    Complex bC = new Complex(b);
                    result = aC.power(bC).toString();
                }
            }
            else if (Complex.isComplex(subB)) {
                trace = subA+"^"+subB;
                Complex bC = new Complex(subB);
                if (b.contains("-$"))
                    bC=bC.multiScalar(-1);
                result = aC.power(bC).toString();
            }
        }
        else if (Complex.isComplex(subB)) {
            Complex aC = new Complex(a);
            Complex bC = new Complex(subB);
            if (b.contains("-$"))
                bC=bC.multiScalar(-1);
            trace = a+"^"+subB;
            result = aC.power(bC).toString();
        }
        
        if (result.equals("NaN")) 
            throw new IllegalArgumentException("The result of a part of the operation is not a valid number. <<"+trace+">>");
        else
            System.out.println(this.getName()+": Operating this math problem: ["+trace+" = "+result+"]");
        return result;
    }

    private String multi(String a ,String b) {
        String result = "NaN";
        String trace = a+"x"+b;
        String subA = OperationUtils.obtenerSubstringEntreTokens(a,'$','$');
        String subB = OperationUtils.obtenerSubstringEntreTokens(b,'$','$');

        if (OperationUtils.isInteger(a) && OperationUtils.isInteger(b)) {
            trace = a+"x"+b;
            int aI = Integer.parseInt(a);
            int bI = Integer.parseInt(b);
            result = String.valueOf(aI*bI);
        }
        else if (OperationUtils.isReal(a) && OperationUtils.isReal(b)){
            trace = a+"x"+b;
            double aD = Double.parseDouble(a);
            double bD = Double.parseDouble(b);
            result = String.valueOf(aD*bD);
        }
        else if (Complex.isComplex(subA) && Complex.isComplex(subB)) {
            trace = subA + "x"+ subB;
            Complex aC = new Complex(subA);
            Complex bC = new Complex(subB);
            if (a.contains("-$"))
                aC=aC.multiScalar(-1);
            if (b.contains("-$"))
                bC=bC.multiScalar(-1);
            result = aC.multi(bC).toString();
        }

        if (result.equals("NaN")) 
            throw new IllegalArgumentException("The result of a part of the operation is not a valid number. <<"+trace+">>");
        else
            System.out.println(this.getName()+": Operating this math problem: ["+trace+" = "+result+"]");
        return result;
    }

    private String divide(String a ,String b) {
        String result = "NaN";
        String trace = a+"/"+b;
        String subA = OperationUtils.obtenerSubstringEntreTokens(a,'$','$');
        String subB = OperationUtils.obtenerSubstringEntreTokens(b,'$','$');

        if (OperationUtils.isInteger(a) && OperationUtils.isInteger(b)) {
            trace = a+"/"+b;
            int aI = Integer.parseInt(a);
            int bI = Integer.parseInt(b);
            result = String.valueOf(aI/(1.0*bI));
        }
        else if (OperationUtils.isReal(a) && OperationUtils.isReal(b)) {
            trace = a+"/"+b;
            double aD = Double.parseDouble(a);
            double bD = Double.parseDouble(b);
            result = String.valueOf(aD/bD);
        }
        else if (Complex.isComplex(subA) && Complex.isComplex(subB)) {
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
                System.out.println("ACaasasasdasdd");
                result = "NaN";
            }
        }

        if (result.equals("NaN")) 
            throw new IllegalArgumentException("The result of a part of the operation is not a valid number. <<"+trace+">>");
        else
            System.out.println(this.getName()+": Operating this math problem: ["+trace+" = "+result+"]");
        return result;
    }

    private String add(String a ,String b) {
        String result = "NaN";
        String trace = a+"+"+b;
        String subA = OperationUtils.obtenerSubstringEntreTokens(a,'$','$');
        String subB = OperationUtils.obtenerSubstringEntreTokens(b,'$','$');

        if (OperationUtils.isInteger(a) && OperationUtils.isInteger(b)) {
            trace = a+"+"+b;
            int aI = Integer.parseInt(a);
            int bI = Integer.parseInt(b);
            result = String.valueOf(aI+bI);
        }
        else if (OperationUtils.isReal(a) && OperationUtils.isReal(b)) {
            trace = a+"+"+b;
            double aD = Double.parseDouble(a);
            double bD = Double.parseDouble(b);
            result = String.valueOf(aD+bD);
        }
        else if (Complex.isComplex(subA) && Complex.isComplex(subB) ) {
            trace = subA+"+"+subB;
            Complex aC = new Complex(subA);
            Complex bC = new Complex(subB);
            if (a.contains("-$"))
                aC=aC.multiScalar(-1);
            if (b.contains("-$"))
                bC=bC.multiScalar(-1);
            result = aC.add(bC).toString();
        }

        if (result.equals("NaN")) 
            throw new IllegalArgumentException("The result of a part of the operation is not a valid number. <<"+trace+">>");
        else
            System.out.println(this.getName()+": Operating this math problem: ["+trace+" = "+result+"]");
        return result;
    }

    private String sub(String a ,String b) {
        String result = "NaN";
        String trace = a+"-"+b;
        String subA = OperationUtils.obtenerSubstringEntreTokens(a,'$','$');
        String subB = OperationUtils.obtenerSubstringEntreTokens(b,'$','$');
        if (OperationUtils.isInteger(a) && OperationUtils.isInteger(b)) {
            trace = a+"-"+b;
            int aI = Integer.parseInt(a);
            int bI = Integer.parseInt(b);
            result = String.valueOf(aI-bI);
        }
        else if (OperationUtils.isReal(a) && OperationUtils.isReal(b)) {
            trace = a+"-"+b;
            double aD = Double.parseDouble(a);
            double bD = Double.parseDouble(b);
            result = String.valueOf(aD-bD);
        }
        else if (Complex.isComplex(subA) && Complex.isComplex(subB)) {
            trace = subA+"-"+subB;
            Complex aC = new Complex(subA);
            Complex bC = new Complex(subB);
            if (a.contains("-$"))
                aC=aC.multiScalar(-1);
            if (b.contains("-$"))
                bC=bC.multiScalar(-1);
            result = aC.sub(bC).toString();
        }

        if (result.equals("NaN")) 
            throw new IllegalArgumentException("The result of a part of the operation is not a valid number. <<"+trace+">>");
        else
            System.out.println(this.getName()+": Operating this math problem: ["+trace+" = "+result+"]");
        return result;
    }
}
