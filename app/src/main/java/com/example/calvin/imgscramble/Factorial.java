package com.example.calvin.imgscramble;

/**
 * Created by calvin on 6/4/15.
 */
import java.math.BigInteger;

public class Factorial {
  BigInteger[] dict;
  boolean memo = true;
  public Factorial (int n) {
    //Set n to zero if you dont want to initialize dict
    if (n == 0){
      this.memo = false;
    }
    //Initialize memoisation dictionary
    else{
      this.dict = new BigInteger[n];
      for(int e=0; e<n; e++){
        dict[e] = BigInteger.ZERO;
      }
      dict[0] = BigInteger.ONE;
      dict[1] = BigInteger.ONE;
    }
  }

  public BigInteger factorial(int n){
    if(memo){
      if (dict[n].compareTo(BigInteger.ZERO)==0){
        BigInteger fact = BigInteger.ONE;
        for(int i=2; i<=n; i++){
          fact = fact.multiply(BigInteger.valueOf(i));
          dict[i]=fact;
        }
        return fact;
      }
      else{
        return dict[n];
      }
    }
    else{
      BigInteger fact = BigInteger.ONE;
      for(int i = 1; i <= n; i++){
        fact = fact.multiply(BigInteger.valueOf(i));
      }
      return fact;
    }
  }
}
