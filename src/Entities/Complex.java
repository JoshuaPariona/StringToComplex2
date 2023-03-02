//@Author: Joshua Pariona

package Entities;

import java.util.regex.Pattern;

import java.lang.Math;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;

public class Complex {
    private final double pi = Math.PI ;
    private final String rounding = "%.14f"; //%.17f  
    private String strComplex;
    private Double partReal = 0.0;
    private Double partImg = 0.0;
    private Double radius;
    private Double angle;
    private String quadrant;
    private final HashMap<Integer,Double> system = new HashMap<Integer,Double>() {{
        put(0,1.0);
        put(1,0.017453292519943295); //DEGREES_TO_RADIANS
        put(2,0.015708); //CENTESIMAL_TO_RADIANS
        put(3,0.000290888); //MINUTEARC_TO_RADIAS
    }};

    public Complex(double partReal,double partImg) {
        this.partReal = partReal;
        this.partImg = partImg;
        String operator = Double.compare(this.partImg, 0.0) < 0 ? "" : "+";
        this.strComplex = String.format(rounding,this.partReal)+operator+String.format(rounding,this.partImg)+"i";
        this.radius = Math.sqrt(Math.pow(this.partReal, 2)+ Math.pow(this.partImg, 2));
        double auxAngle = Math.atan2(this.partImg,this.partReal);
        this.angle = this.convertToPositive(auxAngle);
        this.quadrant = this.quadrant();
    }

    public Complex(String complex) {
        complex = complex.strip();
        boolean valid = isComplex(complex);
        if(valid) {
            this.strComplex = complex;
            this.separate(complex);
            this.radius = Math.sqrt(Math.pow(this.partReal, 2) + Math.pow(this.partImg, 2));
            double auxAngle = Math.atan2(this.partImg,this.partReal);
            this.angle = this.convertToPositive(auxAngle);
            this.quadrant = this.quadrant();
        }
        else
            throw new IllegalArgumentException("The string does not meet the expected format");
    }

    public Complex(double radius, double argument, int system) {
        // system
        // 0 -> radians
        // 1 -> degrees 
        // 3 -> centesimal
        double radiansAng;
        if (system >= 0) {
            radiansAng = this.system.get(system)*argument;
            radiansAng = this.normalizeAngle(radiansAng);
        }
        else {
            System.out.println("Unknown system, will be considered in radians");
            radiansAng = this.normalizeAngle(argument);
        }
        if (radius >= 0.0) {
            this.partReal = radius*Math.cos(radiansAng);
            this.partImg = radius*Math.sin(radiansAng);
            this.angle = radiansAng;
            this.quadrant = this.quadrant();
            this.radius = radius;
            String operator = Double.compare(this.partImg, 0.0) < 0 ? "" : "+";
            this.strComplex = String.format(rounding,this.partReal)+operator+String.format(rounding,this.partImg)+"i";
        }
        else if (Double.isNaN(radius)){
            this.partReal = Double.NaN;
            this.partImg = Double.NaN;
            this.angle = Double.NaN;
            this.quadrant = "NaN";
            this.radius = Double.NaN;
            this.strComplex = "NaN";  
        }
        else{
            throw new IllegalArgumentException("The radius of the complex number must be greater than zero.");
        }
    }

    public boolean isGood() {
        return this.strComplex != null;
    }

    public double getPartReal() {
        return partReal;
    }

    public double getPartImg() {
        return partImg;
    }

    public double getModulus() {
        return this.radius;
    }

    public double getArgument() {
        return this.angle;
    }

    public Pair<Double,Double> getCoordinates() {
        return new Pair<Double,Double>(this.partReal,this.partImg);
    }

    @Override
    public String toString() {
        return this.strComplex;
    }

    public String quadrant() {
        angle = Math.abs(this.angle);
        if (angle >= 0 && angle < pi/2) {
            return "I";
        } 
        else if (angle >= pi/2 && angle < pi) {
            return "II";
        }
        else if (angle >= pi && angle < 3.0*pi/2) {
            return "III";
        }
        else if (angle >= 3.0*pi/2 && angle < 2.0*pi) {
            return "IV";
        }
        else return "null";
    }
    
    private double normalizeAngle(double angle) {
        while (angle < 0.0) {
            angle += 2 * pi;
        }
        while (angle >= 2 * pi) {
            angle -= 2 * pi;
        }
        return angle;
    }

    private double convertToPositive(double angle) {
        if (angle < 0) 
            angle = angle + (2 * pi);
        return angle;
    }

    private void separate(String complex) {
        Pattern pattern = Pattern.compile("^([-+]?\\d+(\\.\\d+)?)?([-+]?(\\d+(\\.\\d+)?)?[ij])?$"); //5 groups
        Matcher match = pattern.matcher(complex);
        
        Pattern reversePattern = Pattern.compile("^([-+]?(\\d+(\\.\\d+)?)?[ij])?([-+]?\\d+(\\.\\d+)?)?$");
        Matcher reverseMatch = reversePattern.matcher(complex);

        boolean c, d, e; 

        if (match.find()) {
            c = match.group(3) == null;
            d = match.group(4) == null;
            e = match.group(5) == null;
        }
        else if (reverseMatch.find()) {
            String reComplex = complexRefactor(complex);
            separate(reComplex);
            return;
        }
        else
            throw new IllegalArgumentException("a refactor error has occurred");

        if (c && d && e) {
            //only real
            this.partReal = Double.parseDouble(match.group(1));
            this.partImg = 0.0;
            return;
        }
        else if (d && e) {
            //only ima
            this.partReal = 0.0;
            if (match.group(1) == null){
                //only i
                String ima = match.group(3);
                if (ima.equals("-i"))
                    this.partImg = -1.0;
                else if (ima.equals("+i") || ima.equals("i"))
                    this.partImg = 1.0;
            } 
            else{
                String ima = match.group(3);
                //exepcion +-a+-i
                if (ima.equals("i"))
                    this.partImg = Double.parseDouble(match.group(1));
                else{
                    this.partReal= Double.parseDouble(match.group(1));
                    if (ima.equals("+i"))
                        this.partImg = 1.0;
                    else if (ima.equals("-i"))
                        this.partImg = -1.0;
                }
            }
            return;
        }
        // format a + bi
        this.partReal = Double.parseDouble(match.group(1));
        String ima = match.group(3);
        String numb = ima.substring(0, ima.length() - 1);
        this.partImg = Double.parseDouble(numb);
    }

    public static boolean isComplex(String complex) {
        if (complex.equals(""))
            return false;
        Pattern pattern = Pattern.compile("^([-+]?\\d+(\\.\\d+)?)?([-+]?(\\d+(\\.\\d+)?)?[ij])?$"); //the best complex regular expression, made by me XD
        Matcher match = pattern.matcher(complex);
        Pattern helpPattern = Pattern.compile("^[+-]?\\d+\\.\\d+\\.\\d+[ij]$"); //the only exception XD'nt
        Matcher helpMatch = helpPattern.matcher(complex);

        Pattern reversePattern = Pattern.compile("^([-+]?(\\d+(\\.\\d+)?)?[ij])?([-+]?\\d+(\\.\\d+)?)?$");
        Matcher reverseMatch = reversePattern.matcher(complex);

        if((reverseMatch.find() || match.find()) && !helpMatch.find()) {
            return true;
        }
        else
            return false;
    }

    public String complexRefactor (String complex) {
        if (complex.contains("+") || complex.contains("-")){
            Pattern pattern = Pattern.compile("([+-])");
            Matcher matcher = pattern.matcher(complex);
            ArrayList<String> parts = new ArrayList<String>();
            int start = 0;
            while (matcher.find()) {
                String part = complex.substring(start, matcher.start());
                parts.add(part);
                parts.add(matcher.group());
                start = matcher.end();
            }
            parts.add(complex.substring(start));
            if (parts.size()>4){
                complex = parts.get(parts.size()-2)+parts.get(parts.size()-1)+parts.get(parts.size()-4)+parts.get(parts.size()-3);
                this.strComplex = complex;
            }
            else if (parts.size()==3 && !parts.get(0).equals("")){
                complex = parts.get(parts.size()-2)+parts.get(parts.size()-1)+"+"+parts.get(parts.size()-3);
                this.strComplex = complex;
            }
        }
        return complex;
    }

    public void showInfo(){
        System.out.println("Complex: "+this.strComplex);
        System.out.println("Part Real: "+this.partReal);
        System.out.println("Part Ima: "+this.partImg);
        System.out.println("Argument: "+this.angle);
        System.out.println("Modulus: "+this.radius);
        System.out.println("Quadrant: "+this.quadrant);
    }

    public Complex conjugate() {
        double real = this.partReal; 
        double img = this.partImg*(-1.0); 
        return new Complex(real,img);
    }

    public Complex opposite() {
        double real = this.partReal*(-1.0); 
        double img = this.partImg*(-1.0); 
        return new Complex(real,img);
    }

    public Complex add(Complex c) {
        double real = 0,img = 0;
        real = this.partReal + c.partReal;
        img = this.partImg + c.partImg;
        return new Complex(real,img);
    }

    public Complex sub(Complex c) {
        double real = 0,img = 0;
        real = this.partReal - c.partReal;
        img = this.partImg - c.partImg;
        return new Complex(real,img);
    }

    public Complex multi(Complex c) {
        double real = 0,img = 0;
        real = this.partReal * c.partReal - this.partImg * c.partImg;
        img = this.partReal * c.partImg + this.partImg * c.partReal;
        return new Complex(real,img);
    }

    public Complex multiScalar(double s) {
        double real = this.partReal*s;
        double img = this.partImg*s;
        return new Complex(real,img);
    }

    public Complex divide(Complex c) {
        double real = 0, img = 0;
        double d1 = c.partReal, d2 = c.partImg;
        double denominator = d1 * d1 + d2 * d2;
        if (denominator != 0) {
            real = (this.partReal*d1 + this.partImg*d2) / denominator;
            img = (this.partImg*d1 - this.partReal*d2) / denominator;
        }
        else {
            throw new IllegalArgumentException("Division by zero");
        }
        return new Complex(real, img);
    }

    public Complex inverse() {
        Complex con = this.conjugate();
        Double denominator = this.radius*this.radius; 
        Complex inv = con.divide(new Complex(denominator,0.0));
        return inv;
    }

    public Complex power(int p) {
        int sgn = (int) Math.signum((float)p);
        if (sgn == 0)
            return new Complex(1,0);
        Complex newComplex = this;
        Complex auxComplex = this;
        p *= sgn; 
        for (int i = 1; i < p; i++)
            newComplex = newComplex.multi(auxComplex);
        if (sgn < 0) {
            Complex unitComplex = new Complex(1,0);
            newComplex = unitComplex.divide(newComplex);
        }
        return newComplex;
    }

    public Complex power(Complex c) {
        Complex unit = new Complex("0+1i");
        Double l = Math.log(this.radius);
        double r1 = l *c.radius;
        Complex c1 = new Complex(r1, c.angle, 0);
        double r2 = this.angle*c.radius;
        Complex c2 = new Complex(r2, c.angle, 0);
        Complex p = c2.multi(unit);
        Complex e = p.add(c1);
        Complex fin = new Complex(Math.exp(e.partReal), e.partImg, 0);
        return fin;
    }

    public Vector<Complex> power(int numerator, int denominator) {
        Complex powerComplex = this.power(numerator);
        Vector<Complex> rootListComplex = powerComplex.root(denominator);
        return rootListComplex;
    }

    public Vector<Complex> root(int index) {
        int sgn = (int) Math.signum((float) index);
        if (sgn == 0) {
            System.out.println("Zero index, null is returned");
            return null;
        }
        Vector<Complex> rootListComplex = new Vector<Complex>();
        index *= sgn;
        double rootRadius = Math.pow(this.radius,(((double)1)/index));
        for (int k = 0; k < index; k++) {
            double rootArgument = (this.angle+(2*k*pi))/index;
            Complex root = new Complex(rootRadius, rootArgument, 0);
            if (sgn < 0) {
                Complex unitComplex = new Complex(1,0);
                root = unitComplex.divide(root);
            }
            rootListComplex.add(root);
        }
        return rootListComplex;
    }

    //TODO:
    public Vector<Complex> root(Complex index){
        Vector<Complex> rootListComplex = new Vector<Complex>();
        return rootListComplex;
    }
}  