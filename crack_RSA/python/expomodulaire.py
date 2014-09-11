from math import log
 
#a=input('Entrez la base a:')
#s=v=input('Entrez la puissance s:')
#n=input ('Entrez le module n:')

def expomodulaire(a, s, n):
        v=s
        f=u=0
        t1,t2,t3,p=[],[a],[],-1
        j=z=a
         
        while s!=f:
                s-=f
                c=int(log(s)/log(2))
                f=2**c
                t1.append(c)
        while u!=t1[0]:
                z=j**2%n
                j=z
                u+=1
                t2.append(z)
        while p<len(t1)-1:
                p+=1
                t3.append(t2[t1[p]])
         
        print a,'^',v,'(mod',n,')','=', reduce(lambda x, y: x *y, t3)%n
