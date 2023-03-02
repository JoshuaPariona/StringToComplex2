package Calculator;

import java.util.regex.Pattern;

public class OperationUtils {
    public static boolean isInteger(String integer) {
        Pattern pattern = Pattern.compile("^[-+]?\\d+$");
        return pattern.matcher(integer).matches();
    }

    public static boolean isReal(String real) {
        Pattern pattern = Pattern.compile("^[-+]?\\d*\\.?\\d+$");
        return pattern.matcher(real).matches();
    }

    public static Integer isRationalInverse(String d) {
        String f;
        double valor = Double.parseDouble(d);
        d = String.format("%.5f",valor);
        for (int i = 2; i <= 40; i++) {
            f = String.format("%.5f", 1.0/i);
            if (d.equals(f))
                return i;
            f = String.format("%.5f", -1.0/i);
            if (d.equals(f))
                return -i;
        } 
        return null;
    }
}
