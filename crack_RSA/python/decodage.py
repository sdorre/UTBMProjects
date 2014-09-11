#travail en cooperation :
#----DORRE Stephane
#----HESTIN Alexis

from math import sqrt

def findPQ(n):
    p, q = 2, 2

    while (p<=q):
        #it begins with p=2 and q=n/2, p grows until finding an integer number.
        q=n//p
        if ((p*q)==n and estpremier(p) and estpremier(q)):
            break
        p+=1 
    return p, q
			
			
def estpremier(n):
    "In this function, the number n is tested with the divison by 2 and by 3"
    "Then all other numbers under n are tested. If there is no integer quotient, the number is prime"
    premier=(n!=1)
    if(n!=2 and n!=3):
        if(n%2==0 or n%3==0):
            premier = False
        else:
            i=1 
            while(premier and (6*i+1)<= sqrt(n)):
                if (n%(6*i+1)==0 or n%(6*i-1)==0):
                    premier = false;
                i+=1
    return premier;

def iterative_egcd(a, b):
    "Algorithme d'Euclide generalise,"
    "it determines the reverse coefficient turn by turn until the remainer becomes null"
    "it returns the modulo reverse of a"
    x, y, u, v, = 0, 1, 1, 0
    while a!=0:

        #it executes the euclidian division
        q, r =b//a, b%a

        #the coefficient we saw in TD ( relation : X(k)=X(k-1)-Q(k-1)*X(k-1) )
        m, n = x-u*q, y-u*q

        #variables change before starting the next turn.
        b, a, x, y, u, v = a, r, u, v, m, n

    
    #it returns the Greatest Commun Divisor of a and b, and it returns also the reverse of a
    return b, x


def modinv(a, mod):
    #we take the GCD and the reverse. if the gcd is 1, the two number a and mod are prime. 
    #and it return the reverse of a modulo mod

    g, x = iterative_egcd(a, mod)
    if g!=1:
        return None
    else:
        return x%mod


def expomod(a, e, m):
    "Calculation of  modulo exponential, a = base, e = exponent, m = modulus"
    "We use the exponent's binary value, get by successive euclidian divison by 2."
    
    x=1
    a=a%m
    while e>0:
        if (e%2)==1:
            #if the bit of e is 1, it multipy the result x by a^(2^m)
            x=(x*a)%m
        e=e//2
        a=(a**2)%m
    return x


def decodeNb(nb):
    "decoding of the number nb, base 30 number --> 3-letter group"
    base = 30
    troisLettre=""
    #we take the first quotient
    un = nb//(base**2)
    nb = nb%(base**2)
    #we take the second quotient and the last remainer
    deux = nb//(base)
    trois = nb%base
    #it searches each letter in the alphabet
    troisLettre = troisLettre + alphabet[un] + alphabet[deux] + alphabet[trois]
    return troisLettre


def decodage():
    "main function decoding the initial message"
    "message, public key, and the alphabet are global variables"

    #it determines p and q, n factors
    p, q = findPQ(n)
    print ("-------------------------------------------------------")
    print (" les facteurs de n =", n, "sont :")
    print ("\t p = ", p,  " ; q = ", q)

    #it determines phi indicator
    print ("-------------------------------------------------------")
    phi=(p-1)*(q-1)
    print ("\n On calcule phi = ", phi)

    #calcutation of d the reverse of e modulo phi
    print ("-------------------------------------------------------")
    d = modinv(e, phi)
    if (d == None):
        print("\n d n'a pas été trouvé, il doit y avoir un problème avec les valeurs trouvées plus haut")
                
    else:
        print ("\n l'inverse de e =", e," est  d =", d)

        #we take each number of the initial message
        messageDecoupe=messageCode.split()
        listedecode=[]

        #we put all coded number in a list
        for nb in messageDecoupe:
            listedecode.append(expomod(int(nb), d, n))

        #We create a new string which is the decoded message
        messageDecode=""
        for x in listedecode:
            messageDecode = messageDecode+decodeNb(x)

        print ("\nMessage de décodé :\n",messageDecode)

    
if __name__ == "__main__":
    #Huge numbers are native supported by python. the different variables are global variables.
    #Program designed on Python 2.7

    alphabet =["A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"," ",".",",","'"]
    messageCode = "30667 53268 51818 59080 36734 37003 1184 280 1674 61934 36260 61869 2958 46846 41683 30667 66805 16105 18037 6485 37951 56027 68066 41041 68554 1918 10489 46330 30720 49602 68374 13460 4444"
    n = 70613
    e = 16633
        
    decodage()


