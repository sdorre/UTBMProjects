def iterative_egcd(a, b):
    "Algorythme d'Euclide generalise,"
    "on calcule les coefficients au fur et a mesure et on renvoi le coef"
    "correspondant a l'inverse de a"
    
    x, y, u, v, = 0, 1, 1, 0
    while a!=0:
        
        #on effectue les divisions entiere successives
        q, r =b//a, b%a

        #on calcule les coefficients directement
        m, n = x-u*q, y-u*q

        #on passe au niveau suivant
        b, a, x, y, u, v = a, r, u, v, m, n

    #on retourne le pgcd des 2 nombres demandes, et l'inverse de a
    return b, x

def modinv(a, mod):

    #on recupere le pgcd et l'inverse, si le pgcd est 1, les 2 nombres sont bien premiers
    #et on retourne l'inverse modulo le modulus.

    g, x = iterative_egcd(a, mod)
    if g!=1:
        return None
    else:
        return x % mod

if __name__== "__main__":
    print modinv(16633, 70080)
