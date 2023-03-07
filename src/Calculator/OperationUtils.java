package Calculator;

import java.util.regex.Pattern;
import Entities.Pair;

public class OperationUtils {
    public static boolean isInteger(String integer) {
        if (integer.length() > String.valueOf(Integer.MAX_VALUE).length())
            return false;
        Pattern pattern = Pattern.compile("^[-+]?\\d+$");
        return pattern.matcher(integer).matches();
    }

    public static boolean isReal(String real) {
        Pattern pattern = Pattern.compile("^[-+]?\\d*\\.?\\d+$");
        if (pattern.matcher(real).matches())
            return true;
        pattern = Pattern.compile("^[+-]?\\d+(\\.\\d+)?([Ee][+-]?\\d+)?$");
        if (pattern.matcher(real).matches())
            return true;
        if (real.equals("Infinity") || real.equals("infinity") || real.equals("NaN"))
            return true;
        try {
            double d = Double.parseDouble(real);
            return d <= Double.MAX_VALUE && d >= Double.MIN_VALUE;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String obtenerSubstringEntreTokens(String cadena, char token1, char token2) {
        int inicio = cadena.indexOf(token1);
        if (inicio == -1)
            return cadena;
        inicio++;
        int fin = cadena.indexOf(token2, inicio);
        if (fin == -1) 
            return cadena;
        return cadena.substring(inicio, fin);
    }

    public static Pair<Integer,Integer> Rational(String d) {
        String f;
        String format = "%.5f";
        double valor = Double.parseDouble(d);
        d = String.format(format,valor);
        for (int i = 1; i <= 40; i++) {
            for (int j = 2; j <= 50; j++) {
                f = String.format(format, 1.0*i/j);
                if (d.equals(f))
                    return new Pair<Integer,Integer>(i,j);
                f = String.format(format, -1.0*i/j);
                if (d.equals(f))
                    return new Pair<Integer,Integer>(i,-j);
            } 
        }
        return null;
    }
}
