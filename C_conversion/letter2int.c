int letter2int(char *Phrase){
	
/*Déclaration des variables nécessaires*/
	char MotEnLettre[24][10]=
	{"un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf",
	 "dix","onze", "douze", "treize", "quatorze", "quinze", "seize", "vingt",
	 "trente", "quarante", "cinquante", "soixante", "cent", "mille", "million"} ;
				/*ce tableau sert de "dictionnaire, contenant tous les mots à utiliser pour
					 traduire un nombre écrit en toute lettre */

	int CorrespondanceChiffre[25]=
	{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 20, 30, 40, 50, 60, 100, 1000, 1000000,0};	
				/*ce tableau est utilisé en parallèle au précédent pour doner la valeur de chaque mot trouvé */
	
	int y=0;
	char *ChaqueMot={""};
	unsigned int chiffreActuel=0, chiffrePrecedent=0;
	unsigned int Total = 0, TotalMillion = 0; //résultat du nombre tappé
	
/*Début du programme*/	
	ChaqueMot = strtok(Phrase, " ");	// je récupère le premier mot de la chaine.

	
	while( ChaqueMot != NULL ) {		// je récupère tous les autres mots de la chaine.
   
 		y = 0;		// variable permettant de parcourir le "dictionnaire", par chaque nouveau mot, on initialise	
		
		while(strcmp(ChaqueMot,MotEnLettre[y])!=0 && y<=24){
			y++;
		}							// je compare chaque mot à mon tableau 
									// pour donner sa valeur correspondante
		
		chiffrePrecedent = chiffreActuel;	//jé récupère le chiffre précédent car, j'ai ai besoin dans l'algorithme
		chiffreActuel = CorrespondanceChiffre[y];	
				// Ici on récupère le nouveau chiffre correspondant au mot étudié
		
		
		if (y<21) {							// on s'occupe des unité et des dizaine ici.
			if (chiffreActuel == 20 && chiffrePrecedent == 4){	// y=21 correspond à la valeurs "100" du tableau
										// "CorrespondanceChiffre"
				Total = Total - 4 + (4*20);
			}			
			else {						// tous les chiffres inférieurs à 60 s'ajoutent au total
				Total += chiffreActuel;
			}
		}
		if(chiffreActuel==100 && chiffrePrecedent < 10){	// quand on trouve le mot cent, on regarde le précédent
			Total = Total -chiffrePrecedent + chiffrePrecedent*100;	//si il y a un nombre avant 100, on prend ce
							// dernier et on le multiplie par 100, on ajoute ce résultat au Total
		}
		else if (chiffreActuel==100){	// si on arrive ici, il n'y avait pas de chiffre avant le 100,
			Total = Total +100;	//   dans ce cas, on ajoute simplement 100 au total
		}

		if(chiffreActuel==1000 && Total==0){
			Total +=1000;
		}
		else if(chiffreActuel==1000){
			Total *=1000;
		}

		if(chiffreActuel == 1000000){			// quand on trouve le mot millions, il faut multiplier le total 
			TotalMillion = Total *= 1000000;	// par 1 000 000, on garde le résultat obtenu dans une variable 
			Total = 0;				// à part, de cette façon, on peut continuer avec les milliers
		}
		
				
		ChaqueMot = strtok( NULL, " " );			// l'appelle de strtok avec null, nous permet de
									// reprendre la lecture là où elle s'etait arrétée.
	}
	
	return(Total+TotalMillion);  // retour du résultat.
}

