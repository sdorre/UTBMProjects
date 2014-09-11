/********************************************
 * TP MI44 : Arithmétique et cryptographie
 * 
 * A rendre à florent.perronnet@utbm.fr
 * 
 * DORRE Stéphane
 * HESTIN Alexis
 ********************************************/

import java.math.*;

public class Cryptography {	
	
	private String[] alphabet ={"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"," ",".",",","'"};
	private long n = 70613;
	private long e = 16633;
	private long data[] = {30667,53268,51818,59080,36734,37003,1184,280,1674,61934,36260,61869,2958,46846,41683,30667,66805,16105,18037,6485,37951,56027,68066,41041,68554,1918,10489,46330,30720,49602,68374,13460,4444};
	private BigInteger dataDechiffre[] = new BigInteger[data.length];
	
	public void initialization()
	{
		System.out.println("Clé publique : (n="+n+" e="+e+")\n");
		
		/*Step 1 : Find p and q*/
		
		long p=2,q=2;
		
		for (p=2; p<=q; p++)
		{
			//Division by all the numbers to find an integer which is not rounded
			q=n/p;
			if (p*q==n)
			{	
				System.out.println("p="+p+" * q="+q+" = "+n);
				break;
			}
		}

		System.out.println("p premier ? "+isPrimal(p));
		System.out.println("q premier ? "+isPrimal(q)+"\n");
		
		/*Step 2 : Calculation of phi(n)*/
		
		long phi = (p-1)*(q-1);
		System.out.println("phi = (p-1)(q-1) = "+phi);
		
		/*Step 3 : Find the decryption exponent d*/
		
		long d = inverse(e, phi);
		System.out.println("d = modInverse(phi) = "+d);
		System.out.println("\nClé privée : (n="+n+" d="+d+")");
		
		/*Step 4 : Decrypting the message*/
		
		//Converting values in BigInteger in order to use the methods of the BigInteger class
		BigInteger n1 = BigInteger.valueOf(n);
		BigInteger d1 = BigInteger.valueOf(d);
		
		System.out.println("\nDéchiffrage du message : ");
		for (int i=0; i<data.length; i++){
			dataDechiffre[i]=BigInteger.valueOf(data[i]).modPow(d1,n1);
			System.out.print(dataDechiffre[i]+" ");
		}
		System.out.println();
		
		/*Step 5 : Reconstruction of the text*/
		
		System.out.println("\nReconstitution du texte : ");
		for (int i=0; i<dataDechiffre.length; i++){
			System.out.print(decomposition(dataDechiffre[i]));
		}
		
	}

	
	/*
	 * Boolean function isPrimal
	 * It indicates if the received parameter is a prime number
	 */
	
	public boolean isPrimal(long nb){
		for(int i=2;i<nb;i++){
			if(nb%i==0)
				return false;
		}
		return true;
	}
	
	/*
	 * Function decodage
	 * It breaks down the decrypted number nb into a string of 3 characters
	 */
	
	public String decomposition(BigInteger nb){
		String troisLettre = "";
		BigInteger div[]=new BigInteger[2];
		
		//The method DivideAndRemainder gives the result of the integer division to div[0] and the modulo to div[1]
		div = nb.divideAndRemainder(BigInteger.valueOf(900));
		troisLettre = troisLettre + alphabet[div[0].intValue()];
		div = div[1].divideAndRemainder(BigInteger.valueOf(30));
		troisLettre = troisLettre + alphabet[div[0].intValue()];
		troisLettre = troisLettre + alphabet[div[1].intValue()];
		return troisLettre;
	}
	
	/*
	 * Function inverse
	 * It returns the inverse of the received e
	 */
	
	public long inverse(long e, long phi){
		long i = phi, v = 0, u = 1;
		while (e>0) {
			long t = i/e, x = e;
			e = i % x;
			i = x;
			x = u;
			u = v - t*x;
			v = x;
		}
		v %= phi;
		if (v<0) 
			v = (v+phi)%phi;
		return v;
	}
	
	
	public static void main(String[] args){
		Cryptography cry = new Cryptography();
		cry.initialization();
	}
}
