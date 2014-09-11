def codageLettre(message, e, n):
    message_code=""
    
    while len(message)%3!=0:
        message+=" "

    while message != "":
        troislettre=message[0:3]
        message=message[3:]
        nb = codageTroisLettre(troislettre)
        message_code+= str(expomod(nb, e, n))+" "

    return message_code

        
def codageTroisLettre(group):
    alphabet =["A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"," ",".",",","'", "!", "?"]
    base = 30
    nb =0
    for i in range(3):
        print alphabet.index(group[i])
        nb*=base
        nb+=(alphabet.index(group[i]))
    return nb

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


if __name__ == "__main__":
    n = 70613
    e = 16633

    message = "THIS SENTENCE IS ENCODED BY SDORRE"
    codee = codageLettre(message, e, n)
    print codee
    
