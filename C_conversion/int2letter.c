char *int2letter(unsigned int chiffre)
{

/*Déclaration des variables*/
	unsigned int centaine, dizaine, unite, reste, y, i;
	int dix = 0;
	char *lettre=malloc (sizeof(char)*256);    		// la chaine "lettre" contiendra le résultat
	reste = chiffre;		// on affecte le nombre étudié à la variable "reste"
	
/*Début de la fonction*/	
	for(i=1000000000; i>=1; i/=1000)	// on divise par des multiple de 1000, pour avoir un groupe 
						// de 3 chiffres appartenant aux millions, milliers et le reste
	{
		y = reste/i;
					
		if(y!=0)			// si y est nul, c'est qu'il n'y a pas de chiffre pour
		{				// les millions, ou les milliers, etc...
		    
		    dix=0;
		    centaine = y/100;		// on récupère le chiffre des centaines
		    dizaine  = (y%100)/10; 	// permet d'obtenir le chiffre correspondant au dizaines
		    unite = y%10;  		// permet d'obtenir le chiffre des unités
		   
		    switch(centaine)		// dans ce switch, on étudie le chiffre des centaines
		    {				// suivant sa valeurs on ajoute à la chaine résultat le mot correspondant
		        case 0:
		            	    break;
		        case 1:
				    strcat(lettre,"cent ");
				    break;
		        case 2:
				    strcat(lettre,"deux cent ");
				    break;
		        case 3:
				    strcat(lettre,"trois cent ");
				    break;
		        case 4:
				    strcat(lettre,"quatre cent ");
				    break;
		        case 5:
				    strcat(lettre,"cinq cent ");
				    break;
		        case 6:
				    strcat(lettre,"six cent ");
				    break;
		        case 7:
				    strcat(lettre,"sept cent ");
				    break;
		        case 8:
				    strcat(lettre,"huit cent ");
				    break;
		        case 9:
		            	strcat(lettre,"neuf cent ");
			default:
			    	    break;
		    }// fin du Switch(centaine), on a mis la valeur du chiffre des centaine dans lachaine "lettre"
	    
		    switch(dizaine)			// on étudie maintenant le chiffre des dizaines
		    {
		        case 0:
		            	    break;
		        case 1:
				    dix = 1; 			// cette variable nous permet de savoir quand 
				    break;			// mettre "onze" à la place de "un"
		        case 2:
				    strcat(lettre,"vingt ");
				    break;
		        case 3:
				    strcat(lettre,"trente ");
				    break;
		        case 4:
				    strcat(lettre,"quarante ");
				    break;
		        case 5:
				    strcat(lettre,"cinquante ");
				    break;
		        case 6:
				    strcat(lettre,"soixante ");
				    break;
		        case 7:
				    dix = 1;				
				    strcat(lettre,"soixante ");
				    break;
		        case 8:
				    strcat(lettre,"quatre-vingt ");
				    break;
		        case 9:
				    dix = 1; 				
				    strcat(lettre,"quatre-vingt ");
				    break;
			default:
			    	    break;                   
		    } // fin du Switch(dizaine), la chiane "lettre" contient maintenant les centaines et les dizaines.
		    
	
		    switch(unite)
		    {
		        case 0:
				    if(dix) strcat(lettre,"dix ");
				    break;
		        
			case 1:
				    if(dix && dizaine==7){		// On traite ici chaque cas, pour savoir quand 
					strcat(lettre,"et onze ");	// il faut écrire "et un" ou "un" ou "et onze" pour 71.
			  	    }
				    else if(dix){
					strcat(lettre,"onze ");
				    }
				    else if(dizaine==2 || dizaine==3 ||dizaine==4 || dizaine==5|| dizaine==6){	// ajout du "et"
				  	strcat(lettre,"et un ");
				    }
				    else if(i==1000 && dizaine==0 && centaine==0){	//ne pas écrire "un mille"
					strcat(lettre, "");				// pour 1900 par exemple
				    }
				    else{
					strcat(lettre,"un ");
				    } 				
				    break;
		        
			case 2:
				    if(dix) strcat(lettre,"douze ");
				    else    strcat(lettre,"deux ");
				    break;

		        case 3:
				    if(dix) strcat(lettre,"treize ");
				    else    strcat(lettre,"trois ");
				    break;

		        case 4:
				    if(dix) strcat(lettre,"quatorze ");
				    else    strcat(lettre,"quatre ");
				    break;

		        case 5:
				    if(dix) strcat(lettre,"quinze ");
				    else    strcat(lettre,"cinq ");
				    break;

		        case 6:
				    if(dix) strcat(lettre,"seize ");
				    else    strcat(lettre,"six ");
				    break;

		        case 7:
				    if(dix) strcat(lettre,"dix-sept ");
				    else    strcat(lettre,"sept ");
				    break;

		        case 8:
				    if(dix) strcat(lettre,"dix-huit ");
				    else    strcat(lettre,"huit ");
				    break;

		        case 9:
				    if(dix) strcat(lettre,"dix-neuf ");
				    else    strcat(lettre,"neuf ");
			default:
				    break;
		    }   // fin du Switch(unite) la chaine "lettre" contient maintenant le résultat :
			// 	"centaine + dizaine + unité"	    
		    
		    switch (i)		// suivant la valeur de i, on ajout à la suite de "lettre" le rang
		    {			// auquel appartient le groupe de 3 chiffres que l'on a étudié
		        case 1000000000:
		            strcat(lettre,"milliard ");
		            break;
		        case 1000000:
		            strcat(lettre,"million ");
		            break;
		        case 1000:
		            strcat(lettre,"mille ");                    
		    }

		} // fin de if(y!=0)
		reste -= y*i;
		dix = 0;
    	} // fin du for

	if(strlen(lettre)==0) strcpy(lettre,"zero");    // on regarde si la chaine renvoyée contient quelque chose ou non
						    	// car si on a demandé "0", la chaîne est restée vide jusqu'à maintenant.
	return(lettre);
}

